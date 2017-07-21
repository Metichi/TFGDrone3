package es.p32gocamuco.tfgdrone3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.SurfaceTexture;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import dji.common.flightcontroller.FlightControllerState;
import dji.common.product.Model;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.RecordingRoute;

public class IniciarVuelo extends AppCompatActivity implements OnMapReadyCallback {

    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallback = null;
    protected DJICodecManager mCodecManager;
    protected RecordingRoute recordingRoute;
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_vuelo);

        mReceivedVideoDataCallback = new VideoFeeder.VideoDataCallback() {
            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if(mCodecManager != null){
                    mCodecManager.sendDataToDecoder(videoBuffer,size);
                }
            }
        };
        initUI();
        recordingRoute = (RecordingRoute) getIntent().getSerializableExtra("RECORDING_ROUTE");
        recordingRoute.initMapOptions();
        recordingRoute.calculateRoute();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReciever,filter);
    }
    protected BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onProductChange();
        }
    };

    protected void onProductChange(){
        initPreviewer();
        initFlightController();
    }

    private void initUI(){
        final TextureView videoView = (TextureView) findViewById(R.id.videoView);
        final View mapView = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView)).getView();
        Button showStatus = (Button) findViewById(R.id.showStatus);
        ToggleButton controlCameraSwitch = (ToggleButton) findViewById(R.id.controlCameraSwitch);
        ToggleButton mapSwitch = (ToggleButton) findViewById(R.id.mapSwitch);
        ToggleButton initRouteSwitch = (ToggleButton) findViewById(R.id.initRouteSwitch);

        showStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mapSwitch.setChecked(false);
        mapSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    mapView.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.INVISIBLE);
                } else {
                    mapView.setVisibility(View.INVISIBLE);
                    videoView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        initPreviewer();
        onProductChange();
    }

    @Override
    protected void onDestroy() {
        uninitPreviewer();
        super.onDestroy();
    }

    private void initPreviewer(){
        BaseProduct product = DJIApplication.getProductInstance();
        TextureView mVideoSurface = (TextureView) findViewById(R.id.videoView);
        if(product == null || !product.isConnected()){
            Toast.makeText(this,getString(R.string.noConectado),Toast.LENGTH_SHORT).show();
        } else {
            mVideoSurface.setSurfaceTextureListener(surfaceTextureListener);
            if(!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)){
                if (VideoFeeder.getInstance().getVideoFeeds() != null
                        && VideoFeeder.getInstance().getVideoFeeds().size()>0){
                    VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(mReceivedVideoDataCallback);
                }
            }
        }
    }
    private void uninitPreviewer(){
        Camera camera = DJIApplication.getProductInstance().getCamera();
        if(camera != null){
            VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(null);
        }
    }

    /*FINDING THE AIRCRAFTS POSITION*/
    private double droneLocationLat;
    private double droneLocationLong;
    private double droneLocationAlt;
    private double droneLocationYaw;
    private void initFlightController(){
        FlightController mFlightController = null;
        BaseProduct product = DJIApplication.getProductInstance();
        if(product!= null && product.isConnected()){
            if(product instanceof Aircraft){
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }
        if (mFlightController != null){
            mFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                    droneLocationLat = flightControllerState.getAircraftLocation().getLatitude();
                    droneLocationLong = flightControllerState.getAircraftLocation().getLongitude();
                    droneLocationAlt = flightControllerState.getAircraftLocation().getAltitude();
                    droneLocationYaw = flightControllerState.getAttitude().yaw;
                    updateDroneLocation();
                }
            });
        }
    }

    /*PLACING THE AIRCRAFT ON THE MAP*/
    Marker droneMarker = null;
    private void updateDroneLocation(){
        LatLng pos = new LatLng(droneLocationLat,droneLocationLong);
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.flat(true);
        markerOptions.rotation((float) droneLocationYaw);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (droneMarker != null) {
                        droneMarker.remove();
                    } else {
                        droneMarker = mMap.addMarker(markerOptions);
                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        });
    }

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            if (mCodecManager == null){
                mCodecManager = new DJICodecManager(IniciarVuelo.this,surfaceTexture,width,height);
            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            mCodecManager = new DJICodecManager(IniciarVuelo.this,surfaceTexture,width,height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            if (mCodecManager != null){
                mCodecManager.cleanSurface();
                mCodecManager = null;
            }
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Toast toast = Toast.makeText(this, "No hay permisos para obtener localización", Toast.LENGTH_SHORT);
            toast.show();
        }
        recordingRoute.updateMap(mMap);
        updateDroneLocation();
        findDevice();
    }

    private void findDevice(){
        LatLng pos = null;
        if (droneMarker != null){
            pos = droneMarker.getPosition();
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = null;
            Criteria criteria = new Criteria();
            try {
                location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            if (location != null){
                pos = new LatLng(location.getLatitude(),location.getLongitude());
            }
        }
        if (pos != null){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos,13f));
        }
    }
}

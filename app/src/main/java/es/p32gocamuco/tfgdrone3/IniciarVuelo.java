package es.p32gocamuco.tfgdrone3;

import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

import java.util.Map;

import dji.common.product.Model;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import es.p32gocamuco.tfgdrone3.R;
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

        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.getMapAsync(this);
    }

    protected void onProductChange(){
        initPreviewer();
    }

    private void initUI(){
        final TextureView videoView = (TextureView) findViewById(R.id.videoView);
        final MapView mapView = (MapView) findViewById(R.id.mapView);
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
    }
}

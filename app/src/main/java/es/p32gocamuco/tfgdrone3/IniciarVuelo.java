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
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypoint.WaypointTurnMode;
import dji.common.model.LocationCoordinate2D;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.products.Aircraft;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.RecordingRoute;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.RoutePoint;

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
        final View mapView = (getSupportFragmentManager().findFragmentById(R.id.mapView)).getView();
        Button showStatus = (Button) findViewById(R.id.showStatus);
        final Button stopRoute = (Button) findViewById(R.id.stopRoute);
        ToggleButton mapSwitch = (ToggleButton) findViewById(R.id.mapSwitch);
        ToggleButton initRouteSwitch = (ToggleButton) findViewById(R.id.initRouteSwitch);

        showStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        stopRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopFlight();
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

        initRouteSwitch.setChecked(false);
        initRouteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    startFlight();
                } else {
                    pauseFlight();
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
            setResultToToast("No hay permisos para obtener localización");
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

    private void startFlight(){
        WaypointMissionOperator waypointMissionOperator = DJIApplication.getWaypointMissionOperator();
        if (waypointMissionOperator.getCurrentState() != WaypointMissionState.EXECUTION_PAUSED) {
            WaypointMission waypointMission = buildWaypointMission(buildWaypointList());

            if (recordingRoute.getHome() == null){
                ((Aircraft) DJIApplication.getProductInstance()).getFlightController().setHomeLocationUsingAircraftCurrentLocation(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null){
                            setResultToToast("Home set at current location");
                        } else {
                            setResultToToast("Error: " + djiError.getDescription());
                            Log.e("SetHome",djiError.getDescription());
                        }
                    }
                });
            } else {
                ((Aircraft) DJIApplication.getProductInstance()).getFlightController()
                        .setHomeLocation(new LocationCoordinate2D(
                                        recordingRoute.getHome().getLatitude(), recordingRoute.getHome().getLongitude()),
                                new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        if (djiError == null){
                                            setResultToToast("Home set by route definition");
                                        } else {
                                            setResultToToast("Error: " + djiError.getDescription());
                                            Log.e("SetHome",djiError.getDescription());
                                        }
                                    }
                                });
            }

            DJIError error = waypointMissionOperator.loadMission(waypointMission);
            if (error == null) {
                setResultToToast("Mission loaded successfully");
                uploadMissionToAircraft();
            } else {
                setResultToToast("Error: " + error.getDescription());
                Log.e("StartFlight", error.getDescription());
            }
        } else {
            waypointMissionOperator.resumeMission(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null){
                        setResultToToast("Resumed successfully");
                    } else {
                        setResultToToast("Error resuming: " + djiError.getDescription());
                        Log.e("ResumeMission", djiError.getDescription());
                    }
                }
            });
        }
    }
    private List<Waypoint> buildWaypointList(){
        RoutePoint[] routePoints = recordingRoute.getRoute();
        RoutePoint previousRoutePoint = null;
        ArrayList<Waypoint> waypoints = new ArrayList<>(routePoints.length);
        for(RoutePoint routePoint : routePoints){
            Waypoint waypoint = new Waypoint(routePoint.getLatitude(),routePoint.getLongitude(),(float) routePoint.getHeight());
            waypoint.heading = (int) Math.round(routePoint.getYaw());
            waypoint.gimbalPitch = (float) routePoint.getPitch();

            if (previousRoutePoint != null) {
                int previousHeading, currentHeading, relativeHeading;
                previousHeading = (int) Math.round(previousRoutePoint.getYaw());
                if (previousHeading > 180){previousHeading = previousHeading - 360;}
                currentHeading = (int) Math.round(routePoint.getYaw());
                if (currentHeading > 180){currentHeading = currentHeading - 360;}
                relativeHeading = currentHeading - previousHeading;

                if (relativeHeading < 180){
                    waypoint.turnMode = WaypointTurnMode.CLOCKWISE;
                } else if (relativeHeading > 180) {
                    waypoint.turnMode = WaypointTurnMode.COUNTER_CLOCKWISE;
                } else {
                    waypoint.turnMode = waypoints.get(waypoints.size()-1).turnMode;
                }
            }
            previousRoutePoint = routePoint;

            waypoint.speed = (float) routePoint.getSpeed().getModulo_v();

            WaypointAction waypointAction;
            switch (routePoint.getAccion()){
                case INICIA_GRABACION:
                    waypointAction = new WaypointAction(WaypointActionType.START_RECORD,0);
                    waypoint.addAction(waypointAction);
                    break;
                case DETENER_GRABACION:
                    waypointAction = new WaypointAction(WaypointActionType.STOP_RECORD,0);
                    waypoint.addAction(waypointAction);
                    break;
                case DETENER_GRABACION_Y_TOMAR_FOTO:
                    waypointAction = new WaypointAction(WaypointActionType.STOP_RECORD,0);
                    waypoint.addAction(waypointAction);
                case TOMAR_FOTO:
                    waypointAction = new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 0);
                    waypoint.addAction(waypointAction);
                    break;
                default:
                    break;
            }
            waypoints.add(waypoint);
        }
        return waypoints;
    }

    private WaypointMission buildWaypointMission(List<Waypoint> waypointList){
        //Options for the waypoint mission
        WaypointMission.Builder waypointMissionBuilder = new WaypointMission.Builder()
                .finishedAction(WaypointMissionFinishedAction.GO_HOME) //TODO: Get this from settings
                .headingMode(WaypointMissionHeadingMode.USING_WAYPOINT_HEADING)
                .maxFlightSpeed(12f) //TODO: Get this from settings
                .flightPathMode(WaypointMissionFlightPathMode.NORMAL)
                .setExitMissionOnRCSignalLostEnabled(true) //TODO: Get this from settings
                .setGimbalPitchRotationEnabled(true)//This has to be true in order for each individual waypoint to be able to set its pitch.
                .waypointList(waypointList);

        return waypointMissionBuilder.build();

    }

    private void uploadMissionToAircraft(){
        final WaypointMissionOperator waypointMissionOperator = DJIApplication.getWaypointMissionOperator();
        waypointMissionOperator.uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    setResultToToast("Upload to aircraft successfull");
                    startMission();
                } else {
                    setResultToToast("Failed to upload: " + djiError.getDescription());
                    Log.e("UploadToAircraft", djiError.getDescription());
                    waypointMissionOperator.retryUploadMission(null);
                }
            }
        });


    }
    private void startMission(){
        DJIApplication.getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                setResultToToast("Mission start :" + (djiError == null ?
                        "Success" : djiError.getDescription()));
            }
        });
    }

    public void pauseFlight(){
        DJIApplication.getWaypointMissionOperator().pauseMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null){
                    setResultToToast("Mission Paused");
                } else {
                    setResultToToast("Error: " + djiError.getDescription());
                    Log.e("PauseFlight", djiError.getDescription());
                }
            }
        });
    }
    public void stopFlight(){
        DJIApplication.getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null){
                    setResultToToast("Mission Stopped");
                } else {
                    setResultToToast("Error: " + djiError.getDescription());
                    Log.e("StopFlight", djiError.getDescription());
                }
            }
        });
    }

    private void setResultToToast(String result){
        Toast.makeText(DJIApplication.getAppContext(),result,Toast.LENGTH_SHORT).show();
    }
}

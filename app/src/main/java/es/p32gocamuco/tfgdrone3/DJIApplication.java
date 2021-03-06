package es.p32gocamuco.tfgdrone3;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * This Class handles DJI methods and objects such as {@link dji.sdk.sdkmanager.DJISDKManager} in a
 * consistent manner that is accessible by all activities across the application, ensuring that the
 * same aircraft reference is always obtained.
 *
 * Created by Manuel Gómez Castro on 10/07/17.
 */

public class DJIApplication extends Application {
    private static final String activityName = MainActivity.class.getName();
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static BaseProduct mProduct;
    private Handler mHandler;
    @Override
    public void onCreate() {
        super.onCreate();
        if(!DJISDKManager.getInstance().hasSDKRegistered()) {
            DJISDKManager.getInstance().registerApp(this, mSDKManagerCallback);
            mHandler = new Handler(Looper.getMainLooper());
        }
    }

    private DJISDKManager.SDKManagerCallback mSDKManagerCallback = new DJISDKManager.SDKManagerCallback() {
        @Override
        public void onRegister(DJIError djiError) {
            Log.d(activityName,djiError == null ? "Success" : djiError.getDescription());
            if(djiError == DJISDKError.REGISTRATION_SUCCESS){
                DJISDKManager.getInstance().startConnectionToProduct();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(getApplicationContext(),"Register Success",Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(getApplicationContext(),"Register sdk failed, check if network is aviable",Toast.LENGTH_LONG).show();
                    }
                });
            }
            try {
                Log.e("TAG", djiError.toString());
            } catch (java.lang.NullPointerException excepcion) {
                Log.e("TAG", "Error desconocido");
            }
        }

        @Override
        public void onProductChange(BaseProduct oldProduct, BaseProduct newProduct) {
            mProduct = newProduct;
            if(mProduct!= null){
                mProduct.setBaseProductListener(mDJIBaseProductListener);
            }
            notifyStatusChange();
        }
    };
    private BaseProduct.BaseProductListener mDJIBaseProductListener = new BaseProduct.BaseProductListener() {
        @Override
        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent, BaseComponent newComponent) {
            if(newComponent != null) newComponent.setComponentListener(mDJIComponentListener);
            notifyStatusChange();
        }

        @Override
        public void onConnectivityChange(boolean isConnected) {
            notifyStatusChange();
        }
    };

    private BaseComponent.ComponentListener mDJIComponentListener = new BaseComponent.ComponentListener() {
        @Override
        public void onConnectivityChange(boolean isConnected) {
            notifyStatusChange();
        }
    };

    private void notifyStatusChange(){
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable,500);
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

    public static synchronized BaseProduct getProductInstance(){
        if (mProduct == null){
            mProduct = DJISDKManager.getInstance().getProduct();
        }
        return mProduct;
    }

    //MISSION CONTROL
    static private WaypointMissionOperator waypointMissionOperator;

    /**
     * Generating a {@link WaypointMissionOperator} from the currently instanciated aircraft.
     *
     * This mission operator will be used in the IniciarVuelo activity.
     * @return WaypointMissionOperator that will hold the recording route.
     */
    public static synchronized WaypointMissionOperator getWaypointMissionOperator() {
        waypointMissionOperator = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
        return waypointMissionOperator;
    }
}

/*
    Main Activity
    Se trata del menú principal de la aplicación y la actividad que se ejecutará al iniciarla.
    Contiene botones hacia las siguientes actividades:
        - Diseñar una ruta
        - Cargar una ruta
        - Ajustes de la aplicación
        - Establecer conexión con el dispositivo

    Además, mostrará información sobre:
        - Versión del SDK
        - Estado de conexión con el dispositivo y, en caso de estar conectado, su nombre.
 */

package es.p32gocamuco.tfgdrone3;
// TODO: Crear botones funcionales que lleven a otras actividades
// TODO: Cambiar el estado de botones para mostrar la conexión o activación de la app

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKManager;

public class MainActivity extends AppCompatActivity {
    private static final String activityName = MainActivity.class.getName();
    private static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static BaseProduct mProduct;
    private Handler mHandler;

    private TextView sdkVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize DJI SDK Manager
        mHandler = new Handler(Looper.getMainLooper());
        DJISDKManager.getInstance().registerApp(this,mSDKManagerCallback);
        sdkVersion = (TextView) findViewById(R.id.sdkVersion);
        sdkVersion.setText(DJISDKManager.getInstance().getSDKVersion());
    }

    //Generamos un SDKManagerCallback para implementar los métodos que actúan en el registro y que gestionan la conexión del producto
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

}

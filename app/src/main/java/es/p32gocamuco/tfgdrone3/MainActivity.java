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

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKManager;

public class MainActivity extends AppCompatActivity {
    BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReciever,filter);

        setContentView(R.layout.activity_main);
        initUI();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReciever);
        super.onDestroy();
    }

    protected void initUI(){
        TextView sdkVersion, modeloDispositivo;
        Button createRoute, loadRoute;
        Button settings, droneStatus;

        sdkVersion = (TextView) findViewById(R.id.sdkVersion);
        createRoute = (Button) findViewById(R.id.crearRuta);
        loadRoute = (Button) findViewById(R.id.cargarRuta);
        settings = (Button) findViewById(R.id.appSettings);
        droneStatus = (Button) findViewById(R.id.droneConnect);

        //Obtenemos la versión del SDK para mostrarla en pantalla
        sdkVersion.setText(DJISDKManager.getInstance().getSDKVersion());

        createRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,CrearRuta.class);
                startActivity(intent);
            }
        });
        loadRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,CargarRuta.class);
                startActivity(intent);
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
        });
        droneStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,DroneConnect.class);
                startActivity(intent);
            }
        });
        updateUI();
    }

    private void updateUI(){
        //Obtenemos el nombre del dispositivo
        BaseProduct mProduct = DJIApplication.getProductInstance();
        TextView modeloDispositivo = (TextView) findViewById(R.id.dispositivo);
        if (mProduct != null) {
            if (mProduct.isConnected()) {
                modeloDispositivo.setText(mProduct.getModel().toString());
            } else {
                modeloDispositivo.setText(R.string.noConectado);
            }
        } else {
            modeloDispositivo.setText(R.string.noDispositivo);
        }
    }

}

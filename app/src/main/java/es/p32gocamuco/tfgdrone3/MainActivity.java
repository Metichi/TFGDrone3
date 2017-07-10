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

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
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

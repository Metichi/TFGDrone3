package es.p32gocamuco.tfgdrone3;

import android.content.DialogInterface;
import android.content.IntentSender;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.Objetivo;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.RecordingRoute;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.TecnicaAcimutal;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.TecnicaGrabacion;

public class CrearRuta extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    AlertDialog alertdialog;
    RecordingRoute recordingRoute = new RecordingRoute(); //TODO: Esta iniciación sólo es valida si se viene del menú principal. Si se viene de cargar ruta, hay que iniciar con la ruta correspondiente.
    Objetivo home;
    TecnicaAcimutal newTechnique;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_ruta);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initUI();
    }

    private void initUI() {
        setTitle(recordingRoute.getName());
        Button addBtn, calcBtn, saveBtn;
        addBtn = (Button) findViewById(R.id.addCR);
        calcBtn = (Button) findViewById(R.id.calcRutaCR);
        saveBtn= (Button) findViewById(R.id.saveCR);

        addBtn.setOnClickListener(this);
        calcBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e){
            Toast toast =  Toast.makeText(this,"No hay permisos para obtener localización",Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    // TODO: Implementar funcionalidad para los botones.
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.addCR:
                displayAddMenu();
                break;
            case R.id.saveCR:
                recordingRoute.saveRoute();
                break;
            case R.id.calcRutaCR:
                recordingRoute.calculateRoute(); //TODO: Cuando la ruta está calculada, reemplazar botón con "Iniciar ruta"
                break;
            case R.id.addAcimutal:
                newTechnique = new TecnicaAcimutal();
                alertdialog.dismiss();
                newTechnique.showTechniqueSettingsMenu(this);
                if (newTechnique.createdSuccesfully()) {
                    recordingRoute.addTechnique(newTechnique);
                }
                newTechnique = null;
                break;
        }
    }

    public void displayAddMenu(){
        ScrollView addMenu = (ScrollView)getLayoutInflater().inflate(R.layout.add_menu, null);
        Button addHome = (Button) addMenu.findViewById(R.id.addHome);
        Button addAcimutal = (Button) addMenu.findViewById(R.id.addAcimutal);

        addHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertdialog.dismiss();
                //TODO: Añade un botón de home cuando se pulse y elimina el anterior si existiera

            }
        });
        addAcimutal.setOnClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.addItemtoMapTitle);
                builder.setView(addMenu);
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        alertdialog = builder.create();
        alertdialog.show();
    }
}

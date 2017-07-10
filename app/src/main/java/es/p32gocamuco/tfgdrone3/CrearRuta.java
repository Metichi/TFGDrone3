package es.p32gocamuco.tfgdrone3;

import android.content.DialogInterface;
import android.support.annotation.StringDef;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import org.w3c.dom.Text;

import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.Camara;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.Objetivo;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.RecordingRoute;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.TecnicaAcimutal;

public class CrearRuta extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    AlertDialog alertdialog;
    RecordingRoute recordingRoute = new RecordingRoute(); //TODO: Esta iniciación sólo es valida si se viene del menú principal. Si se viene de cargar ruta, hay que iniciar con la ruta correspondiente.

    GoogleMap.OnMapClickListener addPoint = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(final LatLng latLng) {
            LinearLayout menu = (LinearLayout) getLayoutInflater().inflate(R.layout.add_objective_menu, null);
            TextView latitude = (TextView) menu.findViewById(R.id.latitude);
            TextView longitude = (TextView) menu.findViewById(R.id.longitude);
            final EditText height = (EditText) menu.findViewById(R.id.objectiveHeight);
            TextView previousTime = (TextView) menu.findViewById(R.id.tiempoPrevio);
            final EditText elapsedTime = (EditText) menu.findViewById(R.id.tiempoTardado);
            final Spinner action = (Spinner) menu.findViewById(R.id.accion);
            ArrayAdapter<CharSequence> adapter;

            //Elegimos qué lista de acciones vamos a mostrar.
            final int id = recordingRoute.isCurrentlyRecording() ? R.array.accionesGrabando : R.array.accionesNoGrabando;
            adapter = ArrayAdapter.createFromResource(CrearRuta.this,id, R.layout.support_simple_spinner_dropdown_item);
            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            action.setAdapter(adapter);

            //Iniciamos la interfaz:
            latitude.setText(String.format("%s",latLng.latitude));
            longitude.setText(String.format("%s",latLng.longitude));
            height.setText("0");
            previousTime.setText(String.format("%s", recordingRoute.getLastObjective().getTime()));
            elapsedTime.setText("30"); //TODO: Get this from settings
            final Objetivo nObjetive = new Objetivo(latLng,0,0);

            action.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Objetivo.Acciones a;

                    if (id == R.array.accionesNoGrabando) {
                        switch (i) {
                            case 0:
                                a = Objetivo.Acciones.NADA;
                                break;
                            case 1:
                                a = Objetivo.Acciones.INICIA_GRABACION;
                                break;
                            case 2:
                                a = Objetivo.Acciones.GRABAR_ESTE_PUNTO;
                                break;
                            case 3:
                                a = Objetivo.Acciones.TOMAR_FOTO;
                                break;
                            default:
                                a = Objetivo.Acciones.NADA;
                        }
                    } else {
                        switch (i) {
                            case 0:
                                a = Objetivo.Acciones.CONTINUA_GRABACION;
                                break;
                            case 1:
                                a = Objetivo.Acciones.DETENER_GRABACION;
                                break;
                            case 2:
                                a = Objetivo.Acciones.DETENER_GRABACION_Y_TOMAR_FOTO;
                                break;
                            default:
                                a = Objetivo.Acciones.DETENER_GRABACION;
                        }
                    }
                    nObjetive.setAccion(a);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    if(id == R.array.accionesGrabando) {nObjetive.setAccion(Objetivo.Acciones.CONTINUA_GRABACION);}
                    if(id == R.array.accionesNoGrabando) {nObjetive.setAccion(Objetivo.Acciones.NADA);}
                }
            });

            new AlertDialog.Builder(CrearRuta.this)
                    .setView(menu)
                    .setTitle(R.string.addObjetivo)
                    .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            double h = Double.parseDouble(height.getText().toString().trim().replace(",","."));
                            double t = recordingRoute.getLastObjective().getTime() +
                                    Double.parseDouble(elapsedTime.getText().toString().trim().replace(",","."));

                            nObjetive.setHeight(h);
                            nObjetive.setTime(t);
                            Marker marker = mMap.addMarker(nObjetive.getMarkerOptions());
                            nObjetive.setMarker(marker);
                            nObjetive.setCurrentTechnique(recordingRoute.getCurrentTechnique());

                            recordingRoute.getCurrentTechnique().addObjetivo(nObjetive);
                            Polyline polyline = mMap.addPolyline(recordingRoute.getCurrentTechnique().getPolylineOptions());
                            recordingRoute.getCurrentTechnique().setPolyline(polyline);

                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
    };

    GoogleMap.OnMapClickListener mapInactive = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
        }
    };

    GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            marker.showInfoWindow();
            /*marker.setSnippet(marker.getTag().toString());
            int index = recordingRoute.getIndexFromMarker(marker);
            if (marker.getTag() instanceof Camara){marker.setTitle(String.format("Camara #%d",index));}
            else if (marker.getTag() instanceof Objetivo) {marker.setTitle(String.format("Objetivo #%d",index));}*/
            return false;
        }
    };
    GoogleMap.OnMarkerClickListener markerInactive = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            return true;
        }
    };

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
        final Button addBtn, calcBtn, saveBtn, finishBtn, initBtn;
        addBtn = (Button) findViewById(R.id.addCR);
        calcBtn = (Button) findViewById(R.id.calcRutaCR);
        saveBtn= (Button) findViewById(R.id.saveCR);
        finishBtn = (Button) findViewById(R.id.finalizar);
        initBtn = (Button) findViewById(R.id.initRuta);

        setTitle(recordingRoute.getName());
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAddMenu();
            }
        });

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordingRoute.setCurrentTechnique(null);
                updateUI();
            }
        });
        calcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordingRoute.calculateRoute();
                if(recordingRoute.getRouteReady()){
                    Camara[] route = recordingRoute.getRoute();
                    for(Camara waypoint : route){
                        Marker marker = mMap.addMarker(waypoint.getMarkerOptions());
                        waypoint.setMarker(marker);
                        Polyline polyline = mMap.addPolyline(recordingRoute.getPolylineOptions());
                        recordingRoute.setPolyline(polyline);
                    }
                }
                updateUI();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordingRoute.saveRoute();
            }
        });
    }
    private void updateUI(){
        final Button addBtn, calcBtn, saveBtn, finishBtn,initBtn;
        addBtn = (Button) findViewById(R.id.addCR);
        calcBtn = (Button) findViewById(R.id.calcRutaCR);
        saveBtn= (Button) findViewById(R.id.saveCR);
        finishBtn = (Button) findViewById(R.id.finalizar);
        initBtn = (Button) findViewById(R.id.initRuta);


        setTitle(recordingRoute.getName());


        if (recordingRoute.getCurrentTechnique() != null) {
            finishBtn.setVisibility(View.VISIBLE);
            addBtn.setVisibility(View.GONE);
            saveBtn.setVisibility(View.GONE);
            mMap.setOnMapClickListener(addPoint);
            mMap.setOnMarkerClickListener(markerInactive);
        } else {
            finishBtn.setVisibility(View.GONE);
            addBtn.setVisibility(View.VISIBLE);
            saveBtn.setVisibility(View.VISIBLE);
            mMap.setOnMapClickListener(mapInactive);
            mMap.setOnMarkerClickListener(markerClickListener);
        }

        //Gestión de los botones de calcular e iniciar ruta.
        if (recordingRoute.calcRouteAviable()) {
            calcBtn.setVisibility(recordingRoute.getRouteReady() ? View.GONE : View.VISIBLE);
            initBtn.setVisibility(!recordingRoute.getRouteReady() ? View.GONE : View.VISIBLE);

        } else {
            calcBtn.setVisibility(View.GONE);
            initBtn.setVisibility(View.GONE);
        }
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
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout infoWindow =(LinearLayout) getLayoutInflater().inflate(R.layout.info_window_marker,null);
                Objetivo o = (Objetivo) marker.getTag();
                TextView latitud = (TextView) infoWindow.findViewById(R.id.latitud);
                TextView longitud = (TextView) infoWindow.findViewById(R.id.longitud);
                TextView altura = (TextView) infoWindow.findViewById(R.id.altura);
                TextView tiempo = (TextView) infoWindow.findViewById(R.id.tiempo);
                TextView accion = (TextView) infoWindow.findViewById(R.id.accion);
                TextView markerIndex = (TextView) infoWindow.findViewById(R.id.markerIndex);

                latitud.setText(String.format("%f",o.getLatitude()));
                longitud.setText(String.format("%f",o.getLongitude()));
                altura.setText(String.format("%s",o.getHeight()));
                tiempo.setText(String.format("%s",o.getTime()));
                accion.setText(o.getAccion().toString().replace("_"," "));

                if(o instanceof Camara){
                    Camara c = (Camara) o;
                    LinearLayout cameraInfo = (LinearLayout) getLayoutInflater().inflate(R.layout.info_camera_marker,null);
                    TextView pitch, yaw, hSpeed, vSpeed, speedBearing, tSpeed;
                    pitch = (TextView) cameraInfo.findViewById(R.id.pitch);
                    yaw = (TextView) cameraInfo.findViewById(R.id.yaw);
                    hSpeed = (TextView) cameraInfo.findViewById(R.id.hSpeed);
                    vSpeed = (TextView) cameraInfo.findViewById(R.id.vSpeed);
                    speedBearing = (TextView) cameraInfo.findViewById(R.id.speedBearing);
                    tSpeed = (TextView) cameraInfo.findViewById(R.id.tSpeed);

                    pitch.setText(String.format("%s",c.getPitch()));
                    yaw.setText(String.format("%s",c.getYaw()));
                    hSpeed.setText(String.format("%s",c.getVelocidad().getVelocidadNESO()));
                    vSpeed.setText(String.format("%s",c.getVelocidad().getVertical()));
                    speedBearing.setText(String.format("%s",c.getVelocidad().getDireccion()));
                    tSpeed.setText(String.format("%s",c.getVelocidad().getModulo_v()));

                    infoWindow.addView(cameraInfo);

                    markerIndex.setText(String.format("Camara #%d",recordingRoute.getIndexFromMarker(marker)));
                } else {
                    markerIndex.setText(String.format("Objetivo #%d",recordingRoute.getIndexFromMarker(marker)));
                }

                return infoWindow;
            }
        });

        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e){
            Toast toast =  Toast.makeText(this,"No hay permisos para obtener localización",Toast.LENGTH_SHORT);
            toast.show();
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
        addAcimutal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final TecnicaAcimutal newTechnique = new TecnicaAcimutal();
                LinearLayout menu = (LinearLayout) getLayoutInflater().inflate(R.layout.settings_menu_acimutal, null);
                final EditText altura = (EditText) menu.findViewById(R.id.alturaSobreObjetivo);
                final EditText NESO = (EditText) menu.findViewById(R.id.orientacionNESO);
                final ToggleButton toggleSigueRuta = (ToggleButton) menu.findViewById(R.id.toggleSigueRuta);
                final TextView orientacionLabel = (TextView) menu.findViewById(R.id.orientacionNESOLabel);
                AlertDialog acimutalDialog;
                AlertDialog.Builder acimutalDialogBuilder = new AlertDialog.Builder(CrearRuta.this);

                altura.setText(String.format("%s",newTechnique.getAlturaSobreObjetivo()));
                NESO.setText(String.format("%s",newTechnique.getOrientacionNESO()));

                toggleSigueRuta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        compoundButton.setChecked(b);
                        if (b){
                            newTechnique.setOrientacionSegunObjetivo(true);
                            NESO.setVisibility(View.GONE);
                            orientacionLabel.setVisibility(View.GONE);
                        } else {
                            newTechnique.setOrientacionSegunObjetivo(false);
                            orientacionLabel.setVisibility(View.VISIBLE);
                            NESO.setVisibility(View.VISIBLE);
                        }
                    }
                });


                acimutalDialogBuilder.setTitle(R.string.addAcimutal);
                acimutalDialogBuilder.setView(menu);
                acimutalDialogBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                acimutalDialogBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (altura.getText().toString().trim().length()==0){
                            newTechnique.setAlturaSobreObjetivo(10);
                        } else {
                            newTechnique.setAlturaSobreObjetivo(Double.parseDouble(altura.getText().toString().replace(",",".")));
                        }
                        if (NESO.getText().toString().trim().length() == 0){
                            newTechnique.setOrientacionNESO(0);
                        } else {
                            newTechnique.setOrientacionNESO(Double.parseDouble(NESO.getText().toString().replace(",",".")));
                        }
                        recordingRoute.addTechnique(newTechnique);
                        CrearRuta.this.updateUI();
                    }
                });

                acimutalDialog = acimutalDialogBuilder.create();
                alertdialog.dismiss();
                acimutalDialog.show();
            }
        });

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

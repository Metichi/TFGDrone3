package es.p32gocamuco.tfgdrone3;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;

import dji.sdk.base.BaseProduct;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.RoutePoint;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.Target;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.RecordingRoute;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.TechniqueAcimutal;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.TechniqueCrane;
import es.p32gocamuco.tfgdrone3.tecnicasgrabacion.TecnicaGrabacion;

public class CrearRuta extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    RecordingRoute recordingRoute;
    boolean settingHome = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_ruta);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();
        if (!intent.hasExtra("RECORDING_ROUTE")){
            recordingRoute = new RecordingRoute();
        } else {
            recordingRoute = (RecordingRoute) intent.getSerializableExtra("RECORDING_ROUTE");
            recordingRoute.initMapOptions();
        }
    }

    private void initUI() {
        final Button addBtn, calcBtn, saveBtn, finishBtn, initBtn;
        addBtn = (Button) findViewById(R.id.addCR);
        calcBtn = (Button) findViewById(R.id.calcRutaCR);
        saveBtn = (Button) findViewById(R.id.saveCR);
        finishBtn = (Button) findViewById(R.id.finalizar);
        initBtn = (Button) findViewById(R.id.initRuta);

        setTitle(recordingRoute.getName());
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAddMenu();
                updateUI();
            }
        });

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordingRoute.setCurrentTechnique(null);
                settingHome = false;
                updateUI();
            }
        });
        calcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double maxSpeed = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(CrearRuta.this)
                        .getString("max_speed_setting_key","10.0"));
                double minHeight = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(CrearRuta.this)
                        .getString("min_height_setting_key","10.0"));
                double maxHeight = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(CrearRuta.this)
                        .getString("max_height_setting_key","100.0"));
                double maxYawSpeed = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(CrearRuta.this)
                        .getString("yaw_speed_setting_key","10.0"));
                double maxPitchSpeed = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(CrearRuta.this)
                        .getString("pitch_speed_setting_key","10.0"));

                RecordingRoute.CalculationCompleteListener listener = new RecordingRoute.CalculationCompleteListener() {
                    @Override
                    public void onCalculationComplete(RecordingRoute.RouteReport report) {
                        GridLayout reportWindow =(GridLayout) LayoutInflater.from(CrearRuta.this).inflate(R.layout.info_route_report,null);
                        TextView minHeight,maxHeight,minSpeed,maxSpeed,targetCount,routePointCount,routeAdjustedWarning;
                        minHeight = (TextView) reportWindow.findViewById(R.id.minHeight);
                        maxHeight = (TextView) reportWindow.findViewById(R.id.maxHeight);
                        minSpeed = (TextView) reportWindow.findViewById(R.id.minSpeed);
                        maxSpeed = (TextView) reportWindow.findViewById(R.id.maxSpeed);
                        targetCount = (TextView) reportWindow.findViewById(R.id.targetCount);
                        routePointCount = (TextView) reportWindow.findViewById(R.id.routePointCount);
                        routeAdjustedWarning = (TextView) reportWindow.findViewById(R.id.routeAdjustedWarning);

                        if(report.isMaxHeightCorrected()||report.isMaxSpeedCorrected()||report.isMinHeightCorrected()){
                            routeAdjustedWarning.setVisibility(View.VISIBLE);
                        }

                        minHeight.setText(String.valueOf(report.getMinHeight()));
                        maxHeight.setText(String.valueOf(report.getMaxHeight()));
                        minSpeed.setText(String.valueOf(report.getMinSpeed()));
                        maxSpeed.setText(String.valueOf(report.getMaxSpeed()));
                        targetCount.setText(String.valueOf(report.getTargetCount()));
                        routePointCount.setText(String.valueOf(report.getRoutePointCount()));

                        if(report.isMaxSpeedCorrected()){maxSpeed.setTextColor(getResources().getColor(R.color.elementChanged));}
                        if(report.isMaxHeightCorrected()){maxHeight.setTextColor(getResources().getColor(R.color.elementChanged));}
                        if(report.isMinHeightCorrected()){minHeight.setTextColor(getResources().getColor(R.color.elementChanged));}
                        new AlertDialog.Builder(CrearRuta.this)
                                .setView(reportWindow)
                                .setTitle("Información de ruta")
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .create()
                                .show();
                    }
                };
                recordingRoute.calculateRoute(maxSpeed,maxYawSpeed,maxPitchSpeed,minHeight,maxHeight,listener);
                if (recordingRoute.getRouteReady()) {
                    RoutePoint[] route = recordingRoute.getRoute();
                    for (RoutePoint waypoint : route) {
                        waypoint.placeAtMap(mMap);
                        recordingRoute.placeAtMap(mMap);
                    }
                }
                updateUI();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSaveMenu();
            }
        });

        initBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseProduct product = DJIApplication.getProductInstance();
                if ((product == null) || !product.isConnected()) {
                    Toast.makeText(CrearRuta.this, getString(R.string.noConectado), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(CrearRuta.this, IniciarVuelo.class);
                    intent.putExtra("RECORDING_ROUTE",recordingRoute);
                    startActivity(intent);
                }
            }
        });
        updateUI();
    }

    /**
     * This method manages the UI status
     *
     * Depending on the status of the route, this method will be called to update the UI to set the visibility
     * of the items on the screen.
     */
    private void updateUI() {
        final Button addBtn, calcBtn, saveBtn, finishBtn, initBtn;
        addBtn = (Button) findViewById(R.id.addCR);
        calcBtn = (Button) findViewById(R.id.calcRutaCR);
        saveBtn = (Button) findViewById(R.id.saveCR);
        finishBtn = (Button) findViewById(R.id.finalizar);
        initBtn = (Button) findViewById(R.id.initRuta);

        GoogleMap.OnMapClickListener addPoint = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                if (settingHome){
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(CrearRuta.this);
                    double radius = Double.parseDouble(
                            pref.getString("max_distance_setting_key",""));

                    recordingRoute.setHome(new RecordingRoute.Home(latLng,radius));
                    recordingRoute.getHome().placeAtMap(mMap);
                } else {
                    addTargetToMap(latLng);
                }
            }
        };
        GoogleMap.OnMapClickListener mapInactive = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        };

        /**
         * This field is used when a marker should respond to a tap by displaying an information menu.
         */
        GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return false;
            }
        };

        /**
         * This field is used when a marker should remain irresponsive to taps.
         */
        GoogleMap.OnMarkerClickListener markerInactive = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        };


        setTitle(recordingRoute.getName());


        if (recordingRoute.getCurrentTechnique() != null || settingHome) {
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
        if (recordingRoute.calcRouteAviable()&&!settingHome) {
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
     * This method innitalizes the map options to enable zoom, locate button, and the adapter that
     * displays the information window.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *
     * @see CrearRuta#buildInfoWindow(Marker)
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
                return buildInfoWindow(marker);
            }
        });

        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Toast toast = Toast.makeText(this, "No hay permisos para obtener localización", Toast.LENGTH_SHORT);
            toast.show();
        }
        recordingRoute.updateMap(mMap);
        initUI();
    }


    /**
     * Adds a target to the current technique
     *
     * While creating a technique, this method is called every time the map is tapped. It displays a
     * menu in wich the properties of a {@link Target} are defined. If the menu is accepted, the new target
     * is placed on the map and added to the current technique. If cancelled, the target is discarded.
     * @param latLng
     */
    private void addTargetToMap(LatLng latLng) {
        final Target nTarget = new Target(latLng, 0, 0);
        nTarget.setCurrentTechnique(recordingRoute.getCurrentTechnique());

        LinearLayout menu = (LinearLayout) getLayoutInflater().inflate(R.layout.add_objective_menu, null);
        TextView latitude = (TextView) menu.findViewById(R.id.latitude);
        TextView longitude = (TextView) menu.findViewById(R.id.longitude);
        final EditText height = (EditText) menu.findViewById(R.id.objectiveHeight);
        final EditText elapsedTime = (EditText) menu.findViewById(R.id.tiempoTardado);
        final Spinner action = (Spinner) menu.findViewById(R.id.accion);
        ArrayAdapter<CharSequence> adapter;

        //Elegimos qué lista de acciones vamos a mostrar.
        final int id = recordingRoute.getCurrentTechnique().getPreviouslyRecording(nTarget) ? R.array.accionesGrabando : R.array.accionesNoGrabando;
        adapter = ArrayAdapter.createFromResource(CrearRuta.this, id, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        action.setAdapter(adapter);

        //Iniciamos la interfaz:
        latitude.setText(String.format("%s", latLng.latitude));
        longitude.setText(String.format("%s", latLng.longitude));
        height.setText("0");
        elapsedTime.setText("30"); //TODO: Get this from settings

        action.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Target.Acciones a;

                if (id == R.array.accionesNoGrabando) {
                    switch (i) {
                        case 0:
                            a = Target.Acciones.NADA;
                            break;
                        case 1:
                            a = Target.Acciones.INICIA_GRABACION;
                            break;
                        case 2:
                            a = Target.Acciones.GRABAR_ESTE_PUNTO;
                            break;
                        case 3:
                            a = Target.Acciones.TOMAR_FOTO;
                            break;
                        default:
                            a = Target.Acciones.NADA;
                    }
                } else {
                    switch (i) {
                        case 0:
                            a = Target.Acciones.CONTINUA_GRABACION;
                            break;
                        case 1:
                            a = Target.Acciones.DETENER_GRABACION;
                            break;
                        case 2:
                            a = Target.Acciones.DETENER_GRABACION_Y_TOMAR_FOTO;
                            break;
                        default:
                            a = Target.Acciones.DETENER_GRABACION;
                    }
                }
                nTarget.setAccion(a);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if (id == R.array.accionesGrabando) {
                    nTarget.setAccion(Target.Acciones.CONTINUA_GRABACION);
                }
                if (id == R.array.accionesNoGrabando) {
                    nTarget.setAccion(Target.Acciones.NADA);
                }
            }
        });

        new AlertDialog.Builder(CrearRuta.this)
                .setView(menu)
                .setTitle(R.string.addTarget)
                .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        double h = Double.parseDouble(height.getText().toString().trim().replace(",", "."));
                        double t = Double.parseDouble(elapsedTime.getText().toString().trim().replace(",", "."));

                        nTarget.setHeight(h);
                        nTarget.setTime(t);
                        nTarget.placeAtMap(mMap);

                        recordingRoute.getCurrentTechnique().addTarget(nTarget);
                        recordingRoute.getCurrentTechnique().placeAtMap(mMap);

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
        updateUI();
    }

    /**
     * Display the menu to add a technique to the route.
     *
     * This method builds a menu that allows the user to pick a technique to add to the route.
     * All the options will be displayed and, once one is selected, {@link CrearRuta#showAddTechniqueMenu(TecnicaGrabacion, String)}
     * will be called. If the menu is cancelled, no changes will be applied.
     */
    private void displayAddMenu() {
        ScrollView addMenu = (ScrollView) getLayoutInflater().inflate(R.layout.add_menu, null);
        Button addHome = (Button) addMenu.findViewById(R.id.addHome);
        RecyclerView techniqueButtons = (RecyclerView) addMenu.findViewById(R.id.techniqueButtons);
        final AlertDialog alertdialog;



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.addItemtoMapTitle);
        builder.setView(addMenu);
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                updateUI();
            }
        });
        alertdialog = builder.create();

        //Creation of the recyclerview
        techniqueButtons.setLayoutManager(new LinearLayoutManager(this));
        HashMap<String,View.OnClickListener> techniqueListeners = new HashMap<>();
        techniqueListeners.put(getString(R.string.addAcimutal), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TechniqueAcimutal newTecnique = new TechniqueAcimutal(recordingRoute.isCurrentlyRecording());
                showAddTechniqueMenu(newTecnique,getString(R.string.addAcimutal));
                alertdialog.dismiss();
                updateUI();
            }
        });
        techniqueListeners.put(getString(R.string.addCrane), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TechniqueCrane newTecnique = new TechniqueCrane(recordingRoute.isCurrentlyRecording());
                showAddTechniqueMenu(newTecnique,getString(R.string.addCrane));
                alertdialog.dismiss();
                updateUI();
            }
        });

        techniqueButtons.setAdapter(new TechniqueAdapter(techniqueListeners));

        alertdialog.show();

        addHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertdialog.dismiss();
                settingHome = true;
                updateUI();
            }
        });
    }

    /**
     * This method serves to display the individual menu of each technique.
     *
     * This method takes a TecnicaGrabacion and a title as parameters, the TecnicaGrabacion
     * @param newTechnique
     * @param menuTitle
     */
    protected void showAddTechniqueMenu(final TecnicaGrabacion newTechnique, String menuTitle){

        new AlertDialog.Builder(CrearRuta.this)
                .setTitle(menuTitle)
                .setView(newTechnique.getInflatedLayout(getLayoutInflater()))
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        updateUI();
                    }
                })
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        recordingRoute.addTechnique(newTechnique);
                        updateUI();
                    }
                })
                .create()
                .show();
        updateUI();
    }

    /**
     * This method creates a savefile menu
     *
     * This menu allows the user to select a name for the file in wich to save the route. if the file already
     * exists, it will be overwritten. If the menu is cancelled, the menu will simply close and no further action will be taken.
     */
    private void showSaveMenu(){
        LinearLayout saveMenu = (LinearLayout) getLayoutInflater().inflate(R.layout.save_menu, null);
        final TextView editName = (TextView) saveMenu.findViewById(R.id.editName);
        editName.setText(recordingRoute.getName());
        new AlertDialog.Builder(this)
                .setTitle("Guardar Ruta")
                .setView(saveMenu)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        recordingRoute.setName(editName.getText().toString());
                        recordingRoute.saveRoute(CrearRuta.this);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();
    }


    /**
     * This method generates a information window for a target
     *
     * Once a target is clicked, if clickable, this method will be called.
     * It will build a view using the selected target information and return it. If the target is also
     * a camera, it will append two views to display the camera specific information as well.
     * @param marker Marker that is tapped
     * @return View of the information window with this target's values.
     */
    protected View buildInfoWindow(Marker marker){
        LinearLayout infoWindow = (LinearLayout) getLayoutInflater().inflate(R.layout.info_window_marker, null);
        Target o = (Target) marker.getTag();
        TextView latitud = (TextView) infoWindow.findViewById(R.id.latitud);
        TextView longitud = (TextView) infoWindow.findViewById(R.id.longitud);
        TextView altura = (TextView) infoWindow.findViewById(R.id.altura);
        TextView tiempo = (TextView) infoWindow.findViewById(R.id.tiempo);
        TextView accion = (TextView) infoWindow.findViewById(R.id.accion);
        TextView markerIndex = (TextView) infoWindow.findViewById(R.id.markerIndex);

        latitud.setText(String.format("%.4f", o.getLatitude()));
        longitud.setText(String.format("%.4f", o.getLongitude()));
        if (o instanceof RecordingRoute.Home){
            markerIndex.setText("HOME");
            altura.setVisibility(View.INVISIBLE);
            tiempo.setVisibility(View.INVISIBLE);
        } else {
            altura.setText(String.format("%.2f", o.getHeight()));
            tiempo.setText(String.format("%.2f", recordingRoute.getAbsoluteTimeOf(o)));
            accion.setText(o.getAccion().toString().replace("_", " "));

            if (o instanceof RoutePoint) {
                RoutePoint c = (RoutePoint) o;
                LinearLayout cameraInfo = (LinearLayout) getLayoutInflater().inflate(R.layout.info_camera_marker, null);
                TextView pitch, yaw, hSpeed, vSpeed, speedBearing, tSpeed;
                pitch = (TextView) cameraInfo.findViewById(R.id.pitch);
                yaw = (TextView) cameraInfo.findViewById(R.id.yaw);
                hSpeed = (TextView) cameraInfo.findViewById(R.id.hSpeed);
                vSpeed = (TextView) cameraInfo.findViewById(R.id.vSpeed);
                speedBearing = (TextView) cameraInfo.findViewById(R.id.speedBearing);
                tSpeed = (TextView) cameraInfo.findViewById(R.id.tSpeed);

                pitch.setText(String.format("%.2f", c.getPitch()));
                yaw.setText(String.format("%.2f", c.getYaw()));
                hSpeed.setText(String.format("%.2f", c.getSpeed().getVelocidadNESO()));
                vSpeed.setText(String.format("%.2f", c.getSpeed().getVertical()));
                speedBearing.setText(String.format("%.2f", c.getSpeed().getDireccion()));
                tSpeed.setText(String.format("%.2f", c.getSpeed().getModulo_v()));

                infoWindow.addView(cameraInfo);

                markerIndex.setText(String.format("RoutePoint #%d", recordingRoute.getIndexFromMarker(marker)));
            } else {
                markerIndex.setText(String.format("Target #%d", recordingRoute.getIndexFromMarker(marker)));
            }
        }

        return infoWindow;
    }

    /**
     * This adapter is used to populate the addMenu with buttons for each technique aviable.
      */
    public static class TechniqueAdapter extends RecyclerView.Adapter<TechniqueAdapter.ViewHolder>{
        HashMap<String,View.OnClickListener> techniqueListeners;
        ArrayList<String> titles;

        public static class ViewHolder extends RecyclerView.ViewHolder{
            public Button techniqueButton;
            public ViewHolder(Button b){
                super(b);
                techniqueButton = b;
            }
        }

        public TechniqueAdapter(HashMap<String,View.OnClickListener> techniqueListeners){
            this.techniqueListeners = techniqueListeners;
            titles = new ArrayList<>(techniqueListeners.keySet());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Button techniqueButton = new Button(parent.getContext());
            TechniqueAdapter.ViewHolder vh = new ViewHolder(techniqueButton);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String title = titles.get(position);
            View.OnClickListener listener = techniqueListeners.get(title);

            holder.techniqueButton.setText(title);
            holder.techniqueButton.setOnClickListener(listener);
        }

        @Override
        public int getItemCount() {
            return titles.size();
        }
    }
}
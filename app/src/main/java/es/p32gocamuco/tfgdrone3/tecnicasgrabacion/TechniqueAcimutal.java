package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import android.location.Location;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ListIterator;

import es.p32gocamuco.tfgdrone3.R;

/**
 * This class represents an acimutal technique
 *
 * In the acimutal technique, the camera is placed directly on top of the target at an specified height.
 * This technique will consider one height above the targets and follow the line they draw.
 *
 * There will be two methods to determine the bearing, it will either keep a constant heading relative
 * to true north or it will point towards the next target in the route, this is specified in {@link TechniqueAcimutal#setBearingFollowsRoute(boolean)}
 * Created by Manuel Gómez Castro on 2/07/17.
 */

public class TechniqueAcimutal extends  TecnicaGrabacion{
    private double heightOverTarget;
    private double bearing; //0 si el marco superior de la imagen coincide con el norte
    private boolean bearingFollowsRoute; //La cámara se ajusta para que el límite superior apunte al siguiente objetivo. No se tiene en cuenta si sólo hay un punto.
    private TechniqueReport report;


    public double getHeightOverTarget() {
        return heightOverTarget;
    }

    public double getBearing() {
        return bearing;
    }

    public TechniqueAcimutal(boolean startsWhileRecording){
        super(startsWhileRecording);
        heightOverTarget = 10;
        bearingFollowsRoute = false;
        bearing = 0;
    }
    @Override
    public TechniqueReport calculateRoute(double maxSpeed,double maxYawSpeed,double maxPitchSpeed,double minHeight, double maxHeight) {
        boolean minHeightChanged = false;
        boolean maxHeightChanged = false;
        boolean maxSpeedChanged = false;

        if (routePoints.size()>0){this.deleteRoutePoints();}
        ListIterator<Target> objectiveIterator = targets.listIterator();
        ListIterator<RoutePoint> cameraIterator; //No se define el iterador todavía porque aún no se ha calculado la posición de las cámaras.
        RoutePoint currentCamera;
        RoutePoint nextCamera;
        Target currentObjective;
        Target nextObjective;
        float[] results = new float[2];
        while(objectiveIterator.hasNext()){
            //Declaracion de variables que se usaran para generar esta ruta
            currentObjective = objectiveIterator.next();
            currentCamera = new RoutePoint(currentObjective);

            //Calculo de la posición de la cámara.
            currentCamera.setHeight(currentObjective.getHeight()+ heightOverTarget);
            if (currentCamera.getHeight() > maxHeight){
                currentCamera.setHeight(maxHeight);
                maxHeightChanged = true;
            }
            if (currentCamera.getHeight()<minHeight){
                currentCamera.setHeight(minHeight);
                minHeightChanged = true;
            }


            //Calculo de el ángulo de la camara
            currentCamera.setPitch(-90);
            if(!bearingFollowsRoute || (targets.size()==1)){
                currentCamera.setYaw(bearing);
            } else {
                if(!objectiveIterator.hasNext()){
                    currentCamera.setYaw(routePoints.get(routePoints.size()-1).getYaw());
                } else {
                    nextObjective = targets.get(objectiveIterator.nextIndex());
                    Location.distanceBetween(currentObjective.getLatitude(),currentObjective.getLongitude(),nextObjective.getLatitude(),nextObjective.getLongitude(),results);
                    currentCamera.setYaw(results[1]);
                }
            }

            routePoints.add(currentCamera);
            if (routePoints.size()>1){
                RoutePoint previousCamera = routePoints.get(routePoints.indexOf(currentCamera)-1);
                double minTime = RoutePoint.minTimeBetween(previousCamera,currentCamera,maxSpeed,maxYawSpeed,maxPitchSpeed);
                if (currentCamera.getTime()<minTime){
                    currentCamera.setTime(minTime);
                    currentObjective.setTime(minTime);
                    maxSpeedChanged = true;
                }
                previousCamera.calculateSpeedTowards(currentCamera);
            }
        }
        report = new TechniqueReport(this,minHeightChanged,maxHeightChanged,maxSpeedChanged);
        return report;
    }

    public void setHeightOverTarget(double heightOverTarget) {
        this.heightOverTarget = heightOverTarget;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public void setBearingFollowsRoute(boolean orientacionSegunObjetivo) {
        this.bearingFollowsRoute = orientacionSegunObjetivo;
    }

    @Override
    public View getInflatedLayout(LayoutInflater inflater){
        LinearLayout menu = (LinearLayout) inflater.inflate(R.layout.add_acimutal_menu, null);
        final EditText altura = (EditText) menu.findViewById(R.id.heightOverTarget);
        final EditText NESO = (EditText) menu.findViewById(R.id.bearing);
        final ToggleButton toggleSigueRuta = (ToggleButton) menu.findViewById(R.id.toggleSigueRuta);
        final TextView orientacionLabel = (TextView) menu.findViewById(R.id.orientacionNESOLabel);

        altura.setText(String.format("%s", this.getHeightOverTarget()));
        NESO.setText(String.format("%s", this.getBearing()));

        toggleSigueRuta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                compoundButton.setChecked(b);
                if (b) {
                    TechniqueAcimutal.this.setBearingFollowsRoute(true);
                    NESO.setVisibility(View.GONE);
                    orientacionLabel.setVisibility(View.GONE);
                } else {
                    TechniqueAcimutal.this.setBearingFollowsRoute(false);
                    orientacionLabel.setVisibility(View.VISIBLE);
                    NESO.setVisibility(View.VISIBLE);
                }
            }
        });

        altura.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (altura.getText().toString().trim().length() == 0) {
                    TechniqueAcimutal.this.setHeightOverTarget(10);
                } else {
                    TechniqueAcimutal.this.setHeightOverTarget(Double.parseDouble(altura.getText().toString().replace(",", ".")));
                }
            }
        });

        NESO.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (NESO.getText().toString().trim().length() == 0) {
                    TechniqueAcimutal.this.setBearing(0);
                } else {
                    TechniqueAcimutal.this.setBearing(Double.parseDouble(NESO.getText().toString().replace(",", ".")));
                }
            }
        });

        return menu;
    }

}
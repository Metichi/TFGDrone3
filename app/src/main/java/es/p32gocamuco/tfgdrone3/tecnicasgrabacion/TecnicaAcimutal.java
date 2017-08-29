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

/*
 * Created by Manuel Gómez Castro on 2/07/17.
 */

public class TecnicaAcimutal extends   TecnicaGrabacion{
    private double alturaSobreObjetivo;
    private double orientacionNESO; //0 si el marco superior de la imagen coincide con el norte
    private boolean orientacionSegunObjetivo; //La cámara se ajusta para que el límite superior apunte al siguiente objetivo. No se tiene en cuenta si sólo hay un punto.
    private TechniqueReport report;


    public double getAlturaSobreObjetivo() {
        return alturaSobreObjetivo;
    }

    public double getOrientacionNESO() {
        return orientacionNESO;
    }

    public TecnicaAcimutal(boolean startsWhileRecording){
        super(startsWhileRecording);
        alturaSobreObjetivo = 10;
        orientacionSegunObjetivo = false;
        orientacionNESO = 0;
    }
    @Override
    public TechniqueReport calculateRoute(double maxSpeed,double maxYawSpeed,double maxPitchSpeed,double minHeight, double maxHeight) {
        boolean minHeightChanged = false;
        boolean maxHeightChanged = false;
        boolean maxSpeedChanged = false;

        if (routePoints.size()>0){this.deleteWaypoints();}
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
            currentCamera.setHeight(currentObjective.getHeight()+alturaSobreObjetivo);
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
            if(!orientacionSegunObjetivo || (targets.size()==1)){
                currentCamera.setYaw(orientacionNESO);
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

    public void setAlturaSobreObjetivo(double alturaSobreObjetivo) {
        this.alturaSobreObjetivo = alturaSobreObjetivo;
    }

    public void setOrientacionNESO(double orientacionNESO) {
        this.orientacionNESO = orientacionNESO;
    }

    public void setOrientacionSegunObjetivo(boolean orientacionSegunObjetivo) {
        this.orientacionSegunObjetivo = orientacionSegunObjetivo;
    }

    @Override
    public View getInflatedLayout(LayoutInflater inflater){
        LinearLayout menu = (LinearLayout) inflater.inflate(R.layout.add_acimutal_menu, null);
        final EditText altura = (EditText) menu.findViewById(R.id.alturaSobreObjetivo);
        final EditText NESO = (EditText) menu.findViewById(R.id.orientacionNESO);
        final ToggleButton toggleSigueRuta = (ToggleButton) menu.findViewById(R.id.toggleSigueRuta);
        final TextView orientacionLabel = (TextView) menu.findViewById(R.id.orientacionNESOLabel);

        altura.setText(String.format("%s", this.getAlturaSobreObjetivo()));
        NESO.setText(String.format("%s", this.getOrientacionNESO()));

        toggleSigueRuta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                compoundButton.setChecked(b);
                if (b) {
                    TecnicaAcimutal.this.setOrientacionSegunObjetivo(true);
                    NESO.setVisibility(View.GONE);
                    orientacionLabel.setVisibility(View.GONE);
                } else {
                    TecnicaAcimutal.this.setOrientacionSegunObjetivo(false);
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
                    TecnicaAcimutal.this.setAlturaSobreObjetivo(10);
                } else {
                    TecnicaAcimutal.this.setAlturaSobreObjetivo(Double.parseDouble(altura.getText().toString().replace(",", ".")));
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
                    TecnicaAcimutal.this.setOrientacionNESO(0);
                } else {
                    TecnicaAcimutal.this.setOrientacionNESO(Double.parseDouble(NESO.getText().toString().replace(",", ".")));
                }
            }
        });

        return menu;
    }

}
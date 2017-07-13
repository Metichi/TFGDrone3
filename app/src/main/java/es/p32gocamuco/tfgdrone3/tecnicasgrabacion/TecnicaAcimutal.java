package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.Nullable;

import java.util.ListIterator;

import static es.p32gocamuco.tfgdrone3.tecnicasgrabacion.Target.Acciones;

/*
 * Created by Manuel Gómez Castro on 2/07/17.
 */

public class TecnicaAcimutal extends   TecnicaGrabacion{
    private double alturaSobreObjetivo;
    private double orientacionNESO; //0 si el marco superior de la imagen coincide con el norte
    private boolean orientacionSegunObjetivo; //La cámara se ajusta para que el límite superior apunte al siguiente objetivo. No se tiene en cuenta si sólo hay un punto.


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
    public void calculateRoute() {
        if (routePoints.size()>0){this.borrarRuta();}
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
            //

            routePoints.add(currentCamera);
        }

        //Iniciamos el iterador en la posición 1 en lugar de 0 para asegurarnos de que siempre hay un previous.
        if (routePoints.size() > 0) {
            cameraIterator = routePoints.listIterator(1);
            while (cameraIterator.hasNext()) {
                currentCamera = routePoints.get(cameraIterator.previousIndex());
                nextCamera = cameraIterator.next();
                currentCamera.calculaVelocidad(nextCamera);
            }
        }
    }

    @Override
    public void showTechniqueSettingsMenu(final Activity activity) { //TODO: Null point exception en los views
        /*
        LinearLayout menu = (LinearLayout) activity.findViewById(R.id.settingsAcimutal);
        final EditText altura = (EditText) activity.findViewById(R.id.alturaSobreObjetivo);
        final EditText NESO = (EditText) activity.findViewById(R.id.orientacionNESO);
        final ToggleButton toggleSigueRuta = (ToggleButton) activity.findViewById(R.id.toggleSigueRuta);
        final TextView orientacionLabel = (TextView) activity.findViewById(R.id.orientacionNESOLabel);
        AlertDialog alertDialog;

        altura.setText(Double.toString(this.alturaSobreObjetivo));
        NESO.setText(Double.toString(orientacionNESO));

        toggleSigueRuta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    orientacionSegunObjetivo = true;
                    NESO.setVisibility(View.GONE);
                    orientacionLabel.setVisibility(View.GONE);
                } else {
                    orientacionSegunObjetivo = false;
                    orientacionLabel.setVisibility(View.VISIBLE);
                    NESO.setVisibility(View.VISIBLE);
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.showAddAcimutalMenu);
        builder.setView(menu);
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (altura.getText().toString().trim().length()==0){
                            setAlturaSobreObjetivo(10);
                        } else {
                            setAlturaSobreObjetivo(Double.parseDouble(altura.getText().toString()));
                        }
                        if (NESO.getText().toString().trim().length() == 0){
                            setOrientacionNESO(0);
                        } else {
                            setOrientacionNESO(Double.parseDouble(NESO.getText().toString()));
                        }
                    }
                });

                alertDialog = builder.create();
        alertDialog.show();
        */
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
}
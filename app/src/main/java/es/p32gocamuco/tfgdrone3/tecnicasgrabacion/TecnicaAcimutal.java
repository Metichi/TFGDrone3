package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.ListIterator;

import es.p32gocamuco.tfgdrone3.CrearRuta;
import es.p32gocamuco.tfgdrone3.R;

import static android.view.View.GONE;
import static es.p32gocamuco.tfgdrone3.tecnicasgrabacion.Objetivo.Acciones;

/*
 * Created by Manuel Gómez Castro on 2/07/17.
 */

public class TecnicaAcimutal implements  TecnicaGrabacion{
    private ArrayList<Objetivo> objectives;
    private ArrayList<Camara> cameras;
    private double alturaSobreObjetivo;
    private double orientacionNESO; //0 si el marco superior de la imagen coincide con el norte
    private boolean orientacionSegunObjetivo; //La cámara se ajusta para que el límite superior apunte al siguiente objetivo. No se tiene en cuenta si sólo hay un punto.
    private boolean comienzaGrabando = false;
    private boolean created = false;


    public TecnicaAcimutal(){
        alturaSobreObjetivo = 10;
        orientacionSegunObjetivo = false;
        orientacionNESO = 0;
        objectives = new ArrayList<>(0);
        cameras = new ArrayList<>(0);
    }

    @Override
    public void addObjetivo(Objetivo puntoActual) {
        Objetivo puntoAnterior;
        puntoActual.setCurrentTechnique(this);
        objectives.add(puntoActual);

        //Al añadir un punto, nos aseguramos de que la acción que sigue sea continuar la grabación si se estaba grabando.
        //También nos aseguramos de que sea coherente en el tiempo.
        if(objectives.size()>1){
            puntoAnterior = objectives.get(objectives.indexOf(puntoActual)-1);
            if (puntoActual.getTime() < puntoAnterior.getTime()){
                objectives.remove(puntoActual);
                puntoActual.setCurrentTechnique(null);
                throw new IllegalArgumentException("Los tiempos deben ser secuenciales");
            } else {
                if (puntoAnterior.getAccion() == Acciones.INICIA_GRABACION ||
                        puntoAnterior.getAccion() == Acciones.CONTINUA_GRABACION) {
                    puntoActual.setAccion(Acciones.CONTINUA_GRABACION);
                }
            }
        } else{
            if(comienzaGrabando){
                puntoActual.setAccion(Acciones.CONTINUA_GRABACION);
            }
        }

    }

    @Override
    public void calcularRuta() {
        ListIterator<Objetivo> objectiveIterator = objectives.listIterator();
        ListIterator<Camara> cameraIterator; //No se define el iterador todavía porque aún no se ha calculado la posición de las cámaras.
        Camara currentCamera;
        Camara nextCamera;
        Objetivo currentObjective;
        Objetivo nextObjective;
        float[] results = new float[2];
        while(objectiveIterator.hasNext()){
            //Declaracion de variables que se usaran para generar esta ruta
            currentObjective = objectiveIterator.next();
            currentCamera = new Camara(currentObjective);

            //Calculo de la posición de la cámara.
            currentCamera.setHeight(currentObjective.getHeight()+alturaSobreObjetivo);


            //Calculo de el ángulo de la camara
            currentCamera.setPitch(-90);
            if(!orientacionSegunObjetivo || (objectives.size()==1)){
                currentCamera.setYaw(orientacionNESO);
            } else {
                if(objectives.indexOf(currentObjective)== objectives.size()){
                    currentCamera.setYaw(cameras.get(cameras.size()).getYaw());
                } else {
                    nextObjective = objectives.get(objectiveIterator.nextIndex());
                    Location.distanceBetween(currentObjective.getLatitude(),currentObjective.getLongitude(),nextObjective.getLatitude(),nextObjective.getLongitude(),results);
                    currentCamera.setYaw(results[2]);
                }
            }
            //

            cameras.add(currentCamera);
        }

        cameraIterator = cameras.listIterator(2);
        while (cameraIterator.hasNext()){
            currentCamera = cameras.get(cameraIterator.previousIndex());
            nextCamera = cameraIterator.next();
            currentCamera.calculaVelocidad(nextCamera);
        }

    }

    @Override
    public void setAccionEnObjetivo(Objetivo o, Acciones a) {
        boolean grabacionEnCurso = checkIfGrabacionEnCurso(o);
        ListIterator<Objetivo> iterador = objectives.listIterator(objectives.indexOf(o));
        Objetivo sigObj;


        switch(a){
            case INICIA_GRABACION:
                if (!grabacionEnCurso){
                    o.setAccion(a);
                }
                break;
            case DETENER_GRABACION:
                if (grabacionEnCurso){
                    o.setAccion(a);}
                break;
            case TOMAR_FOTO:
                if(!grabacionEnCurso){
                    o.setAccion(a);
                }
                break;
            case NADA:
                switch (o.getAccion()){
                    //En caso de que se quiera eliminar una acción de iniciar grabación, se debe eliminar también la siguiente pausa de grabación.
                    case INICIA_GRABACION:

                        while (iterador.hasNext()){
                            sigObj = iterador.next();
                            if(sigObj.getAccion()==Acciones.DETENER_GRABACION){
                                sigObj.setAccion(Acciones.NADA);
                            }
                        }
                        o.setAccion(a);
                        break;

                    //En caso de que se quiera eliminar una detención, se debe eliminar el siguiente inicio.
                    case DETENER_GRABACION:
                        iterador = objectives.listIterator(objectives.indexOf(o));

                        while (iterador.hasNext()){
                            sigObj = iterador.next();
                            if(sigObj.getAccion()==Acciones.INICIA_GRABACION){
                                sigObj.setAccion(Acciones.NADA);
                            }
                        }
                        o.setAccion(a);
                        break;
                    default:
                        o.setAccion(a);
                }

        }
    }
    private boolean checkIfGrabacionEnCurso(Objetivo o){
        return o.getAccion() == Acciones.CONTINUA_GRABACION || o.getAccion() == Acciones.INICIA_GRABACION;
    }

    @Override
    public void borrarObjetivo(@Nullable Objetivo o) {
        objectives.remove(o);
    }

    @Override
    public Objetivo[] verObjetivos() {
        return (Objetivo[]) objectives.toArray();
    }

    @Override
    public Camara[] verRuta() {
        return (Camara[]) cameras.toArray();
    }

    @Override
    public void modificarObjetivo(Objetivo nuevo, @Nullable Objetivo original) {
        if (original != null){
            objectives.set(objectives.indexOf(original),nuevo);
        } else {
            objectives.set(objectives.size()-1,nuevo);
        }

    }

    @Override
    public void borrarRuta() {
        cameras.clear();
    }

    @Override
    public void comienzaGrabando(boolean grabando) {
        comienzaGrabando = grabando;
    }

    @Override
    public boolean finalizaGrabando() {
        Objetivo ultimoPunto = objectives.get(objectives.size());
        return ((ultimoPunto.getAccion() == Acciones.CONTINUA_GRABACION) || (ultimoPunto.getAccion() == Acciones.INICIA_GRABACION));
    }

    @Override
    public void showTechniqueSettingsMenu(final Activity activity) { //TODO: Null point exception en los views
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
        builder.setTitle(R.string.addAcimutal);
        builder.setView(menu);
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                created = false;
            }
        });
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        created = true;
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
    }

    @Override
    public int getNumberCameras() {
        return cameras.size();
    }

    @Override
    public int getNumberObjectives() {
        return objectives.size();
    }

    @Override
    public int getIndexOf(Objetivo o) {
        if (o instanceof Camara){
            return cameras.indexOf(o);
        } else {
            return objectives.indexOf(o);
        }
    }

    @Override
    public Objetivo getPreviousObjective(Objetivo o) {
        int indexOfO = objectives.indexOf(o);
        if (indexOfO == 0){
            return null;
        } else {
            return objectives.get(indexOfO - 1);
        }
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
    public boolean createdSuccesfully() {
        return created;
    }
}
package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static es.p32gocamuco.tfgdrone3.tecnicasgrabacion.Target.Acciones;

/**
 * Created by Manuel Gómez Castro on 2/07/17.
 * Esta técnica define los métodos que debe tener una técnica de grabación.
 */

public abstract class TecnicaGrabacion implements Serializable {
    protected PolylineOptions polylineOptions;
    protected Polyline polyline;
    protected boolean startsWhileRecording;
    protected ArrayList<Target> targets;
    protected ArrayList<RoutePoint> routePoints;

    public TecnicaGrabacion(boolean startsWhileRecording){
        targets = new ArrayList<>(0);
        routePoints = new ArrayList<>(0);
        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.CYAN);
        polylineOptions.width(5);
        this.startsWhileRecording = startsWhileRecording;
    }
    abstract public void calculateRoute();

    public void addTarget(Target puntoActual) {
        puntoActual.setCurrentTechnique(this);
        targets.add(puntoActual);
        polylineOptions.add(puntoActual.getLatLng());

        if(checkTargetActions(puntoActual) != null) {fixTargetActions(puntoActual);}
    }
    public void editTarget(Target newTarget, @Nullable Target oldTarget) {
        if (oldTarget != null){
            targets.set(targets.indexOf(oldTarget), newTarget);
        } else {
            targets.set(targets.size()-1, newTarget);
        }
    }

    public void deleteTarget(@Nullable Target o) {

        targets.remove(o);
    }

    public Target[] verObjetivos() {
        Object[] objects = targets.toArray();
        Target[] targets = new Target[objects.length];
        for(int i = 0; i <= objects.length-1;i++){
            targets[i] = (Target) objects[i];
        }
        return targets;
    }

    public RoutePoint[] verRuta() {
        Object[] objects = this.routePoints.toArray();
        RoutePoint[] routePoints = new RoutePoint[objects.length];
        int i = 0;
        for (Object o : objects){
            routePoints[i] = (RoutePoint) o;
            i++;
        }
        return routePoints;
    }

    public void borrarRuta() {
        routePoints.clear();
    }

    public int getNumberCameras() {
        return routePoints.size();
    }


    public int getNumberObjectives() {
        return targets.size();
    }

    public int getIndexOf(Target o) {
        if (o instanceof RoutePoint){
            return routePoints.indexOf(o);
        } else {
            return targets.indexOf(o);
        }
    }

    public Target getPreviousObjective(Target o) {
        int indexOfO = targets.indexOf(o);
        if (indexOfO == 0){
            return null;
        } else {
            return targets.get(indexOfO - 1);
        }
    }

    public boolean finalizaGrabando(){
        return getCurrentlyRecording(getLastTarget());
    }
    public Target getLastTarget(){
        return targets.get(targets.size()-1);
    }

    public boolean getCurrentlyRecording(Target o){
        if (o.getAccion() == Acciones.DETENER_GRABACION_Y_TOMAR_FOTO ||
                o.getAccion() == Acciones.DETENER_GRABACION){
            return false;
        } else if (o.getAccion()==Acciones.INICIA_GRABACION){
            return true;
        } else {
            return getPreviouslyRecording(o);
        }
    }
    public boolean getPreviouslyRecording(Target o){
        if (targets.contains(o)) {
            if (targets.indexOf(o) == 0) {
                return startsWhileRecording;
            } else {
                return getCurrentlyRecording(targets.get(targets.indexOf(o) - 1));
            }
        } else {
            if (targets.size() == 0){
                return startsWhileRecording;
            } else {
                return getCurrentlyRecording(targets.get(targets.size()-1));
            }
        }
    }


    abstract void showTechniqueSettingsMenu(Activity activity);


    public Polyline getPolyline() {
        return polyline;
    }


    public void setPolyline(Polyline polyline) {
        if (this.polyline != null) {this.polyline.remove();} //Borrar la polyline anterior cuando se actalice el valor.
        this.polyline = polyline;
        this.polyline.setTag(this);
    }

    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }
    public Target checkTargetActions(Target t){
        //Este método comprueba si la acción en un objetivo es legal.
        //Tras iniciar grabación, sólo se puede parar o continuar la grabacion.
        //Si se encuentra un fallo, se devuelve el objetivo que es incorrecto.
        if (getPreviouslyRecording(t)) {
            if ((t.getAccion() == Acciones.CONTINUA_GRABACION)
                    || (t.getAccion() == Acciones.DETENER_GRABACION)
                    || (t.getAccion() == Acciones.DETENER_GRABACION_Y_TOMAR_FOTO)) {
                return null;
            } else {
                return t;
            }
        } else {
            if ((t.getAccion() != Acciones.CONTINUA_GRABACION)
                    || (t.getAccion() != Acciones.DETENER_GRABACION)
                    || (t.getAccion() != Acciones.DETENER_GRABACION_Y_TOMAR_FOTO)) {
                return null;
            } else {
                return t;
            }
        }
    }
    public void fixTargetActions(Target t){
        //Si se está grabando, se continua grabando, si no, no se hace nada.
        if (targets.indexOf(t) > 0){
            t.setAccion(getCurrentlyRecording(targets.get(targets.indexOf(t)-1)) ? Acciones.CONTINUA_GRABACION : Acciones.NADA);
        } else {
            //si se trata del primer objetivo lo mismo, pero se comprueba startsWhile recording y no el objetivo anterior que no existe
            t.setAccion(startsWhileRecording ? Acciones.CONTINUA_GRABACION : Acciones.NADA);
        }
    }
    public void fixAllTargetActions(){
        ListIterator<Target> iterator = targets.listIterator();
        while (iterator.hasNext()){
            Target t = checkTargetActions(iterator.next());
            if (t != null) {fixTargetActions(t);}
        }
    }
}
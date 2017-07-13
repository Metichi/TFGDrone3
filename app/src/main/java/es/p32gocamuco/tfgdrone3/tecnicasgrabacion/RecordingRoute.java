package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

/*
 * Created by Manuel Gómez Castro on 4/07/17.
 */

import android.graphics.Color;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.ListIterator;

import es.p32gocamuco.tfgdrone3.R;

public class RecordingRoute {
    private ArrayList<TecnicaGrabacion> techniques = new ArrayList<>(0);
    private String name;
    private TecnicaGrabacion currentTechnique;
    private boolean routeReady = false;
    private Target home;
    private PolylineOptions polylineOptions; //Reflejan la ruta de todas las cámaras.
    private Polyline polyline;

    public RecordingRoute(){
        name = "Nueva Ruta";
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
        this.polyline.setTag(this);
    }

    public void addTechnique(TecnicaGrabacion t){
        currentTechnique = t;
        techniques.add(currentTechnique);
        routeReady = false;

        while (techniques.indexOf(currentTechnique)>0){
            if (techniques.get(techniques.indexOf(currentTechnique)-1).getNumberObjectives() == 0){
                techniques.remove(techniques.indexOf(currentTechnique)-1);
            } else {
                currentTechnique.comienzaGrabando(
                        techniques.get(techniques.indexOf(currentTechnique) - 1).finalizaGrabando());
                break;
            }
        }
    }
    public void removeTechnique(TecnicaGrabacion t){
        techniques.remove(t);
    }

    public void saveRoute(){} //TODO: Implementar esto
    public void loadRoute(){} //TODO: esto también


    public int getNumberObjectives(){
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        int size = 0;


        while (iterator.hasNext()){
            size += iterator.next().verObjetivos().length;
        }
        return size;
    }
    private Target[] getAllObjetivos(){
        Target[] targets = new Target[getNumberObjectives()];
        int i = 0;
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        while (iterator.hasNext()){

            Target[] o = iterator.next().verObjetivos();
            for(Target target : o){
                targets[i] = target;
                i++;
            }
        }
        return targets;

    }
    public Target getLastObjective(){
        Target[] targets = getAllObjetivos();
        int length = targets.length;
        if (length == 0){
            //En caso de que no haya targets, iniciamos con un objetivo 0 para que la función no devuelva null.
            return new Target();
        } else {
            return targets[length -1];
        }
    }
    public boolean isCurrentlyRecording(){
        Target lastObjective = getLastObjective();
        return lastObjective.getCurrentTechnique().getCurrentlyRecording(lastObjective);

    }

    public int getNumberCameras(){
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        int size = 0;
        while (iterator.hasNext()){
            size += iterator.next().verRuta().length;
        }
        return size;
    }
    public RoutePoint[] getRoute(){
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        RoutePoint[] cameras;
        cameras = new RoutePoint[getNumberCameras()];
        int i = 0;
        while (iterator.hasNext()){
            for(RoutePoint camera : iterator.next().verRuta()){
                cameras[i] = camera;
                i++;
            }
        }
        return cameras;

    }
    private Target getObjetivoFromMarker(Marker marker){
        if (marker.getTag() instanceof Target){
            return (Target) marker.getTag();
        } else {
            return null;
        }
    }
    private TecnicaGrabacion getTechniqueFromMarker(Marker marker){
        Target target = getObjetivoFromMarker(marker);

        if (target == null) {
            return null;
        } else {
            return target.getCurrentTechnique();
        }
    }
    public int getIndexFromMarker(Marker marker){
        Target o = getObjetivoFromMarker(marker);
        TecnicaGrabacion t = getTechniqueFromMarker(marker);
        int index = t.getIndexOf(o);
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator(techniques.indexOf(t));
        if (o instanceof RoutePoint){
            while (iterator.hasPrevious()){
                index += iterator.previous().getNumberCameras();
            }
        } else if (o != null){
            while (iterator.hasPrevious()){
                index += iterator.previous().getNumberObjectives();
            }
        }
        return index;
    }

    public void calculateRoute(){
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        while (iterator.hasNext()) {
            iterator.next().calcularRuta();
        }

        RoutePoint[] route = getRoute();
        if (route.length > 0){
            if(polyline != null) {polyline.remove();}
            routeReady = true;
            initPolylineOptions();
            for (RoutePoint waypoint : route){
                polylineOptions.add(waypoint.getLatLng());
            }
        } else {
            routeReady = false;
        }
    }
    private void initPolylineOptions(){
        polylineOptions = new PolylineOptions();
        polylineOptions.zIndex(1);
        polylineOptions.color(Color.RED);
        polylineOptions.width(8);
        polylineOptions.color(R.color.recordingRouteLine);

    }
    public boolean calcRouteAviable(){
        //Sólo se puede calcular la ruta si no se está editando ninguna técnica.
        boolean aviable = this.currentTechnique == null;
        if (!aviable) {this.routeReady = false;}
        return aviable;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TecnicaGrabacion getCurrentTechnique() {
        return currentTechnique;
    }

    public void setCurrentTechnique(@Nullable TecnicaGrabacion currentTechnique) {
        this.currentTechnique = currentTechnique;
    }

    public boolean getRouteReady(){
        return this.routeReady;
    }

    public void setHome(Target home) {
        this.home = home;
    }

    public Target getHome() {
        return home;
    }

    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }

    public Polyline getPolyline() {
        return polyline;
    }
}

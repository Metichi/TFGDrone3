package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

/*
 * Created by Manuel Gómez Castro on 4/07/17.
 */

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.ListIterator;

public class RecordingRoute {
    private ArrayList<TecnicaGrabacion> techniques = new ArrayList<>(0);
    private String name;
    private TecnicaGrabacion currentTechnique;
    private boolean routeReady = false;
    private Objetivo home;
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
    private Objetivo[] getAllObjetivos(){
        Objetivo[] objetivos = new Objetivo[getNumberObjectives()];
        int i = 0;
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        while (iterator.hasNext()){

            Objetivo[] o = iterator.next().verObjetivos();
            for(Objetivo objetivo : o){
                objetivos[i] = objetivo;
                i++;
            }
        }
        return objetivos;

    }
    public Objetivo getLastObjective(){
        Objetivo[] objetivos = getAllObjetivos();
        int length = objetivos.length;
        if (length == 0){
            //En caso de que no haya objetivos, iniciamos con un objetivo 0 para que la función no devuelva null.
            return new Objetivo();
        } else {
            return objetivos[length -1];
        }
    }
    public boolean isCurrentlyRecording(){
        Objetivo lastObjective = getLastObjective();
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
    public Camara[] getRoute(){
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        Camara[] cameras;
        cameras = new Camara[getNumberCameras()];
        int i = 0;
        while (iterator.hasNext()){
            for(Camara camera : iterator.next().verRuta()){
                cameras[i] = camera;
                i++;
            }
        }
        return cameras;

    }
    private Objetivo getObjetivoFromMarker(Marker marker){
        if (marker.getTag() instanceof Objetivo){
            return (Objetivo) marker.getTag();
        } else {
            return null;
        }
    }
    private TecnicaGrabacion getTechniqueFromMarker(Marker marker){
        Objetivo objetivo = getObjetivoFromMarker(marker);

        if (objetivo == null) {
            return null;
        } else {
            return objetivo.getCurrentTechnique();
        }
    }

    public void calculateRoute(){
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        while (iterator.hasNext()) {
            iterator.next().calcularRuta();
        }

        Camara[] route = getRoute();
        if (route.length > 0){
            if(polyline != null) {polyline.remove();}
            routeReady = true;
            polylineOptions = new PolylineOptions();
            polylineOptions.zIndex(1);
            for (Camara waypoint : route){
                polylineOptions.add(waypoint.getLatLng());
            }
        } else {
            routeReady = false;
        }
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

    public void setHome(Objetivo home) {
        this.home = home;
    }

    public Objetivo getHome() {
        return home;
    }

    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }

    public Polyline getPolyline() {
        return polyline;
    }
}

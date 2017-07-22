package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

/*
 * Created by Manuel Gómez Castro on 4/07/17.
 */

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import es.p32gocamuco.tfgdrone3.DJIApplication;
import es.p32gocamuco.tfgdrone3.R;

public class RecordingRoute implements Serializable {
    private ArrayList<TecnicaGrabacion> techniques = new ArrayList<>(0);
    private String name;
    private TecnicaGrabacion currentTechnique;
    private boolean routeReady = false;
    private Target home;
    private transient PolylineOptions polylineOptions; //Reflejan la ruta de todas las cámaras.
    private transient Polyline polyline;
    private static final long serialVersionUID = 100L;

    public RecordingRoute(){
        name = "NuevaRuta";
    }

    public void placeAtMap(GoogleMap gMap){
        if(this.polyline!= null){this.polyline.remove();}
        this.polyline = gMap.addPolyline(this.polylineOptions);
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
                break;
            }
        }
    }
    public void removeTechnique(TecnicaGrabacion t){
        techniques.remove(t);
    }

    public boolean saveRoute(Context context){
        try{
            File path = context.getFilesDir();
            File savedRoute = new File(path,name+".adp");
            FileOutputStream fos = new FileOutputStream(savedRoute);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
            setResultToToast("Guardado: " + name + ".adp",context);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static RecordingRoute loadRoute(String filename, Context context){
        try {
            FileInputStream fis = context.openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object readObject = ois.readObject();
            ois.close();
            fis.close();

            if(readObject != null && readObject instanceof RecordingRoute){
                return (RecordingRoute) readObject;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //TODO: Probar si la apertura y guardado de archivos funcionan e implementar la clase Cargar Ruta


    public int getNumberObjectives(){
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        int size = 0;


        while (iterator.hasNext()){
            size += iterator.next().getTargets().length;
        }
        return size;
    }
    private Target[] getAllTargets(){
        Target[] targets = new Target[getNumberObjectives()];
        int i = 0;
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        while (iterator.hasNext()){

            Target[] o = iterator.next().getTargets();
            for(Target target : o){
                targets[i] = target;
                i++;
            }
        }
        return targets;

    }
    public Target getLastTarget(){
        int size = getNumberTechniques();
        if (size == 0){
            return new Target();
        } else {
            TecnicaGrabacion t = techniques.get(size-1);
            if (t.getNumberObjectives() == 0) {
                if (size == 1){
                    return new Target();
                } else {
                    t = techniques.get(size - 2);
                }
            }
            return t.getLastTarget();
        }
    }
    public boolean isCurrentlyRecording(){
        Target lastTarget = getLastTarget();
        return lastTarget.getTechnique().getCurrentlyRecording(lastTarget);

    }

    public int getNumberWaypoints(){
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        int size = 0;
        while (iterator.hasNext()){
            size += iterator.next().getWaypoints().length;
        }
        return size;
    }
    public RoutePoint[] getRoute(){
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        RoutePoint[] cameras;
        cameras = new RoutePoint[getNumberWaypoints()];
        int i = 0;
        while (iterator.hasNext()){
            for(RoutePoint camera : iterator.next().getWaypoints()){
                cameras[i] = camera;
                i++;
            }
        }
        return cameras;

    }
    public int getNumberTechniques(){
        return techniques.size();
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
            return target.getTechnique();
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
        for (TecnicaGrabacion t : techniques){
            t.calculateRoute();
        }

        if (getNumberWaypoints() > 0){
            routeReady = true;
            initPolylineOptions();
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
        if(polyline != null) {polyline.remove();}
        if (getNumberWaypoints() > 0) {
            for (RoutePoint waypoint : getRoute()) {
                polylineOptions.add(waypoint.getLatLng());
            }
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

    public void updateMap(GoogleMap gMap){
        for (TecnicaGrabacion t: techniques) {
            t.placeAtMap(gMap);
            Target[] targets = t.getTargets();
            for(Target target : targets){
                target.placeAtMap(gMap);
            }
        }
        RoutePoint[] route = getRoute();
        for (RoutePoint waypoint : route) {
            waypoint.placeAtMap(gMap);
        }
    }

    public void initMapOptions(){
        initPolylineOptions();
        routeReady = false;
        for (TecnicaGrabacion t : techniques){
            t.initMapOptions();
        }
    }

    public void setResultToToast(String string, Context context){
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }
}

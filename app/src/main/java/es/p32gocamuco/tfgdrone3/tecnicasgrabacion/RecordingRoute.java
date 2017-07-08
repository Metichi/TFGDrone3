package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

/*
 * Created by Manuel Gómez Castro on 4/07/17.
 */

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.ListIterator;

public class RecordingRoute {
    private ArrayList<TecnicaGrabacion> techniques = new ArrayList<>(0);
    private String name;
    private TecnicaGrabacion currentTechnique;

    public RecordingRoute(){
        name = "Nueva Ruta";
    }

    public void addTechnique(TecnicaGrabacion t){
        techniques.add(t);
        currentTechnique = t;
        if (techniques.indexOf(t)>0){
            techniques.get(techniques.indexOf(t)).comienzaGrabando(
                    techniques.get(techniques.indexOf(t)-1).finalizaGrabando());
        }
    }
    public void removeTechnique(TecnicaGrabacion t){
        techniques.remove(t);
    }

    public void saveRoute(){} //TODO: Implementar esto
    public void loadRoute(){} //TODO: esto también

    private Objetivo[] getAllObjetivos(){
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        int size = 0;
        Objetivo[] objetivos;

        while (iterator.hasNext()){
            size += iterator.next().verObjetivos().length;
        }
        objetivos = new Objetivo[size];
        iterator = techniques.listIterator();
        while (iterator.hasNext()){
            int i = 0;
            for(Objetivo objetivo : iterator.next().verObjetivos()){
                objetivos[i] = objetivo;
            }
        }
        return objetivos;

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
}

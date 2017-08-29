package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.io.Serializable;

/**
 * Target Class
 *
 * This class represents a target to record. A target is defined by its position, height, time, action and the
 * technique it belongs to.
 * Created by Manuel Gómez Castro on 1/07/17.
 */

public class Target implements Serializable {
    protected double latitude;
    protected double longitude;
    protected double height; //Altura del objeto desde el sistema de referencia (ej: suelo)
    protected double time; //Tiempo en el que se utiliza este objeto relativo al inicio de la sesion
    protected transient Marker marker;
    protected transient MarkerOptions markerOptions; //Almacena la posición del objetivo.
    protected TecnicaGrabacion currentTechnique;
    protected Acciones accion;
    public enum Acciones{
        INICIA_GRABACION,
        CONTINUA_GRABACION,
        DETENER_GRABACION,
        DETENER_GRABACION_Y_TOMAR_FOTO,
        TOMAR_FOTO,
        NADA,
        GRABAR_ESTE_PUNTO
    }



    public Target(){
        LatLng position = new LatLng(0,0);
        latitude = 0;
        longitude = 0;
        this.height = 0d;
        time =0;
        markerOptions = new MarkerOptions();
        markerOptions.position(position);
        accion = Acciones.NADA;
        currentTechnique = new TecnicaGrabacion(false) {
            @Override
            public TechniqueReport calculateRoute(double m, double n, double x, double y, double z) {
                return null;
            }

            @Override
            public View getInflatedLayout(LayoutInflater inflater) {
                return null;
            }

            @Override
            public boolean getCurrentlyRecording(Target o){
                return false;
            }
            @Override
            public Target getPreviousTarget(Target o){
                return new Target();
            }
        };
    }

    public Target(LatLng latlng, double height, double t){
        this.latitude = latlng.latitude;
        this.longitude = latlng.longitude;
        this.height = height;
        this.time = t;
        initMarkerOptions();
        accion = Acciones.NADA;
    }

    /**
     *This method returns the latitude of the target
     * @return Latitude of the target
     */
    public double getLatitude(){
        return this.latitude;
    }

    /**
     * This method returns the longitude of the target
     * @return Longitude of the target
     */
    public double getLongitude(){
        return this.longitude;
    }

    /**
     * This method retursn the latlng of the target as a {@link LatLng}
     * @return Latitude and longitude
     */
    public LatLng getLatLng(){
        return new LatLng(latitude,longitude);
    }

    /**
     * This method returns the height of the target.
     * Height is measured relative to the takeoff position since it will be compared against the drone's
     * measured height wich is relative to takeoff.
     * @return Height of the target
     */
    public double getHeight(){
        return height;
    }

    /**
     * This method will return the time of the target
     * The time is measured relative to the previous target.
     * @return Time of the target
     */
    public double getTime() {return time;}

    /**
     * This method returns the action taken at the target
     * @return Action taken at the target
     */
    public Acciones getAccion() {
        return accion;
    }

    public void setPosition(LatLng newPos){
        this.latitude = newPos.latitude;
        this.longitude = newPos.longitude;
        markerOptions.position(newPos);
    }

    public void setLatitude(double newLat){
        this.latitude = newLat;
        markerOptions.position(new LatLng(this.latitude, this.longitude));
    }
    public void setLongitude(double newLong){
        this.longitude = newLong;
        markerOptions.position(new LatLng(this.latitude, this.longitude));
    }
    public void setHeight(double newHeight){
        this.height = newHeight;
    }
    public void setTime(double t) {
        this.time =t;}

    public void setAccion(Acciones accion) {
        this.accion = accion;
    }

    public Marker getMarker() {
        return marker;
    }

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }

    /**
     * This method uses the information of the target to place it in the specified map.
     *
     * The marker added will have as its tag a reference to this object.
     * @see Marker#getTag()
     * @param gMap Map on wich to place the marker
     * @return Marker placed at the map
     */
    public Marker placeAtMap(GoogleMap gMap){
        if (this.marker != null) {this.marker.remove();} //Si estamos cambiando el marcador de este objetivo, debemos borrar el anterior del mapa.
        this.marker = gMap.addMarker(this.markerOptions);
        this.marker.setTag(this);
        return this.marker;
    }

    public void desplazar(double distancia, double direccion){
        /*
        Desplaza el objetivo una distancia en metros a lo largo de la dirección especificada.
        La dirección es 0º norte, 90 este, 180 sur y 270 oeste.
         */
        LatLng position = SphericalUtil.computeOffset(this.getLatLng(),distancia,direccion);
        this.markerOptions.position(position);
    }

    public void setCurrentTechnique(TecnicaGrabacion currentTechnique) {
        this.currentTechnique = currentTechnique;
    }

    /**
     * Returns the {@link TecnicaGrabacion} that uses this target
     * @return The recordingTechnique that uses this target.
     */
    public TecnicaGrabacion getTechnique() {
        return currentTechnique;
    }

    protected void initMarkerOptions(){
        this.markerOptions = new MarkerOptions();
        this.markerOptions.position(new LatLng(this.latitude,this.longitude));
        this.markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
    }

}

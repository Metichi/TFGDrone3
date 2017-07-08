package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

/*
 * Created by Manuel Gómez Castro on 1/07/17.
 */

public class Objetivo {
    private double height; //Altura del objeto desde el sistema de referencia (ej: suelo)
    private double time; //Tiempo en el que se utiliza este objeto relativo al inicio de la sesion
    private Marker marker;
    private MarkerOptions markerOptions; //Almacena la posición del objetivo.
    private TecnicaGrabacion currentTechnique;

    public enum Acciones{
        INICIA_GRABACION,
        CONTINUA_GRABACION,
        DETENER_GRABACION,
        TOMAR_FOTO,
        NADA,
        GRABAR_ESTE_PUNTO
    }
    private Acciones accion = Acciones.NADA;


    public Objetivo(){
        LatLng position = new LatLng(0,0);
        this.height = 0d;
        time =0;
        markerOptions = new MarkerOptions();
        markerOptions.position(position);
    }

    public Objetivo(LatLng latlng, double height,double t){
        this.height = height;
        this.time = t;
        this.markerOptions = new MarkerOptions();
        this.markerOptions.position(latlng);
    }

    public double getLatitude(){
        return this.markerOptions.getPosition().latitude;
    }
    public double getLongitude(){
        return this.markerOptions.getPosition().longitude;
    }
    public LatLng getLatLng(){
        return this.markerOptions.getPosition();
    }
    public double getHeight(){
        return height;
    }
    public double getTime() {return time;}

    public Acciones getAccion() {
        return accion;
    }

    public void setPosition(LatLng newPos){
        this.markerOptions.position(newPos);
    }

    public void setLatitude(double latitude){
        double oldLong = this.markerOptions.getPosition().longitude;
        LatLng position = new LatLng(latitude,oldLong);
        this.markerOptions.position(position);
    }
    public void setLongitude(double longitude){
        double oldLat = this.markerOptions.getPosition().latitude;
        LatLng position = new LatLng(oldLat,longitude);
        this.markerOptions.position(position);
    }
    public void setHeight(double height){
        this.height = height;
    }
    public void setTime(double t) {
        this.time =t;}

    public void setAccion(Acciones accion) {
        this.accion = accion;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
        marker.setTag(this);
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

    public TecnicaGrabacion getCurrentTechnique() {
        return currentTechnique;
    }
}

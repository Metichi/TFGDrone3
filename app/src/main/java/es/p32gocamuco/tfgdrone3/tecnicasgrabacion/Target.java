package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.io.Serializable;

/*
 * Created by Manuel Gómez Castro on 1/07/17.
 */

public class Target implements Serializable {
    private double height; //Altura del objeto desde el sistema de referencia (ej: suelo)
    private double time; //Tiempo en el que se utiliza este objeto relativo al inicio de la sesion
    private Marker marker;
    private MarkerOptions markerOptions; //Almacena la posición del objetivo.
    private TecnicaGrabacion currentTechnique;
    private Acciones accion;
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
        this.height = 0d;
        time =0;
        markerOptions = new MarkerOptions();
        markerOptions.position(position);
        accion = Acciones.NADA;
        currentTechnique = new TecnicaGrabacion() {
            @Override
            public void calcularRuta() {

            }

            @Override
            public void addObjetivo(Target o) {

            }

            @Override
            public void modificarObjetivo(Target nuevo, @Nullable Target original) {

            }

            @Override
            public void borrarObjetivo(@Nullable Target o) {

            }

            @Override
            public Target[] verObjetivos() {
                return new Target[0];
            }

            @Override
            public RoutePoint[] verRuta() {
                return new RoutePoint[0];
            }

            @Override
            public void borrarRuta() {

            }

            @Override
            public void setAccionEnObjetivo(Target o, Acciones a) {

            }

            @Override
            public int getNumberObjectives() {
                return 0;
            }

            @Override
            public int getNumberCameras() {
                return 0;
            }

            @Override
            public int getIndexOf(Target o) {
                return 0;
            }

            @Override
            public Target getPreviousObjective(Target o) {
                return null;
            }

            @Override
            public void comienzaGrabando(boolean grabando) {

            }

            @Override
            public boolean finalizaGrabando() {
                return false;
            }

            @Override
            public boolean getCurrentlyRecording(Target o) {
                return false;
            }

            @Override
            public void showTechniqueSettingsMenu(Activity activity) {

            }

            @Override
            public void setPolyline(Polyline polyline) {

            }

            @Override
            public Polyline getPolyline() {
                return null;
            }

            @Override
            public PolylineOptions getPolylineOptions() {
                return null;
            }
        };
    }

    public Target(LatLng latlng, double height, double t){
        this.height = height;
        this.time = t;
        this.markerOptions = new MarkerOptions();
        this.markerOptions.position(latlng);
        this.markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        accion = Acciones.NADA;
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

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }

    public void setMarker(Marker marker) {
        if (this.marker != null) {this.marker.remove();} //Si estamos cambiando el marcador de este objetivo, debemos borrar el anterior del mapa.
        this.marker = marker;
        this.marker.setTag(this);
    }

    public void setMarkerOptions(MarkerOptions markerOptions) {
        this.markerOptions = markerOptions;
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

    @Override
    public String toString(){
        String string;
        String posicion = String.format("Latitud: %f \n Longitud: %f \n",getLatitude(),getLongitude());
        String altura = String.format("Altura: %s \n",getHeight());
        String tiempo = String.format("Tiempo establecido: %s \n", getTime());
        String accion = String.format("Accion: %s \n", getAccion().toString());
        string = posicion + altura + tiempo +accion;
        return string;
    }
}

package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

/*
 * Created by Manuel Gómez Castro on 1/07/17.
 */

public class Objetivo {
    private LatLng posicion; //Posición del objeto, en latitud y longitud.
    private double altura; //Altura del objeto desde el sistema de referencia (ej: suelo)
    private double tiempo; //Tiempo en el que se utiliza este objeto relativo al inicio de la sesion

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
        posicion = new LatLng(0,0);
        altura = 0d;
        tiempo=0;
    }
    public Objetivo(double latitude, double longitude, double height,double t){
        posicion = new LatLng(latitude, longitude);
        altura = height;
        tiempo = t;
    }
    public Objetivo(LatLng latlng, double height,double t){
        posicion = latlng;
        altura = height;
        tiempo = t;
    }

    public double getLatitude(){
        return posicion.latitude;
    }
    public double getLongitude(){
        return posicion.longitude;
    }
    public LatLng getLatLng(){
        return posicion;
    }
    public double getAltura(){
        return altura;
    }
    public double getTiempo() {return tiempo;}

    public Acciones getAccion() {
        return accion;
    }

    public void setPosicion(LatLng newPos){
        posicion = newPos;
    }
    public void setPosicion(double latitude, double longitude){
        posicion = new LatLng(latitude,longitude);
    }
    public void setLatitude(double latitude){
        double oldLong = posicion.longitude;
        posicion = new LatLng(latitude,oldLong);
    }
    public void setLongitude(double longitude){
        double oldLat = posicion.latitude;
        posicion = new LatLng(oldLat,longitude);
    }
    public void setAltura(double height){
        altura = height;
    }
    public void setTiempo(double t) {tiempo=t;}

    public void setAccion(Acciones accion) {
        this.accion = accion;
    }

    public void desplazar(double distancia, double direccion){
        /*
        Desplaza el objetivo una distancia en metros a lo largo de la dirección especificada.
        La dirección es 0º norte, 90 este, 180 sur y 270 oeste.
         */
        posicion = SphericalUtil.computeOffset(this.getLatLng(),distancia,direccion);
    }
}

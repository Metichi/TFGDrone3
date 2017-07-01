package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

/**
 * Created by Manuel Gómez Castro on 1/07/17.
 */

public class Objetivo {
    private LatLng posicion;
    private double altura;

    public Objetivo(){
        posicion = new LatLng(0,0);
        altura = 0d;
    }
    public Objetivo(double latitude, double longitude, double height){
        posicion = new LatLng(latitude, longitude);
        altura = height;
    }
    public Objetivo(LatLng latlng, double height){
        posicion = latlng;
        altura = height;
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

    public void desplazar(double distancia,double direccion){
        /*
        Desplaza el objetivo una distancia en metros a lo largo de la dirección especificada.
        La dirección es 0º norte, 90 este, 180 sur y 270 oeste.
         */

        LatLng nPos = SphericalUtil.computeOffset(this.getLatLng(),distancia,direccion);
        posicion = nPos;
    }
}

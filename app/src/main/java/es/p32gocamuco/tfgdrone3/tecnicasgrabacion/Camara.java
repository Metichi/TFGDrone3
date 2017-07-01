package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;


import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import static java.lang.Math.atan;
import static java.lang.Math.floor;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by Manuel Gómez Castro on 1/07/17.
 */

public class Camara extends Objetivo {
    private double pitch; //Entre 0 y -90
    private double yaw; //Entre 0 y 360
    private double roll; //Entre -45 y 45
    private double distanciaFocal; //Distancia al objetivo en metros
    private double velocidad; //Velocidad de la cámara en este punto
    private double direccion; //Dirección de la velocidad de la cámara entre 0 y 360 grados.


    public Camara(){
        super();
        pitch = 0;
        yaw = 0;
        roll = 0;
    }
    public Camara(double latitud, double longitud, double height, double ptch, double yw, double rll){
        super(latitud,longitud,height);
        initPitchYawRoll(ptch,yw,rll);
    }
    public Camara(LatLng latlng, double height, double ptch, double yw, double rll){
        super(latlng,height);
        initPitchYawRoll(ptch,yw,rll);
    }

    private void initPitchYawRoll(double ptch, double yw, double rll){

        yaw = (yw>360) ? (yw-floor(yw/360)*360):yw;

        if ((0>= ptch) && (ptch >= -90)){
            pitch = ptch;
        } else {
            throw new ArithmeticException("Pitch fuera de rango");
        }

        if ((45>= rll) && (rll >= -45)){
            roll = rll;
        } else {
            throw new ArithmeticException("Roll fuera de rango");
        }
    }


    public void setDistanciaFocal(double d){
        distanciaFocal= d;
    }

    public void enfocaOjbetivo(Objetivo o){
        float[] resultado = new float[2];
        double incrementoAltura = this.getAltura()-o.getAltura();

        if (incrementoAltura>=0) {

            Location.distanceBetween(this.getLatitude(), this.getLongitude(), o.getLatitude(), o.getLongitude(), resultado);
            //El primer elemento del vector resultado es la distancia más corta sobre la superficie de la tierra en metros
            //Dado que esto no tiene en cuenta las alturas, aproximamos la distancia como una recta y utilizamos pitagoras.
            distanciaFocal = sqrt(pow(resultado[1], 2) + pow(incrementoAltura, 2));


            yaw = resultado[2];

            //La cámara va a poder variar entre 0 y -90 grados.
            //Si la altura es positiva, y conocemos la distancia, sabemos el valor absoluto del angulo deseado.
            pitch = atan(incrementoAltura/resultado[1]);

        } else{
            throw new ArithmeticException("ALtura del objetivo es mayor que la de la camara") ;
        }
    }

    public double getDistanciaFocal(){
        return distanciaFocal;
    }
    public double getPitch(){
        return pitch;
    }
    public double getYaw(){
        return yaw;
    }
    public double getRoll(){
        return roll;
    }
}
package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;


import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/*
 * Created by Manuel Gómez Castro on 1/07/17.
 */

public class Camara extends Objetivo {
    private double pitch; //Entre 0 y -90
    private double yaw; //Entre 0 y 360
    private double roll; //Entre -45 y 45
    private double distanciaFocal; //Distancia al objetivo en metros
    private VelocidadNESO velocidad = new VelocidadNESO(0,0,0);


    public Camara(){
        super();
        this.pitch = 0;
        this.yaw = 0;
        this.roll = 0;
    }

    public Camara(LatLng latlng, double height, double ptch, double yw, double rll,double t){
        super(latlng,height,t);
        initPitchYawRoll(ptch,yw,rll);
    }
    public Camara(Objetivo o){
        super(o.getLatLng(),o.getHeight(),o.getTime());
        this.pitch = 0;
        this.yaw = 0;
        this.roll = 0;
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
        double incrementoAltura = this.getHeight()-o.getHeight();

        if (incrementoAltura>=0) {

            Location.distanceBetween(this.getLatitude(), this.getLongitude(), o.getLatitude(), o.getLongitude(), resultado);
            //El primer elemento del vector resultado es la distancia más corta sobre la superficie de la tierra en metros
            //Dado que esto no tiene en cuenta las alturas, aproximamos la distancia como una recta y utilizamos pitagoras.
            distanciaFocal = sqrt(pow(resultado[1], 2) + pow(incrementoAltura, 2));

            if(this.getLatLng() == o.getLatLng()) {
                yaw = 0;
            } else{
                yaw = resultado[2];
            }

            //La cámara va a poder variar entre 0 y -90 grados.
            //Si la altura es positiva, y conocemos la distancia, sabemos el valor absoluto del angulo deseado.
            try {
                pitch = -toDegrees(atan(incrementoAltura / resultado[1]));
            } catch (ArithmeticException e){
                pitch = -90;
            }

        } else{
            throw new IllegalArgumentException("ALtura del objetivo es mayor que la de la camara") ;
        }
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public void setRoll(double roll) {
        this.roll = roll;
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

    public void calculaVelocidad(Objetivo o){
        /* Calculamos la velocidad que haría falta para llegar hasta un objetivo (o camara) desde nuestro tiempo
            hasta el tiempo que pide el objetivo
         */
        if(o.getTime()<= this.getTime()){
            throw new IllegalArgumentException("La camara no puede viajar atras en el tiempo");
        } else {
            float[] resultado = new float[2];
            Location.distanceBetween(this.getLatitude(),this.getLongitude(),o.getLatitude(),o.getLongitude(),resultado);
            velocidad.setVelocidadNESO(resultado[1]);
            velocidad.setDireccion(resultado[2]);
            velocidad.setVertical((o.getHeight()-this.getHeight()/(o.getTime()-this.getTime())));
        }
    }

    public double fixToMaxSpeed(double maxSpeed){
        /*
        Este método ajusta la velocidad de la cámara para ajustarse a una velocidad máxima indicada
        y devuelve el factor de escala correspondiente.
        */
        if(maxSpeed >= velocidad.getModulo_v()){
            return 0;
        } else {
            double anguloAscenso = (velocidad.getVelocidadNESO() == 0) ? 90 : toDegrees(velocidad.getVertical()/velocidad.getVelocidadNESO());
            double velocidadPrevia = velocidad.getModulo_v();

            velocidad.setVelocidadNESO(maxSpeed*cos(toRadians(anguloAscenso)));
            velocidad.setVertical(maxSpeed*sin(toRadians(anguloAscenso)));

            return velocidadPrevia/velocidad.getModulo_v();
        }
    }
}
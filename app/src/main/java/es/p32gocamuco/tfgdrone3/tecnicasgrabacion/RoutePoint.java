package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;


import android.location.Location;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;

import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * This class represents a point in the recording route containing not only the position information but the camera orientation and speed.
 *
 *  This class extends {@link Target} and so shares all of its methods, as well as having fields for speed, pitch, yaw, roll and distance.
 *
 * Created by Manuel Gómez Castro on 1/07/17.
 */

public class RoutePoint extends Target implements Serializable{
    private double pitch; //Entre 0 y -90
    private double yaw; //Entre 0 y 360
    private double roll; //Entre -45 y 45
    private double distanciaFocal; //Distancia al objetivo en metros
    private VelocidadNESO speed;


    public RoutePoint(){
        super();
        this.pitch = 0;
        this.yaw = 0;
        this.roll = 0;
    }

    public RoutePoint(LatLng latlng, double height, double ptch, double yw, double rll, double t){
        super(latlng,height,t);
        initPitchYawRoll(ptch,yw,rll);
    }
    public RoutePoint(Target o){
        super(o.getLatLng(),o.getHeight(),o.getTime());
        this.pitch = 0;
        this.yaw = 0;
        this.roll = 0;
        this.setCurrentTechnique(o.getTechnique());
        this.setAccion(o.getAccion());
        speed = new VelocidadNESO(0,0,0);

        initMarkerOptions();
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


    public void setFocusDistance(double d){
        distanciaFocal= d;
    }

    /**
     * This method adjusts the orientation settings to point towards a target
     *
     * When called, this method will calculate the relative orientation of the target from the point
     * of view of the RoutePoint and adjusts the pitch yaw and focal distance of the RoutePoint.
     * @param o Target to focus
     */
    public void focusTarget(Target o){
        float[] resultado = new float[2];
        double incrementoAltura = max( this.getHeight()-o.getHeight(), 0); //El drone no puede apuntar hacia arriba, pero si se le pasa un argumento que le obligara hacer eso, irá lo más cerca posible.
            Location.distanceBetween(this.getLatitude(), this.getLongitude(), o.getLatitude(), o.getLongitude(), resultado);
            //El primer elemento del vector resultado es la distancia más corta sobre la superficie de la tierra en metros
            //Dado que esto no tiene en cuenta las alturas, aproximamos la distancia como una recta y utilizamos pitagoras.
            distanciaFocal = sqrt(pow(resultado[0], 2) + pow(incrementoAltura, 2));

            if(this.getLatLng() == o.getLatLng()) {
                yaw = 0;
            } else{
                yaw = resultado[1];
            }

            //La cámara va a poder variar entre 0 y -90 grados.
            //Si la altura es positiva, y conocemos la distancia, sabemos el valor absoluto del angulo deseado.
            try {
                pitch = -toDegrees(atan(incrementoAltura / resultado[1]));
            } catch (ArithmeticException e){
                pitch = -90;
            }
            markerOptions.rotation((float) this.yaw);
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
        markerOptions.rotation((float) this.yaw);
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

    /**
     * This method sets the speed of this route so it reaches the specified target in the specified
     * time.
     *
     * Given the next target or RoutePoint, this method sets the speed of this RoutePoint so it points
     * towards the objective and has a speed so it reaches the objective in the objective's time.
     * @param o Objective to go towards
     * @return Speed calculated.
     */
    public VelocidadNESO calculateSpeedTowards(Target o){

        float[] resultado = new float[2];
        Location.distanceBetween(this.getLatitude(),this.getLongitude(),o.getLatitude(),o.getLongitude(),resultado);
        speed.setVelocidadNESO(resultado[0]/(o.getTime()));
        speed.setDireccion(resultado[1]);
        speed.setVertical(((o.getHeight()-this.getHeight())/(o.getTime())));

        return speed;
    }

    /**
     * This method adjusts the speed at the routepoint to fit the maxSpeed.
     *
     * If the module of the speed is greater than maxSpeed, it will set the speeds so the module equals
     * maxSpeed without changing its direction and then it will return the factor by wich it changed.
     *
     * The factor is calculated as the previous module by the max speed.
     * If the speed is not greater than the maxSpeed, the speed will remain unchanged and the factor will be exactly 1
     * @param maxSpeed Maximum speed to wich it changes.
     * @return Change factor.
     */
    public double fixToMaxSpeed(double maxSpeed){
        /*
        Este método ajusta la speed de la cámara para ajustarse a una speed máxima indicada
        y devuelve el factor de escala correspondiente.
        */
        if(maxSpeed >= speed.getModulo_v()){
            return 1.0;
        } else {
            double anguloAscenso = (speed.getVelocidadNESO() == 0) ? 90 : toDegrees(atan(speed.getVertical()/ speed.getVelocidadNESO()));
            double velocidadPrevia = speed.getModulo_v();

            speed.setVelocidadNESO(maxSpeed*cos(toRadians(anguloAscenso)));
            speed.setVertical(maxSpeed*sin(toRadians(anguloAscenso)));

            return velocidadPrevia/ speed.getModulo_v();
        }
    }

    public VelocidadNESO getSpeed() {
        return speed;
    }

    public void setSpeed(VelocidadNESO speed) {
        this.speed = speed;
    }

    @Override
    public void initMarkerOptions() {
        super.initMarkerOptions();
        markerOptions.zIndex(1);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        markerOptions.flat(true);
        markerOptions.rotation((float) yaw);
    }

    /**
     * This method calculates the minimum ammount of time required to go from two route points.
     *
     * This method keeps in check the flight speed of the aircraft as well as its rotation speed, so
     * it calculates the time it would take to cover the distance between the two routepoints at maximum speed,
     * the time it would take to rotate around its yaw the specified ammount and the time it would take
     * to change its pitch and return the greater value.
     *
     * @param a Original RoutePoint
     * @param b Destination RoutePoint
     * @param maxLinearSpeed Maximum speed at wich the aircraft is allowed to fly.
     * @param maxYawSpeed Maximum speed at wich the aircraft is allowed to turn on its yaw.
     * @param maxPitchSpeed Maximum speed at wich the aircraft is allowed to turn on its pitch.
     * @return Greater ammount of time between the linear displacement time and the two rotation movements.
     */
    public static double minTimeBetween(RoutePoint a, RoutePoint b, double maxLinearSpeed,double maxYawSpeed,double maxPitchSpeed){
        float[] result = new float[1];
        Location.distanceBetween(a.getLatitude(),a.getLongitude(),b.getLatitude(),b.getLongitude(),result);
        double linearDistance = Math.sqrt(Math.pow(result[0],2)+Math.pow((a.getHeight()-b.getHeight()),2));
        double linearTime = linearDistance/maxLinearSpeed;

        double yawDistance = a.getYaw()-b.getYaw();
        double yawTime = Math.abs(yawDistance/maxYawSpeed);

        double pitchDistance = a.getPitch() - b.getPitch();
        double pitchTime = Math.abs(pitchDistance/maxPitchSpeed);

        double max = Math.max(linearTime,yawTime);
        return Math.max(max,pitchTime);
    }
}
package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

/**
 * This class describes an orbital shooting.
 *
 * The camera will go around each target in a polygon of n sides, determined by user, and a specified radius.
 * The camera will start at a constant height, perform a number of laps, begining and ending in angles relative
 * to true north specified by the user.
 * Created by Manuel Gómez Castro on 3/09/17.
 */

public class TechniqueOrbit extends TecnicaGrabacion {
    private double radius;
    private double heightOverTarget;
    private double entryAngle;
    private double exitAngle;
    private int laps;
    private int polygonPoints;
    private double speed;
    public static double HIGH_SPEED = 10;
    public static double MEDIUM_SPEED = 5;
    public static double LOW_SPEED = 2;

    public TechniqueOrbit(Boolean b){
        super(b);
        radius = 0;
        heightOverTarget = 0;
        entryAngle = 0;
        exitAngle = 0;
        laps = 0;
        polygonPoints = 10;
        speed = MEDIUM_SPEED;
    }
}

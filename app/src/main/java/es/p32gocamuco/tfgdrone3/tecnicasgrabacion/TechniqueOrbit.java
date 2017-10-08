package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import es.p32gocamuco.tfgdrone3.R;

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
    private boolean clockwise;
    public static double HIGH_SPEED = 10;
    public static double MEDIUM_SPEED = 5;
    public static double LOW_SPEED = 2;

    public TechniqueOrbit(Boolean b){
        super(b);
        radius = 10;
        heightOverTarget = 10;
        entryAngle = 0;
        exitAngle = 0;
        laps = 1;
        polygonPoints = 10;
        speed = MEDIUM_SPEED;
        clockwise = true;
    }

    @Override
    public View getInflatedLayout(LayoutInflater inflater) {
        View layout = inflater.inflate(R.layout.add_orbit_menu,null);

        RadioGroup speedSetting = (RadioGroup) layout.findViewById(R.id.speedSetting);
        RadioGroup clockwiseSetting = (RadioGroup) layout.findViewById(R.id.clockwiseSetting);
        EditText radiusEdit = (EditText) layout.findViewById(R.id.radius);
        EditText entryAngleEdit = (EditText) layout.findViewById(R.id.entryAngle);
        EditText exitAngleEdit = (EditText) layout.findViewById(R.id.exitAngle);
        EditText lapsEdit = (EditText) layout.findViewById(R.id.laps);
        EditText heightEdit = (EditText) layout.findViewById(R.id.height);
        EditText pointsEdit = (EditText) layout.findViewById(R.id.points);

        radiusEdit.setText(String.valueOf(this.radius));
        entryAngleEdit.setText(String.valueOf(this.entryAngle));
        exitAngleEdit.setText(String.valueOf(this.exitAngle));
        lapsEdit.setText(String.valueOf(this.laps));
        pointsEdit.setText(String.valueOf(this.polygonPoints));
        heightEdit.setText(String.valueOf(this.heightOverTarget));

        speedSetting.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i){
                    case R.id.lowSpeed:
                        speed = LOW_SPEED;
                        break;
                    case R.id.mediumSpeed:
                        speed = MEDIUM_SPEED;
                        break;
                    case R.id.highSpeed:
                        speed = HIGH_SPEED;
                        break;
                    default:
                        speed = MEDIUM_SPEED;
                }
            }
        });
        clockwiseSetting.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (i == R.id.clockwise){
                    clockwise = true;
                } else {
                    clockwise = false;
                }
            }
        });

        radiusEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                radius = Double.parseDouble(parseText(charSequence));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        entryAngleEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                entryAngle = Double.parseDouble(parseText(charSequence));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        exitAngleEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                exitAngle = Double.parseDouble(parseText(charSequence));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        heightEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                heightOverTarget = Double.parseDouble(parseText(charSequence));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        lapsEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                laps = Integer.parseInt(parseText(charSequence));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        pointsEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                polygonPoints = Integer.parseInt(parseText(charSequence));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return layout;
    }

    /**
     * Method that will convert a CharSequence param into an String representing a number.
     * @param charSequence value to represent
     * @return A string with a comma as decimal marker representing a number. It will be 0 if the CharSequence is empty.
     */
    private String parseText(CharSequence charSequence){
        String string = charSequence.toString();
        if (string.length()==0) {string = "0";}
        string.replace(".",",");
        return string;
    }

    @Override
    public TechniqueReport calculateRoute(double maxSpeed, double maxYawSpeed, double maxPitchSpeed, double minHeight, double maxHeight) {
        boolean maxSpeedCorrected = false;
        boolean minHeightCorrected = false;
        boolean maxHeightCorrected = false;

        //We calculate the ammount of RoutePoints needed
        double angularDistance = exitAngle - entryAngle;
        //The angular distance will be positive if clockwise
        if (clockwise){
            while (angularDistance < 0){
                angularDistance += 360;
            }
        } else {
            while (angularDistance > 0){
                angularDistance -= 360;
            }
        }
        double stepSize = 360.0/polygonPoints;
        int steps =((Double) (angularDistance/stepSize)).intValue();

        double timeStep = Math.toRadians(stepSize)*radius/speed;
        double finalTimeStep = Math.toRadians(Math.abs(angularDistance)-steps*stepSize)*radius/speed;

        steps += polygonPoints*laps;
        if (!clockwise){stepSize = -stepSize;}

        //We generate the routepoints
        for (Target target : targets){
            for (int i = 0; i <= steps; i++){
                double angle = entryAngle + i*stepSize;
                RoutePoint routePoint = new RoutePoint(target);
                routePoint.setHeight(routePoint.getHeight()+heightOverTarget);
                routePoint.desplazar(radius,angle);
                routePoint.focusTarget(target);
                if (i != 0){
                    routePoint.setTime(timeStep);
                }
                routePoints.add(routePoint);
            }
            RoutePoint routePoint = new RoutePoint(target);
            routePoint.setHeight(routePoint.getHeight()+heightOverTarget);
            routePoint.desplazar(radius,exitAngle);
            routePoint.focusTarget(target);
        }

        //We check the heights and speeds
        RoutePoint previousPoint = null;
        for (RoutePoint routePoint : routePoints){
            if (routePoint.getHeight() < minHeight){
                routePoint.setHeight(minHeight);
                minHeightCorrected = true;
            } else if (routePoint.getHeight()>maxHeight){
                routePoint.setHeight(maxHeight);
                maxHeightCorrected = true;
            }

            if (previousPoint != null){
                double minTime = RoutePoint.minTimeBetween(previousPoint,routePoint,maxSpeed,maxYawSpeed,maxPitchSpeed);
                if (routePoint.getTime()<minTime){
                    routePoint.setTime(minTime);
                    maxSpeedCorrected = true;
                }
                previousPoint.calculateSpeedTowards(routePoint);
            }
            previousPoint = routePoint;
        }

        TechniqueReport report = new TechniqueReport(this,minHeightCorrected,maxHeightCorrected,maxSpeedCorrected);

        return report;
    }
}

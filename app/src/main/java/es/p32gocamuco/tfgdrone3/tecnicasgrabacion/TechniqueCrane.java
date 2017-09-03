package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import es.p32gocamuco.tfgdrone3.R;

/**
 * This class defines a crane shot
 *
 * In a crane shot, the camera is set a distance away from the target and a specified height over it,
 * so it shoots at it from an angle.
 *
 * The camera will be placed, for each target, in a line that crosses the target oriented along attitude, relative to true north.
 * In that line, it will be placed an ammount of meters determined by distanceToTarget away from the target.
 * It will hover over that position at a height determined by height over target.
 *
 * Created by Manuel Gómez Castro on 3/09/17.
 */

public class TechniqueCrane extends TecnicaGrabacion {
    private double heightOverTarget;
    private double distanceToTarget;
    private double attitude;

    public TechniqueCrane(Boolean startsWhileRecording){
        super(startsWhileRecording);

        heightOverTarget = 0;
        distanceToTarget = 0;
        attitude = 0;
    }

    @Override
    public View getInflatedLayout(LayoutInflater inflater) {
        View layout = inflater.inflate(R.layout.add_crane_menu,null);
        EditText height = (EditText) layout.findViewById(R.id.height);
        final EditText distance = (EditText) layout.findViewById(R.id.distance);
        final EditText bearing = (EditText) layout.findViewById(R.id.bearing);

        height.setText(String.valueOf(this.heightOverTarget));
        distance.setText(String.valueOf(this.distanceToTarget));
        bearing.setText(String.valueOf(this.attitude));

        height.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()==0){
                    heightOverTarget = 0;
                } else {
                    heightOverTarget = Double.parseDouble(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        distance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()==0){
                    distanceToTarget = 0;
                } else {
                    distanceToTarget = Double.parseDouble(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        bearing.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()==0){
                    attitude = 0;
                } else {
                    attitude = Double.parseDouble(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return layout;
    }

    @Override
    public TechniqueReport calculateRoute(double maxSpeed, double maxYawSpeed, double maxPitchSpeed, double minHeight, double maxHeight) {
        boolean maxHeightCorrected = false;
        boolean minHeightCorrected = false;
        boolean maxSpeedCorrected = false;
        RoutePoint previousPoint = null;

        if (routePoints.size() > 0){
            deleteRoutePoints();
        }
        for (Target t : targets){
            RoutePoint point = new RoutePoint(t);
            point.setHeight(point.getHeight()+heightOverTarget);
            if(point.getHeight()> maxHeight){
                point.setHeight(maxHeight);
                maxHeightCorrected = true;
            }
            if(point.getHeight()< minHeight){
                point.setHeight(minHeight);
                minHeightCorrected = true;
            }
            point.desplazar(distanceToTarget,attitude);
            point.focusTarget(t);
            routePoints.add(point);

            if (previousPoint != null){
                double minTime = RoutePoint.minTimeBetween(previousPoint,point,maxSpeed,maxYawSpeed,maxPitchSpeed);
                if (minTime > point.getTime()){
                    point.setTime(minTime);
                    maxSpeedCorrected = true;
                }

                previousPoint.calculateSpeedTowards(point);
            }

            previousPoint = point;
        }

        TechniqueReport report = new TechniqueReport(this,minHeightCorrected,maxHeightCorrected,maxSpeedCorrected);
        return report;
    }
}

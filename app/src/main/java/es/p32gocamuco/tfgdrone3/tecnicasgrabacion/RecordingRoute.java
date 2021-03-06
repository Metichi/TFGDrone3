package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

/*
 * Created by Manuel Gómez Castro on 4/07/17.
 */

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import es.p32gocamuco.tfgdrone3.R;

public class RecordingRoute implements Serializable {
    private ArrayList<TecnicaGrabacion> techniques = new ArrayList<>(0);
    private String name;
    private TecnicaGrabacion currentTechnique;
    private boolean routeReady = false;
    private RouteReport report;

    private Home home;

    private transient PolylineOptions polylineOptions; //Reflejan la ruta de todas las cámaras.
    private transient Polyline polyline;
    private static final long serialVersionUID = 100L;

    public RecordingRoute(){
        name = "NuevaRuta";
    }

    /**
     * This method places the recording route in the map.
     *
     * The recording route is represented by a polyline that links all the waypoints together in order.
     * @param gMap map in wich the route is represented.
     */
    public void placeAtMap(GoogleMap gMap){
        if(this.polyline!= null){this.polyline.remove();}
        this.polyline = gMap.addPolyline(this.polylineOptions);
        this.polyline.setTag(this);
    }

    public void addTechnique(TecnicaGrabacion t){
        currentTechnique = t;
        techniques.add(currentTechnique);
        routeReady = false;

        while (techniques.indexOf(currentTechnique)>0){
            if (techniques.get(techniques.indexOf(currentTechnique)-1).getNumberObjectives() == 0){
                techniques.remove(techniques.indexOf(currentTechnique)-1);
            } else {
                break;
            }
        }
    }
    public void removeTechnique(TecnicaGrabacion t){
        techniques.remove(t);
    }

    /**
     * Adds time to a specific target and all targets afterwards.
     *
     * This method checks wether a target is in the route's target list and adds a time to it. Then
     * it continues following the list and adding the increased time to all the following targets to
     * ensure that they are all still lineal and they maintain the time dependencies to one another.
     *
     * This method does not replace the absolute time of the target but adds a specific ammount to the already
     * stored one.
     * @param t Target to wich to add time.
     * @param time Time to add.
     */
    public void addTimeTo(Target t, double time){
        boolean found = false;
        for (Target target : getAllTargets()){
            if (t == target) {found = true;}
            if(found){
                target.setTime(time);
            }
        }
    }

    public boolean isCurrentlyRecording(){
        Target lastTarget = getLastTarget();
        return lastTarget.getTechnique().getCurrentlyRecording(lastTarget);
    }

    /**
     * This method calculates the ammount of waypoints present in the route.
     *
     * This method does not calculate the points before return, so in order to get accurate results,
     * {@link RecordingRoute#calculateRoute(double, double, double,double, double)} should be called beforehand.
     * @return
     */
    public int getNumberWaypoints(){
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        int size = 0;
        while (iterator.hasNext()){
            size += iterator.next().getRoutePoints().length;
        }
        return size;
    }

    /**
     * This method returns all the waypoints in the route.
     *
     * This method does not calculate the points before return, so in order to get accurate results,
     * {@link RecordingRoute#calculateRoute(double,double,double, double, double)} should be called beforehand.
     * @return Array of {@link RoutePoint}
     */
    public RoutePoint[] getRoute(){
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        RoutePoint[] cameras;
        cameras = new RoutePoint[getNumberWaypoints()];
        int i = 0;
        while (iterator.hasNext()){
            for(RoutePoint camera : iterator.next().getRoutePoints()){
                cameras[i] = camera;
                i++;
            }
        }
        return cameras;

    }

    private Target getTargetFromMarker(Marker marker){
        if (marker.getTag() instanceof Target){
            return (Target) marker.getTag();
        } else {
            return null;
        }
    }
    private TecnicaGrabacion getTechniqueFromMarker(Marker marker){
        Target target = getTargetFromMarker(marker);

        if (target == null) {
            return null;
        } else {
            return target.getTechnique();
        }
    }

    /**
     * This method returns at wich position in the whole recording route is the Target or Waypoint
     * associated to a marker.
     * @param marker Marker with a target or waypoint associated.
     * @return Position in the overall route.
     */
    public int getIndexFromMarker(Marker marker){
        Target o = getTargetFromMarker(marker);
        TecnicaGrabacion t = getTechniqueFromMarker(marker);
        int index = t.getIndexOf(o);
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator(techniques.indexOf(t));
        if (o instanceof RoutePoint){
            while (iterator.hasPrevious()){
                index += iterator.previous().getNumberCameras();
            }
        } else if (o != null){
            while (iterator.hasPrevious()){
                index += iterator.previous().getNumberObjectives();
            }
        }
        return index;
    }

    /**
     * Calculates the recording route.
     *
     * This method goes through every technique and calls for its specific calculateRoute method.
     * This method also calculates the speed of the last RoutePoint of each technique in relation to the first
     * RoutePoint of the next technique. If the speed is too great, the speed will be adjusted and all the following targets will
     * be adjusted accordingly.
     * It also checks wether the final time of a target changes and applies that change to the rest of the route.
     * If the execution fails, or no waypoints are generated, {@link RecordingRoute#getRouteReady()} is set to false.
     * If the execution succeeds, it is set to true.
     * @param maxSpeed Maximum speed allowed in the route.
     * @param maxYawSpeed Maximum turning speed allowed for the drone along its vertical axis.
     * @param maxPitchSpeed Maximum turning speed allowed for the gimbal on the pitch.
     * @param minHeight Minimum height allowed in the route.
     * @param maxHeight Maximum height allowed in the route.
     * @return Report with stadistics to the route.
     * @see TecnicaGrabacion#calculateRoute(double,double,double, double, double)
     */
    public RouteReport calculateRoute(double maxSpeed, double maxYawSpeed, double maxPitchSpeed, double minHeight, double maxHeight){
        RouteReport r = null;
        if(calcRouteAviable()) {
            for (TecnicaGrabacion t : techniques) {
                if (r == null){
                    TecnicaGrabacion.TechniqueReport techniqueReport = t.calculateRoute(maxSpeed, maxYawSpeed, maxPitchSpeed ,minHeight,maxHeight);
                    r = new RouteReport(techniqueReport);}
                else {
                    TecnicaGrabacion.TechniqueReport techniqueReport = t.calculateRoute(maxSpeed, maxYawSpeed, maxPitchSpeed ,minHeight,maxHeight);
                    r.addReport(techniqueReport);
                }

                if(techniques.indexOf(t)>=1){
                    RoutePoint previous = techniques.get(techniques.indexOf(t)-1)
                            .getRoutePoints()[techniques.get(techniques.indexOf(t)-1).getRoutePoints().length-1];
                    RoutePoint next = t.getRoutePoints()[0];

                    double minTimeElapsed = RoutePoint.minTimeBetween(previous,next,maxSpeed,maxYawSpeed,maxPitchSpeed);
                    double timeElapsed = next.getTime();

                    if (timeElapsed < minTimeElapsed){
                        addTimeTo(next,timeElapsed-minTimeElapsed);
                        r.setMaxSpeedCorrected(true);
                    }

                    previous.calculateSpeedTowards(next);
                }
            }

            if (getNumberWaypoints() > 0) {
                routeReady = true;
                initPolylineOptions();
            } else {
                routeReady = false;
            }
        }
        report = r;
        return r;
    }

    /**
     * This method acts exactly as {@link RecordingRoute#calculateRoute(double,double,double, double, double)} but,
     * after executing and before returning, calls the {@link CalculationCompleteListener#onCalculationComplete(RouteReport)} method in the
     * specified listener.
     * @param listener Implementation of the {@link CalculationCompleteListener} interface.
     */
    public RouteReport calculateRoute(double maxSpeed, double maxYawSpeed, double maxPitchSpeed, double minHeight, double maxHeight, CalculationCompleteListener listener){
        RouteReport r = calculateRoute(maxSpeed,maxYawSpeed,maxPitchSpeed,minHeight,maxHeight);
        listener.onCalculationComplete(r);
        return r;
    }
    /**
     * Tells the user if it is possible to calculate the route.
     *
     * It is only possible to calculate the route, while no techniques are being edited. So this method checks
     * the status of {@link RecordingRoute#getCurrentTechnique()} and returns true if its null. In that case, it
     * also updates the status of {@link RecordingRoute#getRouteReady()} to false.
     * @return Wether its possible to calculate the route.
     */
    public boolean calcRouteAviable(){
        //Sólo se puede calcular la ruta si no se está editando ninguna técnica.
        boolean aviable = this.getCurrentTechnique() == null;
        if (!aviable) {this.routeReady = false;}
        return aviable;
    }

    /**
     * This method follows every technique and every target in them and updates their position on the map.
     * @param gMap
     * @see TecnicaGrabacion#placeAtMap(GoogleMap)
     * @see Target#placeAtMap(GoogleMap)
     * @see RecordingRoute#getRoute()
     */
    public void updateMap(GoogleMap gMap){
        for (TecnicaGrabacion t: techniques) {
            t.placeAtMap(gMap);
            Target[] targets = t.getTargets();
            for(Target target : targets){
                target.placeAtMap(gMap);
            }
        }
        RoutePoint[] route = getRoute();
        for (RoutePoint waypoint : route) {
            waypoint.placeAtMap(gMap);
        }
        if (home != null){ home.placeAtMap(gMap);}
    }

    /**
     * This method is used to innitialize the options for all the elements displayed on a map, such as
     * color, geometry, icon and width.
     *
     * This method calls {@link TecnicaGrabacion#initMapOptions()} for every technique present in the
     * route and {@link Home#initMapOptions()} for the homepoint if set.
     *
     * It also sets the appearance of the route polyline and innitializes it to the points that it might have.
     */
    public void initMapOptions(){
        initPolylineOptions();
        routeReady = false;
        if(!techniques.isEmpty()) {
            for (TecnicaGrabacion t : techniques) {
                t.initMapOptions();
            }
        }
        if (home != null){home.initMarkerOptions();}
    }

    private void initPolylineOptions(){
        polylineOptions = new PolylineOptions();
        polylineOptions.zIndex(1);
        polylineOptions.color(Color.RED);
        polylineOptions.width(8);
        polylineOptions.color(R.color.recordingRouteLine);
        if(polyline != null) {polyline.remove();}
        if (getNumberWaypoints() > 0) {
            for (RoutePoint waypoint : getRoute()) {
                polylineOptions.add(waypoint.getLatLng());
            }
        }
    }


    /**
     * This method returns the absolute time of a target
     *
     * The absolute time is the accumulated sum of all the times of all the previous targets. If the
     * target inputed is the first or is not in the targets this method will return 0.
     * @param t Target to get time from
     * @return Total elapsed time
     */
    public double getAbsoluteTimeOf(Target t){
        boolean contains = false;
        int index = 0;
        double time = 0;
        Target[] targets;
        if (t instanceof RoutePoint){
            targets = getRoute();
        } else {
            targets=getAllTargets();
        }
        for (Target target : targets){
            if (target == t){
                contains = true;
                break;
            }
            index++;
        }

        if (contains){
            for (int iterator = 1; iterator<=index; iterator++){
                time += targets[iterator].getTime();
            }
        }
        return time;
    }


    /**
     * Method used to easily display a short toast in the specified context
     * @param string Message to display
     * @param context Context to display into
     */
    public void setResultToToast(String string, Context context){
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }

    public static class Home extends Target{
        private double radius;
        private CircleOptions circleOptions;
        private Circle circle;
        public Home(LatLng location, double radius){
            super(location,0,0);
            this.radius = radius;
            initMarkerOptions();
        }

        public void setRadius(double radius) {
            this.radius = radius;
        }

        public double getRadius() {
            return radius;
        }

        @Override
        public void initMarkerOptions(){
            super.initMarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            circleOptions = new CircleOptions()
                    .clickable(false)
                    .radius(radius)
                    .center(this.getLatLng())
                    .strokeColor(R.color.circleColor);
        }

        @Override
        public Marker placeAtMap(GoogleMap gMap) {
            Marker marker = super.placeAtMap(gMap);
            if (circle != null) {circle.remove();}
            circleOptions.center(this.getLatLng());
            circle = gMap.addCircle(circleOptions);
            return marker;
        }
    }

    //region Getter and Setter
    public void setHome(Home h) {
        if (home != null){
            home.setPosition(h.getLatLng());
            home.setRadius(h.getRadius());

        } else {
            this.home = h;
        }
    }
    public Home getHome() {
        return home;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }
    public Polyline getPolyline() {
        return polyline;
    }
    public TecnicaGrabacion getCurrentTechnique() {
        return currentTechnique;
    }
    public void setCurrentTechnique(@Nullable TecnicaGrabacion currentTechnique) {
        this.currentTechnique = currentTechnique;
    }
    public int getNumberTechniques(){
        return techniques.size();
    }
    public int getNumberObjectives(){
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        int size = 0;


        while (iterator.hasNext()){
            size += iterator.next().getTargets().length;
        }
        return size;
    }
    private Target[] getAllTargets(){
        Target[] targets = new Target[getNumberObjectives()];
        int i = 0;
        ListIterator<TecnicaGrabacion> iterator = techniques.listIterator();
        while (iterator.hasNext()){

            Target[] o = iterator.next().getTargets();
            for(Target target : o){
                targets[i] = target;
                i++;
            }
        }
        return targets;

    }
    public Target getLastTarget(){
        int size = getNumberTechniques();
        if (size == 0){
            return new Target();
        } else {
            TecnicaGrabacion t = techniques.get(size-1);
            if (t.getNumberObjectives() == 0) {
                if (size == 1){
                    return new Target();
                } else {
                    t = techniques.get(size - 2);
                }
            }
            return t.getLastTarget();
        }
    }

    public RouteReport getReport() {
        return report;
    }

    /**
     * Returns wether the route is ready to be executed or not.
     * It is set to true once {@link RecordingRoute#calculateRoute(double,double,double, double, double)} is executed succesfully.
     * It is set to false once{@link RecordingRoute#calculateRoute(double,double,double, double, double)} is not executed succesfully.
     * It is set to false once{@link RecordingRoute#calcRouteAviable()} returns false.
     *
     * @return Wether the route is ready to execute.
     */
    public boolean getRouteReady(){
        return this.routeReady;
    }
    //endregion

    //region Load and Save
    public boolean saveRoute(Context context){
        try{
            File path = context.getFilesDir();
            File savedRoute = new File(path,name+".adp");
            FileOutputStream fos = new FileOutputStream(savedRoute);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
            setResultToToast("Guardado: " + name + ".adp",context);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static RecordingRoute loadRoute(String filename, Context context){
        try {
            FileInputStream fis = context.openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object readObject = ois.readObject();
            ois.close();
            fis.close();

            if(readObject != null && readObject instanceof RecordingRoute){
                return (RecordingRoute) readObject;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //endregion

    public static class RouteReport{
        private double minHeight;
        private double maxHeight;
        private double minSpeed;
        private double maxSpeed;
        private int routePointCount;
        private int targetCount;

        private boolean minHeightCorrected;
        private boolean maxHeightCorrected;
        private boolean maxSpeedCorrected;

        public RouteReport(TecnicaGrabacion.TechniqueReport t){
            this.minHeight = t.getMinHeight();
            this.maxHeight = t.getMaxHeight();
            this.minSpeed = t.getMinSpeed();
            this.maxSpeed = t.getMaxSpeed();
            this.routePointCount = t.getRoutePointCount();
            this.targetCount = t.getTargetCount();
            this.minHeightCorrected = t.isMinHeightCorrected();
            this.maxHeightCorrected = t.isMaxHeightCorrected();
            this.maxSpeedCorrected = t.isMaxSpeedCorrected();
        }

        public void addReport(TecnicaGrabacion.TechniqueReport t){
            this.minHeight = Math.min(this.minHeight,t.getMinHeight());
            this.maxHeight = Math.max(this.maxHeight,t.getMaxHeight());
            this.minSpeed = Math.min(this.minSpeed,t.getMinSpeed());
            this.maxSpeed = Math.max(this.maxSpeed,t.getMaxSpeed());

            this.routePointCount += t.getRoutePointCount();
            this.targetCount += t.getTargetCount();

            this.minHeightCorrected = t.isMinHeightCorrected()||this.minHeightCorrected;
            this.maxHeightCorrected = t.isMaxHeightCorrected()||this.maxHeightCorrected;
            this.maxSpeedCorrected = t.isMaxSpeedCorrected()||this.maxSpeedCorrected;
        }

        public int getTargetCount() {
            return targetCount;
        }

        public int getRoutePointCount() {
            return routePointCount;
        }

        public double getMaxHeight() {
            return maxHeight;
        }

        public double getMinHeight() {
            return minHeight;
        }

        public double getMaxSpeed() {
            return maxSpeed;
        }

        public double getMinSpeed() {
            return minSpeed;
        }

        public boolean isMinHeightCorrected() {
            return minHeightCorrected;
        }

        public boolean isMaxHeightCorrected() {
            return maxHeightCorrected;
        }

        public boolean isMaxSpeedCorrected() {
            return maxSpeedCorrected;
        }

        public void setMaxSpeedCorrected(boolean maxSpeedCorrected) {
            this.maxSpeedCorrected = maxSpeedCorrected;
        }
    }

    /**
     * This interface is used by the {@link RecordingRoute#calculateRoute(double,double,double, double, double, CalculationCompleteListener)} method
     * to allow the implementation of an action inmediatly upon route calculation.
     */
    public interface CalculationCompleteListener{
        void onCalculationComplete(RouteReport report);
    }
}

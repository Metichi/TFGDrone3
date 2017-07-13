package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import static es.p32gocamuco.tfgdrone3.tecnicasgrabacion.Target.Acciones;

/**
 * Created by Manuel Gómez Castro on 2/07/17.
 * Esta técnica define los métodos que debe tener una técnica de grabación.
 */

public interface TecnicaGrabacion {
    void calcularRuta();
    void addObjetivo(Target o);
    void modificarObjetivo(Target nuevo, @Nullable Target original);
    void borrarObjetivo(@Nullable Target o);

    Target[] verObjetivos();
    RoutePoint[] verRuta();
    void borrarRuta();
    void setAccionEnObjetivo(Target o, Acciones a);
    int getNumberObjectives();
    int getNumberCameras();
    int getIndexOf(Target o);
    Target getPreviousObjective(Target o);

    void comienzaGrabando(boolean grabando);
    boolean finalizaGrabando();
    boolean getCurrentlyRecording(Target o);

    void showTechniqueSettingsMenu(Activity activity);

    void setPolyline(Polyline polyline);
    Polyline getPolyline();
    PolylineOptions getPolylineOptions();
}
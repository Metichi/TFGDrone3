package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import android.app.Activity;
import android.support.annotation.Nullable;
import static es.p32gocamuco.tfgdrone3.tecnicasgrabacion.Objetivo.Acciones;

/**
 * Created by Manuel Gómez Castro on 2/07/17.
 * Esta técnica define los métodos que debe tener una técnica de grabación.
 */

public interface TecnicaGrabacion {
    void calcularRuta();
    void addObjetivo(Objetivo o);
    void modificarObjetivo(Objetivo nuevo, @Nullable Objetivo original);
    void borrarObjetivo(@Nullable Objetivo o);

    Objetivo[] verObjetivos();
    Camara[] verRuta();
    void borrarRuta();
    void setAccionEnObjetivo(Objetivo o, Acciones a);
    int getNumberObjectives();
    int getNumberCameras();
    int getIndexOf(Objetivo o);
    Objetivo getPreviousObjective(Objetivo o);

    void comienzaGrabando(boolean grabando);
    boolean finalizaGrabando();
    boolean createdSuccesfully();

    void showTechniqueSettingsMenu(Activity activity);
}
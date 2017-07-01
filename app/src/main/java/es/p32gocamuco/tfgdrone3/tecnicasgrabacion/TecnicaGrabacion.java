package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import android.support.annotation.FloatRange;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import dji.sdk.flightcontroller.FlightController;

/**
 * Created by Manuel Gomez Castro on 29/06/2017.
 *
 * Esta clase se utilizará como superclase para técnicas de grabación.
 * Una técnica va a estar definida por los siguientes campos
 * - Puntos de grabación objetivo: Están compuestos por latitud, longitud y altura y tiempo.
 * - Ubicación de la cámara: Latitud, longitud, altura y tiempo.
 * - Orientación de la cámara.
 *
 * Deberemos implementar métodos para calcular, a partir de la posición del objetivo,
 * y dependiendo de qué técnica se trate, la ubicación de
 */

public abstract class TecnicaGrabacion {
    private ArrayList<Objetivo> objetivos = new ArrayList<>(0);
    private ArrayList<Camara> camaras = new ArrayList<>(0);

    //Debemos inicializar el elemento de tiempos con el 0 siempre, y mostrar el tiempo absoluto en segundos.
    private ArrayList<Double> tiempos = new ArrayList<>(0);

    public TecnicaGrabacion(Objetivo o){
        objetivos.add(o);
    }

    public void addObjetivo(Objetivo o){
        objetivos.add(o);
    }
    public void quitarObjetivoN(int n){
        objetivos.remove(n);
    }
    public void reemplazarObjetivo(Objetivo o, int n){
        objetivos.set(n,o);
    }

    abstract public void calcularRuta();

    public void borrarRuta(){
        camaras.clear();
    }
    public void borrarTecnica(){
        camaras.clear();
        objetivos.clear();
    }

}

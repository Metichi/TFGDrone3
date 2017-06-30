package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import android.support.annotation.FloatRange;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

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
    private LatLng coordObjetivo;
    private Float alturaObjetivo;

    private class EstadoCamara {
        private List<LatLng> coordCamara;
        private List<Float> alturaCamara;


    }

    public abstract float calcularDistanciaCamara();
    
}

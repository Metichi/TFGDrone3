package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.ListIterator;

import static es.p32gocamuco.tfgdrone3.tecnicasgrabacion.Objetivo.Acciones;

/*
 * Created by Manuel Gómez Castro on 2/07/17.
 */

public class TecnicaAcimutal implements  TecnicaGrabacion{
    private ArrayList<Objetivo> puntosInteres = new ArrayList<>(0);
    private ArrayList<Camara> posicionCamara = new ArrayList<>(0);

    private double alturaSobreObjetivo;
    private double orientacionNESO; //0 si el marco superior de la imagen coincide con el norte
    private boolean orientacionSegunObjetivo; //La cámara se ajusta para que el límite superior apunte al siguiente objetivo. No se tiene en cuenta si sólo hay un punto.

    private boolean comienzaGrabando = false;


    public TecnicaAcimutal(){

    }

    @Override
    public void addObjetivo(Objetivo o) {
        puntosInteres.add(o);

        //Al añadir un punto, nos aseguramos de que la acción que sigue sea continuar la grabación si se estaba grabando.
        Objetivo puntoAnterior;
        Objetivo puntoActual = puntosInteres.get(puntosInteres.size()-1);
        if(puntosInteres.size()>1){
            puntoAnterior = puntosInteres.get(puntosInteres.size()-2);
            if(puntoAnterior.getAccion() == Acciones.INICIA_GRABACION ||
                    puntoAnterior.getAccion() == Acciones.CONTINUA_GRABACION){
                puntoActual.setAccion(Acciones.CONTINUA_GRABACION);
            }
        } else{
            if(comienzaGrabando){
                puntoActual.setAccion(Acciones.CONTINUA_GRABACION);
            }
        }
    }

    @Override
    public void calcularRuta() {
        ListIterator<Objetivo> iteradorObjetivo = puntosInteres.listIterator();
        ListIterator<Camara> iteradorCamara; //No se define el iterador todavía porque aún no se ha calculado la posición de las cámaras.
        Camara camaraActual;
        Camara camaraSiguiente;
        Objetivo objetivoActual;
        Objetivo objetivoSiguiente;
        float[] resultado = new float[2];
        while(iteradorObjetivo.hasNext()){
            //Declaracion de variables que se usaran para generar esta ruta
            objetivoActual = iteradorObjetivo.next();
            camaraActual = new Camara(objetivoActual);

            //Calculo de la posición de la cámara.
            camaraActual.setAltura(objetivoActual.getAltura()+alturaSobreObjetivo);


            //Calculo de el ángulo de la camara
            camaraActual.setPitch(-90);
            if(!orientacionSegunObjetivo || (puntosInteres.size()==1)){
                camaraActual.setYaw(orientacionNESO);
            } else {
                if(puntosInteres.indexOf(objetivoActual)==puntosInteres.size()){
                    camaraActual.setYaw(posicionCamara.get(posicionCamara.size()).getYaw());
                } else {
                    objetivoSiguiente = puntosInteres.get(iteradorObjetivo.nextIndex());
                    Location.distanceBetween(objetivoActual.getLatitude(),objetivoActual.getLongitude(),objetivoSiguiente.getLatitude(),objetivoSiguiente.getLongitude(),resultado);
                    camaraActual.setYaw(resultado[2]);
                }
            }
            //

            posicionCamara.add(camaraActual);
        }

        iteradorCamara = posicionCamara.listIterator(2);
        while (iteradorCamara.hasNext()){
            camaraActual = posicionCamara.get(iteradorCamara.previousIndex());
            camaraSiguiente = iteradorCamara.next();
            camaraActual.calculaVelocidad(camaraSiguiente);
        }

    }

    @Override
    public void setAccionEnObjetivo(Objetivo o, Acciones a) {
        boolean grabacionEnCurso = checkIfGrabacionEnCurso(o);
        ListIterator<Objetivo> iterador = puntosInteres.listIterator(puntosInteres.indexOf(o));
        Objetivo sigObj;


        switch(a){
            case INICIA_GRABACION:
                if (!grabacionEnCurso){
                    o.setAccion(a);
                }
                break;
            case DETENER_GRABACION:
                if (grabacionEnCurso){
                    o.setAccion(a);}
                break;
            case TOMAR_FOTO:
                if(!grabacionEnCurso){
                    o.setAccion(a);
                }
                break;
            case NADA:
                switch (o.getAccion()){
                    //En caso de que se quiera eliminar una acción de iniciar grabación, se debe eliminar también la siguiente pausa de grabación.
                    case INICIA_GRABACION:

                        while (iterador.hasNext()){
                            sigObj = iterador.next();
                            if(sigObj.getAccion()==Acciones.DETENER_GRABACION){
                                sigObj.setAccion(Acciones.NADA);
                            }
                        }
                        o.setAccion(a);
                        break;

                    //En caso de que se quiera eliminar una detención, se debe eliminar el siguiente inicio.
                    case DETENER_GRABACION:
                        iterador = puntosInteres.listIterator(puntosInteres.indexOf(o));

                        while (iterador.hasNext()){
                            sigObj = iterador.next();
                            if(sigObj.getAccion()==Acciones.INICIA_GRABACION){
                                sigObj.setAccion(Acciones.NADA);
                            }
                        }
                        o.setAccion(a);
                        break;
                    default:
                        o.setAccion(a);
                }

        }
    }
    private boolean checkIfGrabacionEnCurso(Objetivo o){
        return o.getAccion() == Acciones.CONTINUA_GRABACION || o.getAccion() == Acciones.INICIA_GRABACION;
    }

    @Override
    public void borrarObjetivo(@Nullable Objetivo o) {
        puntosInteres.remove(o);
    }

    @Override
    public Objetivo[] verObjetivos() {
        return (Objetivo[]) puntosInteres.toArray();
    }

    @Override
    public Camara[] verRuta() {
        return (Camara[]) posicionCamara.toArray();
    }

    @Override
    public void modificarObjetivo(Objetivo nuevo, @Nullable Objetivo original) {
        if (original != null){
            puntosInteres.set(puntosInteres.indexOf(original),nuevo);
        } else {
            puntosInteres.set(puntosInteres.size(),nuevo);
        }

    }

    @Override
    public void borrarRuta() {
        posicionCamara.clear();
    }

    @Override
    public void comienzaGrabando(boolean grabando) {
        comienzaGrabando = grabando;
    }

    @Override
    public boolean finalizaGrabando() {
        Objetivo ultimoPunto = puntosInteres.get(puntosInteres.size());
        return ((ultimoPunto.getAccion() == Acciones.CONTINUA_GRABACION) || (ultimoPunto.getAccion() == Acciones.INICIA_GRABACION));
    }

    @Override
    public void showTechniqueSettingsMenu(Context context) {

    }
}
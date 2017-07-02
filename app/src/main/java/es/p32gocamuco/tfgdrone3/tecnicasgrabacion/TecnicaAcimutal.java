package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

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



    public TecnicaAcimutal(double altura, double orientacion, boolean sigue){
        alturaSobreObjetivo = altura;
        orientacionNESO = orientacion;
        orientacionSegunObjetivo = sigue;
    }
    public TecnicaAcimutal(TecnicaGrabacion tecnicaAnterior,double altura, double orientacion, boolean sigue){
        this(altura,orientacion,sigue);
        comienzaGrabando = tecnicaAnterior.finalizaGrabando();
    }

    @Override
    public void addObjetivo(Objetivo o) {
        puntosInteres.add(o);
    }

    @Override
    public void calcularRuta() {
        ListIterator<Objetivo> iteradorObjetivo = puntosInteres.listIterator();
        ListIterator<Camara> iteradorCamara;
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
        int indiceActual = puntosInteres.indexOf(o);
        ListIterator<Objetivo> iterador = puntosInteres.listIterator(indiceActual);
        boolean grabacionEncurso = false;

        while (iterador.hasPrevious()){
            if(iterador.previous().getAccion() == Acciones.INICIA_GRABACION){
                grabacionEncurso = true;
                break;
            }
        }
        while(iterador.nextIndex()<indiceActual){
            if(iterador.next().getAccion()== Acciones.DETENER_GRABACION){
                grabacionEncurso = false;
                break;
            }
        }
        return grabacionEncurso;
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
        puntosInteres.clear();
        posicionCamara.clear();
    }

    @Override
    public boolean finalizaGrabando() {
        return (checkIfGrabacionEnCurso(puntosInteres.get(puntosInteres.size())) || puntosInteres.get(puntosInteres.size()).getAccion()==Acciones.INICIA_GRABACION);
    }
}

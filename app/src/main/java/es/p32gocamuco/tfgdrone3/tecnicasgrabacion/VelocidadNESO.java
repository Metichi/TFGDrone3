package es.p32gocamuco.tfgdrone3.tecnicasgrabacion;

/*
 * Created by Manuel Gómez Castro on 2/07/17.
 * Esta clase se usa para representar la velocidad de un objeto.
 */
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class VelocidadNESO {
    private double velocidadNESO; //Velocidad respecto al plano Norte Este Sur Oeste en metros por segundo
    private double direccion; //Ángulo de la velocidad respecto al plano NESO, siendo 0 el norte, 90 el este...
    private double vertical; //Velocidad de ascenso o descenso, positiva para velocidad hacia arriba en metros por segundo
    private double modulo_v; //Módulo de la velocidad obtenida a partir de la velocidad NESO y la vertical.

    public VelocidadNESO(double v, double d, double vert){
        velocidadNESO = v;
        direccion = d;
        vertical = vert;
        calculaModulo();
    }

    private void calculaModulo(){
        modulo_v = sqrt(pow(velocidadNESO,2)+pow(vertical,2));
    }

    public void setVelocidadNESO(double velocidadNESO) {
        this.velocidadNESO = velocidadNESO;
        calculaModulo();
    }

    public void setDireccion(double direccion) {
        this.direccion = direccion;
    }

    public void setVertical(double vertical) {
        this.vertical = vertical;
        calculaModulo();
    }

    public double getDireccion() {
        return direccion;
    }

    public double getModulo_v() {
        return modulo_v;
    }

    public double getVelocidadNESO() {
        return velocidadNESO;
    }

    public double getVertical() {
        return vertical;
    }
}

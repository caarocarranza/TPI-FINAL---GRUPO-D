package modelo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Cancha {
    private int numero;
    private TipoCancha tipo;
    private boolean disponibilidad;

    public Cancha(int numero, TipoCancha tipo,boolean disponibilidad) {
        this.numero = numero;
        this.tipo = tipo;
        this.disponibilidad = disponibilidad;
    }

    public int getNumero() {
        return numero;
    }

    public boolean getDisponibilidad(){
        return this.disponibilidad;
    }

    public TipoCancha getTipo() {
        return tipo;
    }

    public double getPrecio() {
        return tipo.getPrecio();
    }

    public boolean estaDisponible(LocalDate fecha, LocalTime hora, List<Reserva> reservas) {
        if (!disponibilidad) {
            return false;
        }

        for (Reserva reserva : reservas) {
            if (reserva.getCancha().getNumero() == numero
                    && reserva.seSuperpone(fecha, hora)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
    return "Cancha " + numero + " (" + tipo + ")";
    }
    
}



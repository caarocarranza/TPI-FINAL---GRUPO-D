package modelo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.Duration;

public class Reserva {
    private int numero;
    private Cliente cliente;
    private Cancha cancha;
    private LocalDate fecha;
    private LocalTime hora;
    private double monto;
    private int cantidadJugadores;
    private String estado;
    private Pago sena;
    private Pago saldo;
    private String resultadoCancelacion = "";

    public Reserva(int numero, Cliente cliente, Cancha cancha, LocalDate fecha,
                   LocalTime hora, MedioPago medio, int cantidadJugadores) {
        this.numero = numero;
        this.cliente = cliente;
        this.cancha = cancha;
        this.fecha = fecha;
        this.hora = hora;
        this.monto = cancha.getPrecio();
        this.cantidadJugadores = cantidadJugadores;
        this.estado = "CONFIRMADA";
        this.sena = new Pago(monto * 0.5, "SEÑA", medio);
        this.saldo = null;
    }

    public Reserva(int numero, Cliente cliente, Cancha cancha, LocalDate fecha, LocalTime hora,
                   double monto, String estado, MedioPago medioSena, String estadoSena,
                   MedioPago medioSaldo, String estadoSaldo, int cantidadJugadores) {
        this.numero = numero; this.cliente = cliente; this.cancha = cancha;
        this.fecha = fecha; this.hora = hora; this.monto = monto; this.estado = estado;
        this.cantidadJugadores = cantidadJugadores;
        this.sena = new Pago(monto * 0.5, "SEÑA", medioSena, estadoSena);
        this.saldo = medioSaldo == null ? null : new Pago(monto * 0.5, "SALDO", medioSaldo, estadoSaldo);
    }

    public boolean seSuperpone(LocalDate otraFecha, LocalTime otraHora) {
        if (!fecha.equals(otraFecha) || estado.equals("CANCELADA") || estado.equals("AUSENTE")) {
            return false;
        }

        int inicio = hora.toSecondOfDay() / 60;
        int otroInicio = otraHora.toSecondOfDay() / 60;
        return Math.abs(inicio - otroInicio) < 120;
    }

    public void cancelar(LocalDateTime ahora) {
        if (!estado.equals("CONFIRMADA")) {
            resultadoCancelacion = "La reserva no se puede cancelar porque su estado es " + estado + ".";
            return;
        }

        LocalDateTime inicioTurno = LocalDateTime.of(fecha, hora);
        long horas = Duration.between(ahora, inicioTurno).toHours();

        if (horas >= 24) {
            sena.reembolsar();
            resultadoCancelacion = "Cancelación realizada con 24 horas o más de anticipación. La seña de $"
                    + sena.getMonto() + " fue marcada como REEMBOLSADA.";
        } else {
            sena.retener();
            resultadoCancelacion = "Cancelación realizada con menos de 24 horas de anticipación. La seña de $"
                    + sena.getMonto() + " quedó RETENIDA.";
        }
        estado = "CANCELADA";
    }

    public String registrarAsistencia(boolean asistio, MedioPago medioSaldo) {
        if (!estado.equals("CONFIRMADA")) {
            return "La reserva ya fue cerrada.";
        }

        if (asistio) {
            if (medioSaldo == null) {
                return "Debe indicar el medio de pago del saldo.";
            }
            saldo = new Pago(monto * 0.5, "SALDO", medioSaldo);
            estado = "COMPLETADA";
            return "Asistencia registrada. La reserva quedó COMPLETADA.";
        } else {
            sena.retener();
            cliente.registrarInasistencia();
            estado = "AUSENTE";
            return "Inasistencia registrada. La seña quedó RETENIDA y se actualizó el contador del cliente.";
        }
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Cancha getCancha() {
        return cancha;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public int getNumero() {
    return numero;
    }

    public String getEstado() { return estado; }
    public double getMonto() { return monto; }
    public int getCantidadJugadores() { return cantidadJugadores; }
    public Pago getSena() { return sena; }
    public Pago getSaldo() { return saldo; }
    public String getResultadoCancelacion() { return resultadoCancelacion; }

    @Override
    public String toString() {
        return "Reserva " + numero + " | " + cancha + " | " + fecha + " " + hora
                + " | " + cliente.getNombreCompleto() + " | Jugadores: " + cantidadJugadores
                + " | $" + monto + " | " + estado;
    }
}

package controlador;

import modelo.*;
import persistencia.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class GestorReserva {
    public static final LocalTime HORA_APERTURA = LocalTime.of(8, 0);
    public static final LocalTime HORA_CIERRE = LocalTime.of(23, 0);
    public static final int DURACION_TURNO_MINUTOS = 120;
    public static final int CUPO_MAXIMO_JUGADORES = 4;

    private ArrayList<Cliente> clientes;
    private ArrayList<Cancha> canchas;
    private ArrayList<Reserva> reservas;
    private int numeroReserva = 1;
    private final RepositorioClientes repoClientes = new RepositorioClientes();
    private final RepositorioReservas repoReservas = new RepositorioReservas();
    private final RepositorioCanchas repoCanchas = new RepositorioCanchas();

    public GestorReserva() throws SQLException {
        repoCanchas.cargarPrecios();
        clientes = repoClientes.leerTodos();
        canchas = repoCanchas.leerTodas();
        if (canchas.isEmpty()) {
            registrarCanchaPorDefecto(1, TipoCancha.PARED);
            registrarCanchaPorDefecto(2, TipoCancha.PARED);
            registrarCanchaPorDefecto(3, TipoCancha.BLINDEX);
            registrarCanchaPorDefecto(4, TipoCancha.BLINDEX);
        }
        reservas = repoReservas.leerTodas(clientes, canchas);
        for (Reserva r : reservas) {
            if (r.getNumero() >= numeroReserva) numeroReserva = r.getNumero() + 1;
        }
    }

    public ArrayList<Cliente> getClientes() { return clientes; }
    public ArrayList<Cancha> getCanchas() { return canchas; }

    public Cliente buscarClientePorDni(int dni) {
        for (Cliente c : clientes) if (c.getDni() == dni) return c;
        return null;
    }

    public Cliente registrarCliente(String nombre, String apellido, String telefono, int dni) {
        if (!dniValido(dni)) {
            System.out.println("Error: el DNI debe tener exactamente 8 dígitos.");
            return null;
        }
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()
                || telefono == null || telefono.isBlank()) {
            System.out.println("Error: completá todos los datos del cliente.");
            return null;
        }
        if (buscarClientePorDni(dni) != null) {
            System.out.println("Error: ya existe un cliente con ese DNI.");
            return null;
        }
        Cliente cliente = new Cliente(nombre.trim(), apellido.trim(), telefono.trim(), dni);
        try {
            repoClientes.guardar(cliente);
            clientes.add(cliente);
            return cliente;
        } catch (SQLException e) {
            System.out.println("Error al guardar cliente: " + e.getMessage());
            return null;
        }
    }

    public static boolean dniValido(int dni) {
        return dni >= 10000000 && dni <= 99999999;
    }

    public boolean horarioValido(LocalDate fecha, LocalTime hora) {
        if (fecha == null || hora == null) return false;
        int minutosDeFin = hora.toSecondOfDay() / 60 + DURACION_TURNO_MINUTOS;
        int minutosDeCierre = HORA_CIERRE.toSecondOfDay() / 60;
        if (hora.isBefore(HORA_APERTURA) || minutosDeFin > minutosDeCierre) return false;
        if (fecha.isBefore(LocalDate.now())) return false;
        if (fecha.equals(LocalDate.now()) && !LocalDateTime.of(fecha, hora).isAfter(LocalDateTime.now())) return false;
        return true;
    }

    public ArrayList<Cancha> consultarDisponibilidad(LocalDate fecha, LocalTime hora) {
        ArrayList<Cancha> libres = new ArrayList<>();
        if (!horarioValido(fecha, hora)) return libres;
        for (Cancha cancha : canchas) {
            if (cancha.estaDisponible(fecha, hora, reservas)) {
                libres.add(cancha);
            }
        }
        return libres;
    }

    public Reserva registrarReserva(Cliente cliente, Cancha cancha, LocalDate fecha, LocalTime hora, MedioPago medio, int cantidadJugadores) {
        if (cliente == null || cancha == null || medio == null) {
            System.out.println("Error: faltan datos para registrar la reserva.");
            return null;
        }
        if (cantidadJugadores < 1 || cantidadJugadores > CUPO_MAXIMO_JUGADORES) {
            System.out.println("Error: cada turno permite entre 1 y 4 jugadores, incluido el cliente organizador.");
            return null;
        }
        if (!horarioValido(fecha, hora)) {
            System.out.println("Error: el club atiende de 08:00 a 23:00 y cada turno dura 120 minutos.");
            System.out.println("Los turnos pueden comenzar entre las 08:00 y las 21:00.");
            System.out.println("Además, no se pueden reservar fechas u horarios pasados.");
            return null;
        }
        if (!consultarDisponibilidad(fecha, hora).contains(cancha)) {
            System.out.println("Error: la cancha no está disponible en ese horario.");
            return null;
        }
        Reserva reserva = new Reserva(numeroReserva, cliente, cancha, fecha, hora, medio, cantidadJugadores);
        try {
            repoReservas.guardar(reserva);
            reservas.add(reserva);
            numeroReserva++;
            return reserva;
        } catch (SQLException e) {
            System.out.println("Error al guardar reserva: " + e.getMessage());
            return null;
        }
    }

    public Cancha buscarCanchaPorNumero(int numero) {
        for (Cancha c : canchas) if (c.getNumero() == numero) return c;
        return null;
    }

    public Cancha registrarCancha(int numero, TipoCancha tipo, boolean disponible) {
        if (numero <= 0) {
            System.out.println("Error: el número de cancha debe ser positivo.");
            return null;
        }
        if (buscarCanchaPorNumero(numero) != null) {
            System.out.println("Error: ya existe esa cancha.");
            return null;
        }
        Cancha cancha = new Cancha(numero, tipo, disponible);
        try {
            repoCanchas.guardar(cancha);
            canchas.add(cancha);
            return cancha;
        } catch (SQLException e) {
            System.out.println("Error al guardar cancha: " + e.getMessage());
            return null;
        }
    }

    public boolean eliminarCancha(int numero) {
        Cancha cancha = buscarCanchaPorNumero(numero);
        if (cancha == null) {
            System.out.println("No existe una cancha con ese número.");
            return false;
        }
        for (Reserva reserva : reservas) {
            if (reserva.getCancha().getNumero() == numero) {
                System.out.println("No se puede eliminar la cancha porque tiene reservas registradas (incluidas las históricas). ");
                System.out.println("Esto permite conservar el historial y evitar errores en MySQL.");
                return false;
            }
        }
        try {
            repoCanchas.eliminar(numero);
            canchas.remove(cancha);
            System.out.println("Cancha " + numero + " eliminada correctamente.");
            return true;
        } catch (SQLException e) {
            System.out.println("No se pudo eliminar la cancha: " + e.getMessage());
            return false;
        }
    }

    public Reserva buscarReservaPorNumero(int numero) {
        for (Reserva r : reservas) if (r.getNumero() == numero) return r;
        return null;
    }

    public void cancelarReserva(Reserva reserva, LocalDateTime ahora) {
        if (reserva == null) return;
        reserva.cancelar(ahora);
        guardarCambioReserva(reserva);
    }

    public void registrarAsistencia(Reserva reserva, boolean asistio, MedioPago medio) {
        if (reserva == null) return;
        LocalDateTime inicio = LocalDateTime.of(reserva.getFecha(), reserva.getHora());
        if (LocalDateTime.now().isBefore(inicio)) {
            System.out.println("No se puede registrar la asistencia antes del inicio del turno.");
            return;
        }
        String mensaje = reserva.registrarAsistencia(asistio, medio);
        System.out.println(mensaje);
        guardarCambioReserva(reserva);
        if (!asistio) {
            try { repoClientes.actualizar(reserva.getCliente()); }
            catch (SQLException e) { System.out.println("Error al actualizar inasistencias: " + e.getMessage()); }
        }
    }

    private void guardarCambioReserva(Reserva reserva) {
        try { repoReservas.actualizar(reserva); }
        catch (SQLException e) { System.out.println("Error al actualizar reserva: " + e.getMessage()); }
    }

    public void consultarReservas(Cliente cliente) {
        boolean encontro = false;
        for (Reserva r : reservas) {
            if (r.getCliente().getDni() == cliente.getDni()) {
                System.out.println(r);
                encontro = true;
            }
        }
        if (!encontro) System.out.println("El cliente no tiene reservas.");
    }

    public void modificarPrecio(TipoCancha tipo, double precio) {
        if (tipo == null || !Double.isFinite(precio) || precio <= 0) {
            System.out.println("Error: el precio debe ser un número válido mayor a cero.");
            return;
        }
        double precioAnterior = tipo.getPrecio();
        tipo.setPrecio(precio);
        try {
            repoCanchas.guardarPrecios();
            System.out.println("Precio actualizado correctamente.");
        } catch (SQLException e) {
            tipo.setPrecio(precioAnterior);
            System.out.println("Error al guardar el precio: " + e.getMessage());
        }
    }

    private void registrarCanchaPorDefecto(int numero, TipoCancha tipo) throws SQLException {
        Cancha cancha = new Cancha(numero, tipo, true);
        repoCanchas.guardar(cancha);
        canchas.add(cancha);
    }
}

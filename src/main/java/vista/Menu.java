package vista;

import controlador.GestorReserva;
import modelo.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Menu {
    private final GestorReserva sistema;
    private final Scanner teclado;

    public Menu(GestorReserva sistema) {
        this.sistema = sistema;
        this.teclado = new Scanner(System.in);
    }

    public void iniciar() {
        int opcion = -1;
        while (opcion != 0) {
            mostrarMenu();
            try {
                opcion = Integer.parseInt(teclado.nextLine().trim());
                switch (opcion) {
                    case 1: registrarCliente(); break;
                    case 2: listarClientes(); break;
                    case 3: registrarCancha(); break;
                    case 4: eliminarCancha(); break;
                    case 5: listarCanchas(); break;
                    case 6: consultarDisponibilidad(); break;
                    case 7: registrarReserva(); break;
                    case 8: consultarReservas(); break;
                    case 9: cancelarReserva(); break;
                    case 10: registrarAsistencia(); break;
                    case 11: modificarPrecio(); break;
                    case 0: System.out.println("Saliendo del sistema..."); break;
                    default: System.out.println("Opción incorrecta.");
                }
            } catch (NumberFormatException e) {
                System.out.println("ERROR: ingresá un número válido.");
            } catch (DateTimeParseException e) {
                System.out.println("ERROR: ingresá una fecha u hora válida.");
            } catch (Exception e) {
                System.out.println("No se pudo realizar la operación: " + e.getMessage());
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("\n========== RESERVAS DE PÁDEL ==========");
        System.out.println("\nHorario del club: 08:00 a 23:00");
        System.out.println("Duración de cada turno: 120 minutos");
        System.out.println("\n1. Registrar cliente");
        System.out.println("2. Listar clientes");
        System.out.println("3. Registrar cancha");
        System.out.println("4. Eliminar cancha");
        System.out.println("5. Listar canchas y precios");
        System.out.println("6. Consultar disponibilidad");
        System.out.println("7. Registrar reserva");
        System.out.println("8. Consultar reservas de un cliente");
        System.out.println("9. Cancelar reserva");
        System.out.println("10. Registrar asistencia / ausencia");
        System.out.println("11. Modificar precio de cancha");
        System.out.println("0. Salir");
        System.out.print("\nOpción: ");
    }

    private int pedirDni() {
        System.out.print("DNI (8 dígitos): ");
        String texto = teclado.nextLine().trim();
        if (!texto.matches("[0-9]{8}")) {
            System.out.println("ERROR: el DNI debe tener exactamente 8 dígitos.");
            return -1;
        }
        return Integer.parseInt(texto);
    }

    private void registrarCliente() {
        System.out.print("Nombre: ");
        String nombre = teclado.nextLine().trim();
        System.out.print("Apellido: ");
        String apellido = teclado.nextLine().trim();
        System.out.print("Teléfono: ");
        String telefono = teclado.nextLine().trim();
        int dni = pedirDni();
        if (dni == -1) return;
        if (nombre.isEmpty() || apellido.isEmpty() || telefono.isEmpty()) {
            System.out.println("ERROR: completá todos los datos.");
            return;
        }
        Cliente cliente = sistema.registrarCliente(nombre, apellido, telefono, dni);
        if (cliente != null) System.out.println("Cliente registrado correctamente.");
    }

    private void listarClientes() {
        ArrayList<Cliente> clientes = sistema.getClientes();
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados.");
            return;
        }
        for (Cliente c : clientes) {
        System.out.println("\n----------------------------------");
        System.out.println("CLIENTE: " + c.getNombreCompleto());
        System.out.println("DNI: " + c.getDni());
        System.out.println("Teléfono: " + c.getTelefono());

        System.out.println("\nReservas del cliente:");
        sistema.consultarReservas(c);
    }
}

    private void registrarCancha() {
        System.out.print("Número de cancha: ");
        int numero = Integer.parseInt(teclado.nextLine().trim());
        System.out.print("Tipo (1 PARED / 2 BLINDEX): ");
        int tipo = Integer.parseInt(teclado.nextLine().trim());
        TipoCancha tipoCancha;
        if (tipo == 1) tipoCancha = TipoCancha.PARED;
        else if (tipo == 2) tipoCancha = TipoCancha.BLINDEX;
        else { System.out.println("Tipo incorrecto."); return; }
        Cancha cancha = sistema.registrarCancha(numero, tipoCancha, true);
        if (cancha != null) System.out.println("Cancha registrada: " + cancha);
    }

    private void eliminarCancha() {
        System.out.print("Número de cancha que desea eliminar: ");
        int numero = Integer.parseInt(teclado.nextLine().trim());
        System.out.print("¿Confirmás que querés eliminar la cancha " + numero + "? (S/N): ");
        String confirmacion = teclado.nextLine().trim();
        if (confirmacion.equalsIgnoreCase("S")) {
            sistema.eliminarCancha(numero);
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    private void listarCanchas() {
        ArrayList<Cancha> canchas = sistema.getCanchas();
        if (canchas.isEmpty()) { System.out.println("No hay canchas registradas."); return; }
        for (Cancha c : canchas) {
            System.out.println(c + " | Precio: $" + c.getPrecio() + " | Disponible: " + c.getDisponibilidad());
        }
    }

    private LocalDate pedirFecha() {
        System.out.print("Fecha (AAAA-MM-DD): ");
        return LocalDate.parse(teclado.nextLine().trim());
    }

    private LocalTime pedirHora() {
        System.out.print("Hora (HH:MM): ");
        return LocalTime.parse(teclado.nextLine().trim());
    }

    private void consultarDisponibilidad() {
        LocalDate fecha = pedirFecha();
        LocalTime hora = pedirHora();
        if (!sistema.horarioValido(fecha, hora)) {
            mostrarErrorHorario();
            return;
        }
        ArrayList<Cancha> disponibles = sistema.consultarDisponibilidad(fecha, hora);
        if (disponibles.isEmpty()) System.out.println("No hay canchas disponibles.");
        else {
            System.out.println("Canchas disponibles:");
            for (Cancha c : disponibles) System.out.println(c + " | Precio: $" + c.getPrecio());
        }
    }

    private void registrarReserva() {
        int dni = pedirDni();
        if (dni == -1) return;
        Cliente cliente = sistema.buscarClientePorDni(dni);
        if (cliente == null) { System.out.println("No existe ese cliente. Registralo primero."); return; }
        System.out.print("Número de cancha: ");
        int numero = Integer.parseInt(teclado.nextLine().trim());
        Cancha cancha = sistema.buscarCanchaPorNumero(numero);
        if (cancha == null) { System.out.println("No existe esa cancha."); return; }
        LocalDate fecha = pedirFecha();
        LocalTime hora = pedirHora();
        if (!sistema.horarioValido(fecha, hora)) { mostrarErrorHorario(); return; }
        System.out.print("Cantidad de jugadores (incluido el organizador, máximo 4): ");
        int cantidadJugadores = Integer.parseInt(teclado.nextLine().trim());
        if (cantidadJugadores < 1 || cantidadJugadores > GestorReserva.CUPO_MAXIMO_JUGADORES) {
            System.out.println("ERROR: cada turno permite entre 1 y 4 jugadores.");
            return;
        }
        MedioPago medio = pedirMedioPago();
        Reserva reserva = sistema.registrarReserva(cliente, cancha, fecha, hora, medio, cantidadJugadores);
        if (reserva != null) {
            System.out.println("Reserva confirmada.");
            System.out.println("Número de reserva: " + reserva.getNumero());
            System.out.println("Monto total: $" + reserva.getMonto());
            System.out.println("Seña abonada: $" + reserva.getSena().getMonto());
            System.out.println("Cantidad de jugadores: " + reserva.getCantidadJugadores());
        }
    }

    private void mostrarErrorHorario() {
        System.out.println("Horario no permitido.");
        System.out.println("El club abre de 08:00 a 23:00 y cada turno dura 120 minutos.");
        System.out.println("Se puede comenzar un turno entre las 08:00 y las 21:00.");
        System.out.println("No se aceptan fechas ni horarios pasados.");
    }

    private void consultarReservas() {
        int dni = pedirDni();
        if (dni == -1) return;
        Cliente cliente = sistema.buscarClientePorDni(dni);
        if (cliente == null) { System.out.println("No existe ese cliente."); return; }
        System.out.println("Reservas de " + cliente.getNombreCompleto() + ":");
        sistema.consultarReservas(cliente);
    }

    private void cancelarReserva() {
        System.out.print("Número de reserva: ");
        int numero = Integer.parseInt(teclado.nextLine().trim());
        Reserva reserva = sistema.buscarReservaPorNumero(numero);
        if (reserva == null) { System.out.println("No se encontró esa reserva."); return; }
        sistema.cancelarReserva(reserva, LocalDateTime.now());
        System.out.println("Estado actual: " + reserva.getEstado());
        if (!reserva.getResultadoCancelacion().isBlank()) {
            System.out.println(reserva.getResultadoCancelacion());
        }
        mostrarDetallePagos(reserva);
    }

    private void registrarAsistencia() {
        System.out.print("Número de reserva: ");
        int numero = Integer.parseInt(teclado.nextLine().trim());
        Reserva reserva = sistema.buscarReservaPorNumero(numero);
        if (reserva == null) { System.out.println("No se encontró esa reserva."); return; }
        System.out.print("¿Asistió? (S/N): ");
        String respuesta = teclado.nextLine().trim();
        if (!respuesta.equalsIgnoreCase("S") && !respuesta.equalsIgnoreCase("N")) {
            System.out.println("Ingresá S o N.");
            return;
        }
        boolean asistio = respuesta.equalsIgnoreCase("S");
        MedioPago medio = asistio ? pedirMedioPago() : null;
        sistema.registrarAsistencia(reserva, asistio, medio);
        System.out.println("Estado actual: " + reserva.getEstado());
        mostrarDetallePagos(reserva);
    }

    private void mostrarDetallePagos(Reserva reserva) {
        System.out.println("\n--- DETALLE DE PAGOS ---");
        System.out.println("Precio total: $" + reserva.getMonto());
        System.out.println("Seña (50%): $" + reserva.getSena().getMonto());
        System.out.println("Estado de la seña: " + reserva.getSena().getEstado());
        System.out.println("Medio de pago de la seña: " + reserva.getSena().getMedio());
        if (reserva.getSaldo() == null) {
            System.out.println("Saldo pendiente (50%): $" + (reserva.getMonto() * 0.5));
            System.out.println("Estado del saldo: PENDIENTE");
            System.out.println("Medio de pago del saldo: todavía no registrado");
        } else {
            System.out.println("Saldo (50%): $" + reserva.getSaldo().getMonto());
            System.out.println("Estado del saldo: " + reserva.getSaldo().getEstado());
            System.out.println("Medio de pago del saldo: " + reserva.getSaldo().getMedio());
        }
    }

    private void modificarPrecio() {
        System.out.print("Tipo de cancha (1 PARED / 2 BLINDEX): ");
        int tipo = Integer.parseInt(teclado.nextLine().trim());
        TipoCancha tipoCancha;
        if (tipo == 1) tipoCancha = TipoCancha.PARED;
        else if (tipo == 2) tipoCancha = TipoCancha.BLINDEX;
        else { System.out.println("Tipo incorrecto."); return; }
        System.out.print("Nuevo precio: ");
        double precio = Double.parseDouble(teclado.nextLine().trim());
        sistema.modificarPrecio(tipoCancha, precio);
    }

    private MedioPago pedirMedioPago() {
        System.out.println("Medio de pago:");
        System.out.println("1. EFECTIVO");
        System.out.println("2. TRANSFERENCIA");
        System.out.println("3. QR");
        int opcion = Integer.parseInt(teclado.nextLine().trim());
        if (opcion == 1) return MedioPago.EFECTIVO;
        if (opcion == 2) return MedioPago.TRANSFERENCIA;
        if (opcion == 3) return MedioPago.QR;
        throw new IllegalArgumentException("Medio de pago incorrecto.");
    }
}

import controlador.GestorReserva;
import persistencia.InicializadorBD;
import vista.Menu;
import java.sql.SQLException;

public class Principal {
    public static void main(String[] args) {
        try {
            InicializadorBD.inicializar();
            GestorReserva sistema = new GestorReserva();
            Menu menu = new Menu(sistema);
            menu.iniciar();
        } catch (SQLException e) {
            System.out.println("No se pudo iniciar el sistema por un problema con la base de datos.");
            System.out.println("Detalle: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("No se pudo iniciar el sistema porque hay un dato inválido en la configuración o en la base de datos.");
            System.out.println("Detalle: " + e.getMessage());
        }
    }
}

package modelo;

public class Cliente extends Persona {
    private int inasistencias;

    public Cliente(String nombre, String apellido, String telefono, int dni) {
        super(nombre, apellido, telefono, dni);
        inasistencias = 0;
    }

    public void registrarInasistencia() {
        inasistencias++;
    }

    public int getInasistencias() {
        return inasistencias;
    }

    public void setInasistencias(int inasistencias) {
        if (inasistencias < 0) {
            throw new IllegalArgumentException("Las inasistencias no pueden ser negativas.");
        }
        this.inasistencias = inasistencias;
    }
    
}

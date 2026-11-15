package modelo;

public enum TipoCancha {
    PARED(35000), BLINDEX(45000);

    private double precio;

    TipoCancha(double precio) {
        this.precio = precio;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (!Double.isFinite(precio) || precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero.");
        }
        this.precio = precio;
    }
}
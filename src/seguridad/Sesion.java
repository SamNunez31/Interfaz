package seguridad;

public class Sesion {
    private static Sesion instancia;
    
    // Datos del usuario logueado
    private int idUsuario;
    private String nombreUsuario;
    private String rolSistema; // 'ADCM', 'OVEN', 'BOD', etc.
    private String nombreReal;

    private Sesion() {}

    public static Sesion getInstancia() {
        if (instancia == null) {
            instancia = new Sesion();
        }
        return instancia;
    }

    public void login(int id, String usuario, String rol, String nombre) {
        this.idUsuario = id;
        this.nombreUsuario = usuario;
        this.rolSistema = rol;
        this.nombreReal = nombre;
    }

    public void logout() {
        this.idUsuario = 0;
        this.nombreUsuario = null;
        this.rolSistema = null;
        instancia = null;
    }

    // Getters para usar en tus paneles (para validar permisos)
    public String getRolSistema() { return rolSistema; }
    public String getNombreReal() { return nombreReal; }
    public int getIdUsuario() { return idUsuario; }
}

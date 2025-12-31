package seguridad;

import ConexionBD.ConexionBD; // Usando el paquete de tu equipo
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {
    
    public boolean autenticar(String usuario, String password) {
        String sql = "SELECT u.id_Usuario_PK, u.usu_nombre, u.id_RolSistema, u.usu_NombreReal " +
                     "FROM USUARIOS u " +
                     "WHERE u.usu_nombre = ? AND u.usu_clave = ? AND u.usu_Estado = 'ACT'";
        
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, usuario);
            pst.setString(2, password);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // LOGIN EXITOSO: Guardamos en la Sesión
                    Sesion.getInstancia().login(
                        rs.getInt("id_Usuario_PK"),
                        rs.getString("usu_nombre"),
                        rs.getString("id_RolSistema"),
                        rs.getString("usu_NombreReal")
                    );
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en Login: " + e.getMessage());
        }
        return false;
    }
}

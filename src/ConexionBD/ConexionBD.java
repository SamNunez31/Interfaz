
package ConexionBD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    
    // Configuración centralizada
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=ContextoComercialAcme;encrypt=true;trustServerCertificate=true;";
    private static final String USUARIO = "SuperAdmin";
    private static final String CLAVE = "root";

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}

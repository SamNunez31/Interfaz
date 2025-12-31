package app;

import shared.ui.Theme;
import ui.sidebar.Sidebar;
import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {

    private Sidebar sidebar;
    private JPanel contentPanel;
    private CardLayout cardLayout; 

    public DashboardFrame() {
        setTitle("Dashboard - Contexto Comercial");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1366, 768);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Sidebar
        sidebar = new Sidebar();
        
        // >>> CORRECCIÓN: USAR DATOS DE LA SESIÓN <<<
        // Verificamos si hay una sesión activa (debería haberla si venimos del Login)
        if (seguridad.Sesion.getInstancia() != null) {
            String nombreReal = seguridad.Sesion.getInstancia().getNombreReal();
            String rol = seguridad.Sesion.getInstancia().getRolSistema();
            
            // Si por alguna razón es nulo (pruebas directas), ponemos un fallback
            if (nombreReal == null) nombreReal = "Usuario Prueba";
            if (rol == null) rol = "INVITADO";
            
            sidebar.setUser(nombreReal, rol); 
        } else {
            // Fallback por si ejecutas DashboardFrame directamente sin pasar por Login
            sidebar.setUser("Modo Desarrollo", "DEV");
        }
        
        // Conectamos el listener del menú
        sidebar.setMenuListener(screenName -> showScreen(screenName));
        add(sidebar, BorderLayout.WEST);
        
        // Conectamos el listener del menú
        sidebar.setMenuListener(screenName -> showScreen(screenName));
        add(sidebar, BorderLayout.WEST);

        // 2. Panel Central (CardLayout)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Theme.BG);
        
        // --- DEFINICIÓN DE PANTALLAS ---
        
        contentPanel.add(new SimpleModulePanel("Bienvenido", "Selecciona una opción del menú"), "HOME");
        
        // Aquí cargamos tu módulo real de empleados
        contentPanel.add(new PanelEmpleados(), "EMPLEADOS"); 
        
        // Los demás siguen siendo de prueba por ahora...
        contentPanel.add(new SimpleModulePanel("Roles de Pago", "Cálculos de nómina..."), "ROLES");
        contentPanel.add(new SimpleModulePanel("Facturación", "Formulario de facturas..."), "FACTURACION");
        contentPanel.add(new SimpleModulePanel("Clientes", "Directorio de clientes..."), "CLIENTES");
        contentPanel.add(new PanelProveedores(), "PROVEEDORES");        
        contentPanel.add(new SimpleModulePanel("Kardex", "Movimientos de inventario..."), "KARDEX");
        contentPanel.add(new SimpleModulePanel("Balances", "Estado de situación financiera..."), "BALANCES");

        add(contentPanel, BorderLayout.CENTER);
        
        // Mostrar inicio
        cardLayout.show(contentPanel, "HOME");
    }

    private void showScreen(String name) {
        if ("LOGOUT".equals(name)) {
            
            // Usamos el diálogo personalizado
            LogoutDialog dialog = new LogoutDialog(this);
            dialog.setVisible(true); 
            
            if (dialog.isConfirmed()) {
                this.dispose(); // Cierra el Dashboard
                new LoginFrame().setVisible(true); // Abre el Login
            }
            
        } else {
            // Lógica normal de cambio de pantalla
            cardLayout.show(contentPanel, name);
        }
    }

    // Clase auxiliar para pantallas que aún no programas
    class SimpleModulePanel extends JPanel {
        public SimpleModulePanel(String title, String description) {
            setLayout(new GridBagLayout()); 
            setBackground(Theme.BG); 
            
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(new Color(40, 40, 45)); 
            card.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
            
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
            lblTitle.setForeground(Theme.ACCENT); 
            lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel lblDesc = new JLabel(description);
            lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lblDesc.setForeground(Theme.TEXT);
            lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            card.add(lblTitle);
            card.add(Box.createVerticalStrut(10));
            card.add(lblDesc);
            
            add(card);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf()); } catch (Exception ex) {}
        
        SwingUtilities.invokeLater(() -> {
            new DashboardFrame().setVisible(true);
        });
    }
}
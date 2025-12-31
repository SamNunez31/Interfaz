package app;

import shared.ui.Theme;
import ui.sidebar.Sidebar;
import javax.swing.*;
import java.awt.*;

import seguridad.Sesion;
import modules.tthh.ui.PanelRoles;
// import modules.tthh.ui.PanelEmpleados;  // ← asegúrate que exista
// import app.LogoutDialog;
// import app.LoginFrame;

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

        // ===============================
        // 1. SIDEBAR
        // ===============================
        sidebar = new Sidebar();

        // >>> USO DE SESIÓN <<<
        Sesion sesion = Sesion.getInstancia();
        if (sesion != null) {
            String nombre = sesion.getNombreReal();
            String rol = sesion.getRolSistema();

            if (nombre == null) nombre = "Usuario";
            if (rol == null) rol = "INVITADO";

            sidebar.setUser(nombre, rol);
        } else {
            // Fallback si abres Dashboard directamente
            sidebar.setUser("Modo Desarrollo", "DEV");
        }

    sidebar.setMenuListener(new java.util.function.Consumer<String>() {
    @Override
    public void accept(String screenName) {
           showScreen(screenName);
        }
    });



        add(sidebar, BorderLayout.WEST);

        // ===============================
        // 2. PANEL CENTRAL (CardLayout)
        // ===============================
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Theme.BG);

        // ===============================
        // 3. DEFINICIÓN DE PANTALLAS
        // ===============================
        contentPanel.add(
                new SimpleModulePanel(
                        "Bienvenido",
                        "Selecciona una opción del menú"
                ),
                "HOME"
        );

        // 👉 MÓDULOS REALES
        contentPanel.add(new PanelEmpleados(), "EMPLEADOS");
        contentPanel.add(new PanelRoles(), "ROLES");

        // 👉 MÓDULOS TEMPORALES
        contentPanel.add(new SimpleModulePanel("Facturación", "Formulario de facturas..."), "FACTURACION");
        contentPanel.add(new SimpleModulePanel("Clientes", "Directorio de clientes..."), "CLIENTES");
        contentPanel.add(new SimpleModulePanel("Proveedores", "Gestión de proveedores..."), "PROVEEDORES");
        contentPanel.add(new SimpleModulePanel("Kardex", "Movimientos de inventario..."), "KARDEX");
        contentPanel.add(new SimpleModulePanel("Balances", "Estado de situación financiera..."), "BALANCES");

        add(contentPanel, BorderLayout.CENTER);

        // Mostrar inicio
        cardLayout.show(contentPanel, "HOME");
    }

    // ===============================
    // CAMBIO DE PANTALLA
    // ===============================
    private void showScreen(String name) {

        if ("LOGOUT".equals(name)) {

            LogoutDialog dialog = new LogoutDialog(this);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                dispose();
                new LoginFrame().setVisible(true);
            }

        } else {
            cardLayout.show(contentPanel, name);
        }
    }

    // ===============================
    // PANEL SIMPLE (PLACEHOLDER)
    // ===============================
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

    // ===============================
    // MAIN
    // ===============================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
        } catch (Exception ex) {
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DashboardFrame().setVisible(true);
            }
        });
    }
}

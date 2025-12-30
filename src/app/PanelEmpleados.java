package app;

import ConexionBD.ConexionBD; // <--- OJO: Asegúrate que este paquete sea correcto según tu proyecto
import raven.toast.Notifications;
import shared.ui.IconUtil; 
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PanelEmpleados extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField txtBuscar;

    // Colores del Tema
    private final Color COLOR_VERDE = new Color(46, 204, 113);
    private final Color COLOR_ROJO = new Color(231, 76, 60);
    private final Color COLOR_AZUL = new Color(52, 152, 219);
    private final Color TEXTO_BLANCO = new Color(240, 240, 240);

    public PanelEmpleados() {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 24)); // Fondo oscuro

        // 1. Panel Superior
        add(crearPanelSuperior(), BorderLayout.NORTH);

        // 2. Tabla Central
        add(crearPanelTabla(), BorderLayout.CENTER);
        
        // 3. Cargar datos
        cargarEmpleados("");
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titulo = new JLabel("Lista de Empleados");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(TEXTO_BLANCO);
        
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        // Botones de acción
        JButton btnNuevo = crearBoton("Nuevo Empleado", COLOR_VERDE);
        btnNuevo.addActionListener(e -> abrirFormulario(0));

        JButton btnEliminar = crearBoton("Eliminar Empleado", COLOR_ROJO);
        btnEliminar.addActionListener(e -> eliminarEmpleado());
        
        // --- AQUÍ ESTÁ EL BOTÓN ACTUALIZAR ---
        JButton btnRefrescar = crearBoton("Actualizar Empleado", new Color(100, 100, 100));
        btnRefrescar.addActionListener(e -> cargarEmpleados(""));

        // --- BUSCADOR MODERNO ---
        JPanel panelBuscador = new JPanel(new BorderLayout());
        panelBuscador.setOpaque(false);
        panelBuscador.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 65), 1)); 

        txtBuscar = new JTextField(15);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar...");
        txtBuscar.putClientProperty("JTextField.showClearButton", true);
        txtBuscar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
        txtBuscar.setPreferredSize(new Dimension(200, 35));
        
        // Icono
        Icon iconoBuscar = IconUtil.load("/icono/buscar.png", 16); 
        JButton btnBuscar = new JButton();
        if (iconoBuscar != null) {
            btnBuscar.setIcon(iconoBuscar);
        } else {
            btnBuscar.setText("🔍");
        }

        btnBuscar.setBackground(COLOR_AZUL);
        btnBuscar.setBorderPainted(false);
        btnBuscar.setFocusPainted(false);
        btnBuscar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Botón de búsqueda compacto
        btnBuscar.setPreferredSize(new Dimension(30, 35)); 
        
        btnBuscar.addActionListener(e -> cargarEmpleados(txtBuscar.getText().trim()));

        panelBuscador.add(txtBuscar, BorderLayout.CENTER);
        panelBuscador.add(btnBuscar, BorderLayout.EAST);

        // Agregar todo al toolbar
        toolbar.add(btnNuevo);
        toolbar.add(btnEliminar);
        toolbar.add(btnRefrescar);
        toolbar.add(Box.createHorizontalStrut(30)); // Espacio separador
        toolbar.add(panelBuscador);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(15), BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        modelo = new DefaultTableModel(
            new Object[]{"ID", "Cédula", "Apellidos", "Nombres", "Departamento", "Rol", "Estado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modelo);
        
        // --- ESTILO MODERNO ---
        tabla.setRowHeight(40); 
        tabla.setIntercellSpacing(new Dimension(0, 0)); 
        tabla.setShowVerticalLines(false); 
        tabla.setShowHorizontalLines(true);
        tabla.setFillsViewportHeight(true); 
        
        // Header
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 40)); 

        // Centrar contenido
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            if (i != 6) { 
                tabla.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
        
        // Renderizador ESTADO
        tabla.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String estado = (String) value;
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setHorizontalAlignment(JLabel.CENTER);
                if ("ACT".equals(estado)) {
                    l.setForeground(COLOR_VERDE);
                    l.setText("● ACTIVO");
                } else {
                    l.setForeground(COLOR_ROJO);
                    l.setText("● INACTIVO");
                }
                return l;
            }
        });

        // Doble Clic
        tabla.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tabla.getSelectedRow();
                    if (fila != -1) {
                        int id = Integer.parseInt(tabla.getValueAt(fila, 0).toString());
                        abrirFormulario(id);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder()); 
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // --- LÓGICA (Sin cambios) ---
    private void cargarEmpleados(String filtro) {
        modelo.setRowCount(0);
        String sql = "SELECT e.id_Empleado_PK, e.emp_ciruc, e.emp_Apellido1, e.emp_Nombre1, " +
                     "d.dep_Nombre, r.rol_Descripcion, e.emp_Estado " +
                     "FROM Empleados e " +
                     "JOIN Departamentos d ON e.id_Departamento = d.id_Departamento_PK " +
                     "JOIN Roles r ON e.id_Rol = r.id_Rol_PK ";

        if (!filtro.isEmpty()) {
            sql += " WHERE e.emp_ciruc LIKE ? OR e.emp_Apellido1 LIKE ? OR e.emp_Nombre1 LIKE ? ";
        }
        
        sql += " ORDER BY e.emp_Apellido1 ASC";

        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement pst = cn.prepareStatement(sql)) {

            if (!filtro.isEmpty()) {
                String f = "%" + filtro + "%";
                pst.setString(1, f); pst.setString(2, f); pst.setString(3, f);
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                    rs.getString(5), rs.getString(6), rs.getString(7)
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error al cargar datos");
        }
    }

    private void eliminarEmpleado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Seleccione un empleado");
            return;
        }

        int id = Integer.parseInt(tabla.getValueAt(fila, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro deseas eliminar este empleado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection cn = ConexionBD.getConexion();
                 PreparedStatement pst = cn.prepareStatement("UPDATE Empleados SET emp_Estado = 'INA' WHERE id_Empleado_PK = ?")) {
                pst.setInt(1, id);
                if (pst.executeUpdate() > 0) {
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Empleado inactivado");
                    cargarEmpleados("");
                }
            } catch (Exception e) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error al eliminar");
            }
        }
    }

    private void abrirFormulario(int id) {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        DialogoEmpleado dialogo = new DialogoEmpleado((Frame) ventanaPadre, id);
        dialogo.setVisible(true); 
        cargarEmpleados(""); 
    }

    private JButton crearBoton(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // >>> CAMBIO: Aumenté el ancho a 170 para que quepa "Actualizar Empleado"
        btn.setPreferredSize(new Dimension(170, 35));
        
        return btn;
    }
}
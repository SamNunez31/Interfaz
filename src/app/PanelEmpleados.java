package app;

import ConexionBD.ConexionBD;
import raven.toast.Notifications;
import shared.ui.IconUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import javax.swing.text.JTextComponent;

public class PanelEmpleados extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField txtBuscar;
    private JCheckBox chkVerTodos; 

    // Colores del Tema
    private final Color COLOR_VERDE = new Color(46, 204, 113);
    private final Color COLOR_ROJO = new Color(231, 76, 60);
    private final Color COLOR_AZUL = new Color(52, 152, 219);
    private final Color TEXTO_BLANCO = new Color(240, 240, 240);
    private final Color FONDO_OSCURO = new Color(30, 30, 30); 
    
    // Colores para la tabla blanca
    private final Color TABLA_FONDO = Color.WHITE;
    private final Color TABLA_TEXTO = new Color(50, 50, 50);
    private final Color TABLA_GRID = new Color(230, 230, 230);

    public PanelEmpleados() {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 24)); 

        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearPanelTabla(), BorderLayout.CENTER);
        
        cargarEmpleados("");
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titulo = new JLabel("Lista de Empleados");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(TEXTO_BLANCO);
        
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS)); 
        toolbar.setOpaque(false);

        // BOTONES
        JButton btnNuevo = crearBoton("Nuevo Empleado", COLOR_VERDE, "/icono/agregar.png", 20);
        btnNuevo.addActionListener(e -> abrirFormulario(0));

        JButton btnEliminar = crearBoton("Eliminar Empleado", COLOR_ROJO, "/icono/borrar.png", 16);
        btnEliminar.addActionListener(e -> eliminarEmpleado());
        
        JButton btnEditar = crearBoton("Actualizar Empleado", new Color(100, 100, 100), "/icono/editar.png", 26);
        btnEditar.setPreferredSize(new Dimension(200, 35)); 
        btnEditar.setMaximumSize(new Dimension(200, 35));
        btnEditar.addActionListener(e -> editarEmpleadoSeleccionado()); 

        // --- BUSCADOR ---
        JPanel panelBuscador = new JPanel(new BorderLayout());
        panelBuscador.setOpaque(false);
        panelBuscador.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 65), 1)); 
        panelBuscador.setMaximumSize(new Dimension(350, 35)); 
        panelBuscador.setPreferredSize(new Dimension(300, 35));

        txtBuscar = new JTextField(15);
        txtBuscar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
        
        TextPrompt placeholder = new TextPrompt("Ingrese cédula, nombre o apellido...", txtBuscar);
        placeholder.changeAlpha(0.6f); 
        placeholder.changeStyle(Font.ITALIC); 
        
        txtBuscar.putClientProperty("JTextField.placeholderText", "Ingrese cédula, nombre o apellido...");
        txtBuscar.putClientProperty("JTextField.showClearButton", true);
        
        Icon iconoBuscar = IconUtil.load("/icono/buscar.png", 20); 
        JButton btnBuscar = new JButton();
        if (iconoBuscar != null) btnBuscar.setIcon(iconoBuscar);
        else btnBuscar.setText("🔍");

        btnBuscar.setBackground(COLOR_AZUL);
        btnBuscar.setBorderPainted(false);
        btnBuscar.setFocusPainted(false);
        btnBuscar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBuscar.setPreferredSize(new Dimension(40, 35)); 
        btnBuscar.addActionListener(e -> cargarEmpleados(txtBuscar.getText().trim()));

        panelBuscador.add(txtBuscar, BorderLayout.CENTER);
        panelBuscador.add(btnBuscar, BorderLayout.EAST);

        toolbar.add(btnNuevo);
        toolbar.add(Box.createHorizontalStrut(10)); 
        toolbar.add(btnEliminar);
        toolbar.add(Box.createHorizontalStrut(10)); 
        toolbar.add(btnEditar);
        
        toolbar.add(Box.createHorizontalGlue()); 
        toolbar.add(panelBuscador);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(15), BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

        modelo = new DefaultTableModel(
            new Object[]{"ID", "Cédula", "Apellidos", "Nombres", "Departamento", "Rol", "Sueldo ($)", "Estado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modelo);
        configurarTabla();

        tabla.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarEmpleadoSeleccionado();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(scroll.getPreferredSize().width, 350));

        panel.add(scroll, BorderLayout.CENTER);

        JPanel panelFooter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFooter.setOpaque(false);
        panelFooter.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        chkVerTodos = new JCheckBox("Ver todos");
        chkVerTodos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkVerTodos.setForeground(Color.WHITE); 
        chkVerTodos.setOpaque(false);
        chkVerTodos.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        chkVerTodos.addActionListener(e -> cargarEmpleados(txtBuscar.getText().trim()));

        panelFooter.add(chkVerTodos);
        panel.add(panelFooter, BorderLayout.SOUTH); 

        return panel;
    }

    private void configurarTabla() {
        tabla.setRowHeight(40); 
        tabla.setIntercellSpacing(new Dimension(0, 0)); 
        tabla.setShowVerticalLines(false); 
        tabla.setShowHorizontalLines(true);
        
        tabla.setBackground(TABLA_FONDO); 
        tabla.setForeground(TABLA_TEXTO); 
        tabla.setGridColor(TABLA_GRID);    
        
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = tabla.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 45)); 
        
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(TABLA_FONDO); 
                setForeground(TABLA_TEXTO);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setHorizontalAlignment(JLabel.CENTER);
                
                // --- CAMBIO CLAVE AQUÍ: Se agrega el borde derecho (1) para marcar las separaciones ---
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(200,200,200)));
                
                return this;
            }
        });

        // Configuración de anchos
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(1).setMinWidth(90);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(200); 
        tabla.getColumnModel().getColumn(2).setMinWidth(150);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(3).setMinWidth(150);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(4).setMinWidth(100);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(5).setMinWidth(100);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(6).setMaxWidth(120);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(7).setMaxWidth(110);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (value != null && value.toString().equals("-1")) {
                    setText("");
                }
                return this;
            }
        };
        tabla.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); 
        tabla.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT); 
        tabla.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        tabla.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setHorizontalAlignment(JLabel.CENTER);
                l.setIcon(null); 
                
                if ("ACT".equals(value)) {
                    l.setForeground(new Color(39, 174, 96)); 
                    l.setText("● ACTIVO");
                } else if ("NODATA".equals(value)) { 
                    l.setText("");
                    l.setForeground(TABLA_TEXTO);
                } else {
                    l.setForeground(COLOR_ROJO);
                    l.setText("● INACTIVO");
                }
                
                if (isSelected && !"NODATA".equals(value)) {
                    l.setForeground(Color.WHITE); 
                    l.setBackground(new Color(124, 77, 255)); 
                } else {
                    l.setBackground(Color.WHITE);
                }
                return l;
            }
        });
        
        tabla.setSelectionBackground(new Color(124, 77, 255)); 
        tabla.setSelectionForeground(Color.WHITE);
    }

    private void cargarEmpleados(String filtro) {
        modelo.setRowCount(0);

        String sql = "SELECT e.id_Empleado_PK, e.emp_ciruc, e.emp_Apellido1, e.emp_Nombre1, " +
                     "d.dep_Nombre, r.rol_Descripcion, e.emp_Sueldo, e.emp_Estado, " +
                     "e.emp_Apellido2, e.emp_Nombre2 " +
                     "FROM Empleados e " +
                     "JOIN Departamentos d ON e.id_Departamento = d.id_Departamento_PK " +
                     "JOIN Roles r ON e.id_Rol = r.id_Rol_PK " +
                     "WHERE 1=1 "; 

        if (chkVerTodos != null && !chkVerTodos.isSelected()) {
            sql += " AND e.emp_Estado = 'ACT' ";
        }

        if (!filtro.isEmpty()) {
            sql += " AND (e.emp_ciruc LIKE ? OR e.emp_Apellido1 LIKE ? OR e.emp_Apellido2 LIKE ? OR e.emp_Nombre1 LIKE ? OR e.emp_Nombre2 LIKE ?) ";
        }

        sql += " ORDER BY e.emp_Apellido1 ASC";

        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement pst = cn.prepareStatement(sql)) {

            if (!filtro.isEmpty()) {
                String f = "%" + filtro + "%";
                pst.setString(1, f); pst.setString(2, f); pst.setString(3, f); pst.setString(4, f); pst.setString(5, f);
            }

            ResultSet rs = pst.executeQuery();
            DecimalFormat df = new DecimalFormat("#,##0.00");
            boolean hayDatos = false;

            while (rs.next()) {
                hayDatos = true;
                String apellidos = "";
                if (rs.getString("emp_Apellido1") != null) apellidos += rs.getString("emp_Apellido1").trim();
                if (rs.getString("emp_Apellido2") != null && !rs.getString("emp_Apellido2").trim().isEmpty()) {
                    apellidos += " " + rs.getString("emp_Apellido2").trim();
                }
                String nombres = "";
                if (rs.getString("emp_Nombre1") != null) nombres += rs.getString("emp_Nombre1").trim();
                if (rs.getString("emp_Nombre2") != null && !rs.getString("emp_Nombre2").trim().isEmpty()) {
                    nombres += " " + rs.getString("emp_Nombre2").trim();
                }

                modelo.addRow(new Object[]{
                    rs.getInt("id_Empleado_PK"), 
                    rs.getString("emp_ciruc"),
                    apellidos, 
                    nombres,   
                    rs.getString("dep_Nombre"),
                    rs.getString("rol_Descripcion"),
                    df.format(rs.getDouble("emp_Sueldo")),
                    rs.getString("emp_Estado")              
                });
            }

            if (!hayDatos) {
                modelo.addRow(new Object[]{-1, "", "No se han encontrado resultados", "", "", "", "", "NODATA"});
            }

        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error al cargar datos");
        }
    }

    private void editarEmpleadoSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Seleccione un empleado para ver");
            return;
        }

        int id = Integer.parseInt(tabla.getValueAt(fila, 0).toString());
        Object valorEstado = tabla.getValueAt(fila, 7); 
        String estado = (valorEstado != null) ? valorEstado.toString() : "";
        
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        DialogoEmpleado dialogo = new DialogoEmpleado((Frame) ventanaPadre, id);

        if ("INA".equals(estado) || estado.contains("INACTIVO")) {
            Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, "Modo Lectura: Empleado Inactivo");
            dialogo.activarModoSoloLectura(); 
        }

        dialogo.setVisible(true); 
        
        if (dialogo.isOperacionExitosa()) {
            cargarEmpleados(""); 
        }
    }

    private void eliminarEmpleado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Seleccione un empleado");
            return;
        }

        Object valorEstado = tabla.getValueAt(fila, 7);
        String estado = (valorEstado != null) ? valorEstado.toString() : "";

        if ("INA".equals(estado) || estado.contains("INACTIVO")) {
            Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_RIGHT, "El empleado ya se encuentra inactivo");
            return; 
        }

        int id = Integer.parseInt(tabla.getValueAt(fila, 0).toString());
        String nombre = tabla.getValueAt(fila, 3).toString() + " " + tabla.getValueAt(fila, 2).toString();

        if (mostrarConfirmacionEliminar(nombre)) {
            try (Connection cn = ConexionBD.getConexion();
                 PreparedStatement pst = cn.prepareStatement("UPDATE Empleados SET emp_Estado = 'INA' WHERE id_Empleado_PK = ?")) {
                pst.setInt(1, id);
                if (pst.executeUpdate() > 0) {
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Empleado eliminado (Inactivo)");
                    cargarEmpleados(txtBuscar.getText().trim());
                }
            } catch (Exception e) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error al eliminar");
            }
        }
    }

    private boolean mostrarConfirmacionEliminar(String nombreEmpleado) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 40, 45)); 
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(new Color(231, 76, 60)); 
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth()-2, getHeight()-2, 20, 20));
            }
        };
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitulo = new JLabel("Eliminar Empleado");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(231, 76, 60)); 
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel lblMsg = new JLabel("<html><center>¿Estás seguro de eliminar a:<br><b>" + nombreEmpleado + "</b>?</center></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMsg.setForeground(Color.WHITE); 
        lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        
        JButton btnSi = new JButton("SÍ, ELIMINAR");
        btnSi.setBackground(new Color(231, 76, 60)); 
        btnSi.setForeground(Color.WHITE);
        btnSi.setFocusPainted(false);
        btnSi.setBorderPainted(false);
        btnSi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSi.setPreferredSize(new Dimension(120, 35));
        
        JButton btnNo = new JButton("CANCELAR");
        btnNo.setBackground(new Color(80, 80, 80)); 
        btnNo.setForeground(Color.WHITE);
        btnNo.setFocusPainted(false);
        btnNo.setBorderPainted(false);
        btnNo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNo.setPreferredSize(new Dimension(120, 35));
        
        final boolean[] respuesta = {false};
        
        btnSi.addActionListener(e -> { respuesta[0] = true; dialog.dispose(); });
        btnNo.addActionListener(e -> { respuesta[0] = false; dialog.dispose(); });
        
        btnPanel.add(btnNo);
        btnPanel.add(btnSi);
        
        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(lblMsg, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        return respuesta[0];
    }

    private void abrirFormulario(int id) {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        DialogoEmpleado dialogo = new DialogoEmpleado((Frame) ventanaPadre, id);
        dialogo.setVisible(true); 
        
        if (dialogo.isOperacionExitosa()) {
            cargarEmpleados(""); 
        }
    }

    private JButton crearBoton(String texto, Color bg, String rutaIcono, int iconSize) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(170, 35));
        btn.setMaximumSize(new Dimension(170, 35));
        
        if (rutaIcono != null && !rutaIcono.isEmpty()) {
            Icon icon = IconUtil.load(rutaIcono, iconSize); 
            if (icon != null) {
                btn.setIcon(icon);
                btn.setIconTextGap(8); 
            }
        }
        return btn;
    }

    class TextPrompt extends JLabel implements FocusListener, DocumentListener {
        JTextComponent component;
        DocumentListener documentListener;

        public TextPrompt(String text, JTextComponent component) {
            this.component = component;
            setShow(Show.ALWAYS);
            documentListener = this;
            setText(text);
            setFont(component.getFont());
            setForeground(component.getForeground());
            setBorder(new EmptyBorder(component.getInsets()));
            setHorizontalAlignment(JLabel.LEADING);

            component.addFocusListener(this);
            component.getDocument().addDocumentListener(this);
            component.setLayout(new BorderLayout());
            component.add(this);
            checkForPrompt();
        }

        public void changeAlpha(float alpha) {
            changeAlpha((int) (alpha * 255));
        }

        public void changeAlpha(int alpha) {
            alpha = alpha > 255 ? 255 : alpha < 0 ? 0 : alpha;
            setForeground(new Color(getForeground().getRed(), getForeground().getGreen(), getForeground().getBlue(), alpha));
        }

        public void changeStyle(int style) {
            setFont(getFont().deriveFont(style));
        }

        public void checkForPrompt() {
            if (component.getText().length() > 0) {
                setVisible(false);
                return;
            }
            if (show == Show.ALWAYS || (show == Show.FOCUS_LOST && !component.hasFocus()) || (show == Show.FOCUS_GAINED && component.hasFocus())) {
                setVisible(true);
            } else {
                setVisible(false);
            }
        }

        public void focusGained(FocusEvent e) { checkForPrompt(); }
        public void focusLost(FocusEvent e) { checkForPrompt(); }
        public void insertUpdate(DocumentEvent e) { checkForPrompt(); }
        public void removeUpdate(DocumentEvent e) { checkForPrompt(); }
        public void changedUpdate(DocumentEvent e) {}

        private Show show;
        public void setShow(Show show) { this.show = show; }
        public enum Show { ALWAYS, FOCUS_GAINED, FOCUS_LOST; }
    }
}
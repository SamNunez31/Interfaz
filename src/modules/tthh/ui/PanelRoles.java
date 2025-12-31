package modules.tthh.ui;

import ConexionBD.ConexionBD;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class PanelRoles extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField txtBuscar;

    // --- PALETA DE COLORES AJUSTADA (Match exacto con Empleados) ---
    private final Color BG_PANEL = new Color(20, 20, 24);       // Fondo OSCURO (Igual a Empleados)
    private final Color BG_TABLE = Color.WHITE;                 // Tabla Blanca
    private final Color TEXT_TABLE = new Color(50, 50, 50);     // Texto negro/gris

    private final Color BG_HEADER = new Color(250, 250, 250);   // Cabecera casi blanca
    private final Color TEXT_HEADER = new Color(20, 20, 20);    // Texto cabecera negro fuerte
    private final Color HEADER_LINE = new Color(220, 220, 220); // Línea sutil bajo cabecera

    private final Color TEXT_TITLE = new Color(240, 240, 240);  // Título principal blanco

    // Colores de Botones y Estados
    private final Color COLOR_VERDE = new Color(39, 174, 96);   
    private final Color COLOR_AMARILLO = new Color(243, 156, 18); 
    private final Color COLOR_ROJO = new Color(231, 76, 60);
    private final Color COLOR_AZUL_SEARCH = new Color(52, 152, 219); 
    private final Color COLOR_PURPLE = new Color(124, 77, 255); // Morado selección

    public PanelRoles() {
        setLayout(new BorderLayout());
        setBackground(BG_PANEL);

        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearPanelTabla(), BorderLayout.CENTER);

        cargarRoles("");
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 30, 10, 30));

        // 1. Título
        JLabel titulo = new JLabel("Gestión de Roles de Pago");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(TEXT_TITLE);

        // 2. Toolbar (Botones y Buscador)
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setOpaque(false);

        // Botón Nuevo con Ícono (Verde) - AUMENTADO DE TAMAÑO
        // Usamos "/icono/agregar.png" si existe, o solo texto
        JButton btnNuevo = crearBotonConIcono("Generar Nuevo Rol", COLOR_VERDE, "/icono/add.png");
        btnNuevo.setPreferredSize(new Dimension(190, 40)); // <--- ALTURA AUMENTADA A 40
        btnNuevo.addActionListener(e -> abrirGenerarRol());

        // --- BUSCADOR (Estilo Largo y Automático) ---
        JPanel panelBuscador = new JPanel(new BorderLayout());
        panelBuscador.setBackground(Color.WHITE); 
        panelBuscador.setMaximumSize(new Dimension(350, 35)); 
        panelBuscador.setPreferredSize(new Dimension(350, 35));
        panelBuscador.setBorder(new LineBorder(new Color(200, 200, 200), 1));

        txtBuscar = new JTextField();
        txtBuscar.setBackground(Color.WHITE);
        txtBuscar.setForeground(Color.BLACK);
        txtBuscar.setBorder(new EmptyBorder(0, 10, 0, 5)); 
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // EVENTO AUTOMÁTICO
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                cargarRoles(txtBuscar.getText().trim());
            }
        });

        // Botón Lupa (Corregido para que no salgan los "...")
        JButton btnBuscar = new JButton();
        btnBuscar.setBackground(COLOR_AZUL_SEARCH);
        btnBuscar.setBorderPainted(false);
        btnBuscar.setFocusPainted(false);
        btnBuscar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBuscar.setPreferredSize(new Dimension(45, 35)); // Ancho suficiente

        // Carga de ícono corregida (Ruta estándar usada en tu proyecto)
        ImageIcon iconoLupa = cargarIcono("/icono/buscar.png"); 
        if (iconoLupa != null) {
            btnBuscar.setIcon(iconoLupa);
            btnBuscar.setText(""); // IMPORTANTE: Borrar texto para evitar "..."
        } else {
            // Si falla la imagen, intentamos cargar "search.png" o dejamos una lupa de texto
            ImageIcon iconoAlt = cargarIcono("/icons/search.png");
            if (iconoAlt != null) {
                 btnBuscar.setIcon(iconoAlt);
                 btnBuscar.setText("");
            } else {
                 btnBuscar.setText("Q"); 
                 btnBuscar.setForeground(Color.WHITE);
                 btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
        }

        btnBuscar.addActionListener(e -> cargarRoles(txtBuscar.getText().trim()));

        panelBuscador.add(txtBuscar, BorderLayout.CENTER);
        panelBuscador.add(btnBuscar, BorderLayout.EAST);

        // Armado del Toolbar
        toolbar.add(btnNuevo);
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
        panel.setBorder(new EmptyBorder(10, 30, 30, 30));

        modelo = new DefaultTableModel(new Object[]{"ID", "Periodo", "Cédula", "Empleado", "Neto ($)", "Estado"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(45); // Filas cómodas
        tabla.setBackground(BG_TABLE);       
        tabla.setForeground(TEXT_TABLE);     
        tabla.setGridColor(new Color(230, 230, 230)); 
        
        // --- SELECCIÓN MORADA ---
        tabla.setSelectionBackground(COLOR_PURPLE); 
        tabla.setSelectionForeground(Color.WHITE);  
        
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.setShowVerticalLines(false);

        // Estilo del Header
        JTableHeader header = tabla.getTableHeader();
        header.setBackground(BG_HEADER);
        header.setForeground(TEXT_HEADER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, HEADER_LINE));
        header.setPreferredSize(new Dimension(0, 40));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(BG_TABLE);
        scroll.setBorder(BorderFactory.createEmptyBorder()); 

        // --- RENDERIZADO DE CELDAS ---
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        tabla.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); 
        tabla.getColumnModel().getColumn(0).setMaxWidth(50); 

        tabla.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); 
        tabla.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); 

        // Columna Neto
        tabla.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
             public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                 super.getTableCellRendererComponent(t, v, s, f, r, c);
                 setHorizontalAlignment(JLabel.RIGHT);
                 setFont(new Font("Segoe UI", Font.BOLD, 14));
                 
                 if (s) {
                     setForeground(Color.WHITE); 
                 } else {
                     setForeground(new Color(40, 40, 40)); 
                 }
                 return this;
             }
        });

        // Estilo de Estado
        tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(JLabel.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));

                String estado = (String) value;
                if ("ABI".equals(estado)) { l.setForeground(COLOR_AMARILLO); l.setText("● ABIERTO"); }
                else if ("APR".equals(estado)) { l.setForeground(COLOR_VERDE); l.setText("● APROBADO"); }
                else { l.setForeground(COLOR_ROJO); l.setText("● ANULADO"); }

                if (isSelected) {
                    l.setBackground(table.getSelectionBackground());
                    l.setForeground(Color.WHITE); 
                } else {
                    l.setBackground(BG_TABLE);
                }

                return l;
            }
        });

        tabla.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) verDetalles();
            }
        });

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void cargarRoles(String filtro) {
        modelo.setRowCount(0);

        String sql = "SELECT p.id_Pago_PK, p.pag_Fecha_Inicio, e.emp_ciruc, " +
                     "e.emp_Apellido1, e.emp_Nombre1, " +
                     "pe.emp_Valor_Neto, p.pag_Estado " +
                     "FROM PAGOS p " +
                     "JOIN PAGXEMP pe ON p.id_Pago_PK = pe.id_Pago " +
                     "JOIN EMPLEADOS e ON pe.id_Empleado = e.id_Empleado_PK " +
                     "WHERE e.emp_ciruc LIKE ? OR e.emp_Apellido1 LIKE ? OR e.emp_Nombre1 LIKE ? " +
                     "ORDER BY p.id_Pago_PK DESC";

        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement pst = cn.prepareStatement(sql)) {

            String busqueda = "%" + filtro + "%";
            pst.setString(1, busqueda);
            pst.setString(2, busqueda);
            pst.setString(3, busqueda);

            ResultSet rs = pst.executeQuery();

            SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
            DecimalFormat df = new DecimalFormat("#,##0.00"); 

            while (rs.next()) {
                String ape = rs.getString("emp_Apellido1");
                String nom = rs.getString("emp_Nombre1");
                if (ape != null) ape = ape.trim();
                if (nom != null) nom = nom.trim();
                String nombreCompleto = ape + " " + nom;

                modelo.addRow(new Object[]{
                    rs.getInt("id_Pago_PK"),
                    sdf.format(rs.getDate("pag_Fecha_Inicio")),
                    rs.getString("emp_ciruc"),
                    nombreCompleto,
                    df.format(rs.getDouble("emp_Valor_Neto")),
                    rs.getString("pag_Estado")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("Error", "No se pudo cargar la lista: " + e.getMessage(), "ERROR");
        }
    }

    private ImageIcon cargarIcono(String ruta) {
        try {
            URL url = getClass().getResource(ruta);
            if (url != null) return new ImageIcon(url);
        } catch (Exception e) {}
        return null;
    }

    // --- MENSAJE PERSONALIZADO ---
    private void mostrarMensaje(String titulo, String mensaje, String tipo) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog d = new JDialog(parent, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        d.setUndecorated(true);
        d.setSize(400, 150);
        d.setLocationRelativeTo(parent);
        ((JPanel)d.getContentPane()).setBorder(new LineBorder(COLOR_AZUL_SEARCH, 1));

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_PANEL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(tipo.equals("ERROR") ? COLOR_ROJO : COLOR_VERDE);
        p.add(lblTitulo, gbc);

        gbc.gridy++;
        JLabel lblMsg = new JLabel("<html><div style='width:300px; text-align:center'>"+mensaje+"</div></html>");
        lblMsg.setForeground(Color.WHITE);
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(lblMsg, gbc);

        gbc.gridy++;
        JButton btnOk = crearBoton("OK", new Color(60, 60, 70));
        btnOk.setPreferredSize(new Dimension(80, 30));
        btnOk.addActionListener(e -> d.dispose());
        p.add(btnOk, gbc);

        d.add(p);
        d.setVisible(true);
    }

    private void abrirGenerarRol() {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        DialogoGenerarRol dialogo = new DialogoGenerarRol((Frame) ventanaPadre);
        dialogo.setVisible(true);
        if (dialogo.isExito()) cargarRoles("");
    }

    private void verDetalles() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) return;
        int idPago = Integer.parseInt(tabla.getValueAt(fila, 0).toString());

        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        DialogoDetalleRol dialogo = new DialogoDetalleRol((Frame) ventanaPadre, idPago);
        dialogo.setVisible(true);

        if (dialogo.isCambioRealizado()) cargarRoles("");
    }

    private JButton crearBoton(String texto, Color bg) {
        return crearBotonGen(texto, bg, null);
    }

    private JButton crearBotonConIcono(String texto, Color bg, String iconPath) {
        ImageIcon icon = cargarIcono(iconPath);
        return crearBotonGen(texto, bg, icon);
    }

    private JButton crearBotonGen(String texto, Color bg, Icon icon) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(bg.darker());
                else if (getModel().isRollover()) g2.setColor(bg.brighter());
                else g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        if (icon != null) {
            btn.setIcon(icon);
            btn.setIconTextGap(10); 
            btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        }
        
        return btn;
    }
}
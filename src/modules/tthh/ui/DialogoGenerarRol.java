package modules.tthh.ui;

import ConexionBD.ConexionBD;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.sql.*;

public class DialogoGenerarRol extends JDialog {

    private JComboBox<EmpleadoItem> cmbEmpleado;
    private JDateChooser dcFecha;
    private boolean exito = false;

    // --- PALETA DE COLORES ---
    private final Color BG_MODAL = new Color(20, 20, 24);
    private final Color BG_CARD  = new Color(32, 32, 40);
    private final Color ACCENT_PURPLE = new Color(124, 77, 255); // Morado vibrante
    private final Color PURPLE_DARK = new Color(75, 35, 160);   // Morado oscuro para gradiente
    private final Color TEXT_WHITE = new Color(240, 240, 240);
    private final Color TEXT_GRAY = new Color(150, 150, 160);

    public DialogoGenerarRol(Frame parent) {
        super(parent, "Generar Rol", true);
        setSize(480, 450);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // Transparencia para bordes redondeados

        initUI();
    }

    public boolean isExito() { return exito; }

    private void initUI() {
        // Panel con Fondo Especial (Gradiente y Círculos)
        JPanel mainPanel = new BackgroundPanel(25);
        mainPanel.setLayout(new BorderLayout());

        // --- 1. HEADER ---
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 5));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(40, 20, 20, 20));
        
        JLabel lblTitulo = new JLabel("Generar Rol Mensual");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(TEXT_WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel lblSub = new JLabel("Seleccione un colaborador para procesar su nómina");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_GRAY);
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        
        header.add(lblTitulo);
        header.add(lblSub);

        // --- 2. CONTENEDOR CENTRAL ---
        JPanel cardPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setBorder(new EmptyBorder(25, 35, 25, 35));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Label Colaborador
        cardPanel.add(crearLabelForm("Colaborador:"), gbc);
        gbc.gridy = 1;
        cmbEmpleado = new JComboBox<>();
        estilizarCombo(cmbEmpleado);
        cargarEmpleados();
        cardPanel.add(cmbEmpleado, gbc);

        // Label Periodo
        gbc.gridy = 2;
        gbc.insets = new Insets(25, 0, 5, 0);
        cardPanel.add(crearLabelForm("Periodo de Proceso:"), gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 0, 5, 0);
        dcFecha = new JDateChooser();
        dcFecha.setDateFormatString("MMMM yyyy");
        dcFecha.setDate(new java.util.Date());
        estilizarDateChooser(dcFecha);
        cardPanel.add(dcFecha, gbc);

        // --- 3. FOOTER (BOTONES) ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 30));
        footer.setOpaque(false);
        
        // Botón Generar (Ahora se crea y agrega primero)
        JButton btnGenerar = crearBotonModerno("GENERAR ROL", ACCENT_PURPLE);
        btnGenerar.addActionListener(e -> ejecutarGeneracion());
        
        // Botón Cancelar (Ahora se agrega segundo)
        JButton btnCancelar = crearBotonModerno("CANCELAR", new Color(60, 60, 70));
        btnCancelar.addActionListener(e -> dispose());
        
        // El orden de agregar define la posición de izquierda a derecha en el FlowLayout
        footer.add(btnGenerar);
        footer.add(btnCancelar);

        // Ensamblaje Final
        JPanel contentWrap = new JPanel(new BorderLayout());
        contentWrap.setOpaque(false);
        contentWrap.setBorder(new EmptyBorder(0, 40, 0, 40));
        contentWrap.add(cardPanel, BorderLayout.CENTER);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(contentWrap, BorderLayout.CENTER);
        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    // --- CLASE PARA EL FONDO CON DIFEUMINADO Y CÍRCULOS ---
    class BackgroundPanel extends JPanel {
        private int radius;
        public BackgroundPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Fondo Base Oscuro
            g2.setColor(BG_MODAL);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            // Difuminado Morado en la esquina superior derecha
            GradientPaint gradiente = new GradientPaint(
                getWidth() * 0.5f, 0, new Color(0,0,0,0), 
                getWidth(), 0, PURPLE_DARK
            );
            g2.setPaint(gradiente);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            // Dibujar Círculos de adorno
            g2.setColor(new Color(255, 255, 255, 20)); // Blanco muy transparente
            g2.fill(new Ellipse2D.Double(getWidth() - 100, -20, 150, 150));
            g2.fill(new Ellipse2D.Double(getWidth() - 140, 60, 60, 60));
            g2.fill(new Ellipse2D.Double(getWidth() - 50, 150, 80, 80));

            g2.dispose();
        }
    }

    // --- MÉTODOS DE ESTILO ---
    private JLabel crearLabelForm(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(ACCENT_PURPLE);
        return l;
    }

    private void estilizarCombo(JComboBox cb) {
        cb.setBackground(Color.WHITE);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cb.setPreferredSize(new Dimension(0, 42));
    }

    private void estilizarDateChooser(JDateChooser dc) {
        dc.setPreferredSize(new Dimension(0, 42));
        for (Component c : dc.getComponents()) {
            if (c instanceof JTextField) {
                ((JTextField) c).setBorder(new EmptyBorder(0, 10, 0, 10));
                ((JTextField) c).setFont(new Font("Segoe UI", Font.PLAIN, 14));
            }
        }
    }

    private JButton crearBotonModerno(String texto, Color bg) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(160, 48));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void cargarEmpleados() {
        try (Connection cn = ConexionBD.getConexion();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_Empleado_PK, emp_Nombre1, emp_Apellido1 FROM EMPLEADOS WHERE emp_Estado='ACT'")) {
            while (rs.next()) {
                String full = (rs.getString("emp_Apellido1").trim() + " " + rs.getString("emp_Nombre1").trim());
                cmbEmpleado.addItem(new EmpleadoItem(rs.getInt(1), full));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    private void mostrarMensajePersonalizado(String titulo, String mensaje, String tipo) {
    JDialog dialog = new JDialog(this, true);
    dialog.setUndecorated(true);
    dialog.setSize(400, 180);
    dialog.setLocationRelativeTo(this);
    
    // Panel principal del mensaje con borde morado
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(new Color(30, 30, 35));
    panel.setBorder(new LineBorder(ACCENT_PURPLE, 2));

    // Título
    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
    lblTitulo.setForeground(tipo.equals("ERROR") ? new Color(231, 76, 60) : ACCENT_PURPLE);
    lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
    lblTitulo.setBorder(new EmptyBorder(15, 0, 10, 0));

    // Cuerpo del mensaje (soporta varias líneas)
    JLabel lblMsg = new JLabel("<html><center>" + mensaje + "</center></html>");
    lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblMsg.setForeground(Color.WHITE);
    lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
    lblMsg.setBorder(new EmptyBorder(0, 25, 10, 25));

    // Botón de cerrar estilizado
    JButton btnCerrar = crearBotonModerno("ENTENDIDO", new Color(60, 60, 70));
    btnCerrar.setPreferredSize(new Dimension(120, 35));
    btnCerrar.addActionListener(e -> dialog.dispose());
    
    JPanel pnlBoton = new JPanel();
    pnlBoton.setOpaque(false);
    pnlBoton.setBorder(new EmptyBorder(0, 0, 15, 0));
    pnlBoton.add(btnCerrar);

    panel.add(lblTitulo, BorderLayout.NORTH);
    panel.add(lblMsg, BorderLayout.CENTER);
    panel.add(pnlBoton, BorderLayout.SOUTH);

    dialog.add(panel);
    dialog.setVisible(true);
}

 private void ejecutarGeneracion() {
    if (cmbEmpleado.getSelectedItem() == null || dcFecha.getDate() == null) {
        mostrarMensajePersonalizado("DATOS INCOMPLETOS", "Por favor, seleccione un colaborador y un periodo válido.", "WARN");
        return;
    }

    EmpleadoItem emp = (EmpleadoItem) cmbEmpleado.getSelectedItem();
    java.sql.Date fechaSql = new java.sql.Date(dcFecha.getDate().getTime());

    try (Connection cn = ConexionBD.getConexion();
         CallableStatement cs = cn.prepareCall("{CALL sp_Pagos_GenerarMensual(?, ?, ?)}")) {
        
        cs.setInt(1, emp.id);
        cs.setDate(2, fechaSql);
        cs.registerOutParameter(3, Types.INTEGER);
        
        cs.execute();
        
        this.exito = true;
        mostrarMensajePersonalizado("ÉXITO", "Nómina generada correctamente para " + emp.nombre, "SUCCESS");
        dispose();

    } catch (SQLException e) {
        // INTERCEPTAMOS EL ERROR DE SQL AQUÍ
        if (e.getErrorCode() == 51000 || e.getMessage().contains("Ya existe un rol")) {
            mostrarMensajePersonalizado("PROCESO DENEGADO", 
                "El colaborador " + emp.nombre + " ya tiene un rol generado para el mes seleccionado.", 
                "ERROR");
        } else {
            mostrarMensajePersonalizado("ERROR TÉCNICO", "Ocurrió un error en la base de datos: " + e.getMessage(), "ERROR");
        }
    }
}

    class EmpleadoItem {
        int id; String nombre;
        public EmpleadoItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
}
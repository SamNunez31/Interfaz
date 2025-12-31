package app;

import ConexionBD.ConexionBD;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DialogoProveedor extends JDialog {

    private int idProveedor;
    
    // Componentes de la interfaz
    private JTextField txtRuc, txtNombre, txtTelefono, txtCelular, txtEmail, txtDireccion;
    private JComboBox<String> cmbCiudad;
    private JButton btnGuardar; 
    
    private boolean operacionExitosa = false;
    private List<Integer> listaIdsCiudades = new ArrayList<>();

    // Colores del Tema (Idénticos a DialogoEmpleado)
    private final Color ACCENT_PURPLE = new Color(124, 77, 255);
    private final Color INPUT_BORDER_IDLE = new Color(80, 80, 90);
    private final Color CARD_BG = new Color(30, 25, 40, 240);
    
    // >>> COLORES DEFINITIVOS <<<
    private final Color FIELD_BG = new Color(40, 35, 50); // Fondo Oscuro
    private final Color TEXT_WHITE = Color.WHITE;         // Texto Blanco Puro

    public DialogoProveedor(Frame padre, int id) {
        super(padre, true);
        this.idProveedor = id;
        
        setTitle(id == 0 ? "Nuevo Proveedor" : "Detalles del Proveedor");
        setSize(900, 650); // Ajuste de tamaño
        setLocationRelativeTo(padre);
        setResizable(false);
        
        JPanel background = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(40, 20, 60), getWidth(), getHeight(), new Color(10, 10, 15));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        background.setLayout(new GridBagLayout());
        setContentPane(background);
        
        iniciarUI(background);
        cargarCombos();
        
        if (id > 0) {
            cargarDatos(id);
        }
    }
    
    public boolean isOperacionExitosa() {
        return operacionExitosa;
    }

    // ========================================================================
    // MODO LECTURA (PROVEEDORES INACTIVOS)
    // ========================================================================
    public void activarModoSoloLectura() {
        // Bloqueamos campos de texto
        estilarCampoBloqueado(txtRuc);
        estilarCampoBloqueado(txtNombre);
        estilarCampoBloqueado(txtTelefono);
        estilarCampoBloqueado(txtCelular);
        estilarCampoBloqueado(txtEmail);
        estilarCampoBloqueado(txtDireccion);

        // Combos
        configurarComboLectura(cmbCiudad);

        if (btnGuardar != null) btnGuardar.setVisible(false);
    }

    private void estilarCampoBloqueado(JTextField txt) {
        txt.setEditable(false);
        txt.setFocusable(false);
        txt.setEnabled(true); 
        
        txt.setOpaque(true); 
        txt.setBackground(FIELD_BG); 
        txt.setForeground(TEXT_WHITE); 
        txt.setDisabledTextColor(TEXT_WHITE);
        
        txt.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(80,80,80)), 
            new EmptyBorder(0, 5, 0, 5)
        ));
    }

    private void configurarComboLectura(JComboBox cmb) {
        cmb.setEditable(true);
        Component editorComp = cmb.getEditor().getEditorComponent();
        if (editorComp instanceof JTextField) {
            estilarCampoBloqueado((JTextField) editorComp);
        }
        cmb.setEnabled(false);
    }

    private void iniciarUI(JPanel background) {
        JPanel card = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(30, 50, 30, 50));
        
        JLabel lblTitle = new JLabel(idProveedor == 0 ? "NUEVO PROVEEDOR" : "INFORMACIÓN DEL PROVEEDOR");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        // --- COLUMNA IZQUIERDA ---
        gbc.gridx = 0; int y = 0; 
        addSectionTitle(formPanel, "Datos Principales", 0, y++, gbc);
        
        txtRuc = addInput(formPanel, "RUC / Cédula", 0, y, gbc); y += 2;
        txtRuc.addKeyListener(new KeyAdapter() { 
            public void keyTyped(KeyEvent evt) { 
                if(!Character.isDigit(evt.getKeyChar())) evt.consume(); 
                if(txtRuc.getText().length()>=13) evt.consume(); 
            }
        });

        txtNombre = addInput(formPanel, "Razón Social / Nombre", 0, y, gbc); y += 2; 
        
        txtEmail = addInput(formPanel, "Correo Electrónico", 0, y, gbc); y += 2;

        txtDireccion = addInput(formPanel, "Dirección", 0, y, gbc); y += 2;
        
        // --- COLUMNA DERECHA ---
        gbc.gridx = 1; y = 0; 
        addSectionTitle(formPanel, "Contacto y Ubicación", 1, y++, gbc);
        
        txtCelular = addInput(formPanel, "Celular (09...)", 1, y, gbc); y += 2;
        txtCelular.addKeyListener(new KeyAdapter() { 
            public void keyTyped(KeyEvent evt) { 
                if(!Character.isDigit(evt.getKeyChar())) evt.consume(); 
                if(txtCelular.getText().length()>=10) evt.consume(); 
            }
        });

        txtTelefono = addInput(formPanel, "Teléfono Convencional", 1, y, gbc); y += 2;
        txtTelefono.addKeyListener(new KeyAdapter() { 
            public void keyTyped(KeyEvent evt) { 
                if(!Character.isDigit(evt.getKeyChar())) evt.consume(); 
                if(txtTelefono.getText().length()>=10) evt.consume(); 
            }
        });
        
        addLabel(formPanel, "Ciudad", 1, y++, gbc);
        cmbCiudad = new MaterialComboBox<>(); formPanel.add(cmbCiudad, gbc); y++;

        card.add(formPanel, BorderLayout.CENTER);

        // --- BOTONES ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        
        btnGuardar = new JButton("GUARDAR DATOS"); 
        styleButtonPrimary(btnGuardar);
        btnGuardar.addActionListener(e -> guardarDatos());
        
        JButton btnCancel = new JButton("CERRAR"); 
        styleButtonSecondary(btnCancel);
        btnCancel.addActionListener(e -> dispose());
        
        btnPanel.add(btnGuardar); 
        btnPanel.add(btnCancel);
        
        card.add(btnPanel, BorderLayout.SOUTH);
        background.add(card);
    }

    private void cargarDatos(int id) {
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement pst = cn.prepareStatement("SELECT * FROM PROVEEDORES WHERE id_Proveedor = ?")) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                txtRuc.setText(validarNull(rs.getString("prv_RUC_CED")));
                txtNombre.setText(validarNull(rs.getString("prv_Nombre")));
                txtTelefono.setText(validarNull(rs.getString("prv_Telefono"))); 
                txtCelular.setText(validarNull(rs.getString("prv_Celular")));
                txtEmail.setText(validarNull(rs.getString("prv_Mail")));
                txtDireccion.setText(validarNull(rs.getString("prv_Direccion")));
                
                int idCiu = rs.getInt("id_Ciudad");
                if(listaIdsCiudades.contains(idCiu)) cmbCiudad.setSelectedIndex(listaIdsCiudades.indexOf(idCiu));
                
                // MODO ACTUALIZAR (Normal):
                // Bloqueamos el RUC (Llave de negocio) visualmente para evitar problemas de duplicidad al editar
                txtRuc.setEditable(false);
                txtRuc.setForeground(Color.GRAY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarToast("Error al cargar datos: " + e.getMessage(), true);
        }
    }

    private String validarNull(String texto) {
        return (texto == null) ? "" : texto.trim();
    }

    private void cargarCombos() {
        try (Connection cn = ConexionBD.getConexion()) {
            // Cargar Ciudades
            ResultSet rs = cn.createStatement().executeQuery("SELECT id_Ciudad_PK, ciu_descripcion FROM CIUDADES ORDER BY ciu_descripcion");
            while(rs.next()) { 
                listaIdsCiudades.add(rs.getInt(1)); 
                cmbCiudad.addItem(rs.getString(2)); 
            }
        } catch (Exception e) {}
    }

    private void guardarDatos() {
        if (!validarTodo()) return; 
        
        try (Connection cn = ConexionBD.getConexion()) {
            String sql; PreparedStatement pst;
            
            int idCiudad = -1;
            if (cmbCiudad.getSelectedIndex() >= 0) {
                 idCiudad = listaIdsCiudades.get(cmbCiudad.getSelectedIndex());
            }

            if (idProveedor == 0) {
                // INSERT
                sql = "INSERT INTO PROVEEDORES (prv_RUC_CED, prv_Nombre, prv_Telefono, prv_Celular, prv_Mail, prv_Direccion, id_Ciudad, ESTADO_PRV) VALUES (?,?,?,?,?,?,?, 'ACT')";
                pst = cn.prepareStatement(sql);
                pst.setString(1, txtRuc.getText().trim());
                pst.setString(2, txtNombre.getText().trim().toUpperCase());
                pst.setString(3, txtTelefono.getText().trim().isEmpty() ? null : txtTelefono.getText().trim());
                pst.setString(4, txtCelular.getText().trim());
                pst.setString(5, txtEmail.getText().trim());
                pst.setString(6, txtDireccion.getText().trim().toUpperCase());
                
                if (idCiudad != -1) pst.setInt(7, idCiudad); else pst.setNull(7, java.sql.Types.INTEGER);
                
            } else {
                // UPDATE
                sql = "UPDATE PROVEEDORES SET prv_Nombre=?, prv_Telefono=?, prv_Celular=?, prv_Mail=?, prv_Direccion=?, id_Ciudad=? WHERE id_Proveedor=?";
                pst = cn.prepareStatement(sql);
                pst.setString(1, txtNombre.getText().trim().toUpperCase());
                pst.setString(2, txtTelefono.getText().trim().isEmpty() ? null : txtTelefono.getText().trim());
                pst.setString(3, txtCelular.getText().trim());
                pst.setString(4, txtEmail.getText().trim());
                pst.setString(5, txtDireccion.getText().trim().toUpperCase());
                
                if (idCiudad != -1) pst.setInt(6, idCiudad); else pst.setNull(6, java.sql.Types.INTEGER);
                
                pst.setInt(7, idProveedor);
            }
            
            pst.executeUpdate();
            
            this.operacionExitosa = true; 
            dispose(); 
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarToast("Error crítico en BD: " + e.getMessage(), true);
        }
    }

    private boolean validarTodo() {
        if (txtRuc.getText().trim().isEmpty() || txtNombre.getText().trim().isEmpty() || txtCelular.getText().trim().isEmpty()) {
            mostrarToast("Campos obligatorios: RUC, Nombre y Celular.", true); return false;
        }
        
        if (idProveedor == 0) {
            String ruc = txtRuc.getText().trim();
            if(ruc.length() != 10 && ruc.length() != 13) { mostrarToast("Identificación inválida (10 o 13 dígitos).", true); return false; }
            
            // Validación básica de dígitos
            if(!ruc.matches("\\d+")) { mostrarToast("El RUC debe contener solo números.", true); return false; }

            if (!validarRucUnicoEnBD(ruc)) { mostrarToast("Ya existe un proveedor con esa identificación.", true); return false; }
        }

        if (!txtCelular.getText().startsWith("09") || txtCelular.getText().length() != 10) {
            mostrarToast("El celular debe empezar con 09 y tener 10 dígitos.", true); return false;
        }

        if (!txtEmail.getText().isEmpty() && !validarEmail(txtEmail.getText())) { mostrarToast("Correo inválido (ej: usuario@dominio.com).", true); return false; }
        
        if (cmbCiudad.getSelectedIndex() < 0) { mostrarToast("Seleccione una ciudad.", true); return false; }
        
        return true;
    }

    private boolean validarEmail(String e) {
        String regex = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.compile(regex).matcher(e).matches();
    }

    private boolean validarRucUnicoEnBD(String ruc) {
        try (Connection cn = ConexionBD.getConexion(); PreparedStatement pst = cn.prepareStatement("SELECT id_Proveedor FROM PROVEEDORES WHERE prv_RUC_CED=?")) {
            pst.setString(1, ruc); ResultSet rs = pst.executeQuery();
            if (rs.next()) return false; 
        } catch (Exception e) {} return true;
    }

    private void mostrarToast(String mensaje, boolean esError) {
        JWindow toast = new JWindow(this);
        toast.setBackground(new Color(0,0,0,0));
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(esError ? new Color(220, 53, 69) : new Color(40, 167, 69));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
            }
        };
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(mensaje);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setIcon(UIManager.getIcon(esError ? "OptionPane.errorIcon" : "OptionPane.informationIcon"));
        panel.add(lbl); toast.add(panel); toast.pack();
        Point p = this.getLocationOnScreen();
        toast.setLocation(p.x + (this.getWidth() - toast.getWidth()) / 2, p.y + 50);
        toast.setVisible(true);
        new Timer(3000, e -> { toast.setVisible(false); toast.dispose(); }).start();
    }

    private void addSectionTitle(JPanel p, String t, int x, int y, GridBagConstraints g) {
        JLabel l = new JLabel(t); l.setForeground(ACCENT_PURPLE); l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        GridBagConstraints g2 = (GridBagConstraints)g.clone(); g2.gridx=x; g2.gridy=y; g2.insets=new Insets(10,15,20,15); p.add(l,g2);
    }
    private void addLabel(JPanel p, String t, int x, int y, GridBagConstraints g) {
        JLabel l = new JLabel(t); l.setForeground(new Color(180,180,180)); l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        GridBagConstraints g2 = (GridBagConstraints)g.clone(); g2.gridx=x; g2.gridy=y; g2.insets=new Insets(10,15,2,15); p.add(l,g2);
        g.gridx=x; g.gridy=y+1; g.insets=new Insets(0,15,0,15);
    }
    private JTextField addInput(JPanel p, String t, int x, int y, GridBagConstraints g) {
        addLabel(p, t, x, y, g);
        MaterialTextField txt = new MaterialTextField();
        GridBagConstraints g2 = (GridBagConstraints)g.clone(); g2.insets=new Insets(2,15,0,15); p.add(txt, g2);
        return txt;
    }
    private void styleButtonPrimary(JButton b) {
        b.setBackground(ACCENT_PURPLE); b.setForeground(Color.WHITE); b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false); b.setBorderPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setPreferredSize(new Dimension(160, 45));
    }
    private void styleButtonSecondary(JButton b) {
        b.setBackground(new Color(255,255,255,0)); b.setForeground(new Color(200,200,200)); b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false); b.setBorder(BorderFactory.createLineBorder(new Color(100,100,100))); b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setPreferredSize(new Dimension(140, 45));
    }
    class MaterialTextField extends JTextField {
        public MaterialTextField() { setOpaque(false); setForeground(Color.WHITE); setFont(new Font("Segoe UI", Font.PLAIN, 14)); setPreferredSize(new Dimension(200,30)); setBorder(new MatteBorder(0,0,2,0, INPUT_BORDER_IDLE)); setCaretColor(ACCENT_PURPLE); addFocusListener(new FocusAdapter(){public void focusGained(FocusEvent e){setBorder(new MatteBorder(0,0,2,0,ACCENT_PURPLE));} public void focusLost(FocusEvent e){setBorder(new MatteBorder(0,0,2,0,INPUT_BORDER_IDLE));}});}
    }
    class MaterialComboBox<E> extends JComboBox<E> {
        public MaterialComboBox(E[] i) {super(i);style();} public MaterialComboBox(){super();style();} private void style(){setBackground(new Color(40,35,50)); setForeground(Color.WHITE); setFont(new Font("Segoe UI", Font.PLAIN, 14)); setPreferredSize(new Dimension(200,35)); setBorder(new MatteBorder(0,0,2,0,INPUT_BORDER_IDLE));}
    }
}
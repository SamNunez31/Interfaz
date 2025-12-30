package app;

import ConexionBD.ConexionBD; // Asegúrate de que este import sea el correcto en tu proyecto
import com.toedter.calendar.JDateChooser;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class DialogoEmpleado extends JDialog {

    private int idEmpleado;
    private JTextField txtCedula, txtNom1, txtNom2, txtApe1, txtApe2, txtEmail, txtSueldo;
    private JComboBox<String> cmbSexo, cmbDpto, cmbRol;
    private JDateChooser dcFecha;
    
    // Bandera de éxito para que el padre sepa qué hacer al cerrar
    private boolean operacionExitosa = false;

    private List<Integer> listaIdsDpto = new ArrayList<>();
    private List<Integer> listaIdsRol = new ArrayList<>();

    // Colores del Tema
    private final Color ACCENT_PURPLE = new Color(124, 77, 255);
    private final Color INPUT_BORDER_IDLE = new Color(80, 80, 90);
    private final Color CARD_BG = new Color(30, 25, 40, 240);

    public DialogoEmpleado(Frame padre, int id) {
        super(padre, true); // Modal = true
        this.idEmpleado = id;
        
        setTitle(id == 0 ? "Nuevo Empleado" : "Editar Empleado");
        setSize(950, 750);
        setLocationRelativeTo(padre);
        setResizable(false);
        
        // Panel de Fondo con Gradiente
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
        cargarCombos(); // Cargar listas desplegables
        
        if (id > 0) {
            cargarDatos(id); // Si es editar, cargamos datos
        }
    }
    
    public boolean isOperacionExitosa() {
        return operacionExitosa;
    }

    private void iniciarUI(JPanel background) {
        // Tarjeta Central
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
        
        JLabel lblTitle = new JLabel(idEmpleado == 0 ? "NUEVO INGRESO" : "ACTUALIZAR DATOS");
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
        addSectionTitle(formPanel, "Información Personal", 0, y++, gbc);
        
        txtCedula = addInput(formPanel, "Cédula / RUC", 0, y, gbc); y += 2;
        // Validación solo números y longitud max 13
        txtCedula.addKeyListener(new KeyAdapter() { 
            public void keyTyped(KeyEvent evt) { 
                if(!Character.isDigit(evt.getKeyChar())) evt.consume(); 
                if(txtCedula.getText().length()>=13) evt.consume(); 
            }
        });

        txtNom1 = addInput(formPanel, "Primer Nombre", 0, y, gbc); y += 2; addTextoValidation(txtNom1);
        txtNom2 = addInput(formPanel, "Segundo Nombre", 0, y, gbc); y += 2; addTextoValidation(txtNom2);
        txtApe1 = addInput(formPanel, "Primer Apellido", 0, y, gbc); y += 2; addTextoValidation(txtApe1);
        txtApe2 = addInput(formPanel, "Segundo Apellido", 0, y, gbc); y += 2; addTextoValidation(txtApe2);
        
        // --- COLUMNA DERECHA ---
        gbc.gridx = 1; y = 0; 
        addSectionTitle(formPanel, "Detalles Corporativos", 1, y++, gbc);
        
        addLabel(formPanel, "Fecha Nacimiento", 1, y++, gbc);
        dcFecha = new JDateChooser(); 
        dcFecha.setDateFormatString("dd/MM/yyyy");
        dcFecha.setBackground(CARD_BG); 
        dcFecha.setPreferredSize(new Dimension(200, 35));
        // Estilo oscuro para el JDateChooser
        JTextField dateEditor = ((JTextField)dcFecha.getDateEditor().getUiComponent());
        dateEditor.setBackground(new Color(40,35,50));
        dateEditor.setForeground(Color.WHITE);
        dateEditor.setBorder(new MatteBorder(0,0,2,0, INPUT_BORDER_IDLE));
        formPanel.add(dcFecha, gbc); y++; 

        addLabel(formPanel, "Género", 1, y++, gbc);
        cmbSexo = new MaterialComboBox<>(new String[]{"M", "F"});
        formPanel.add(cmbSexo, gbc); y++;

        txtEmail = addInput(formPanel, "Correo Electrónico", 1, y, gbc); y += 2;
        
        txtSueldo = addInput(formPanel, "Sueldo Mensual ($)", 1, y, gbc); y += 2;
        
        // >>> VALIDACIÓN CORRECTA DE SALARIO (UN SOLO PUNTO) <<<
        txtSueldo.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                char c = evt.getKeyChar();
                String text = txtSueldo.getText();
                // 1. Solo dígitos y punto
                if (!Character.isDigit(c) && c != '.') {
                    evt.consume();
                    return;
                }
                // 2. Si es punto, verificar que no exista ya
                if (c == '.' && text.contains(".")) {
                    evt.consume();
                }
            }
        });
        
        addLabel(formPanel, "Departamento", 1, y++, gbc);
        cmbDpto = new MaterialComboBox<>(); formPanel.add(cmbDpto, gbc); y++;
        
        addLabel(formPanel, "Rol", 1, y++, gbc);
        cmbRol = new MaterialComboBox<>(); formPanel.add(cmbRol, gbc); y++;

        card.add(formPanel, BorderLayout.CENTER);

        // --- BOTONES ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        
        JButton btnSave = new JButton("GUARDAR DATOS"); styleButtonPrimary(btnSave);
        btnSave.addActionListener(e -> guardarDatos());
        
        JButton btnCancel = new JButton("CANCELAR"); styleButtonSecondary(btnCancel);
        btnCancel.addActionListener(e -> dispose());
        
        btnPanel.add(btnSave); btnPanel.add(btnCancel);
        
        card.add(btnPanel, BorderLayout.SOUTH);
        background.add(card);
    }

    // ========================================================================
    // CARGA DE DATOS (Con corrección de NULLs y Bloqueo)
    // ========================================================================
    private void cargarDatos(int id) {
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement pst = cn.prepareStatement("SELECT * FROM Empleados WHERE id_Empleado_PK = ?")) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                // Usamos validarNull para evitar que el campo se quede en blanco si la BD tiene null
                txtCedula.setText(validarNull(rs.getString("emp_ciruc")));
                txtNom1.setText(validarNull(rs.getString("emp_Nombre1")));
                txtNom2.setText(validarNull(rs.getString("emp_Nombre2"))); 
                txtApe1.setText(validarNull(rs.getString("emp_Apellido1")));
                txtApe2.setText(validarNull(rs.getString("emp_Apellido2")));
                txtEmail.setText(validarNull(rs.getString("emp_Mail")));
                
                double sueldo = rs.getDouble("emp_Sueldo");
                txtSueldo.setText(String.valueOf(sueldo));
                
                String sexo = rs.getString("emp_Sexo");
                if (sexo != null) cmbSexo.setSelectedItem(sexo);
                
                if(rs.getDate("emp_FechaNacimiento") != null) {
                    dcFecha.setDate(rs.getDate("emp_FechaNacimiento"));
                }
                
                // Selección de combos
                int idDep = rs.getInt("id_Departamento");
                if(listaIdsDpto.contains(idDep)) cmbDpto.setSelectedIndex(listaIdsDpto.indexOf(idDep));
                
                int idRol = rs.getInt("id_Rol");
                if(listaIdsRol.contains(idRol)) cmbRol.setSelectedIndex(listaIdsRol.indexOf(idRol));
                
                // >>> BLOQUEAR CAMPOS INMUTABLES <<<
                txtCedula.setEditable(false);
                txtCedula.setForeground(Color.GRAY);
                
                dcFecha.setEnabled(false); 
                ((JTextField)dcFecha.getDateEditor().getUiComponent()).setDisabledTextColor(Color.GRAY);
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
            ResultSet rs = cn.createStatement().executeQuery("SELECT id_Departamento_PK, dep_Nombre FROM Departamentos WHERE ESTADO_DEP = 'ACT'");
            while(rs.next()) { listaIdsDpto.add(rs.getInt(1)); cmbDpto.addItem(rs.getString(2)); }
            rs = cn.createStatement().executeQuery("SELECT id_Rol_PK, rol_Descripcion FROM Roles WHERE ESTADO_ROL = 'ACT'");
            while(rs.next()) { listaIdsRol.add(rs.getInt(1)); cmbRol.addItem(rs.getString(2)); }
        } catch (Exception e) {}
    }

    private void guardarDatos() {
        if (!validarTodo()) return; 
        
        try (Connection cn = ConexionBD.getConexion()) {
            String sql; PreparedStatement pst;
            java.sql.Date fechaSQL = new java.sql.Date(dcFecha.getDate().getTime());
            double sueldo = Double.parseDouble(txtSueldo.getText().trim());
            
            int idDpto = listaIdsDpto.get(cmbDpto.getSelectedIndex());
            int idRol = listaIdsRol.get(cmbRol.getSelectedIndex());

            if (idEmpleado == 0) {
                // INSERT
                sql = "INSERT INTO Empleados (emp_ciruc, emp_Nombre1, emp_Nombre2, emp_Apellido1, emp_Apellido2, emp_Sexo, emp_FechaNacimiento, emp_Mail, emp_Sueldo, id_Departamento, id_Rol, emp_Estado) VALUES (?,?,?,?,?,?,?,?,?,?,?, 'ACT')";
                pst = cn.prepareStatement(sql);
                pst.setString(1, txtCedula.getText().trim());
                pst.setString(2, txtNom1.getText().trim().toUpperCase());
                pst.setString(3, txtNom2.getText().trim().toUpperCase());
                pst.setString(4, txtApe1.getText().trim().toUpperCase());
                pst.setString(5, txtApe2.getText().trim().toUpperCase());
                pst.setString(6, cmbSexo.getSelectedItem().toString());
                pst.setDate(7, fechaSQL);
                pst.setString(8, txtEmail.getText().trim());
                pst.setDouble(9, sueldo);
                pst.setInt(10, idDpto); pst.setInt(11, idRol);
            } else {
                // UPDATE (Sin tocar Cédula ni Fecha)
                sql = "UPDATE Empleados SET emp_Nombre1=?, emp_Nombre2=?, emp_Apellido1=?, emp_Apellido2=?, emp_Sexo=?, emp_Mail=?, emp_Sueldo=?, id_Departamento=?, id_Rol=? WHERE id_Empleado_PK=?";
                pst = cn.prepareStatement(sql);
                pst.setString(1, txtNom1.getText().trim().toUpperCase());
                pst.setString(2, txtNom2.getText().trim().toUpperCase());
                pst.setString(3, txtApe1.getText().trim().toUpperCase());
                pst.setString(4, txtApe2.getText().trim().toUpperCase());
                pst.setString(5, cmbSexo.getSelectedItem().toString());
                pst.setString(6, txtEmail.getText().trim());
                pst.setDouble(7, sueldo);
                pst.setInt(8, idDpto); 
                pst.setInt(9, idRol);
                pst.setInt(10, idEmpleado);
            }
            pst.executeUpdate();
            
            this.operacionExitosa = true; 
            dispose(); // Cerramos para que el padre muestre el mensaje de éxito
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarToast("Error crítico en BD: " + e.getMessage(), true);
        }
    }

    // ========================================================================
    // VALIDACIONES COMPLETAS
    // ========================================================================

    private boolean validarTodo() {
        if (txtCedula.getText().trim().isEmpty() || txtNom1.getText().trim().isEmpty() || txtApe1.getText().trim().isEmpty()) {
            mostrarToast("Campos incompletos: Cédula, Nombres y Apellidos.", true); return false;
        }
        
        // Solo validar Cédula si es nuevo registro (si edita, ya es confiable)
        if (idEmpleado == 0) {
            String cedula = txtCedula.getText().trim();
            if(cedula.length() == 13 && !cedula.endsWith("001")) { mostrarToast("El RUC debe terminar en 001.", true); return false; }
            if(cedula.length() != 10 && cedula.length() != 13) { mostrarToast("Identificación inválida (10 o 13 dígitos).", true); return false; }
            
            if(!validarCedulaEcuatoriana(cedula.length() == 13 ? cedula.substring(0,10) : cedula)) {
                mostrarToast("Cédula o RUC incorrecto (Dígito verificador).", true); return false;
            }
            if (!validarCedulaUnicaEnBD(cedula)) { mostrarToast("Ya existe un empleado con esa identificación.", true); return false; }
        }

        if (!txtEmail.getText().isEmpty() && !validarEmail(txtEmail.getText())) { mostrarToast("Correo inválido (ej: usuario@dominio.com).", true); return false; }
        
        // Validar fecha solo si es nuevo
        if (idEmpleado == 0) {
            if (dcFecha.getDate() == null) { mostrarToast("Ingrese fecha nacimiento.", true); return false; }
            if (!validarEdad(dcFecha.getDate())) return false;
        }
        
        if (txtSueldo.getText().isEmpty()) { mostrarToast("Ingrese un sueldo.", true); return false; }
        if (cmbDpto.getSelectedIndex() < 0 || cmbRol.getSelectedIndex() < 0) { mostrarToast("Seleccione Departamento y Rol.", true); return false; }
        
        return true;
    }

    private boolean validarCedulaEcuatoriana(String cedula) {
        try {
            int prov = Integer.parseInt(cedula.substring(0, 2));
            if (prov < 1 || prov > 24) return false;
            int suma = 0;
            for (int i = 0; i < 9; i++) {
                int num = Character.getNumericValue(cedula.charAt(i));
                if (i % 2 == 0) { num *= 2; if (num > 9) num -= 9; }
                suma += num;
            }
            int verif = (10 - (suma % 10)) % 10;
            if (verif == 10) verif = 0;
            return verif == Character.getNumericValue(cedula.charAt(9));
        } catch (Exception e) { return false; }
    }

    // >>> VALIDACIÓN EMAIL MEJORADA <<<
    private boolean validarEmail(String e) {
        // Regex estricta: usuario@dominio.extensión (al menos 2 letras)
        String regex = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.compile(regex).matcher(e).matches();
    }

    private boolean validarEdad(Date f) {
        Calendar nac = Calendar.getInstance(); nac.setTime(f);
        Calendar hoy = Calendar.getInstance();
        if (nac.after(hoy)) { mostrarToast("Fecha futura no permitida.", true); return false; }
        int edad = hoy.get(Calendar.YEAR) - nac.get(Calendar.YEAR);
        if (hoy.get(Calendar.DAY_OF_YEAR) < nac.get(Calendar.DAY_OF_YEAR)) edad--;
        if (edad < 18) { mostrarToast("Debe ser mayor de edad (+18).", true); return false; }
        if (edad > 65) { mostrarToast("Edad excede límite (65 años).", true); return false; }
        return true;
    }

    private boolean validarCedulaUnicaEnBD(String c) {
        try (Connection cn = ConexionBD.getConexion(); PreparedStatement pst = cn.prepareStatement("SELECT id_Empleado_PK FROM Empleados WHERE emp_ciruc=?")) {
            pst.setString(1, c); ResultSet rs = pst.executeQuery();
            if (rs.next()) return false; 
        } catch (Exception e) {} return true;
    }

    // =========================================================================
    // VISUALES (TOAST + ESTILOS)
    // =========================================================================
    
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
    private void addTextoValidation(JTextField txt) {
        txt.addKeyListener(new KeyAdapter() { public void keyTyped(KeyEvent e) { if(!Character.isLetter(e.getKeyChar()) && e.getKeyChar()!=' ') e.consume(); }});
    }
    class MaterialTextField extends JTextField {
        public MaterialTextField() { setOpaque(false); setForeground(Color.WHITE); setFont(new Font("Segoe UI", Font.PLAIN, 14)); setPreferredSize(new Dimension(200,30)); setBorder(new MatteBorder(0,0,2,0, INPUT_BORDER_IDLE)); setCaretColor(ACCENT_PURPLE); addFocusListener(new FocusAdapter(){public void focusGained(FocusEvent e){setBorder(new MatteBorder(0,0,2,0,ACCENT_PURPLE));} public void focusLost(FocusEvent e){setBorder(new MatteBorder(0,0,2,0,INPUT_BORDER_IDLE));}});}
    }
    class MaterialComboBox<E> extends JComboBox<E> {
        public MaterialComboBox(E[] i) {super(i);style();} public MaterialComboBox(){super();style();} private void style(){setBackground(new Color(40,35,50)); setForeground(Color.WHITE); setFont(new Font("Segoe UI", Font.PLAIN, 14)); setPreferredSize(new Dimension(200,35)); setBorder(new MatteBorder(0,0,2,0,INPUT_BORDER_IDLE));}
    }
}
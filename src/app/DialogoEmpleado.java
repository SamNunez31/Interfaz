package app;

import ConexionBD.ConexionBD;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
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

    // Componentes
    private JTextField txtCedula, txtNom1, txtNom2, txtApe1, txtApe2, txtEmail, txtSueldo;
    private JComboBox<String> cmbSexo, cmbDpto, cmbRol;
    private JDateChooser dcFecha;
    private JButton btnGuardar;

    private boolean operacionExitosa = false;
    private final List<Integer> listaIdsDpto = new ArrayList<>();
    private final List<Integer> listaIdsRol = new ArrayList<>();

    // Negocio
    private final double SALARIO_BASICO = 460.00;

    // Tema (estética del 1)
    private final Color ACCENT_PURPLE = new Color(124, 77, 255);
    private final Color PURPLE_DARK = new Color(75, 35, 160);
    private final Color INPUT_BORDER_IDLE = new Color(80, 80, 90);
    private final Color CARD_BG = new Color(30, 25, 40, 240);
    private final Color FIELD_BG = new Color(40, 35, 50);
    private final Color TEXT_WHITE = Color.WHITE;
    private final Color BG_OSCURO = new Color(20, 20, 24);

    public DialogoEmpleado(Frame padre, int id) {
        super(padre, true);
        this.idEmpleado = id;

        setTitle(id == 0 ? "Nuevo Empleado" : "Detalles del Empleado");
        setSize(950, 750);
        setLocationRelativeTo(padre);
        setResizable(false);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        // Fondo premium con bombitas
        JPanel background = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(BG_OSCURO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                GradientPaint gradiente = new GradientPaint(
                        getWidth() * 0.5f, getHeight() * 0.5f, new Color(0, 0, 0, 0),
                        getWidth(), getHeight(), PURPLE_DARK
                );
                g2.setPaint(gradiente);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2.setColor(new Color(255, 255, 255, 12));
                g2.fill(new Ellipse2D.Double(getWidth() - 250, getHeight() - 180, 300, 300));
                g2.fill(new Ellipse2D.Double(getWidth() - 120, getHeight() - 280, 120, 120));
                g2.fill(new Ellipse2D.Double(getWidth() - 350, getHeight() - 100, 80, 80));

                g2.dispose();
            }
        };

        background.setLayout(new GridBagLayout());
        setContentPane(background);

        iniciarUI(background);
        cargarCombos();

        if (id > 0) cargarDatos(id);

        // Validaciones live (del 2)
        engancharValidacionesLive();
    }

    public boolean isOperacionExitosa() {
        return operacionExitosa;
    }

    // ========================================================================
    // ✅ MODO SOLO LECTURA (NO SE BORRA)
    // ========================================================================
    public void activarModoSoloLectura() {
        estilarCampoBloqueado(txtCedula);
        estilarCampoBloqueado(txtNom1);
        estilarCampoBloqueado(txtNom2);
        estilarCampoBloqueado(txtApe1);
        estilarCampoBloqueado(txtApe2);
        estilarCampoBloqueado(txtEmail);
        estilarCampoBloqueado(txtSueldo);

        dcFecha.setEnabled(false);
        forzarEstiloFecha();

        configurarComboLectura(cmbSexo);
        configurarComboLectura(cmbDpto);
        configurarComboLectura(cmbRol);

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
                new MatteBorder(0, 0, 1, 0, new Color(80, 80, 80)),
                new EmptyBorder(0, 5, 0, 5)
        ));
    }

    private void configurarComboLectura(JComboBox<?> cmb) {
        cmb.setEditable(true);
        Component editorComp = cmb.getEditor().getEditorComponent();
        if (editorComp instanceof JTextField) estilarCampoBloqueado((JTextField) editorComp);
        cmb.setEnabled(false);
    }

    private void forzarEstiloFecha() {
        if (dcFecha == null) return;
        JTextField dateEditor = (JTextField) dcFecha.getDateEditor().getUiComponent();
        dateEditor.setOpaque(true);
        dateEditor.setBackground(FIELD_BG);
        dateEditor.setForeground(TEXT_WHITE);
        dateEditor.setDisabledTextColor(TEXT_WHITE);
        dateEditor.setCaretColor(TEXT_WHITE);
        dateEditor.setBorder(new MatteBorder(0, 0, 2, 0, INPUT_BORDER_IDLE));
        dateEditor.repaint();
    }

    // ========================================================================
    // UI
    // ========================================================================
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

        JLabel lblTitle = new JLabel(idEmpleado == 0 ? "NUEVO INGRESO" : "INFORMACIÓN DEL EMPLEADO");
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

        // Columna izquierda
        gbc.gridx = 0;
        int y = 0;
        addSectionTitle(formPanel, "Información Personal", 0, y++, gbc);

        txtCedula = addInput(formPanel, "Cédula", 0, y, gbc); y += 2;
        txtNom1 = addInput(formPanel, "Primer Nombre", 0, y, gbc); y += 2;
        txtNom2 = addInput(formPanel, "Segundo Nombre", 0, y, gbc); y += 2;
        txtApe1 = addInput(formPanel, "Primer Apellido", 0, y, gbc); y += 2;
        txtApe2 = addInput(formPanel, "Segundo Apellido", 0, y, gbc); y += 2;

        // Columna derecha
        gbc.gridx = 1;
        y = 0;
        addSectionTitle(formPanel, "Detalles Corporativos", 1, y++, gbc);

        addLabel(formPanel, "Fecha Nacimiento", 1, y++, gbc);
        dcFecha = new JDateChooser();
        dcFecha.setDateFormatString("dd/MM/yyyy");
        dcFecha.setBackground(CARD_BG);
        dcFecha.setPreferredSize(new Dimension(200, 35));
        forzarEstiloFecha();
        formPanel.add(dcFecha, gbc);
        y++;

        addLabel(formPanel, "Género", 1, y++, gbc);
        cmbSexo = new MaterialComboBox<>(new String[]{"M", "F"});
        formPanel.add(cmbSexo, gbc);
        y++;

        txtEmail = addInput(formPanel, "Correo Electrónico", 1, y, gbc); y += 2;
        txtSueldo = addInput(formPanel, "Sueldo Mensual ($)", 1, y, gbc); y += 2;

        addLabel(formPanel, "Departamento", 1, y++, gbc);
        cmbDpto = new MaterialComboBox<>();
        formPanel.add(cmbDpto, gbc);
        y++;

        addLabel(formPanel, "Rol", 1, y++, gbc);
        cmbRol = new MaterialComboBox<>();
        formPanel.add(cmbRol, gbc);

        card.add(formPanel, BorderLayout.CENTER);

        // Botones animados
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);

        btnGuardar = crearBotonAnimado("GUARDAR DATOS", ACCENT_PURPLE);
        btnGuardar.addActionListener(e -> guardarDatos());

        JButton btnCancel = crearBotonAnimado("CANCELAR", new Color(60, 60, 70));
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnGuardar);
        btnPanel.add(btnCancel);

        card.add(btnPanel, BorderLayout.SOUTH);

        background.add(card);
    }

    private JButton crearBotonAnimado(String texto, Color bg) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(bg.darker());
                else if (getModel().isRollover()) g2.setColor(bg.brighter());
                else g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(160, 45));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ========================================================================
    // BD
    // ========================================================================
    private void cargarDatos(int id) {
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement pst = cn.prepareStatement("SELECT * FROM Empleados WHERE id_Empleado_PK = ?")) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                txtCedula.setText(validarNull(rs.getString("emp_ciruc")));
                txtNom1.setText(validarNull(rs.getString("emp_Nombre1")));
                txtNom2.setText(validarNull(rs.getString("emp_Nombre2")));
                txtApe1.setText(validarNull(rs.getString("emp_Apellido1")));
                txtApe2.setText(validarNull(rs.getString("emp_Apellido2")));
                txtEmail.setText(validarNull(rs.getString("emp_Mail")));
                txtSueldo.setText(String.valueOf(rs.getDouble("emp_Sueldo")));

                String sexo = rs.getString("emp_Sexo");
                if (sexo != null) cmbSexo.setSelectedItem(sexo);

                if (rs.getDate("emp_FechaNacimiento") != null) dcFecha.setDate(rs.getDate("emp_FechaNacimiento"));

                int idDep = rs.getInt("id_Departamento");
                if (listaIdsDpto.contains(idDep)) cmbDpto.setSelectedIndex(listaIdsDpto.indexOf(idDep));

                int idRol = rs.getInt("id_Rol");
                if (listaIdsRol.contains(idRol)) cmbRol.setSelectedIndex(listaIdsRol.indexOf(idRol));

                txtCedula.setEditable(false);
                txtCedula.setForeground(Color.GRAY);

                forzarEstiloFecha();
            }
        } catch (Exception e) {
            mostrarToast("Error al cargar datos: " + e.getMessage(), true);
        }
    }

    private String validarNull(String texto) {
        return (texto == null) ? "" : texto.trim();
    }

    private void cargarCombos() {
        try (Connection cn = ConexionBD.getConexion()) {
            ResultSet rs = cn.createStatement().executeQuery(
                    "SELECT id_Departamento_PK, dep_Nombre FROM Departamentos WHERE ESTADO_DEP = 'ACT'");
            while (rs.next()) {
                listaIdsDpto.add(rs.getInt(1));
                cmbDpto.addItem(rs.getString(2));
            }

            rs = cn.createStatement().executeQuery(
                    "SELECT id_Rol_PK, rol_Descripcion FROM Roles WHERE ESTADO_ROL = 'ACT'");
            while (rs.next()) {
                listaIdsRol.add(rs.getInt(1));
                cmbRol.addItem(rs.getString(2));
            }
        } catch (Exception e) {
            mostrarToast("Error al cargar combos: " + e.getMessage(), true);
        }
    }

    private void guardarDatos() {
        if (!validarTodo()) return;

        try (Connection cn = ConexionBD.getConexion()) {
            String sql;
            PreparedStatement pst;

            java.sql.Date fechaSQL = new java.sql.Date(dcFecha.getDate().getTime());
            double sueldo = Double.parseDouble(txtSueldo.getText().trim());

            int idDpto = listaIdsDpto.get(cmbDpto.getSelectedIndex());
            int idRol = listaIdsRol.get(cmbRol.getSelectedIndex());

            if (idEmpleado == 0) {
                sql = "INSERT INTO Empleados (emp_ciruc, emp_Nombre1, emp_Nombre2, emp_Apellido1, emp_Apellido2, emp_Sexo, emp_FechaNacimiento, emp_Mail, emp_Sueldo, id_Departamento, id_Rol, emp_Estado) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?, 'ACT')";
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
                pst.setInt(10, idDpto);
                pst.setInt(11, idRol);
            } else {
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
            operacionExitosa = true;
            dispose();

        } catch (Exception e) {
            mostrarToast("Error crítico en BD: " + e.getMessage(), true);
        }
    }

    // ========================================================================
    // VALIDACIONES (del 2)
    // ========================================================================
    private boolean validarTodo() {
        if (txtCedula.getText().trim().isEmpty()
                || txtNom1.getText().trim().isEmpty()
                || txtApe1.getText().trim().isEmpty()) {
            mostrarToast("Campos incompletos: Cédula, Nombres y Apellidos.", true);
            return false;
        }

        if (idEmpleado == 0) {
            String cedula = txtCedula.getText().trim();
            if (cedula.length() != 10) {
                mostrarToast("La cédula debe tener 10 dígitos.", true);
                return false;
            }
            if (!validarCedulaEcuatoriana(cedula)) {
                mostrarToast("Número de cédula inválido.", true);
                return false;
            }
            if (!validarCedulaUnicaEnBD(cedula)) {
                mostrarToast("Ya existe un empleado con esa cédula.", true);
                return false;
            }
        }

        if (!txtEmail.getText().trim().isEmpty() && !validarEmail(txtEmail.getText().trim())) {
            mostrarToast("Correo inválido.", true);
            return false;
        }

        if (idEmpleado == 0) {
            if (dcFecha.getDate() == null) {
                mostrarToast("Ingrese fecha nacimiento.", true);
                return false;
            }
            if (!validarEdad(dcFecha.getDate())) return false;
        } else {
            if (dcFecha.getDate() == null) {
                mostrarToast("Fecha requerida.", true);
                return false;
            }
        }

        if (txtSueldo.getText().trim().isEmpty()) {
            mostrarToast("Ingrese un sueldo.", true);
            return false;
        }

        try {
            double sueldo = Double.parseDouble(txtSueldo.getText().trim());
            if (sueldo <= 0) {
                mostrarToast("El sueldo debe ser mayor a cero.", true);
                return false;
            }
            if (sueldo < SALARIO_BASICO) {
                boolean continuar = mostrarConfirmacionSueldo(sueldo, SALARIO_BASICO);
                if (!continuar) {
                    txtSueldo.requestFocus();
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            mostrarToast("Sueldo inválido.", true);
            return false;
        }

        if (cmbDpto.getSelectedIndex() < 0 || cmbRol.getSelectedIndex() < 0) {
            mostrarToast("Seleccione Departamento y Rol.", true);
            return false;
        }

        return true;
    }

    private boolean validarCedulaEcuatoriana(String cedula) {
        try {
            if (cedula == null || cedula.length() != 10) return false;
            if (!cedula.matches("\\d+")) return false;

            int prov = Integer.parseInt(cedula.substring(0, 2));
            if (prov < 1 || prov > 24) return false;

            int[] coef = {2, 1, 2, 1, 2, 1, 2, 1, 2};
            int suma = 0;
            int digitoVerificador = Character.getNumericValue(cedula.charAt(9));

            for (int i = 0; i < 9; i++) {
                int valor = Character.getNumericValue(cedula.charAt(i)) * coef[i];
                if (valor >= 10) valor -= 9;
                suma += valor;
            }

            int residuo = suma % 10;
            int resultado = (residuo == 0) ? 0 : 10 - residuo;
            return resultado == digitoVerificador;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validarEmail(String e) {
        String regex = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.compile(regex).matcher(e).matches();
    }

    private boolean validarEdad(Date f) {
        Calendar nac = Calendar.getInstance();
        nac.setTime(f);
        Calendar hoy = Calendar.getInstance();

        if (nac.after(hoy)) {
            mostrarToast("Fecha futura no permitida.", true);
            return false;
        }

        int edad = hoy.get(Calendar.YEAR) - nac.get(Calendar.YEAR);
        if (hoy.get(Calendar.DAY_OF_YEAR) < nac.get(Calendar.DAY_OF_YEAR)) edad--;

        if (edad < 18) {
            mostrarToast("Debe ser mayor de edad (+18).", true);
            return false;
        }
        if (edad > 65) {
            mostrarToast("Edad excede límite (65 años).", true);
            return false;
        }

        return true;
    }

    private boolean validarCedulaUnicaEnBD(String c) {
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement pst = cn.prepareStatement("SELECT id_Empleado_PK FROM Empleados WHERE emp_ciruc=?")) {
            pst.setString(1, c);
            ResultSet rs = pst.executeQuery();
            return !rs.next();
        } catch (Exception e) {
            return true;
        }
    }

    // ========================================================================
    // CONFIRMACIÓN SUELDO (del 2)
    // ========================================================================
    private boolean mostrarConfirmacionSueldo(double sueldo, double basico) {
        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(45, 40, 55));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 193, 7), 2),
                new EmptyBorder(20, 30, 20, 30)
        ));

        JLabel lblTitulo = new JLabel("ADVERTENCIA DE SUELDO BAJO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(255, 193, 7));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
        lblTitulo.setIconTextGap(15);
        panel.add(lblTitulo, BorderLayout.NORTH);

        String mensajeHTML = String.format(
                "<html><div style='text-align: center; color: white; font-family: Segoe UI; font-size: 11px;'>" +
                        "El sueldo ingresado <font color='#FF5252' size='5'><b>$%.2f</b></font> es menor<br>" +
                        "al Salario Básico Unificado <font color='#4CAF50' size='4'><b>($%.2f)</b></font>.<br><br>" +
                        "<i>¿Está seguro que desea continuar con este valor?</i>" +
                        "</div></html>",
                sueldo, basico
        );

        JLabel lblMsg = new JLabel(mensajeHTML);
        lblMsg.setBorder(new EmptyBorder(20, 0, 20, 0));
        lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblMsg, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);

        JButton btnSi = new JButton("SÍ, CONTINUAR");
        btnSi.setBackground(new Color(255, 193, 7));
        btnSi.setForeground(new Color(40, 40, 40));
        btnSi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSi.setFocusPainted(false);
        btnSi.setBorderPainted(false);
        btnSi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSi.setPreferredSize(new Dimension(140, 35));

        JButton btnNo = new JButton("CORREGIR");
        btnNo.setBackground(new Color(80, 80, 80));
        btnNo.setForeground(Color.WHITE);
        btnNo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNo.setFocusPainted(false);
        btnNo.setBorderPainted(false);
        btnNo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNo.setPreferredSize(new Dimension(120, 35));

        final boolean[] respuesta = {false};
        btnSi.addActionListener(e -> { respuesta[0] = true; dialog.dispose(); });
        btnNo.addActionListener(e -> { respuesta[0] = false; dialog.dispose(); });

        btnPanel.add(btnNo);
        btnPanel.add(btnSi);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return respuesta[0];
    }

    // ========================================================================
    // TOAST (del 2)
    // ========================================================================
    private void mostrarToast(String mensaje, boolean esError) {
        JWindow toast = new JWindow(this);
        toast.setBackground(new Color(0, 0, 0, 0));

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

        panel.add(lbl);
        toast.add(panel);
        toast.pack();

        Point p = this.getLocationOnScreen();
        toast.setLocation(p.x + (this.getWidth() - toast.getWidth()) / 2, p.y + 50);

        toast.setVisible(true);
        new Timer(3000, e -> { toast.setVisible(false); toast.dispose(); }).start();
    }

    // ========================================================================
    // VALIDACIONES LIVE (KEYLISTENERS) (del 2)
    // ========================================================================
    private void engancharValidacionesLive() {

        // Cédula: solo dígitos y máximo 10
        txtCedula.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                if (!Character.isDigit(evt.getKeyChar())) evt.consume();
                if (txtCedula.getText().length() >= 10) evt.consume();
            }
        });

        // Letras + espacio
        addTextoValidation(txtNom1);
        addTextoValidation(txtNom2);
        addTextoValidation(txtApe1);
        addTextoValidation(txtApe2);

        // Sueldo: dígitos + punto, solo un punto
        txtSueldo.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                char c = evt.getKeyChar();
                String text = txtSueldo.getText();
                if (!Character.isDigit(c) && c != '.') { evt.consume(); return; }
                if (c == '.' && text.contains(".")) evt.consume();
            }
        });
    }

    private void addTextoValidation(JTextField txt) {
        txt.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isLetter(e.getKeyChar()) && e.getKeyChar() != ' ') e.consume();
            }
        });
    }

    // ========================================================================
    // HELPERS UI
    // ========================================================================
    private void addSectionTitle(JPanel p, String t, int x, int y, GridBagConstraints g) {
        JLabel l = new JLabel(t);
        l.setForeground(ACCENT_PURPLE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        GridBagConstraints g2 = (GridBagConstraints) g.clone();
        g2.gridx = x;
        g2.gridy = y;
        g2.insets = new Insets(10, 15, 20, 15);
        p.add(l, g2);
    }

    private void addLabel(JPanel p, String t, int x, int y, GridBagConstraints g) {
        JLabel l = new JLabel(t);
        l.setForeground(new Color(180, 180, 180));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        GridBagConstraints g2 = (GridBagConstraints) g.clone();
        g2.gridx = x;
        g2.gridy = y;
        g2.insets = new Insets(10, 15, 2, 15);
        p.add(l, g2);

        g.gridx = x;
        g.gridy = y + 1;
        g.insets = new Insets(0, 15, 0, 15);
    }

    private JTextField addInput(JPanel p, String t, int x, int y, GridBagConstraints g) {
        addLabel(p, t, x, y, g);
        MaterialTextField txt = new MaterialTextField();
        GridBagConstraints g2 = (GridBagConstraints) g.clone();
        g2.insets = new Insets(2, 15, 0, 15);
        p.add(txt, g2);
        return txt;
    }

    // ========================================================================
    // COMPONENTES MATERIAL
    // ========================================================================
    class MaterialTextField extends JTextField {
        public MaterialTextField() {
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setPreferredSize(new Dimension(200, 30));
            setBorder(new MatteBorder(0, 0, 2, 0, INPUT_BORDER_IDLE));
            setCaretColor(ACCENT_PURPLE);
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    setBorder(new MatteBorder(0, 0, 2, 0, ACCENT_PURPLE));
                }
                public void focusLost(FocusEvent e) {
                    setBorder(new MatteBorder(0, 0, 2, 0, INPUT_BORDER_IDLE));
                }
            });
        }
    }

    class MaterialComboBox<E> extends JComboBox<E> {
        public MaterialComboBox(E[] i) { super(i); style(); }
        public MaterialComboBox() { super(); style(); }
        private void style() {
            setBackground(FIELD_BG);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setPreferredSize(new Dimension(200, 35));
            setBorder(new MatteBorder(0, 0, 2, 0, INPUT_BORDER_IDLE));
        }
    }
}

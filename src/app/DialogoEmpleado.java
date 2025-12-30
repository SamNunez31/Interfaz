package app;

import ConexionBD.ConexionBD;
import raven.toast.Notifications;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DialogoEmpleado extends JDialog {

    private int idEmpleado;
    private JTextField txtCedula, txtNom1, txtNom2, txtApe1, txtApe2, txtEmail, txtSueldo;
    private JComboBox<String> cmbSexo, cmbDpto, cmbRol;
    private JDateChooser dcFecha;
    
    private List<Integer> listaIdsDpto = new ArrayList<>();
    private List<Integer> listaIdsRol = new ArrayList<>();

    // Colores
    private final Color ACCENT_PURPLE = new Color(124, 77, 255);
    private final Color INPUT_BORDER_IDLE = new Color(80, 80, 90);
    private final Color CARD_BG = new Color(30, 25, 40, 240);

    public DialogoEmpleado(Frame padre, int id) {
        super(padre, true);
        this.idEmpleado = id;
        
        setTitle(id == 0 ? "Nuevo Empleado" : "Editar Empleado");
        setSize(950, 750); // Un poco más alto para asegurar espacio
        setLocationRelativeTo(padre);
        setResizable(false);
        
        // Fondo General
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
        
        if (id > 0) cargarDatos(id);
    }

    private void iniciarUI(JPanel background) {
        // Tarjeta Central
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(30, 50, 30, 50));
        
        // --- HEADER ---
        JLabel lblTitle = new JLabel(idEmpleado == 0 ? "NUEVO INGRESO" : "ACTUALIZAR DATOS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblTitle, BorderLayout.NORTH);

        // --- FORMULARIO ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        // -- COLUMNA IZQUIERDA (0) --
        gbc.gridx = 0; 
        int y = 0; // Reiniciamos contador de filas
        
        addSectionTitle(formPanel, "Información Personal", 0, y++, gbc);
        
        // IMPORTANTE: Cada campo ocupa 2 filas (Label + Input), por eso incrementamos y+=2
        txtCedula = addInput(formPanel, "Cédula / RUC", 0, y, gbc); y += 2;
        txtNom1 = addInput(formPanel, "Primer Nombre", 0, y, gbc); y += 2;
        txtNom2 = addInput(formPanel, "Segundo Nombre", 0, y, gbc); y += 2;
        txtApe1 = addInput(formPanel, "Primer Apellido", 0, y, gbc); y += 2;
        txtApe2 = addInput(formPanel, "Segundo Apellido", 0, y, gbc); y += 2;
        
        // -- COLUMNA DERECHA (1) --
        gbc.gridx = 1; 
        y = 0; // Reiniciamos Y para la segunda columna para que empiece arriba
        
        addSectionTitle(formPanel, "Detalles Corporativos", 1, y++, gbc);
        
        // Fecha
        addLabel(formPanel, "Fecha Nacimiento", 1, y++, gbc);
        dcFecha = new JDateChooser();
        dcFecha.setDateFormatString("dd/MM/yyyy");
        dcFecha.setBackground(CARD_BG);
        dcFecha.setPreferredSize(new Dimension(200, 35));
        JTextField dateEditor = ((JTextField)dcFecha.getDateEditor().getUiComponent());
        dateEditor.setBackground(new Color(40, 35, 50));
        dateEditor.setForeground(Color.WHITE);
        dateEditor.setBorder(new MatteBorder(0,0,2,0, INPUT_BORDER_IDLE));
        formPanel.add(dcFecha, gbc);
        y++; // Siguiente fila

        // Género
        addLabel(formPanel, "Género", 1, y++, gbc);
        cmbSexo = new MaterialComboBox<>(new String[]{"M", "F"});
        formPanel.add(cmbSexo, gbc);
        y++;

        txtEmail = addInput(formPanel, "Correo Electrónico", 1, y, gbc); y += 2;
        txtSueldo = addInput(formPanel, "Sueldo Mensual ($)", 1, y, gbc); y += 2;
        
        // Depto
        addLabel(formPanel, "Departamento", 1, y++, gbc);
        cmbDpto = new MaterialComboBox<>();
        formPanel.add(cmbDpto, gbc);
        y++;
        
        // Rol
        addLabel(formPanel, "Rol", 1, y++, gbc);
        cmbRol = new MaterialComboBox<>();
        formPanel.add(cmbRol, gbc);
        y++;

        card.add(formPanel, BorderLayout.CENTER);

        // --- BOTONES ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        
        // 1. Guardar (Morado)
        JButton btnSave = new JButton("GUARDAR DATOS");
        styleButtonPrimary(btnSave);
        btnSave.addActionListener(e -> guardarDatos());
        
        // 2. Cancelar (Ghost)
        JButton btnCancel = new JButton("CANCELAR");
        styleButtonSecondary(btnCancel);
        btnCancel.addActionListener(e -> dispose());
        
        // Orden solicitado: Guardar -> Cancelar
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        
        card.add(btnPanel, BorderLayout.SOUTH);

        background.add(card);
    }

    // --- MÉTODOS VISUALES ---

    private void addSectionTitle(JPanel p, String text, int x, int y, GridBagConstraints gbc) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(ACCENT_PURPLE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        GridBagConstraints titleGbc = (GridBagConstraints) gbc.clone();
        titleGbc.gridx = x; 
        titleGbc.gridy = y;
        titleGbc.insets = new Insets(10, 15, 20, 15); // Más espacio abajo del título
        p.add(lbl, titleGbc);
    }

    private JTextField addInput(JPanel p, String label, int x, int y, GridBagConstraints gbc) {
        // Etiqueta (Fila Y)
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(180, 180, 180));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        GridBagConstraints lblGbc = (GridBagConstraints) gbc.clone();
        lblGbc.gridx = x; 
        lblGbc.gridy = y;
        lblGbc.insets = new Insets(10, 15, 0, 15); // Espacio arriba
        p.add(lbl, lblGbc);
        
        // Campo (Fila Y+1)
        MaterialTextField txt = new MaterialTextField();
        
        GridBagConstraints txtGbc = (GridBagConstraints) gbc.clone();
        txtGbc.gridx = x; 
        txtGbc.gridy = y + 1;
        txtGbc.insets = new Insets(2, 15, 0, 15); // Pegado al label
        p.add(txt, txtGbc);
        
        return txt;
    }
    
    private void addLabel(JPanel p, String text, int x, int y, GridBagConstraints gbc) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(180, 180, 180));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        GridBagConstraints lblGbc = (GridBagConstraints) gbc.clone();
        lblGbc.gridx = x; 
        lblGbc.gridy = y;
        lblGbc.insets = new Insets(10, 15, 2, 15);
        p.add(lbl, lblGbc);
        
        // El componente siguiente se agregará manualmente en y+1
        // Aseguramos que el gbc pasado se actualice para el siguiente componente
        gbc.gridx = x;
        gbc.gridy = y + 1;
        gbc.insets = new Insets(0, 15, 0, 15);
    }

    private void styleButtonPrimary(JButton b) {
        b.setBackground(ACCENT_PURPLE);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160, 45));
    }

    private void styleButtonSecondary(JButton b) {
        b.setBackground(new Color(255, 255, 255, 0));
        b.setForeground(new Color(200, 200, 200));
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(140, 45));
    }

    class MaterialTextField extends JTextField {
        public MaterialTextField() {
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setPreferredSize(new Dimension(200, 30)); // Altura fija importante
            setBorder(new MatteBorder(0, 0, 2, 0, INPUT_BORDER_IDLE));
            setCaretColor(ACCENT_PURPLE);
            
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    setBorder(new MatteBorder(0, 0, 2, 0, ACCENT_PURPLE));
                    repaint();
                }
                @Override
                public void focusLost(FocusEvent e) {
                    setBorder(new MatteBorder(0, 0, 2, 0, INPUT_BORDER_IDLE));
                    repaint();
                }
            });
        }
    }
    
    class MaterialComboBox<E> extends JComboBox<E> {
        public MaterialComboBox(E[] items) { super(items); style(); }
        public MaterialComboBox() { super(); style(); }
        private void style() {
            setBackground(new Color(40, 35, 50));
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setPreferredSize(new Dimension(200, 35));
            setBorder(new MatteBorder(0, 0, 2, 0, INPUT_BORDER_IDLE));
        }
    }

    // --- LOGICA (Sin cambios) ---
    private void cargarDatos(int id) {
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement pst = cn.prepareStatement("SELECT * FROM Empleados WHERE id_Empleado_PK = ?")) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                txtCedula.setText(rs.getString("emp_ciruc"));
                txtCedula.setEditable(false);
                txtNom1.setText(rs.getString("emp_Nombre1"));
                txtNom2.setText(rs.getString("emp_Nombre2"));
                txtApe1.setText(rs.getString("emp_Apellido1"));
                txtApe2.setText(rs.getString("emp_Apellido2"));
                txtEmail.setText(rs.getString("emp_Mail"));
                txtSueldo.setText(rs.getString("emp_Sueldo"));
                cmbSexo.setSelectedItem(rs.getString("emp_Sexo"));
                if(rs.getDate("emp_FechaNacimiento") != null) dcFecha.setDate(rs.getDate("emp_FechaNacimiento"));
                
                int idDep = rs.getInt("id_Departamento");
                if(listaIdsDpto.contains(idDep)) cmbDpto.setSelectedIndex(listaIdsDpto.indexOf(idDep));
                
                int idRol = rs.getInt("id_Rol");
                if(listaIdsRol.contains(idRol)) cmbRol.setSelectedIndex(listaIdsRol.indexOf(idRol));
            }
        } catch (Exception e) {}
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
        if (txtCedula.getText().trim().isEmpty() || txtApe1.getText().trim().isEmpty() || txtNom1.getText().trim().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Campos obligatorios vacíos"); return;
        }
        if (dcFecha.getDate() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Fecha inválida"); return;
        }
        
        try (Connection cn = ConexionBD.getConexion()) {
            String sql; PreparedStatement pst;
            java.sql.Date fechaSQL = new java.sql.Date(dcFecha.getDate().getTime());
            double sueldo = 0; try { sueldo = Double.parseDouble(txtSueldo.getText().trim()); } catch(Exception e){}
            
            int idDpto = listaIdsDpto.get(cmbDpto.getSelectedIndex());
            int idRol = listaIdsRol.get(cmbRol.getSelectedIndex());

            if (idEmpleado == 0) {
                sql = "INSERT INTO Empleados (emp_ciruc, emp_Nombre1, emp_Nombre2, emp_Apellido1, emp_Apellido2, emp_Sexo, emp_FechaNacimiento, emp_Mail, emp_Sueldo, id_Departamento, id_Rol, emp_Estado) VALUES (?,?,?,?,?,?,?,?,?,?,?, 'ACT')";
                pst = cn.prepareStatement(sql);
                pst.setString(1, txtCedula.getText()); pst.setString(2, txtNom1.getText()); pst.setString(3, txtNom2.getText());
                pst.setString(4, txtApe1.getText()); pst.setString(5, txtApe2.getText()); pst.setString(6, cmbSexo.getSelectedItem().toString());
                pst.setDate(7, fechaSQL); pst.setString(8, txtEmail.getText()); pst.setDouble(9, sueldo);
                pst.setInt(10, idDpto); pst.setInt(11, idRol);
            } else {
                sql = "UPDATE Empleados SET emp_Nombre1=?, emp_Nombre2=?, emp_Apellido1=?, emp_Apellido2=?, emp_Sexo=?, emp_FechaNacimiento=?, emp_Mail=?, emp_Sueldo=?, id_Departamento=?, id_Rol=? WHERE id_Empleado_PK=?";
                pst = cn.prepareStatement(sql);
                pst.setString(1, txtNom1.getText()); pst.setString(2, txtNom2.getText()); pst.setString(3, txtApe1.getText());
                pst.setString(4, txtApe2.getText()); pst.setString(5, cmbSexo.getSelectedItem().toString()); pst.setDate(6, fechaSQL);
                pst.setString(7, txtEmail.getText()); pst.setDouble(8, sueldo); pst.setInt(9, idDpto); pst.setInt(10, idRol);
                pst.setInt(11, idEmpleado);
            }
            pst.executeUpdate();
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Guardado Correctamente");
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Error: " + e.getMessage());
        }
    }
}
package modules.tthh.ui;

import ConexionBD.ConexionBD;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class DialogoDetalleRol extends JDialog {

    private int idPago;
    private int idEmpleado;
    private boolean cambioRealizado = false;
    private String estadoRol = "ABI";

    // Componentes UI
    private JTable tablaBonos, tablaDescuentos;
    private DefaultTableModel modelBonos, modelDescuentos;
    private JComboBox<ItemCombo> cmbBonos, cmbDescuentos;
    
    // Labels HEADER
    private JLabel lblEmpleadoNombre;
    private JLabel lblPeriodo, lblSueldoBase, lblEstado;
    
    // Labels TOTALES
    private JLabel lblTotalIngresos, lblTotalEgresos, lblNeto;
    
    // Botones
    private JButton btnAddBono, btnDelBono, btnAddDesc, btnDelDesc;
    private JButton btnAprobar, btnAnular, btnCerrar;

    // --- PALETA DE COLORES OPTIMIZADA (Estilo Material Dark Blue) ---
    private final Color BG_PANEL = new Color(18, 22, 31);       // Azul Medianoche Profundo (Más profesional)
    private final Color BG_CARD  = new Color(28, 33, 46);       // Gris Azulado para tarjetas
    private final Color BG_TABLE = Color.WHITE;                 
    private final Color TEXT_TABLE = new Color(50, 50, 50);     
    private final Color BG_HEADER = new Color(242, 244, 246);   
    private final Color TEXT_HEADER = new Color(70, 75, 85);    
    private final Color TEXT_PRIMARY = new Color(245, 245, 245);
    private final Color LINE_COLOR = new Color(55, 62, 78);      // Bordes más sutiles
    
    private final Color ACCENT_GREEN = new Color(46, 204, 113);
    private final Color ACCENT_RED = new Color(231, 76, 60);
    private final Color ACCENT_BLUE = new Color(52, 152, 219);  // Neto a pagar
    private final Color ACCENT_YELLOW = new Color(241, 196, 15);

    private final DecimalFormat df = new DecimalFormat("$ #,##0.00");
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");

    public DialogoDetalleRol(Frame parent, int idPago) {
        super(parent, "Detalle del Rol de Pagos", true);
        this.idPago = idPago;
        setSize(1100, 700); 
        setLocationRelativeTo(parent);
        setUndecorated(true);
        getRootPane().setBorder(new LineBorder(LINE_COLOR, 1));
        
        initUI();
        cargarCombos();
        cargarDatosGenerales(); 
        cargarTablas(); 
    }

    public boolean isCambioRealizado() { return cambioRealizado; }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_PANEL);

        // --- 1. HEADER ---
        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(25, 35, 15, 35));

        lblEmpleadoNombre = new JLabel("CARGANDO...");
        lblEmpleadoNombre.setFont(new Font("Segoe UI", Font.BOLD, 26)); // Más grande
        lblEmpleadoNombre.setForeground(TEXT_PRIMARY);
        
        JPanel subInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        subInfo.setOpaque(false);
        
        lblPeriodo = new JLabel("Periodo: --/--");
        styleLabelSub(lblPeriodo);
        
        lblSueldoBase = new JLabel("Sueldo Base: $ 0.00");
        styleLabelSub(lblSueldoBase);
        
        lblEstado = new JLabel("ESTADO");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        subInfo.add(lblPeriodo);
        subInfo.add(new JLabel("|"){{setForeground(Color.DARK_GRAY);}});
        subInfo.add(lblSueldoBase);
        subInfo.add(new JLabel("|"){{setForeground(Color.DARK_GRAY);}});
        subInfo.add(lblEstado);

        header.add(lblEmpleadoNombre);
        header.add(subInfo);

        // --- 2. CUERPO (TABLAS) ---
        JPanel pTablas = new JPanel(new GridLayout(1, 2, 25, 0)); 
        pTablas.setOpaque(false);
        pTablas.setBorder(new EmptyBorder(5, 35, 10, 35));

        pTablas.add(crearCardSeccion("INGRESOS (+)", true));
        pTablas.add(crearCardSeccion("EGRESOS (-)", false));

        // --- 3. FOOTER (TOTALES Y BOTONES) ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(15, 35, 30, 35));

        JPanel pTotales = new JPanel(new GridLayout(3, 1, 5, 5));
        pTotales.setOpaque(false);
        
        lblTotalIngresos = crearLabelTotal("Total Bonificaciones: $ 0.00", ACCENT_GREEN);
        lblTotalEgresos  = crearLabelTotal("Total Descuentos:  $ 0.00", ACCENT_RED);
        lblNeto          = crearLabelTotal("NETO A PAGAR:   $ 0.00", ACCENT_BLUE);
        lblNeto.setFont(new Font("Segoe UI", Font.BOLD, 28));

        pTotales.add(lblTotalIngresos);
        pTotales.add(lblTotalEgresos);
        pTotales.add(lblNeto);

        JPanel pBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pBotones.setOpaque(false);
        
        btnAnular = crearBoton("ANULAR ROL", ACCENT_RED, Color.WHITE);
        btnAprobar = crearBoton("APROBAR ROL", ACCENT_GREEN, Color.WHITE);
        btnCerrar = crearBoton("GUARDAR Y SALIR", new Color(65, 75, 90), Color.WHITE);
        
        btnAnular.addActionListener(e -> cambiarEstado("ANU"));
        btnAprobar.addActionListener(e -> cambiarEstado("APR"));
        btnCerrar.addActionListener(e -> dispose());

        pBotones.add(btnAnular);
        pBotones.add(btnAprobar);
        pBotones.add(btnCerrar);

        footer.add(pTotales, BorderLayout.WEST);
        footer.add(pBotones, BorderLayout.EAST);

        main.add(header, BorderLayout.NORTH);
        main.add(pTablas, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void styleLabelSub(JLabel l) {
        l.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        l.setForeground(new Color(180, 190, 210));
    }

    private JPanel crearCardSeccion(String titulo, boolean esBono) {
        JPanel card = new JPanel(new BorderLayout(0, 15)); 
        card.setBackground(BG_CARD); 
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(LINE_COLOR, 1), 
            new EmptyBorder(20, 20, 20, 20)));

        JLabel lTitulo = new JLabel(titulo); 
        lTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16)); 
        lTitulo.setForeground(esBono ? ACCENT_GREEN : ACCENT_RED); 
        card.add(lTitulo, BorderLayout.NORTH);

        JPanel pInput = new JPanel(new BorderLayout(12, 0)); 
        pInput.setOpaque(false);

        JComboBox<ItemCombo> combo = new JComboBox<>(); 
        styleComboBox(combo); 
        if(esBono) cmbBonos = combo; else cmbDescuentos = combo;

        JPanel pBtnsSmall = new JPanel(new GridLayout(1, 2, 8, 0)); 
        pBtnsSmall.setOpaque(false);

        JButton btnAdd = crearBotonIcono("+", BG_TABLE); 
        btnAdd.setForeground(Color.BLACK);
        JButton btnDel = crearBotonIcono("-", BG_TABLE); 
        btnDel.setForeground(Color.BLACK);

        btnAdd.addActionListener(e -> agregarItem(esBono)); 
        btnDel.addActionListener(e -> eliminarItem(esBono));

        if(esBono) { btnAddBono = btnAdd; btnDelBono = btnDel; } else { btnAddDesc = btnAdd; btnDelDesc = btnDel; }

        pBtnsSmall.add(btnAdd); 
        pBtnsSmall.add(btnDel); 
        pInput.add(combo, BorderLayout.CENTER); 
        pInput.add(pBtnsSmall, BorderLayout.EAST);

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Concepto", "Valor"}, 0) { 
            public boolean isCellEditable(int row, int col) { return false; } 
        };
        JTable table = new JTable(model); 
        estilizarTabla(table); 
        if(esBono) { modelBonos = model; tablaBonos = table; } else { modelDescuentos = model; tablaDescuentos = table; }

        JScrollPane scroll = new JScrollPane(table); 
        scroll.getViewport().setBackground(BG_TABLE); 
        scroll.setBorder(new LineBorder(new Color(210, 210, 215), 1));

        JPanel pCenter = new JPanel(new BorderLayout(0, 12)); 
        pCenter.setOpaque(false); 
        pCenter.add(pInput, BorderLayout.NORTH); 
        pCenter.add(scroll, BorderLayout.CENTER);

        card.add(pCenter, BorderLayout.CENTER); 
        return card;
    }

    private void estilizarTabla(JTable t) {
        t.setRowHeight(35); 
        t.setBackground(BG_TABLE); 
        t.setForeground(TEXT_TABLE); 
        t.setGridColor(new Color(235, 235, 240)); 
        t.setShowVerticalLines(false);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        t.setSelectionBackground(new Color(230, 240, 255)); 
        t.setSelectionForeground(Color.BLACK);

        JTableHeader h = t.getTableHeader(); 
        h.setBackground(BG_HEADER); 
        h.setForeground(TEXT_HEADER); 
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 205))); 
        h.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer(); 
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT); 
        t.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        t.getColumnModel().getColumn(0).setMaxWidth(45);
    }

    private void styleComboBox(JComboBox box) { 
        box.setBackground(Color.WHITE); 
        box.setForeground(TEXT_TABLE); 
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        box.setBorder(new LineBorder(new Color(200, 200, 205))); 
    }

    private JButton crearBoton(String texto, Color bg, Color fg) { 
        JButton btn = new JButton(texto) { 
            protected void paintComponent(Graphics g) { 
                Graphics2D g2 = (Graphics2D)g.create(); 
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); 
                g2.dispose(); 
                super.paintComponent(g); 
            } 
        }; 
        btn.setForeground(fg); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13)); 
        btn.setFocusPainted(false); 
        btn.setBorderPainted(false); 
        btn.setContentAreaFilled(false); 
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); 
        btn.setPreferredSize(new Dimension(170, 45)); 
        return btn; 
    }

    private JButton crearBotonIcono(String txt, Color bg) { 
        JButton btn = new JButton(txt); 
        btn.setBackground(bg); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18)); 
        btn.setBorder(new LineBorder(new Color(200, 200, 205))); 
        btn.setFocusPainted(false); 
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); 
        btn.setPreferredSize(new Dimension(45, 35)); 
        return btn; 
    }

    private JLabel crearLabelTotal(String txt, Color c) { 
        JLabel l = new JLabel(txt); 
        l.setFont(new Font("Segoe UI", Font.BOLD, 16)); 
        l.setForeground(c); 
        return l; 
    }

    // --- LÓGICA DE DATOS ---
    private void cargarDatosGenerales() {
        String sql = "SELECT p.pag_Fecha_Inicio, p.pag_Estado, pe.id_Empleado, e.emp_Apellido1, e.emp_Nombre1, " +
                     "pe.emp_Sueldo, pe.emp_Bonificaciones, pe.emp_Descuentos, pe.emp_Valor_Neto " +
                     "FROM PAGOS p JOIN PAGXEMP pe ON p.id_Pago_PK = pe.id_Pago JOIN EMPLEADOS e ON pe.id_Empleado = e.id_Empleado_PK " +
                     "WHERE p.id_Pago_PK = ?";

        try (Connection cn = ConexionBD.getConexion(); PreparedStatement pst = cn.prepareStatement(sql)) { 
            pst.setInt(1, idPago);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                idEmpleado = rs.getInt("id_Empleado");
                estadoRol = rs.getString("pag_Estado");
                String full = rs.getString("emp_Apellido1").trim() + " " + rs.getString("emp_Nombre1").trim();
                lblEmpleadoNombre.setText(full.toUpperCase());
                lblPeriodo.setText("Periodo: " + sdf.format(rs.getDate("pag_Fecha_Inicio")));
                lblSueldoBase.setText("Sueldo Base: " + df.format(rs.getDouble("emp_Sueldo")));

                if ("ABI".equals(estadoRol)) { lblEstado.setText("● ABIERTO"); lblEstado.setForeground(ACCENT_YELLOW); } 
                else if ("APR".equals(estadoRol)) { lblEstado.setText("● APROBADO"); lblEstado.setForeground(ACCENT_GREEN); } 
                else { lblEstado.setText("● ANULADO"); lblEstado.setForeground(ACCENT_RED); }

                lblTotalIngresos.setText("Total Ingresos: " + df.format(rs.getDouble("emp_Bonificaciones")));
                lblTotalEgresos.setText("Total Descuentos:  " + df.format(rs.getDouble("emp_Descuentos")));
                lblNeto.setText("NETO A PAGAR:   " + df.format(rs.getDouble("emp_Valor_Neto")));

                boolean editable = "ABI".equals(estadoRol);
                btnAddBono.setEnabled(editable); btnDelBono.setEnabled(editable);
                btnAddDesc.setEnabled(editable); btnDelDesc.setEnabled(editable);
                btnAprobar.setEnabled(editable); btnAnular.setEnabled(editable);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarTablas() { 
        modelBonos.setRowCount(0); modelDescuentos.setRowCount(0); 
        try (Connection cn = ConexionBD.getConexion()) { 
            PreparedStatement pstB = cn.prepareStatement("SELECT b.id_Bonificacion_PK, b.bon_Descripcion, x.bxe_Valor FROM BONXEMPXPAG x JOIN BONIFICACIONES b ON x.id_Bonificacion = b.id_Bonificacion_PK WHERE x.id_Pago = ? AND x.ESTADO_BXE = 'ACT'"); 
            pstB.setInt(1, idPago); 
            ResultSet rsB = pstB.executeQuery(); 
            while(rsB.next()) modelBonos.addRow(new Object[]{rsB.getInt(1), rsB.getString(2), df.format(rsB.getDouble(3))}); 

            PreparedStatement pstD = cn.prepareStatement("SELECT d.id_Descuento_PK, d.des_Descripcion, x.dxe_Valor FROM DESXEMPXPAG x JOIN DESCUENTOS d ON x.id_Descuento = d.id_Descuento_PK WHERE x.id_Pago = ? AND x.ESTADO_DXE = 'ACT'"); 
            pstD.setInt(1, idPago); 
            ResultSet rsD = pstD.executeQuery(); 
            while(rsD.next()) modelDescuentos.addRow(new Object[]{rsD.getInt(1), rsD.getString(2), df.format(rsD.getDouble(3))}); 
        } catch (Exception e) { e.printStackTrace(); } 
    }

    private void agregarItem(boolean esBono) { 
        JComboBox<ItemCombo> combo = esBono ? cmbBonos : cmbDescuentos; 
        if(combo.getSelectedItem() == null) return; 
        ItemCombo item = (ItemCombo) combo.getSelectedItem(); 
        String input = JOptionPane.showInputDialog(this, "Monto para: " + item.desc); 
        if(input == null || input.isEmpty()) return; 
        try { 
            double valor = Double.parseDouble(input.replace(",", ".")); 
            String sp = esBono ? "{CALL sp_Pagos_AgregarBonificacion(?,?,?,?)}" : "{CALL sp_Pagos_AgregarDescuento(?,?,?,?)}"; 
            try (Connection cn = ConexionBD.getConexion(); CallableStatement cs = cn.prepareCall(sp)) { 
                cs.setInt(1, idPago); cs.setInt(2, idEmpleado); cs.setInt(3, item.id); cs.setDouble(4, valor); 
                cs.execute(); cambioRealizado = true; cargarTablas(); cargarDatosGenerales(); 
            } 
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); } 
    }

    private void eliminarItem(boolean esBono) { 
        JTable table = esBono ? tablaBonos : tablaDescuentos; 
        int row = table.getSelectedRow(); 
        if(row == -1) return; 
        int idConcepto = Integer.parseInt(table.getValueAt(row, 0).toString()); 
        String sp = esBono ? "{CALL sp_Pagos_EliminarBonificacion(?,?,?)}" : "{CALL sp_Pagos_EliminarDescuento(?,?,?)}"; 
        try (Connection cn = ConexionBD.getConexion(); CallableStatement cs = cn.prepareCall(sp)) { 
            cs.setInt(1, idPago); cs.setInt(2, idEmpleado); cs.setInt(3, idConcepto); 
            cs.execute(); cambioRealizado = true; cargarTablas(); cargarDatosGenerales(); 
        } catch (Exception ex) { ex.printStackTrace(); } 
    }

    private void cargarCombos() { 
        try (Connection cn = ConexionBD.getConexion()) { 
            ResultSet rsB = cn.createStatement().executeQuery("SELECT id_Bonificacion_PK, bon_Descripcion FROM BONIFICACIONES WHERE ESTADO_BON='ACT'"); 
            while(rsB.next()) cmbBonos.addItem(new ItemCombo(rsB.getInt(1), rsB.getString(2))); 
            ResultSet rsD = cn.createStatement().executeQuery("SELECT id_Descuento_PK, des_Descripcion FROM DESCUENTOS WHERE ESTADO_DES='ACT'"); 
            while(rsD.next()) cmbDescuentos.addItem(new ItemCombo(rsD.getInt(1), rsD.getString(2))); 
        } catch (Exception e) {} 
    }

    private void cambiarEstado(String est) { 
        try (Connection cn = ConexionBD.getConexion(); CallableStatement cs = cn.prepareCall(est.equals("APR") ? "{CALL sp_Pagos_Aprobar(?)}" : "{CALL sp_Pagos_Anular(?)}")) { 
            cs.setInt(1, idPago); 
            cs.execute(); cambioRealizado = true; 
            dispose(); 
        } catch (Exception e) { e.printStackTrace(); } 
    }

    class ItemCombo { 
        int id; String desc; 
        public ItemCombo(int id, String desc) { this.id = id; this.desc = desc; } 
        public String toString() { return desc; } 
    }
}
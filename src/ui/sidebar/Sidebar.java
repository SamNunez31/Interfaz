package ui.sidebar;

import shared.ui.Theme;
import shared.ui.IconUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Sidebar extends JPanel {

    // --- PALETA DE COLORES ---
    private final Color SIDEBAR_BG_MODERN = new Color(30, 30, 40); 
    private final Color ACCENT_PURPLE = new Color(110, 85, 255); 
    private final Color TEXT_PRIMARY = new Color(240, 240, 255);
    private final Color TEXT_SECONDARY = new Color(160, 160, 180);
    private final Color PROFILE_BG = new Color(45, 45, 55); 

    private boolean expanded = true;
    private int width = 260;
    private final int W_EXPANDED = 260;
    private final int W_COLLAPSED = 80;
    private Timer anim;
    
    private JLabel titleLabel;
    
    // Componentes del Usuario
    private JPanel userTextPanel;
    private JLabel lblUserName;
    private JLabel lblUserRole;
    private JLabel avatarLabel; // Label para el avatar
    private String userInitials = "U"; // Inicial por defecto
    
    private List<AccordionMenu> menus = new ArrayList<>();
    private Consumer<String> menuListener;

    public Sidebar() {
        setBackground(SIDEBAR_BG_MODERN); 
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(width, 0));

        // CONTENEDOR SUPERIOR
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        
        topContainer.add(topBar()); 
        topContainer.add(createProfilePanel()); 
        
        add(topContainer, BorderLayout.NORTH);
        
        // MENÚ
        JScrollPane scrollPane = new JScrollPane(menuPanel());
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0,0));
        add(scrollPane, BorderLayout.CENTER);

        // BOTÓN SALIR
        add(bottomBar(), BorderLayout.SOUTH);
    }
    
    // --- MÉTODO ACTUALIZADO: CALCULA INICIALES ---
    public void setUser(String name, String role) {
        lblUserName.setText(name);
        lblUserRole.setText(role);
        
        // Lógica para extraer iniciales (ej: "Admin User" -> "AU")
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split(" ");
            String initials = "";
            if (parts.length > 0 && !parts[0].isEmpty()) initials += parts[0].charAt(0);
            if (parts.length > 1 && !parts[1].isEmpty()) initials += parts[1].charAt(0);
            this.userInitials = initials.toUpperCase();
        }
        
        // Repintar el avatar con las nuevas letras
        avatarLabel.repaint();
    }

    public void setMenuListener(Consumer<String> listener) {
        this.menuListener = listener;
    }

    private void fireEvent(String screenName) {
        if (menuListener != null) menuListener.accept(screenName);
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                if (expanded) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(PROFILE_BG);
                    g2.fill(new RoundRectangle2D.Double(5, 5, getWidth()-10, getHeight()-10, 15, 15));
                }
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 15, 20, 15)); 
        panel.setMaximumSize(new Dimension(1000, 90));

        // 1. Avatar (Usa la clase interna actualizada)
        avatarLabel = new JLabel(new AvatarIcon()); 
        avatarLabel.setPreferredSize(new Dimension(45, 45));
        
        // 2. Textos
        userTextPanel = new JPanel(new GridLayout(2, 1));
        userTextPanel.setOpaque(false);
        
        lblUserName = new JLabel("Usuario");
        lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUserName.setForeground(TEXT_PRIMARY);
        
        lblUserRole = new JLabel("Rol");
        lblUserRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUserRole.setForeground(ACCENT_PURPLE); 
        
        userTextPanel.add(lblUserName);
        userTextPanel.add(lblUserRole);

        panel.add(avatarLabel, BorderLayout.WEST);
        panel.add(userTextPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JComponent topBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(20, 12, 10, 12));
        top.setMaximumSize(new Dimension(1000, 70));

        Icon menuIcon = IconUtil.load("/icono/iconmenu.jpg", 20);
        JButton btnToggle = new JButton();
        if (menuIcon != null) btnToggle.setIcon(menuIcon);
        else btnToggle.setText("☰");
        
        styleIconButton(btnToggle);
        btnToggle.addActionListener(e -> toggle());

        titleLabel = new JLabel("ACME");
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        top.add(btnToggle, BorderLayout.WEST);
        top.add(titleLabel, BorderLayout.CENTER);
        return top;
    }

    private JComponent menuPanel() {
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Iconos
        Icon iconTTHH    = IconUtil.load("/icono/rhicon.jpg", 18);
        Icon iconVentas  = IconUtil.load("/icono/ventasicon.jpg", 18);
        Icon iconCompras = IconUtil.load("/icono/comprasicon.jpg", 18);
        Icon iconInv     = IconUtil.load("/icono/iconinventario.png", 18);
        Icon iconConta   = IconUtil.load("/icono/iconcontabilidad.jpg", 18);

        // --- TALENTO HUMANO ---
        AccordionMenu tthh = new AccordionMenu("Talento Humano", iconTTHH);
        tthh.addItem("Empleados", e -> fireEvent("EMPLEADOS"));
        tthh.addItem("Roles de Pago", e -> fireEvent("ROLES"));
        // >>> NUEVA OPCIÓN AGREGADA AQUÍ <<<
        tthh.addItem("Asientos Contables", e -> fireEvent("ASIENTOS_CONTABLES")); 
        menus.add(tthh);

        AccordionMenu ventas = new AccordionMenu("Ventas", iconVentas);
        ventas.addItem("Facturación", e -> fireEvent("FACTURACION"));
        ventas.addItem("Clientes", e -> fireEvent("CLIENTES"));
        menus.add(ventas);

        AccordionMenu compras = new AccordionMenu("Compras", iconCompras);
        compras.addItem("Proveedores", e -> fireEvent("PROVEEDORES"));
        menus.add(compras);

        AccordionMenu inventarios = new AccordionMenu("Inventarios", iconInv);
        inventarios.addItem("Kardex", e -> fireEvent("KARDEX"));
        menus.add(inventarios);

        AccordionMenu contabilidad = new AccordionMenu("Contabilidad", iconConta);
        contabilidad.addItem("Balances", e -> fireEvent("BALANCES"));
        menus.add(contabilidad);

        list.add(tthh); list.add(Box.createVerticalStrut(8));
        list.add(ventas); list.add(Box.createVerticalStrut(8));
        list.add(compras); list.add(Box.createVerticalStrut(8));
        list.add(inventarios); list.add(Box.createVerticalStrut(8));
        list.add(contabilidad);
        list.add(Box.createVerticalGlue());

        return list;
    }

    private JComponent bottomBar() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(20, 15, 30, 15));

        RoundedButton btnLogout = new RoundedButton("Cerrar Sesión", ACCENT_PURPLE);
        btnLogout.addActionListener(e -> fireEvent("LOGOUT"));

        bottom.add(btnLogout, BorderLayout.CENTER);
        return bottom;
    }

    private void styleIconButton(AbstractButton b) {
        b.setForeground(TEXT_SECONDARY);
        b.setBackground(SIDEBAR_BG_MODERN); 
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setPreferredSize(new Dimension(40, 40));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setForeground(TEXT_PRIMARY); }
            public void mouseExited(MouseEvent e) { b.setForeground(TEXT_SECONDARY); }
        });
    }

    private void toggle() {
        if (anim != null && anim.isRunning()) return;
        final int target = expanded ? W_COLLAPSED : W_EXPANDED;
        
        if (expanded) {
            titleLabel.setText(""); 
            userTextPanel.setVisible(false); 
            for (AccordionMenu menu : menus) menu.setCollapsedMode(true);
            ((RoundedButton) ((JPanel)getComponent(2)).getComponent(0)).setText("");
        }
        
        anim = new Timer(10, e -> {
            if (width < target) width += 15;
            else if (width > target) width -= 15;
            if (Math.abs(width - target) <= 15) {
                width = target;
                anim.stop();
                expanded = !expanded;
                if (expanded) {
                     titleLabel.setText("ACME");
                     userTextPanel.setVisible(true); 
                     for (AccordionMenu menu : menus) menu.setCollapsedMode(false);
                     ((RoundedButton) ((JPanel)getComponent(2)).getComponent(0)).setText("Cerrar Sesión");
                }
            }
            setPreferredSize(new Dimension(width, 0));
            revalidate(); repaint();
        });
        anim.start();
    }

    // =================================================================================
    // CLASE INTERNA AVATAR ACTUALIZADA (AHORA DIBUJA LETRAS)
    // =================================================================================
    
    private class AvatarIcon implements Icon {
        private int size = 45;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 1. Fondo Circular Morado (o el color que gustes)
            g2.setColor(ACCENT_PURPLE);
            g2.fillOval(x, y, size, size);
            
            // 2. Borde sutil brillante
            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawOval(x, y, size, size);
            
            // 3. Dibujar Iniciales Centradas
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            
            FontMetrics fm = g2.getFontMetrics();
            // Calcular posición para centrar el texto
            int txtWidth = fm.stringWidth(userInitials);
            int txtHeight = fm.getAscent();
            
            int textX = x + (size - txtWidth) / 2;
            int textY = y + (size + txtHeight) / 2 - 4; // Ajuste fino vertical (-4)
            
            g2.drawString(userInitials, textX, textY);
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }

    private class RoundedButton extends JButton {
        private Color normalColor;
        private Color hoverColor;

        public RoundedButton(String text, Color bg) {
            super(text);
            this.normalColor = bg;
            this.hoverColor = bg.brighter();
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setForeground(Color.WHITE); setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(0, 45));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(hoverColor); repaint(); }
                public void mouseExited(MouseEvent e) { setBackground(normalColor); repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? hoverColor : normalColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
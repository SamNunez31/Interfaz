package app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class LogoutDialog extends JDialog {

    private boolean confirmed = false;
    private final Color BG_COLOR = new Color(30, 30, 40);
    private final Color ACCENT_COLOR = new Color(124, 77, 255); // Tu morado
    private final Color TEXT_COLOR = new Color(230, 230, 230);

    public LogoutDialog(Frame parent) {
        super(parent, true); // Modal = true (bloquea la ventana de atrás)
        setUndecorated(true); // Quita los bordes de Windows
        setBackground(new Color(0, 0, 0, 0)); // Fondo transparente para los bordes redondos
        setSize(400, 200);
        setLocationRelativeTo(parent);

        // Panel Principal con Bordes Redondeados
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo oscuro
                g2.setColor(BG_COLOR);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                
                // Borde sutil morado
                g2.setColor(new Color(124, 77, 255, 50));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, 20, 20));
            }
        };
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- TÍTULO Y MENSAJE ---
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Cerrar Sesión");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(ACCENT_COLOR);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel lblMsg = new JLabel("¿Estás seguro que deseas salir?");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMsg.setForeground(TEXT_COLOR);
        lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
        
        textPanel.add(lblTitle);
        textPanel.add(lblMsg);
        contentPanel.add(textPanel, BorderLayout.CENTER);

        // --- BOTONES ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);

        JButton btnYes = createButton("SÍ, SALIR", ACCENT_COLOR);
        JButton btnNo = createButton("CANCELAR", new Color(100, 100, 100));

        btnYes.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        btnNo.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        btnPanel.add(btnNo);
        btnPanel.add(btnYes);
        contentPanel.add(btnPanel, BorderLayout.SOUTH);

        add(contentPanel);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setPreferredSize(new Dimension(120, 35));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
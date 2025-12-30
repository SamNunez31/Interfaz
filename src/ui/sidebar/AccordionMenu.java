package ui.sidebar;

import shared.ui.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AccordionMenu extends JPanel {

    private boolean open = false;
    private JPanel itemsPanel;
    private JButton headerBtn;
    private String originalTitle; // Para recordar el nombre

    public AccordionMenu(String title, Icon icon) {
        this.originalTitle = title;
        
        setLayout(new BorderLayout());
        setOpaque(false);
        // Evita que el botón crezca verticalmente más de lo necesario
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));

        headerBtn = new JButton(title);
        headerBtn.setIcon(icon);
        
        // --- ESTILOS ---
        headerBtn.setHorizontalAlignment(SwingConstants.LEFT);
        headerBtn.setIconTextGap(15);
        headerBtn.setForeground(Theme.TEXT);
        headerBtn.setBackground(Theme.SIDEBAR_BG);
        headerBtn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        headerBtn.setFocusPainted(false);
        headerBtn.setBorderPainted(false);
        headerBtn.setContentAreaFilled(false);
        headerBtn.setOpaque(true); 
        headerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        headerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // --- HOVER ---
        headerBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                headerBtn.setBackground(Theme.HOVER_BG);
                headerBtn.setForeground(Theme.ACCENT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if(!open) {
                    headerBtn.setBackground(Theme.SIDEBAR_BG);
                    headerBtn.setForeground(Theme.TEXT);
                }
            }
        });

        headerBtn.addActionListener(e -> toggle());

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setOpaque(false);
        itemsPanel.setVisible(false);

        add(headerBtn, BorderLayout.NORTH);
        add(itemsPanel, BorderLayout.CENTER);
    }

// Modifica este método para aceptar una "accion"
    public void addItem(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setForeground(Theme.MUTED);
        btn.setBackground(Theme.SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 50, 8, 10)); 
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // EVENTO CLICK: Ejecuta la acción que le pasemos
        btn.addActionListener(action);

        // Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setForeground(Theme.ACCENT); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setForeground(Theme.MUTED); }
        });

        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        itemsPanel.add(btn);
    }

    private void toggle() {
        open = !open;
        itemsPanel.setVisible(open);
        
        if (open) {
            // Al abrir, forzamos que se vea activo
            headerBtn.setBackground(Theme.HOVER_BG);
            headerBtn.setForeground(Theme.ACCENT);
        } else {
            headerBtn.setBackground(Theme.SIDEBAR_BG);
            headerBtn.setForeground(Theme.TEXT);
        }
        
        revalidate();
        repaint();
    }

    // --- MAGIA PARA QUITAR LOS "..." ---
    public void setCollapsedMode(boolean collapsed) {
        if (collapsed) {
            headerBtn.setText(""); // Borra texto, deja solo icono
        } else {
            headerBtn.setText(originalTitle); // Restaura texto
        }
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
}
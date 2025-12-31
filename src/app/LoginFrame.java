package app;

import seguridad.UsuarioDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import raven.toast.Notifications;

public class LoginFrame extends JFrame {

    // --- PALETA DE COLORES ---
    private static final Color BG_BASE = new Color(20, 15, 30); 
    private static final Color SHAPE_COLOR = new Color(124, 77, 255); 
    private static final Color BUTTON_BG = new Color(124, 77, 255); 
    private static final Color TEXT_WHITE = new Color(240, 240, 255);
    private static final Color INPUT_BG = Color.WHITE;
    private static final Color INPUT_TEXT = Color.DARK_GRAY;
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private JTextField txtUser;
    private JPasswordField txtPass;
    private boolean isPasswordVisible = false;

    public LoginFrame() {
        Notifications.getInstance().setJFrame(this);
        setTitle("Login ACME");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600); 
        setLocationRelativeTo(null);
        setResizable(false);

        // 1. Fondo con Figuras Geométricas
        BackgroundPanel background = new BackgroundPanel();
        background.setLayout(new GridBagLayout());
        setContentPane(background);

        // 2. Tarjeta Central
        CardPanel loginCard = new CardPanel();
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBorder(new EmptyBorder(40, 40, 40, 40)); 
        loginCard.setPreferredSize(new Dimension(360, 480)); 

        // --- ELEMENTOS ---
        JLabel profileIcon = new JLabel(createProfileOutlineIcon());
        profileIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel title = new JLabel("Bienvenido");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- INPUTS ---
        JPanel userBox = createWhiteBoxInput("Usuario", true);
        JPanel passBox = createWhiteBoxPassword("Contraseña");

        JPanel groupUsuario = createLabelAndInputPanel("Usuario", userBox);
        JPanel groupPass = createLabelAndInputPanel("Contraseña", passBox);

        // Botón
        JButton btnSignIn = new JButton("INGRESAR");
        styleButton(btnSignIn);
        btnSignIn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSignIn.addActionListener(e -> attemptLogin());

        // --- ARMADO ---
        loginCard.add(Box.createVerticalGlue());
        loginCard.add(profileIcon);
        loginCard.add(Box.createVerticalStrut(10));
        loginCard.add(title);
        loginCard.add(Box.createVerticalStrut(30));
        loginCard.add(groupUsuario);
        loginCard.add(Box.createVerticalStrut(15));
        loginCard.add(groupPass);
        loginCard.add(Box.createVerticalStrut(40));
        loginCard.add(btnSignIn);
        loginCard.add(Box.createVerticalGlue());

        background.add(loginCard);
        
        // >>> MAGIA PARA LA TECLA ENTER <<<
        
        // 1. Define el botón por defecto de la ventana (Enter global)
        getRootPane().setDefaultButton(btnSignIn);
        
        // 2. Si estás en el campo de usuario y das Enter, salta a la contraseña
        txtUser.addActionListener(e -> txtPass.requestFocus());
        
        // 3. Si estás en la contraseña y das Enter, ejecuta el login
        txtPass.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String usuario = txtUser.getText().trim();
        // Usamos getPassword() por seguridad
        String clave = new String(txtPass.getPassword()).trim();

        // Validación básica: Evitar vacíos o placeholders
        if (usuario.isEmpty() || usuario.equals("Usuario") || 
            clave.isEmpty() || clave.equals("Contraseña")) {
            
            Notifications.getInstance().show(Notifications.Type.WARNING, 
                    Notifications.Location.TOP_RIGHT, "Por favor ingrese sus credenciales");
            return;
        }

        // --- LÓGICA REAL CON BASE DE DATOS ---
        UsuarioDAO dao = new UsuarioDAO();
        
        if (dao.autenticar(usuario, clave)) {
             // 1. Éxito: Notificación visual
             Notifications.getInstance().show(Notifications.Type.SUCCESS, 
                     Notifications.Location.TOP_RIGHT, "Bienvenido al Sistema");
             
             // 2. Timer para efecto visual suave
             Timer timer = new Timer(800, e -> {
                 // 3. Abrir el Dashboard
                 new DashboardFrame().setVisible(true);
                 // 4. Cerrar Login
                 this.dispose();
             });
             timer.setRepeats(false); 
             timer.start();
             
        } else {
             // Fallo: Usuario no existe o clave incorrecta
             Notifications.getInstance().show(Notifications.Type.ERROR, 
                     Notifications.Location.TOP_RIGHT, "Credenciales Inválidas o Usuario Inactivo");
        }
    }

    // --- MÉTODOS DE UI ---
    private JPanel createLabelAndInputPanel(String labelText, JPanel inputBox) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT); 
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); 

        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(new Color(200, 200, 200)); 
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        inputBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(lbl);
        wrapper.add(Box.createVerticalStrut(5)); 
        wrapper.add(inputBox);
        return wrapper;
    }

    private JPanel createWhiteBoxInput(String placeholder, boolean isUserIcon) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(INPUT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10)); 
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); 

        JLabel iconLabel = new JLabel(drawIconWithSeparator(isUserIcon));
        txtUser = new JTextField();
        setupTextField(txtUser, placeholder);

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(txtUser, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createWhiteBoxPassword(String placeholder) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(INPUT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel iconLabel = new JLabel(drawIconWithSeparator(false));
        txtPass = new JPasswordField();
        setupTextField(txtPass, placeholder);

        JButton btnEye = new JButton("👁");
        btnEye.setBorder(null);
        btnEye.setContentAreaFilled(false);
        btnEye.setFocusPainted(false);
        btnEye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEye.setForeground(Color.GRAY);
        btnEye.addActionListener(e -> {
            isPasswordVisible = !isPasswordVisible;
            txtPass.setEchoChar(isPasswordVisible ? (char) 0 : '•');
        });

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(txtPass, BorderLayout.CENTER);
        panel.add(btnEye, BorderLayout.EAST);
        return panel;
    }

    private Icon drawIconWithSeparator(boolean isUser) {
        int w = 40; int h = 24;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.GRAY);
        if (isUser) {
            g2.fillOval(7, 2, 10, 10);
            g2.fillArc(4, 13, 16, 10, 0, 180);
        } else {
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(7, 3, 10, 10, 5, 5);
            g2.fillRoundRect(5, 10, 14, 12, 4, 4);
            g2.setColor(INPUT_BG); 
            g2.fillOval(10, 14, 4, 4);
        }
        g2.setColor(new Color(220, 220, 220)); 
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(30, 2, 30, 22);
        g2.dispose();
        return new ImageIcon(img);
    }

    private Icon createProfileOutlineIcon() {
        int size = 90;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(TEXT_WHITE);
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(new Ellipse2D.Double(2, 2, size-4, size-4));
        int headSize = 28;
        g2.draw(new Ellipse2D.Double((size - headSize) / 2.0, 22, headSize, headSize));
        g2.draw(new Arc2D.Double(22, size - 40, size - 44, 60, 0, 180, Arc2D.OPEN));
        g2.dispose();
        return new ImageIcon(image);
    }

    private void setupTextField(JTextField tf, String placeholder) {
        tf.setBorder(null);
        tf.setFont(MAIN_FONT);
        tf.setText(placeholder);
        tf.setForeground(Color.GRAY);
        tf.setBackground(INPUT_BG); 
        tf.setCaretColor(BUTTON_BG);
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(INPUT_TEXT); }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (tf.getText().isEmpty()) { tf.setText(placeholder); tf.setForeground(Color.GRAY); }
            }
        });
    }

    private void styleButton(JButton b) {
        b.setBackground(BUTTON_BG);
        b.setForeground(TEXT_WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        b.setPreferredSize(new Dimension(280, 50));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(BUTTON_BG.brighter()); }
            public void mouseExited(MouseEvent e) { b.setBackground(BUTTON_BG); }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

// --- CLASES DE DIBUJO ---

class BackgroundPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        // Fondo Base
        g2.setColor(new Color(25, 20, 35));
        g2.fillRect(0, 0, w, h);

        // Figuras diagonales
        AffineTransform old = g2.getTransform();
        g2.rotate(Math.toRadians(-30), w/2.0, h/2.0);

        g2.setColor(new Color(124, 77, 255, 20)); 
        g2.fillRoundRect(w/4, -100, 400, 800, 200, 200);

        g2.setColor(new Color(124, 77, 255, 40)); 
        g2.fillRoundRect(w/2 + 50, 100, 150, 600, 150, 150);

        g2.setColor(new Color(124, 77, 255, 15));
        g2.fillOval(w/2 - 300, 300, 200, 200);

        g2.setTransform(old);
    }
}

class CardPanel extends JPanel {
    public CardPanel() { setOpaque(false); }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int arc = 25; 

        g2.setColor(new Color(35, 30, 45, 245)); 
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, arc, arc));
        
        g2.setColor(new Color(0, 0, 0, 50));
        g2.draw(new RoundRectangle2D.Double(0, 0, w-1, h-1, arc, arc));
        
        super.paintComponent(g);
    }
}
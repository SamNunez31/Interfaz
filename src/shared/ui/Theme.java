package shared.ui;

import java.awt.Color;

public class Theme {
    // --- PALETA DE COLORES DARK MODE ---

    // SIDEBAR_BG: El fondo de la barra lateral (Gris muy oscuro / casi negro)
    public static final Color SIDEBAR_BG = new Color(20, 20, 24); 
    
    // BG: El fondo principal de las pantallas (Un poco más claro o diferente para contraste)
    public static final Color BG = new Color(28, 28, 32); 

    // PANEL: Color para tarjetas o paneles flotantes (si los usas)
    public static final Color PANEL = new Color(40, 40, 45); 

    // TEXTO: Blanco suave (no quema la vista)
    public static final Color TEXT = new Color(230, 230, 230);
    
    // MUTED: Texto secundario (Grisáceo)
    public static final Color MUTED = new Color(150, 150, 160);

    // ACENTO: Morado Neón (Purple) - Tu color principal
    public static final Color ACCENT = new Color(187, 134, 252); 
    
    // HOVER: Fondo al pasar el mouse
    public static final Color HOVER_BG = new Color(50, 30, 60); 
    
    // ERROR: Rojo suave (por si necesitas validar campos)
    public static final Color ERROR = new Color(207, 102, 121);
}
package shared.ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class IconUtil {

    public static Icon load(String path, int size) {
        // 1. Intentamos obtener la URL del recurso
        URL url = IconUtil.class.getResource(path);
        
        // Si la URL es nula, significa que no se encontró (aunque tu test dijo que sí)
        if (url == null) {
            System.err.println("IconUtil: No se pudo cargar la imagen en " + path);
            return null;
        }

        // 2. Cargamos la imagen original
        ImageIcon originalIcon = new ImageIcon(url);
        
        // Verificamos si la imagen cargó datos reales
        if (originalIcon.getIconWidth() == -1) {
            System.err.println("IconUtil: La imagen existe pero está corrupta o el formato no es válido: " + path);
            return null;
        }

        // 3. Redimensionamos (Escalado suave para que no se vea pixelada)
        Image img = originalIcon.getImage();
        Image newImg = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);

        // 4. Devolvemos el ícono listo
        return new ImageIcon(newImg);
    }
}
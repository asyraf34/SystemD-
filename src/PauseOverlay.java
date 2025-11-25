import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.io.InputStream;

/**
 * Draws a blurred background from snapshot + translucent dark overlay + centered text.
 * Tries to use the same custom font as MenuPanel ("/04B_03__.ttf") for consistency.
 */
public class PauseOverlay {

    private static final float[] BLUR_KERNEL = {
            1/9f, 1/9f, 1/9f,
            1/9f, 1/9f, 1/9f,
            1/9f, 1/9f, 1/9f
    };

    private final Font customFont; // may be fallback if resource missing

    public PauseOverlay() {
        Font loaded = null;
        try (InputStream is = getClass().getResourceAsStream("/04B_03__.ttf")) {
            if (is != null) {
                loaded = Font.createFont(Font.TRUETYPE_FONT, is);
            }
        } catch (FontFormatException | IOException ignored) {
            // fallback below
        }
        if (loaded != null) {
            customFont = loaded;
        } else {
            customFont = new Font("SansSerif", Font.PLAIN, 18);
        }
    }

    public void renderPaused(Graphics2D g, BufferedImage snapshot, int width, int height) {
        // draw blurred snapshot (if available)
        if (snapshot != null) {
            BufferedImage blurred = blur(snapshot);
            g.drawImage(blurred, 0, 0, width, height, null);
        } else {
            Composite oldC = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
            g.setComposite(oldC);
        }

        // extra dimming overlay
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.setComposite(old);

        // Title "PAUSED" - use bold derived custom font if available
        Font titleFont;
        try {
            titleFont = customFont.deriveFont(Font.BOLD, Math.max(36, width / 20));
        } catch (Exception ex) {
            titleFont = new Font("SansSerif", Font.BOLD, Math.max(36, width / 20));
        }
        g.setFont(titleFont);
        FontRenderContext frc = g.getFontRenderContext();
        String pausedText = "PAUSED";
        Rectangle2D bounds = titleFont.getStringBounds(pausedText, frc);
        int tx = (int) ((width - bounds.getWidth()) / 2);
        int ty = (int) ((height - bounds.getHeight()) / 2 - bounds.getY());

        // drop shadow + title
        g.setColor(new Color(0, 0, 0, 160));
        g.drawString(pausedText, tx + 3, ty + 3);
        g.setColor(Color.WHITE);
        g.drawString(pausedText, tx, ty);

        // Hint: use the exact same custom font (plain) like MenuPanel's instruction text
        Font hintFont;
        try {
            // MenuPanel used deriveFont(Font.PLAIN, 20) for ESC; earlier pause hint used ~18.
            // Use 18-20 depending on width to keep it similar but not huge.
            //float size = Math.max(14f, Math.min(20f, width / 60f));
            hintFont = customFont.deriveFont(Font.PLAIN, 20);
        } catch (Exception ex) {
            hintFont = new Font("SansSerif", Font.PLAIN, Math.max(14, width / 60));
        }
        g.setFont(hintFont);
        String hint = "Press 'P' to resume";
        Rectangle2D hb = hintFont.getStringBounds(hint, frc);
        int hx = (int) ((width - hb.getWidth()) / 2);
        int hy = ty + 50;
        g.setColor(new Color(255, 255, 255, 220));
        g.drawString(hint, hx, hy);
    }

    private BufferedImage blur(BufferedImage src) {
        Kernel kernel = new Kernel(3, 3, BLUR_KERNEL);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        op.filter(src, dest);
        return dest;
    }
}

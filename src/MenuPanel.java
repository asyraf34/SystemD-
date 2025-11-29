import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.Serial;
import javax.imageio.ImageIO;

/**
 * MenuPanel: uses AssetManager to attempt to load decorative icons via public getters.
 * If icons are not present on the classpath, they simply won't be drawn.
 */
public class MenuPanel extends JPanel implements ActionListener {
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean showPressStart = true;
    private final Timer blinkTimer = new Timer(500, this);

    private Font customFont;

    // volume control state (menu-local)
    private final Rectangle volumeTrackBounds = new Rectangle();
    private boolean volumeDragging = false;
    private long unusedTooltipExpiry = 0L; // not used

    // Decorative icons (menu) grabbed from AssetManager
    private Image iconMute;
    private Image iconVolume;

    public MenuPanel(Runnable startGameCallback) {
        setFocusable(true);
        setBackground(Color.BLACK);

        try (InputStream is = getClass().getResourceAsStream("/04B_03__.ttf")) {
            if (is != null) customFont = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FontFormatException | IOException e) {
            // ignore
        }
        if (customFont == null) customFont = new Font("SansSerif", Font.PLAIN, 18);

        // Use a local AssetManager to load images the same way the rest of the game does.
        try {
            AssetManager am = new AssetManager(GameConstants.TILE_SIZE);
            iconMute = am.getIconMuteImage();
            iconVolume = am.getIconVolumeImage();
        } catch (Throwable ignored) {
            // if AssetManager construction fails (unlikely) fall back to direct tries
            iconMute = tryLoadFallback("/icons/mute.png", "res/icons/mute.png");
            iconVolume = tryLoadFallback("/icons/volume.png", "res/icons/volume.png");
        }

        blinkTimer.start();

        // Mouse interactions
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMenuMousePressed(e.getX(), e.getY());
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                volumeDragging = false;
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (volumeDragging) handleMenuMouseDragged(e.getX(), e.getY());
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int kc = e.getKeyCode();
                if (kc == KeyEvent.VK_ENTER) {
                    blinkTimer.stop();
                    startGameCallback.run();
                } else if (kc == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                requestFocusInWindow();
            }
        });
    }

    private Image tryLoadFallback(String classpathPath, String diskPath) {
        try (InputStream is = getClass().getResourceAsStream(classpathPath)) {
            if (is != null) return ImageIO.read(is);
        } catch (IOException ignored) {}
        try {
            java.io.File f = new java.io.File(diskPath);
            if (f.isFile()) return ImageIO.read(f);
        } catch (IOException ignored) {}
        return null;
    }

    private void handleMenuMousePressed(int mx, int my) {
        if (volumeTrackBounds.contains(mx, my)) {
            volumeDragging = true;
            updateMenuVolumeFromX(mx);
        }
    }

    private void handleMenuMouseDragged(int mx, int my) {
        updateMenuVolumeFromX(mx);
    }

    private void updateMenuVolumeFromX(int mx) {
        int topBarH = Math.max(32, GameConstants.TILE_SIZE);
        int centerX = getWidth() / 2;
        int trackW = Math.min(getWidth() / 3, 260);
        int trackX = centerX - trackW / 2;

        float rel = (float) (mx - trackX) / (float) Math.max(1, trackW);
        rel = Math.max(0f, Math.min(1f, rel));
        try {
            SoundManager.getInstance().setBackgroundVolume(rel);
            unusedTooltipExpiry = System.currentTimeMillis() + 600;
            repaint();
        } catch (Throwable ignored) {}
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(Color.YELLOW);
        g2.setFont(customFont.deriveFont(Font.BOLD, 72f));
        String title = "MAN - HUNT";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        int deltaY = 30;
        g2.drawString(title, (getWidth() - titleWidth) / 2, 285 + deltaY);

        if (showPressStart) {
            g2.setColor(Color.WHITE);
            g2.setFont(customFont.deriveFont(Font.BOLD, 32f));
            String msg = "PRESS ENTER TO START";
            int msgWidth = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (getWidth() - msgWidth) / 2, 350 + deltaY);
        }

        g2.setFont(customFont.deriveFont(Font.PLAIN, 20f));
        g2.setColor(Color.GRAY);
        String escMsg = "Press ESC to Exit";
        int escWidth = g2.getFontMetrics().stringWidth(escMsg);
        g2.drawString(escMsg, (getWidth() - escWidth) / 2, 400 + deltaY);

        String pauseMsg = "Press 'P' to Pause";
        g2.drawString(pauseMsg, (getWidth() - g2.getFontMetrics().stringWidth(pauseMsg)) / 2, 430 + deltaY);

        drawMenuVolumeControl(g2);
    }

    private void drawMenuVolumeControl(Graphics2D g2) {
        int topBarH = Math.max(32, GameConstants.TILE_SIZE);
        int centerX = getWidth() / 2;
        int centerY = topBarH / 2;

        int trackW = Math.min(getWidth() / 3, 260);
        int trackH = 6;
        int trackX = centerX - trackW / 2;
        int trackY = centerY - trackH / 2;

        volumeTrackBounds.setBounds(trackX, trackY - 8, trackW, trackH + 16);

        float vol = 1f;
        try {
            vol = SoundManager.getInstance().getBackgroundVolume();
        } catch (Throwable ignored) {}

        int iconSize = topBarH - 8;
        int iconY = (topBarH - iconSize) / 2;

        if (iconMute != null) {
            int leftX = trackX - iconSize - 8;
            g2.drawImage(iconMute, leftX, iconY, iconSize, iconSize, null);
        }

        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillRoundRect(trackX, trackY, trackW, trackH, trackH, trackH);

        int fillW = (int) (vol * trackW);
        g2.setColor(new Color(255, 215, 64));
        g2.fillRoundRect(trackX, trackY, fillW, trackH, trackH, trackH);

        int knobRadius = 5;
        int knobCx = trackX + fillW;
        int knobCy = trackY + trackH / 2;
        g2.setColor(Color.WHITE);
        g2.fillOval(knobCx - knobRadius, knobCy - knobRadius, knobRadius * 2, knobRadius * 2);
        g2.setColor(new Color(0, 0, 0, 30));
        g2.drawOval(knobCx - knobRadius, knobCy - knobRadius, knobRadius * 2, knobRadius * 2);

        if (iconVolume != null) {
            int rightX = trackX + trackW + 8;
            g2.drawImage(iconVolume, rightX, iconY, iconSize, iconSize, null);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        showPressStart = !showPressStart;
        repaint();
    }
}
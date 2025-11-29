import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import java.awt.FontFormatException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Renderer: uses the AssetManager getters to access decorative icons (if present).
 * No direct reflection/private access to AssetManager is used.
 */
public class Renderer {

    private final AssetManager assetManager;
    private final GameMap gameMap;
    private final int tileSize;

    // --- Pre-calculated Dimensions
    private final int boardWidth;
    private final int boardHeight;
    private final int topBarH;
    private final int bottomBarH;
    private final int totalH;

    // Pause support
    private final PauseManager pauseManager = PauseManager.getInstance();
    private final PauseOverlay pauseOverlay = new PauseOverlay();

    // Audio integration
    private final SoundManager soundManager;

    // Volume-control interaction state
    private boolean volumeDragging = false;
    private final Rectangle volumeTrackBounds = new Rectangle(); // computed each frame
    private long showVolumeTooltipUntil = 0L; // not used to show bubble in current UI

    // custom font (optional)
    private Font customFont;

    public Renderer(AssetManager assetManager, GameMap gameMap, int tileSize, SoundManager soundManager) {
        this.assetManager = assetManager;
        this.gameMap = gameMap;
        this.tileSize = tileSize;
        this.soundManager = soundManager;

        this.boardWidth  = tileSize * gameMap.getColumnCount();
        this.boardHeight = tileSize * gameMap.getRowCount();
        this.topBarH     = Math.max(32, tileSize);
        this.bottomBarH  = Math.max(40, (int)(tileSize * 1.2));
        this.totalH      = topBarH + boardHeight + bottomBarH;

        // Try to load the custom font used by the menu so overlays match the menu style.
        try (InputStream is = getClass().getResourceAsStream("/04B_03__.ttf")) {
            if (is != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            }
        } catch (FontFormatException | IOException ex) {
            // keep customFont == null and fall back later
        }
    }

    public void drawGame(Graphics g, JPanel panel, GameState state) {
        // Draw Background + top/bottom bars
        g.drawImage(assetManager.getBackgroundImage(), 0, 0, boardWidth, totalH, null);
        drawBarBackgrounds(g);

        // Draw game entities shifted down by topBarH
        Graphics2D gm = (Graphics2D) g.create();
        gm.translate(0, topBarH);
        drawEntities(gm, state);
        gm.dispose();

        // Draw animations (also translated)
        if (state.animations != null && !state.animations.isEmpty()) {
            Graphics2D gAnim = (Graphics2D) g.create();
            gAnim.translate(0, topBarH);
            for (DeathAnimation da : state.animations) {
                da.render(gAnim);
            }
            gAnim.dispose();
        }

        // Draw HUD (score/level) and volume control in top bar
        drawHUD(g, state);

        // Pause overlay capture & draw
        {
            Graphics2D g2 = (g instanceof Graphics2D) ? (Graphics2D) g : (Graphics2D) g.create();

            // Capture snapshot once when entering pause
            if (pauseManager.isPaused() && pauseManager.getPauseSnapshot() == null) {
                BufferedImage snap = new BufferedImage(boardWidth, totalH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gs = snap.createGraphics();

                gs.drawImage(assetManager.getBackgroundImage(), 0, 0, boardWidth, totalH, null);
                drawBarBackgrounds(gs);

                Graphics2D gmSnap = (Graphics2D) gs.create();
                gmSnap.translate(0, topBarH);
                drawEntities(gmSnap, state);
                gmSnap.dispose();

                if (state.animations != null && !state.animations.isEmpty()) {
                    Graphics2D gAnimSnap = (Graphics2D) gs.create();
                    gAnimSnap.translate(0, topBarH);
                    for (DeathAnimation da : state.animations) {
                        da.render(gAnimSnap);
                    }
                    gAnimSnap.dispose();
                }

                // Draw HUD into snapshot as well so paused snapshot looks the same
                drawHUD(gs, state);

                gs.dispose();
                pauseManager.setPauseSnapshot(snap);
            }

            // If paused, draw overlay on top of current frame
            if (pauseManager.isPaused()) {
                BufferedImage snap = pauseManager.getPauseSnapshot();
                if (snap != null) {
                    pauseOverlay.renderPaused(g2, snap, boardWidth, totalH);
                } else {
                    Composite old = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                    g2.setColor(Color.BLACK);
                    g2.fillRect(0, 0, boardWidth, totalH);
                    g2.setComposite(old);
                    // Use custom font for PAUSED fallback if available
                    Font titleFont = (customFont != null) ? customFont.deriveFont(Font.BOLD, 48f)
                            : new Font("SansSerif", Font.BOLD, 48);
                    g2.setFont(titleFont);
                    FontMetrics fm = g2.getFontMetrics();
                    String paused = "PAUSED";
                    int px = (boardWidth - fm.stringWidth(paused)) / 2;
                    int py = topBarH + boardHeight / 2;
                    g2.setColor(Color.WHITE);
                    g2.drawString(paused, px, py);
                }
            }
        }
    }

    private void drawBarBackgrounds(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, boardWidth, topBarH);
        g2.fillRect(0, topBarH + boardHeight, boardWidth, bottomBarH);
        g2.dispose();
    }

    private void drawEntities(Graphics2D g2d, GameState state) {
        for (Entity wall : state.walls)       drawEntity(g2d, wall);
        for (Entity food : state.foods)       drawEntity(g2d, food);
        for (Entity knife : state.knives)     drawEntity(g2d, knife);
        for (Actor ghost : state.ghosts)      drawActor(g2d, ghost);
        if (state.boss != null)               drawActor(g2d, state.boss);
        for (Actor proj : state.projectiles)  drawActor(g2d, proj);
        if (state.pacman != null)             drawActor(g2d, state.pacman);
    }

    private void drawEntity(Graphics2D g, Entity e) {
        if (e.image != null) g.drawImage(e.image, e.x, e.y, e.width, e.height, null);
    }
    private void drawActor(Graphics2D g, Actor a) {
        if (a.image != null) g.drawImage(a.image, a.x, a.y, a.width, a.height, null);
    }

    private void drawHUD(Graphics g, GameState state) {
        int pad = Math.max(8, tileSize / 6);

        // Overlays (Game Over / Level Up)
        if (state.gameOver || state.gameWon || state.interLevel) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRect(0, 0, boardWidth, totalH);

            String title = state.gameOver ? "GAME OVER" : (state.gameWon ? "YOU WIN!" : ("Level " + (state.nextLevelToStart > 0 ? state.nextLevelToStart : state.currentLevel + 1)));
            float titleSize = 56f;

            // Use custom font for overlay title if available
            Font titleFont = (customFont != null) ? customFont.deriveFont(Font.BOLD, titleSize)
                    : g2.getFont().deriveFont(Font.BOLD, titleSize);
            g2.setFont(titleFont);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (boardWidth - fm.stringWidth(title)) / 2;
            int ty = topBarH + boardHeight / 2 - fm.getHeight();
            g2.setColor(Color.WHITE);
            g2.drawString(title, tx, ty);

            if (!state.interLevel) {
                String sub = "Press any key to restart";

                // Use custom font for subtext if available
                Font subFont = (customFont != null) ? customFont.deriveFont(Font.PLAIN, 28f)
                        : g2.getFont().deriveFont(Font.PLAIN, 28f);
                g2.setFont(subFont);
                FontMetrics fm2 = g2.getFontMetrics();
                int sx = (boardWidth - fm2.stringWidth(sub)) / 2;
                int sy = ty + fm.getHeight() + 40;
                g2.drawString(sub, sx, sy);
            }
            g2.dispose();
            if (!state.interLevel) return;
        }

        Graphics2D g2 = (Graphics2D) g.create();

        // Score (left)
        g2.setFont(new Font("Arial", Font.BOLD, Math.max(18, tileSize / 2)));
        String scoreText = String.valueOf(state.score);
        FontMetrics fm = g2.getFontMetrics();
        int ty = (topBarH - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(Color.WHITE);
        g2.drawString(scoreText, pad, ty);

        // Level (right)
        String levelText = "Level " + state.currentLevel;
        int tx = boardWidth - pad - fm.stringWidth(levelText);
        g2.drawString(levelText, tx, ty);

        // Volume control (top-center)
        drawVolumeControl(g2);

        // Bottom Bar Icons & meters (unchanged)
        int iconH = (int) (bottomBarH * 0.8);
        int gap   = Math.max(6, tileSize / 6);
        int baseY = topBarH + boardHeight + (bottomBarH - iconH) / 2;

        Image lifeIcon = assetManager.getPacmanRightImage();
        int count = Math.max(0, state.lives);
        int x = pad;
        for (int i = 0; i < count; i++) {
            if (lifeIcon != null) g2.drawImage(lifeIcon, x, baseY, iconH, iconH, null);
            x += iconH + gap;
        }

        Image knifeIcon = assetManager.getKnifeImage();
        int kCount = Math.max(0, state.knifeCount);
        int kx = boardWidth - pad - iconH;
        for (int i = 0; i < kCount; i++) {
            if (knifeIcon != null) g2.drawImage(knifeIcon, kx, baseY, iconH, iconH, null);
            kx -= iconH + gap;
        }

        drawSprintMeter(g2, state, baseY, iconH, gap);

        g2.dispose();
    }

    private void drawVolumeControl(Graphics2D g2) {
        int centerX = boardWidth / 2;
        int centerY = topBarH / 2;
        int trackW = Math.min(boardWidth / 3, 260);
        int trackH = 6;
        int trackX = centerX - trackW / 2;
        int trackY = centerY - trackH / 2;

        // store hit area slightly larger than the visual track for easier dragging
        volumeTrackBounds.setBounds(trackX, trackY - 8, trackW, trackH + 16);

        float vol = 1f;
        try {
            vol = (soundManager != null) ? soundManager.getBackgroundVolume() : 1f;
        } catch (Throwable ignored) {}

        // Decorative icons size and position
        int iconSize = topBarH - 8;
        int iconY = (topBarH - iconSize) / 2;

        // Draw left icon (decorative) if available via AssetManager getter
        Image iconMute = assetManager.getIconMuteImage();
        if (iconMute != null) {
            int leftX = trackX - iconSize - 8;
            g2.drawImage(iconMute, leftX, iconY, iconSize, iconSize, null);
        }

        // Track background
        g2.setColor(new Color(255, 255, 255, 50));
        g2.fillRoundRect(trackX, trackY, trackW, trackH, trackH, trackH);

        // Filled part (yellow)
        int fillW = (int) (vol * trackW);
        g2.setColor(new Color(255, 215, 64));
        g2.fillRoundRect(trackX, trackY, fillW, trackH, trackH, trackH);

        // Knob (smaller)
        int knobRadius = 5; // small knob per request
        int knobCx = trackX + fillW;
        int knobCy = trackY + trackH / 2;
        knobCx = Math.max(trackX, Math.min(trackX + trackW, knobCx));

        g2.setColor(Color.WHITE);
        g2.fillOval(knobCx - knobRadius, knobCy - knobRadius, knobRadius * 2, knobRadius * 2);
        g2.setColor(new Color(0, 0, 0, 30));
        g2.drawOval(knobCx - knobRadius, knobCy - knobRadius, knobRadius * 2, knobRadius * 2);

        // Draw right icon (decorative) if available via AssetManager getter
        Image iconVolume = assetManager.getIconVolumeImage();
        if (iconVolume != null) {
            int rightX = trackX + trackW + 8;
            g2.drawImage(iconVolume, rightX, iconY, iconSize, iconSize, null);
        }
    }

    // ---- Mouse / interaction helpers ----
    public void onVolumeMousePressed(int mx, int my) {
        if (volumeTrackBounds.contains(mx, my)) {
            volumeDragging = true;
            updateVolumeFromX(mx);
        }
    }

    public void onVolumeMouseDragged(int mx, int my) {
        if (!volumeDragging) return;
        updateVolumeFromX(mx);
    }

    public void onVolumeMouseReleased(int mx, int my) {
        if (volumeDragging) {
            updateVolumeFromX(mx);
        }
        volumeDragging = false;
    }

    private void updateVolumeFromX(int mx) {
        int trackX = volumeTrackBounds.x;
        int trackW = volumeTrackBounds.width;
        float rel = (float) (mx - trackX) / (float) Math.max(1, trackW);
        rel = Math.max(0f, Math.min(1f, rel));
        try {
            // set background volume only; sound effects unaffected
            soundManager.setBackgroundVolume(rel);
            showVolumeTooltipUntil = System.currentTimeMillis() + 600;
        } catch (Throwable ignored) {}
    }

    // --- Restored original wall texture creation (ensures walls appear) ---
    public Image createWallTexture(boolean[][] wallMatrix, int row, int column) {
        Image wallImage = assetManager.getWallImage();

        if (wallImage == null) {
            BufferedImage fallback = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D fallbackG = fallback.createGraphics();
            fallbackG.setColor(new Color(30, 30, 30));
            fallbackG.fillRect(0, 0, tileSize, tileSize);
            fallbackG.setColor(new Color(80, 80, 80));
            fallbackG.drawRect(0, 0, tileSize - 1, tileSize - 1);
            fallbackG.dispose();
            return fallback;
        }

        BufferedImage texture = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = texture.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(wallImage, 0, 0, tileSize, tileSize, null);
        g2d.dispose();
        return texture;
    }

    private void drawSprintMeter(Graphics2D g2, GameState state, int baseY, int iconH, int gap) { /* existing code */ }
    private void drawBossHud(Graphics2D g2, GameState state, int pad) { /* existing code */ }
}
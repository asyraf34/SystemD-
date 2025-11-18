import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import javax.swing.JPanel;

/**
 * Manages all rendering for the game.
 * It draws the game state (entities, actors, HUD) onto the panel.
 */
public class Renderer {

    private final AssetManager assetManager;
    private final GameMap gameMap;
    private final int tileSize;
    private int boardWidth;
    private int boardHeight;


    public Renderer(AssetManager assetManager, GameMap gameMap, int tileSize) {
        this.assetManager = assetManager;
        this.gameMap = gameMap;
        this.tileSize = tileSize;
    }

    /**
     * Main draw method called by PacMan's paintComponent.
     */
    public void drawGame(Graphics g, JPanel panel, PacMan.GameState state) {
        final int boardWidth  = tileSize * gameMap.getColumnCount();
        final int boardHeight = tileSize * gameMap.getRowCount();
        final int topBarH     = Math.max(32, tileSize);
        final int bottomBarH  = Math.max(40, (int)(tileSize * 1.2));
        final int totalH      = topBarH + boardHeight + bottomBarH;

        // Full background across panel
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, boardWidth, totalH);
        // Bars background (outside the map)
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, boardWidth, topBarH);                                  // top bar
        g2.fillRect(0, topBarH + boardHeight, boardWidth, bottomBarH);           // bottom bar
        g2.dispose();

        // Draw map + entities shifted down by topBarH
        Graphics2D gm = (Graphics2D) ((Graphics2D) g).create();
        gm.translate(0, topBarH);

        for (Entity wall : state.walls) {
            gm.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }
        for (Entity food : state.foods) {
            gm.drawImage(food.image, food.x, food.y, food.width, food.height, null);
        }
        for (Entity knife : state.knives) {
            gm.drawImage(knife.image, knife.x, knife.y, knife.width, knife.height, null);
        }
        for (Actor ghost : state.ghosts) {
            gm.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }
        if (state.pacman != null) {
            gm.drawImage(state.pacman.image, state.pacman.x, state.pacman.y, state.pacman.width, state.pacman.height, null);
        }
        gm.dispose();

        // --- Insert animation : draw procedural death animations on top ---
        if (state.animations != null && !state.animations.isEmpty()) {
            Graphics2D gAnim = (Graphics2D) g.create();
            gAnim.translate(0, topBarH);
            for (DeathAnimation da : state.animations) {
                da.render(gAnim);
            }
            gAnim.dispose();
        }

        // HUD on the bars (score/level/lives/knives + overlays)
        drawHUD(g, state);
    }


    /**
     * Draws the heads-up display (score, lives, etc.).
     * Draws Game Over, Game Win and inter-level banner
     */

    private void drawHUD(Graphics g, PacMan.GameState state) {
        final int boardWidth  = tileSize * gameMap.getColumnCount();
        final int boardHeight = tileSize * gameMap.getRowCount();
        final int topBarH     = Math.max(32, tileSize);
        final int bottomBarH  = Math.max(40, (int)(tileSize * 1.2));
        final int totalH      = topBarH + boardHeight + bottomBarH;
        final int pad         = Math.max(8, tileSize / 6);

        // ===== Overlays (dim full screen) =====
        if (state.gameOver || state.gameWon || state.interLevel) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRect(0, 0, boardWidth, totalH);

            String title = state.gameOver ? "GAME OVER" : (state.gameWon ? "YOU WIN!" : ("Level " + (state.nextLevelToStart > 0 ? state.nextLevelToStart : state.currentLevel + 1)));
            float titleSize = state.interLevel ? 56f : 56f;

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, titleSize));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (boardWidth - fm.stringWidth(title)) / 2;
            int ty = topBarH + boardHeight / 2 - fm.getHeight();
            g2.setColor(Color.WHITE);
            g2.drawString(title, tx, ty);

            if (!state.interLevel) {
                String sub = "Press any key to restart";
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28f));
                FontMetrics fm2 = g2.getFontMetrics();
                int sx = (boardWidth - fm2.stringWidth(sub)) / 2;
                int sy = ty + fm.getHeight() + 40;
                g2.drawString(sub, sx, sy);
            }

            g2.dispose();
            if (!state.interLevel) return; // keep banner visible; skip bars text
            // if interLevel, still draw top/bottom bar content below
        }

        Graphics2D g2 = (Graphics2D) g.create();

        // ===== TOP BAR =====
        // Score (top-left) – number only
        {
            g2.setFont(new Font("Arial", Font.BOLD, Math.max(18, tileSize / 2)));
            String scoreText = String.valueOf(state.score);
            FontMetrics fm = g2.getFontMetrics();
            int tx = pad;
            int ty = (topBarH - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString(scoreText, tx, ty);
        }

        // Level (top-right) – "Level N"
        {
            g2.setFont(new Font("Arial", Font.BOLD, Math.max(18, tileSize / 2)));
            String levelText = "Level " + state.currentLevel;
            FontMetrics fm = g2.getFontMetrics();
            int tx = boardWidth - pad - fm.stringWidth(levelText);
            int ty = (topBarH - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString(levelText, tx, ty);
        }

        // ===== BOTTOM BAR =====
        int iconH = (int) (bottomBarH * 0.8);   // BIGGER icons
        int iconW = iconH;
        int gap   = Math.max(6, tileSize / 6);
        int baseY = topBarH + boardHeight + (bottomBarH - iconH) / 2;

        // Lives (bottom-left) – icons only
        {
            Image lifeIcon = assetManager.getPacmanRightImage();
            int count = Math.max(0, state.lives);
            int x = pad;
            for (int i = 0; i < count; i++) {
                if (lifeIcon != null) g2.drawImage(lifeIcon, x, baseY, iconW, iconH, null);
                else { g2.setColor(Color.WHITE); g2.fillOval(x, baseY, iconW, iconH); }
                x += iconW + gap;
            }
        }

        // Knives (bottom-right) – icons only
        {
            Image knifeIcon = assetManager.getKnifeImage();
            int count = Math.max(0, state.knifeCount);
            int x = boardWidth - pad - iconW;
            for (int i = 0; i < count; i++) {
                if (knifeIcon != null) g2.drawImage(knifeIcon, x, baseY, iconW, iconH, null);
                else { g2.setColor(Color.WHITE); g2.fillRect(x, baseY, iconW, iconH); }
                x -= iconW + gap;
            }
        }

        g2.dispose();
    }

    // -----------------------------------------------------------------
    //  WALL TEXTURE REFACTOR
    // -----------------------------------------------------------------

    /**
     * Dynamically creates a wall texture based on adjacent walls.
     * This is the "facade" method that coordinates the drawing.
     * Its complexity is now very low.
     */
    public Image createWallTexture(boolean[][] wallMatrix, int row, int column) {
        BufferedImage texture = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = texture.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        paintFlatBlack(g2d);
        drawGlowBorder(g2d);
        drawCoreOutline(g2d);
        g2d.dispose();
        return texture;
    }

    private void paintFlatBlack(Graphics2D g2d) {
        g2d.setComposite(AlphaComposite.SrcOver);

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, tileSize, tileSize);
    }

    private void drawGlowBorder(Graphics2D g2d) {
        Stroke original = g2d.getStroke();
        g2d.setStroke(new BasicStroke(1f));
        int glowLayers = Math.max(6, tileSize / 3);
        for (int i = 0; i < glowLayers; i++) {
            float falloff = 1f - (float) i / glowLayers;
            int alpha = (int) (160 * falloff);
            if (alpha <= 0) continue;
            g2d.setColor(new Color(255, 0, 0, Math.min(255, Math.max(0, alpha))));
            int inset = i;
            int size = tileSize - inset * 2 - 1;
            if (size <= 0) break;
            g2d.drawRect(inset, inset, size, size);
        }

        g2d.setStroke(original);
    }

    private void drawCoreOutline(Graphics2D g2d) {
        int outlineThickness = Math.max(2, tileSize / 12);
        Stroke original = g2d.getStroke();
        g2d.setStroke(new BasicStroke(outlineThickness, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(255, 70, 70));
        int inset = outlineThickness / 2;
        int size = tileSize - outlineThickness;
        g2d.drawRect(inset, inset, size, size);
        g2d.setStroke(original);
    }
}
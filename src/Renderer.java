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
        g.drawImage(assetManager.getBackgroundImage(), 0, 0, boardWidth, totalH, null);

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
        if (state.boss != null) {
            gm.drawImage(state.boss.image, state.boss.x, state.boss.y, state.boss.width, state.boss.height, null);
        }
        for (Actor proj : state.projectiles) {
            gm.drawImage(proj.image, proj.x, proj.y, proj.width, proj.height, null);
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

        // Boss lives (bottom-center)
        if (state.boss != null) {
            g2.setFont(new Font("Arial", Font.BOLD, Math.max(16, tileSize / 2 - 2)));
            String bossText = "BOSS: " + state.boss.getLives();
            FontMetrics fm = g2.getFontMetrics();
            int tx = (boardWidth - fm.stringWidth(bossText)) / 2;
            int ty = baseY + (iconH - fm.getHeight()) / 2 + fm.getAscent(); // Vertically center with icons
            g2.setColor(Color.RED);
            g2.drawString(bossText, tx, ty);
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

        // Determine neighbors
        boolean hasTop = row > 0 && wallMatrix[row - 1][column];
        boolean hasBottom = row < gameMap.getRowCount() - 1 && wallMatrix[row + 1][column];
        boolean hasLeft = column > 0 && wallMatrix[row][column - 1];
        boolean hasRight = column < gameMap.getColumnCount() - 1 && wallMatrix[row][column + 1];

        // --- Delegate drawing to private helper methods ---
        drawBase(g2d);
        drawInner(g2d, hasTop, hasBottom, hasLeft, hasRight);

        // Save original stroke
        Stroke originalStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(Math.max(1f, (tileSize / 8) / 3f))); // accentThickness / 3

        if (!hasTop)    drawTopBorder(g2d);
        else            drawGenericBorder(g2d, "TOP");

        if (!hasBottom) drawBottomBorder(g2d);
        else            drawGenericBorder(g2d, "BOTTOM");

        if (!hasLeft)   drawLeftBorder(g2d);
        else            drawGenericBorder(g2d, "LEFT");

        if (!hasRight)  drawRightBorder(g2d);
        else            drawGenericBorder(g2d, "RIGHT");

        g2d.setStroke(originalStroke);
        g2d.dispose();
        return texture;
    }

    // --- Private Helper Methods for Wall Texture ---
    // (Each of these has a *much* lower complexity score)

    private void drawBase(Graphics2D g2d) {
        Color baseShadow = new Color(70, 50, 20);
        Color baseLight = new Color(235, 190, 90);

        if (assetManager.getWallImage() != null) {
            g2d.drawImage(assetManager.getWallImage(), 0, 0, tileSize, tileSize, null);
        }
        GradientPaint basePaint = new GradientPaint(0, 0, baseShadow, tileSize, tileSize, baseLight);
        g2d.setPaint(basePaint);
        g2d.fillRect(0, 0, tileSize, tileSize);
    }

    private void drawInner(Graphics2D g2d, boolean hasTop, boolean hasBottom, boolean hasLeft, boolean hasRight) {
        int borderThickness = Math.max(3, tileSize / 9);
        int accentThickness = Math.max(3, tileSize / 8);
        int cornerDiameter = borderThickness * 2;

        int innerWidth = Math.max(0, tileSize - borderThickness * 2);
        int innerHeight = Math.max(0, tileSize - borderThickness * 2);

        if (innerWidth <= 0 || innerHeight <= 0) return; // Nothing to draw

        Color innerHighlight = new Color(255, 220, 130);
        Color innerShadow = new Color(120, 90, 40);
        Color accentBright = new Color(255, 210, 100);

        // Main inner panel
        GradientPaint innerPaint = new GradientPaint(0, borderThickness, innerShadow, 0, tileSize - borderThickness, innerHighlight);
        g2d.setPaint(innerPaint);
        g2d.fillRoundRect(borderThickness, borderThickness, innerWidth, innerHeight, cornerDiameter, cornerDiameter);

        // Overlay
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g2d.setPaint(new GradientPaint(0, tileSize / 4f, accentBright, 0, tileSize * 3 / 4f, innerShadow));
        g2d.fillRoundRect(borderThickness, borderThickness, innerWidth, innerHeight, cornerDiameter, cornerDiameter);
        g2d.setComposite(AlphaComposite.SrcOver);

        // Highlight stroke
        g2d.setColor(new Color(255, 217, 89));
        g2d.setStroke(new BasicStroke(Math.max(1, tileSize / 32f)));
        g2d.drawRoundRect(borderThickness, borderThickness, innerWidth, innerHeight, cornerDiameter, cornerDiameter);

        // Vertical lines
        int accentLineWidth = Math.max(1, tileSize / 18);
        g2d.setColor(new Color(1, 8, 1));
        g2d.fillRect(tileSize / 3 - accentLineWidth / 2, borderThickness + accentThickness, accentLineWidth, innerHeight - accentThickness * 2);
        g2d.fillRect(tileSize * 2 / 3 - accentLineWidth / 2, borderThickness + accentThickness, accentLineWidth, innerHeight - accentThickness * 2);
    }

    private void drawGenericBorder(Graphics2D g2d, String side) {
        int accentThickness = Math.max(3, tileSize / 8);
        int thickness = Math.max(2, accentThickness / 3);
        g2d.setColor(new Color(44, 16, 94));

        switch(side) {
            case "TOP":    g2d.fillRect(0, 0, tileSize, thickness); break;
            case "BOTTOM": g2d.fillRect(0, tileSize - thickness, tileSize, thickness); break;
            case "LEFT":   g2d.fillRect(0, 0, thickness, tileSize); break;
            case "RIGHT":  g2d.fillRect(tileSize - thickness, 0, thickness, tileSize); break;
        }
    }

    private void drawTopBorder(Graphics2D g2d) {
        int accentThickness = Math.max(3, tileSize / 8);
        Color accentBright = new Color(255, 210, 100);
        Color accentDark = new Color(180, 130, 50);
        Color accentHighlight = new Color(255, 235, 180);

        g2d.setPaint(new GradientPaint(0, 0, accentBright, 0, accentThickness, accentDark));
        g2d.fillRect(0, 0, tileSize, accentThickness);

        int segmentWidth = Math.max(3, tileSize / 6);
        int gap = Math.max(2, segmentWidth / 2);
        int yOffset = Math.max(1, accentThickness / 3);
        for (int x = 0; x < tileSize; x += segmentWidth + gap) {
            int width = Math.min(segmentWidth, tileSize - x);
            g2d.setColor(accentHighlight);
            g2d.fillRect(x, yOffset, width, Math.max(1, accentThickness / 3));
            g2d.setColor(accentDark);
            g2d.drawLine(x, accentThickness - 1, x + width, accentThickness - 1);
        }
    }

    private void drawBottomBorder(Graphics2D g2d) {
        int accentThickness = Math.max(3, tileSize / 8);
        Color accentBright = new Color(255, 210, 100);
        Color accentDark = new Color(180, 130, 50);
        Color accentHighlight = new Color(255, 235, 180);

        g2d.setPaint(new GradientPaint(0, tileSize - accentThickness, accentDark, 0, tileSize, accentBright));
        g2d.fillRect(0, tileSize - accentThickness, tileSize, accentThickness);

        int segmentWidth = Math.max(3, tileSize / 6);
        int gap = Math.max(2, segmentWidth / 2);
        int yOffset = tileSize - accentThickness + Math.max(1, accentThickness / 4);
        for (int x = 0; x < tileSize; x += segmentWidth + gap) {
            int width = Math.min(segmentWidth, tileSize - x);
            g2d.setColor(accentHighlight);
            g2d.fillRect(x, yOffset, width, Math.max(1, accentThickness / 3));
            g2d.setColor(accentDark.darker());
            g2d.drawLine(x, tileSize - 1, x + width, tileSize - 1);
        }
    }

    private void drawLeftBorder(Graphics2D g2d) {
        int accentThickness = Math.max(3, tileSize / 8);
        Color accentBright = new Color(255, 210, 100);
        Color accentDark = new Color(180, 130, 50);
        Color accentHighlight = new Color(255, 235, 180);

        g2d.setPaint(new GradientPaint(0, 0, accentBright, accentThickness, 0, accentDark));
        g2d.fillRect(0, 0, accentThickness, tileSize);

        int segmentHeight = Math.max(3, tileSize / 6);
        int gap = Math.max(2, segmentHeight / 2);
        int xOffset = Math.max(1, accentThickness / 3);
        for (int y = 0; y < tileSize; y += segmentHeight + gap) {
            int height = Math.min(segmentHeight, tileSize - y);
            g2d.setColor(accentHighlight);
            g2d.fillRect(xOffset, y, Math.max(1, accentThickness / 3), height);
            g2d.setColor(accentDark);
            g2d.drawLine(accentThickness - 1, y, accentThickness - 1, y + height);
        }
    }

    private void drawRightBorder(Graphics2D g2d) {
        int accentThickness = Math.max(3, tileSize / 8);
        Color accentBright = new Color(255, 210, 100);
        Color accentDark = new Color(180, 130, 50);
        Color accentHighlight = new Color(255, 235, 180);

        g2d.setPaint(new GradientPaint(tileSize - accentThickness, 0, accentDark, tileSize, 0, accentBright));
        g2d.fillRect(tileSize - accentThickness, 0, accentThickness, tileSize);

        int segmentHeight = Math.max(3, tileSize / 6);
        int gap = Math.max(2, segmentHeight / 2);
        int xOffset = tileSize - accentThickness + Math.max(1, accentThickness / 4);
        for (int y = 0; y < tileSize; y += segmentHeight + gap) {
            int height = Math.min(segmentHeight, tileSize - y);
            g2d.setColor(accentHighlight);
            g2d.fillRect(xOffset, y, Math.max(1, accentThickness / 3), height);
            g2d.setColor(accentDark.darker());
            g2d.drawLine(tileSize - 1, y, tileSize - 1, y + height);
        }
    }
}

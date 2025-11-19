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
        // Draw background
        g.drawImage(assetManager.getBackgroundImage(), 0, 0, panel.getWidth(), panel.getHeight(), null);

        // Draw static entities
        for (Entity wall : state.walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }
        for (Entity food : state.foods) {
            g.drawImage(food.image, food.x, food.y, food.width, food.height, null);
        }
        for (Entity knife : state.knives) {
            g.drawImage(knife.image, knife.x, knife.y, knife.width, knife.height, null);
        }

        // Draw actors
        for (Actor ghost : state.ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }
        if (state.boss != null) {
            gm.drawImage(state.boss.image, state.boss.x, state.boss.y, state.boss.width, state.boss.height, null);
        }
        for (Actor proj : state.projectiles) {
            gm.drawImage(proj.image, proj.x, proj.y, proj.width, proj.height, null);
        }
        if (state.pacman != null) {
            g.drawImage(state.pacman.image, state.pacman.x, state.pacman.y, state.pacman.width, state.pacman.height, null);
        }

        // Draw Score/HUD
        drawHUD(g, state);
    }

    /**
     * Draws the heads-up display (score, lives, etc.).
     * Draws Game Over, Game Win and inter-level banner
     */

    private void drawHUD(Graphics g, PacMan.GameState state) {
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.setColor(Color.WHITE);
        String text;
        boardWidth = tileSize * gameMap.getColumnCount();
        boardHeight = tileSize * gameMap.getRowCount();

        // Draws Game Over Bannner
        if (state.gameOver) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRect(0, 0, boardWidth, boardHeight);

            String title = "GAME OVER";
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 56f));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (boardWidth - fm.stringWidth(title)) / 2;
            int ty = boardHeight / 2 - fm.getHeight();
            g2.setColor(Color.WHITE);
            g2.drawString(title, tx, ty);

            // Subtext
            String sub = "Press any key to restart";
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28f));
            FontMetrics fm2 = g2.getFontMetrics();
            int sx = (boardWidth - fm2.stringWidth(sub)) / 2;
            int sy = ty + fm.getHeight() + 40;
            g2.drawString(sub, sx, sy);
            g2.dispose();
            return;
        }

        // Draws Game Won Banner
        if (state.gameWon) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRect(0, 0, boardWidth, boardHeight);

            String title = "YOU WIN!";
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 56f));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (boardWidth - fm.stringWidth(title)) / 2;
            int ty = boardHeight / 2 - fm.getHeight();
            g2.setColor(Color.WHITE);
            g2.drawString(title, tx, ty);

            String sub = "Press any key to restart";
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28f));
            FontMetrics fm2 = g2.getFontMetrics();
            int sx = (boardWidth - fm2.stringWidth(sub)) / 2;
            int sy = ty + fm.getHeight() + 40;
            g2.drawString(sub, sx, sy);
            g2.dispose();
            return;
        }

        if (state.interLevel) {
            // Dim background
            Graphics2D g2d = (Graphics2D) g;
            Composite old = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2d.setColor(Color.black);
            g2d.fillRect(0, 0, tileSize * gameMap.getColumnCount(), tileSize * gameMap.getRowCount());
            g2d.setComposite(old);

            // Banner “LEVEL X”
            String banner = "LEVEL " + state.nextLevelToStart;
            Font oldFont = g.getFont();
            g.setFont(oldFont.deriveFont(Font.BOLD, 56f));
            FontMetrics fm = g.getFontMetrics();
            int x = (tileSize * gameMap.getColumnCount() - fm.stringWidth(banner)) / 2;
            int y = (tileSize * gameMap.getRowCount()) / 2;

            g.setColor(Color.WHITE);
            g.drawString(banner, x, y);
            g.setFont(oldFont);
            return; // Don’t draw the normal HUD underneath
        }

        if (state.gameWon) {
            text = "You Win! Final Score: " + state.score;
        } else if (state.gameOver) {
            text = "Game Over: " + state.score;
        } else {
            text = String.format("Level: %d Lives: x%d Score: %d Knives: %d",
                    state.currentLevel, state.lives, state.score, state.knifeCount);
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
    //  The "Bloody hell" method is now broken into smaller parts.
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
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

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

    public Renderer(AssetManager assetManager, GameMap gameMap, int tileSize) {
        this.assetManager = assetManager;
        this.gameMap = gameMap;
        this.tileSize = tileSize;

        // 1. Calculate dimensions here in the constructor
        this.boardWidth  = tileSize * gameMap.getColumnCount();
        this.boardHeight = tileSize * gameMap.getRowCount();
        this.topBarH     = Math.max(32, tileSize);
        this.bottomBarH  = Math.max(40, (int)(tileSize * 1.2));
        this.totalH      = topBarH + boardHeight + bottomBarH;
    }

    public void drawGame(Graphics g, JPanel panel, PacMan.GameState state) {
        // 1. Draw Backgrounds (uses pre-calculated fields)
        g.drawImage(assetManager.getBackgroundImage(), 0, 0, boardWidth, totalH, null);
        drawBarBackgrounds(g);

        // 2. Draw Game Entities
        Graphics2D gm = (Graphics2D) g.create();
        gm.translate(0, topBarH); // Use the field
        drawEntities(gm, state);
        gm.dispose();

        // 3. Draw Animations
        if (state.animations != null && !state.animations.isEmpty()) {
            Graphics2D gAnim = (Graphics2D) g.create();
            gAnim.translate(0, topBarH);
            for (DeathAnimation da : state.animations) {
                da.render(gAnim);
            }
            gAnim.dispose();
        }

        // 4. Draw HUD
        drawHUD(g, state);
    }

    private void drawBarBackgrounds(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, boardWidth, topBarH);
        g2.fillRect(0, topBarH + boardHeight, boardWidth, bottomBarH);
        g2.dispose();
    }

    private void drawEntities(Graphics2D g2d, PacMan.GameState state) {
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

    private void drawHUD(Graphics g, PacMan.GameState state) {
        int pad = Math.max(8, tileSize / 6);

        // Overlays (Game Over / Level Up)
        if (state.gameOver || state.gameWon || state.interLevel) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRect(0, 0, boardWidth, totalH);

            String title = state.gameOver ? "GAME OVER" : (state.gameWon ? "YOU WIN!" : ("Level " + (state.nextLevelToStart > 0 ? state.nextLevelToStart : state.currentLevel + 1)));
            float titleSize = 56f;

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
            if (!state.interLevel) return;
        }

        Graphics2D g2 = (Graphics2D) g.create();

        // Score
        g2.setFont(new Font("Arial", Font.BOLD, Math.max(18, tileSize / 2)));
        String scoreText = String.valueOf(state.score);
        FontMetrics fm = g2.getFontMetrics();
        int ty = (topBarH - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(Color.WHITE);
        g2.drawString(scoreText, pad, ty);

        // Level
        String levelText = "Level " + state.currentLevel;
        int tx = boardWidth - pad - fm.stringWidth(levelText);
        g2.drawString(levelText, tx, ty);

        // Bottom Bar Icons
        int iconH = (int) (bottomBarH * 0.8);
        int gap   = Math.max(6, tileSize / 6);
        int baseY = topBarH + boardHeight + (bottomBarH - iconH) / 2;

        // Lives
        Image lifeIcon = assetManager.getPacmanRightImage();
        int count = Math.max(0, state.lives);
        int x = pad;
        for (int i = 0; i < count; i++) {
            if (lifeIcon != null) g2.drawImage(lifeIcon, x, baseY, iconH, iconH, null);
            x += iconH + gap;
        }

        // Knives
        Image knifeIcon = assetManager.getKnifeImage();
        int kCount = Math.max(0, state.knifeCount);
        int kx = boardWidth - pad - iconH;
        for (int i = 0; i < kCount; i++) {
            if (knifeIcon != null) g2.drawImage(knifeIcon, kx, baseY, iconH, iconH, null);
            kx -= iconH + gap;
        }

        // Boss HP
        if (state.boss != null) {
            g2.setFont(new Font("Arial", Font.BOLD, Math.max(16, tileSize / 2 - 2)));
            String bossText = "BOSS: " + state.boss.getLives();
            FontMetrics bfm = g2.getFontMetrics();
            int btx = (boardWidth - bfm.stringWidth(bossText)) / 2;
            int bty = baseY + (iconH - bfm.getHeight()) / 2 + bfm.getAscent();
            g2.setColor(Color.RED);
            g2.drawString(bossText, btx, bty);
        }

        g2.dispose();
    }

    // -----------------------------------------------------------------
    //  WALL TEXTURE LOGIC
    // -----------------------------------------------------------------

    public Image createWallTexture(boolean[][] wallMatrix, int row, int column) {
        BufferedImage texture = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = texture.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean hasTop    = row > 0 && wallMatrix[row - 1][column];
        boolean hasBottom = row < gameMap.getRowCount() - 1 && wallMatrix[row + 1][column];
        boolean hasLeft   = column > 0 && wallMatrix[row][column - 1];
        boolean hasRight  = column < gameMap.getColumnCount() - 1 && wallMatrix[row][column + 1];

        drawBase(g2d);
        drawInner(g2d);

        Stroke originalStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(Math.max(1f, ((float) tileSize / 8) / 3f)));

        if (!hasTop)    drawHorizontalDetailBorder(g2d, true);
        else            drawGenericBorder(g2d, "TOP");

        if (!hasBottom) drawHorizontalDetailBorder(g2d, false);
        else            drawGenericBorder(g2d, "BOTTOM");

        if (!hasLeft)   drawVerticalDetailBorder(g2d, true);
        else            drawGenericBorder(g2d, "LEFT");

        if (!hasRight)  drawVerticalDetailBorder(g2d, false);
        else            drawGenericBorder(g2d, "RIGHT");

        g2d.setStroke(originalStroke);
        g2d.dispose();
        return texture;
    }

    private void drawBase(Graphics2D g2d) {
        if (assetManager.getWallImage() != null) {
            g2d.drawImage(assetManager.getWallImage(), 0, 0, tileSize, tileSize, null);
        }
        g2d.setPaint(new GradientPaint(0, 0, new Color(70, 50, 20), tileSize, tileSize, new Color(235, 190, 90)));
        g2d.fillRect(0, 0, tileSize, tileSize);
    }

    private void drawInner(Graphics2D g2d) {
        int borderT = Math.max(3, tileSize / 9);
        int innerW = Math.max(0, tileSize - borderT * 2);
        if (innerW == 0) return;
        g2d.setPaint(new GradientPaint(0, borderT, new Color(120, 90, 40), 0, tileSize - borderT, new Color(255, 220, 130)));
        g2d.fillRoundRect(borderT, borderT, innerW, innerW, borderT * 2, borderT * 2);
        g2d.setColor(new Color(255, 217, 89));
        g2d.setStroke(new BasicStroke(Math.max(1, tileSize / 32f)));
        g2d.drawRoundRect(borderT, borderT, innerW, innerW, borderT * 2, borderT * 2);
    }

    private void drawGenericBorder(Graphics2D g2d, String side) {
        int th = Math.max(2, Math.max(3, tileSize / 8) / 3);
        g2d.setColor(new Color(44, 16, 94));
        if (side.equals("TOP"))    g2d.fillRect(0, 0, tileSize, th);
        if (side.equals("BOTTOM")) g2d.fillRect(0, tileSize - th, tileSize, th);
        if (side.equals("LEFT"))   g2d.fillRect(0, 0, th, tileSize);
        if (side.equals("RIGHT"))  g2d.fillRect(tileSize - th, 0, th, tileSize);
    }

    private void drawHorizontalDetailBorder(Graphics2D g2d, boolean isTop) {
        int accentTh = Math.max(3, tileSize / 8);
        Color bright = new Color(255, 210, 100);
        Color dark   = new Color(180, 130, 50);
        int y = isTop ? 0 : tileSize - accentTh;
        g2d.setPaint(new GradientPaint(0, isTop ? 0 : y, isTop ? bright : dark, 0, isTop ? accentTh : tileSize, isTop ? dark : bright));
        g2d.fillRect(0, y, tileSize, accentTh);
        drawBorderSegments(g2d, accentTh, true, isTop);
    }

    private void drawVerticalDetailBorder(Graphics2D g2d, boolean isLeft) {
        int accentTh = Math.max(3, tileSize / 8);
        Color bright = new Color(255, 210, 100);
        Color dark   = new Color(180, 130, 50);
        int x = isLeft ? 0 : tileSize - accentTh;
        g2d.setPaint(new GradientPaint(isLeft ? 0 : x, 0, isLeft ? bright : dark, isLeft ? accentTh : tileSize, 0, isLeft ? dark : bright));
        g2d.fillRect(x, 0, accentTh, tileSize);
        drawBorderSegments(g2d, accentTh, false, isLeft);
    }

    private void drawBorderSegments(Graphics2D g2d, int accentTh, boolean isHorizontal, boolean isStart) {
        int segW = Math.max(3, tileSize / 6);
        int gap = Math.max(2, segW / 2);
        Color highlight = new Color(255, 235, 180);
        Color lineCol = new Color(180, 130, 50).darker();
        if (isStart) lineCol = new Color(180, 130, 50);

        for (int i = 0; i < tileSize; i += segW + gap) {
            int len = Math.min(segW, tileSize - i);
            int offsetPos = isStart ? Math.max(1, accentTh / 3) : (tileSize - accentTh) + Math.max(1, accentTh / 4);
            int linePos = isStart ? accentTh - 1 : tileSize - 1;

            g2d.setColor(highlight);
            if (isHorizontal) g2d.fillRect(i, offsetPos, len, Math.max(1, accentTh / 3));
            else              g2d.fillRect(offsetPos, i, Math.max(1, accentTh / 3), len);

            g2d.setColor(lineCol);
            if (isHorizontal) g2d.drawLine(i, linePos, i + len, linePos);
            else              g2d.drawLine(linePos, i, linePos, i + len);
        }
    }
}
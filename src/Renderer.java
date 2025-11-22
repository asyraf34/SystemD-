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

    public void drawGame(Graphics g, JPanel panel, GameState state) {
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

        // Sprint Meter
        drawSprintMeter(g2, state, baseY, iconH, gap);

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

    private void drawSprintMeter(Graphics2D g2, GameState state, int baseY, int iconH, int gap) {
        int meterHeight = Math.max(iconH / 2, (int) (iconH * 0.6));
        int meterWidth = Math.max(tileSize * 6, boardWidth / 2);
        int mx = (boardWidth - meterWidth) / 2;
        int my = baseY + (iconH - meterHeight) / 2;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background and border
        g2.setColor(new Color(255, 255, 255, 50));
        g2.fillRoundRect(mx, my, meterWidth, meterHeight, 12, 12);
        g2.setColor(new Color(255, 255, 255, 140));
        g2.drawRoundRect(mx, my, meterWidth, meterHeight, 12, 12);

        // Determine status
        boolean onCooldown = state.sprintCooldownTicks > 0;
        boolean active = state.sprintActive;
        float fillRatio;
        Color fillColor;
        String label;

        if (active) {
            fillRatio = Math.max(0f, Math.min(1f, (float) state.sprintTicksRemaining / GameConstants.TIMER_SPRINT_DURATION));
            fillColor = new Color(255, 200, 0);
            label = "Sprinting";
        } else if (onCooldown) {
            float cooldownRatio = 1f - (float) state.sprintCooldownTicks / GameConstants.TIMER_SPRINT_COOLDOWN;
            fillRatio = Math.max(0f, Math.min(1f, cooldownRatio));
            fillColor = new Color(180, 60, 60);
            label = "Cooldown";
        } else {
            fillRatio = 1f;
            fillColor = new Color(60, 180, 90);
            label = "Sprint";
        }

        int fillWidth = (int) (fillRatio * (meterWidth - 4));
        g2.setColor(fillColor);
        g2.fillRoundRect(mx + 2, my + 2, fillWidth, meterHeight - 4, 10, 10);

        // Label
        g2.setFont(new Font("Arial", Font.BOLD, Math.max(12, tileSize / 3)));
        FontMetrics fm = g2.getFontMetrics();
        int textX = mx + (meterWidth - fm.stringWidth(label)) / 2;
        int textY = my + (meterHeight - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(Color.BLACK);
        g2.drawString(label, textX + 1, textY + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(label, textX, textY);
    }

    // -----------------------------------------------------------------
    //  WALL TEXTURE LOGIC
    // -----------------------------------------------------------------

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

}
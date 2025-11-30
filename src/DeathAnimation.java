/* DeathAnimation.java
 * - fades + shrinks a snapshot of the killed actor image
 * - spawns particles that manage their own physics
 * - shows a floating score popup*/
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class DeathAnimation {

    // =============================================================
    // 1. TUNING & CONFIGURATION
    // =============================================================

    // Animation
    private static final float ANIMATION_SHRINK_SCALE = 0.6f;

    // Particles
    private static final int   PARTICLE_COUNT_MIN     = 5;
    private static final float PARTICLE_GRAVITY       = 0.12f;
    private static final float PARTICLE_FADE_SPEED    = 0.04f;
    private static final float PARTICLE_SPEED_BASE    = 0.8f;
    private static final float PARTICLE_SPEED_RANDOM  = 2.5f;
    private static final float PARTICLE_SPREAD        = 0.4f;

    // Popup Text
    private static final int   POPUP_FLOAT_HEIGHT     = 28;
    private static final Color POPUP_COLOR_MAIN       = Color.YELLOW;
    private static final Color POPUP_COLOR_SHADOW     = Color.BLACK;

    private static final Random RAND = new Random();

    // =============================================================
    // 2. FIELDS
    // =============================================================

    private final Image actorImage;
    private final int x, y, width, height;
    private final int totalTicks;
    private final String popupText;
    private final Color popupColor;

    private int ticksRemaining;
    private int popupTicksRemaining;
    // Using concrete ArrayList for older Java compiler safety
    private final ArrayList<Particle> particles = new ArrayList<>();
    // =============================================================
    // 3. INNER CLASS: PARTICLE
    // =============================================================

    private static class Particle {
        float px, py;
        float vx, vy;
        float life;
        Color color;

        Particle(float x, float y, float vx, float vy, Color color) {
            this.px = x; this.py = y;
            this.vx = vx; this.vy = vy;
            this.life = 1.0f;
            this.color = color;
        }

        boolean update() {
            px += vx;
            py += vy;
            vy += PARTICLE_GRAVITY;
            life -= PARTICLE_FADE_SPEED;
            return life > 0;
        }

        void render(Graphics2D g2, float globalAlpha) {
            if (life <= 0) return;
            float combinedAlpha = Math.max(0f, life * globalAlpha);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, combinedAlpha));
            g2.setColor(color);
            int size = Math.max(2, Math.round(6 * life));
            g2.fillOval((int)(px - size/2f), (int)(py - size/2f), size, size);
        }
    }

    // =============================================================
    // 4. CONSTRUCTOR
    // =============================================================

    public DeathAnimation(Image image, int x, int y, int w, int h, int totalTicks, String scoreText, Color textColor) {
        this.actorImage = image;
        this.x = x;
        this.y = y;
        this.width = Math.max(1, w);
        this.height = Math.max(1, h);
        this.totalTicks = Math.max(1, totalTicks);
        this.popupText = scoreText;
        this.popupColor = textColor;

        this.ticksRemaining = this.totalTicks;
        this.popupTicksRemaining = this.totalTicks;

        spawnParticles();
    }

    private void spawnParticles() {
        int count = Math.max(PARTICLE_COUNT_MIN, totalTicks / 2);
        float centerX = x + width / 2f;
        float centerY = y + height / 2f;

        for (int i = 0; i < count; i++) {
            double angle = RAND.nextDouble() * Math.PI * 2.0;
            double speed = PARTICLE_SPEED_BASE + RAND.nextDouble() * PARTICLE_SPEED_RANDOM;

            float vx = (float) (Math.cos(angle) * speed);
            float vy = (float) (Math.sin(angle) * speed - (0.5 + RAND.nextDouble()));

            float px = centerX + (RAND.nextFloat() - 0.5f) * width * PARTICLE_SPREAD;
            float py = centerY + (RAND.nextFloat() - 0.5f) * height * PARTICLE_SPREAD;

            Color c = new Color(255, 230 - RAND.nextInt(80), 60 + RAND.nextInt(120));
            particles.add(new Particle(px, py, vx, vy, c));
        }
    }

    // =============================================================
    // 5. UPDATE
    // =============================================================

    public boolean tick() {
        boolean isAlive = false;

        // Update timers
        if (ticksRemaining > 0) {
            ticksRemaining--;
            isAlive = true;
        }
        if (popupTicksRemaining > 0) {
            popupTicksRemaining--;
            isAlive = true;
        }

        // Update Particles using Iterator (Safe for Java 7)
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            if (p.update()) {
                isAlive = true; // At least one particle is still alive
            } else {
                it.remove();    // Remove dead particle
            }
        }

        return isAlive;
    }

    // =============================================================
    // 6. RENDER
    // =============================================================

    public void render(Graphics2D g2) {
        AffineTransform oldTx = g2.getTransform();
        Composite oldComp = g2.getComposite();

        float progress = 1f - (float)ticksRemaining / totalTicks;
        float alpha = Math.max(0f, 1f - progress);
        float scale = 1f - (ANIMATION_SHRINK_SCALE * progress);

        try {
            drawActor(g2, alpha, scale);
            drawParticles(g2, alpha);
            drawPopup(g2);
        } finally {
            g2.setTransform(oldTx);
            g2.setComposite(oldComp);
        }
    }

    private void drawActor(Graphics2D g2, float alpha, float scale) {
        int cx = x + width / 2;
        int cy = y + height / 2;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (actorImage != null) {
            AffineTransform at = new AffineTransform();
            at.translate(cx, cy);
            at.scale(scale, scale);
            at.translate(-width / 2.0, -height / 2.0);
            g2.drawImage(actorImage, at, null);
        } else {

            g2.setColor(new Color(255, 255, 255, 100));
            int size = Math.max(4, Math.round(width * scale));
            g2.fillOval(cx - size/2, cy - size/2, size, size);

            g2.setColor(Color.WHITE);
            g2.drawOval(cx - size/2, cy - size/2, size, size);
        }
    }

    private void drawParticles(Graphics2D g2, float alpha) {
        for (Particle p : particles) {
            p.render(g2, alpha);
        }
    }

    private void drawPopup(Graphics2D g2) {
        if (popupText == null || popupTicksRemaining <= 0) return;

        float progress = 1f - (float)popupTicksRemaining / totalTicks;
        float alpha = Math.max(0f, 1f - progress);
        int yOffset = (int)(-POPUP_FLOAT_HEIGHT * progress);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setFont(new Font("Arial", Font.BOLD, Math.max(12, height / 3)));

        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (width - fm.stringWidth(popupText)) / 2;
        int ty = y + height / 2 + yOffset;

        g2.setColor(POPUP_COLOR_SHADOW);
        g2.drawString(popupText, tx + 1, ty + 1);
        g2.setColor(popupColor);
        g2.drawString(popupText, tx, ty);
    }
}
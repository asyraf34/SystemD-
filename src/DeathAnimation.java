/*
 * DeathAnimation.java
 *
 * A small, self-contained procedural death animation.
 * - fades + shrinks a snapshot of the killed actor image
 * - spawns a handful of particle dots that fly out and fade
 * - shows a floating score popup
 *
 * Drop this file into src/ (same package as your other classes).
 */
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeathAnimation {
    private final Image image;
    private final int baseX;
    private final int baseY;
    private final int baseW;
    private final int baseH;
    private int ticksRemaining;
    private final int totalTicks;

    private final List<Particle> particles = new ArrayList<>();
    private final String popupText;
    private int popupTicksRemaining;

    private static final Random RAND = new Random();

    private static class Particle {
        float x, y;
        float vx, vy;
        float life; // 0..1
        Color color;
        Particle(float x, float y, float vx, float vy, Color color) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.life = 1f; this.color = color;
        }
    }

    /**
     * @param image     snapshot of actor image at kill time (may be null)
     * @param x         screen x
     * @param y         screen y
     * @param w         width
     * @param h         height
     * @param totalTicks lifetime in ticks (1 tick = Timer interval, default 50ms)
     * @param scoreText floating popup text (e.g. "+100")
     */
    public DeathAnimation(Image image, int x, int y, int w, int h, int totalTicks, String scoreText) {
        this.image = image;
        this.baseX = x;
        this.baseY = y;
        this.baseW = Math.max(1, w);
        this.baseH = Math.max(1, h);
        this.totalTicks = Math.max(1, totalTicks);
        this.ticksRemaining = this.totalTicks;
        this.popupText = scoreText;
        this.popupTicksRemaining = this.totalTicks;

        // spawn a few particles
        int particleCount = Math.max(4, this.totalTicks / 2);
        for (int i = 0; i < particleCount; i++) {
            double dir = RAND.nextDouble() * Math.PI * 2.0;
            double speed = 0.8 + RAND.nextDouble() * 2.0;
            float vx = (float) (Math.cos(dir) * speed);
            float vy = (float) (Math.sin(dir) * speed - (0.5 + RAND.nextDouble()));
            Color c = new Color(255, 230 - RAND.nextInt(80), 60 + RAND.nextInt(120));
            float px = x + w/2f + (RAND.nextFloat() - 0.5f) * w * 0.4f;
            float py = y + h/2f + (RAND.nextFloat() - 0.5f) * h * 0.4f;
            particles.add(new Particle(px, py, vx, vy, c));
        }
    }

    /** Update one tick. Returns true while animation is still active. */
    public boolean tick() {
        if (ticksRemaining <= 0 && !anyParticleAlive()) return false;

        // update particles
        for (Particle p : particles) {
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 0.12f;        // gravity-ish
            p.life -= 0.04f;     // fade
            if (p.life < 0f) p.life = 0f;
        }

        // popup rises (handled in render)
        popupTicksRemaining = Math.max(0, popupTicksRemaining - 1);

        ticksRemaining = Math.max(0, ticksRemaining - 1);
        return ticksRemaining > 0 || anyParticleAlive() || popupTicksRemaining > 0;
    }

    private boolean anyParticleAlive() {
        for (Particle p : particles) if (p.life > 0f) return true;
        return false;
    }

    /** Render the animation onto the given Graphics2D */
    public void render(Graphics2D g2) {
        float progress = 1f - (float)ticksRemaining / (float)totalTicks;
        float alpha = Math.max(0f, 1f - progress); // fade out
        float scale = 1f - 0.6f * progress; // shrink to ~40%

        // Save state
        AffineTransform oldTx = g2.getTransform();
        Composite oldComp = g2.getComposite();
        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();
        Font oldFont = g2.getFont();

        int cx = baseX + baseW/2;
        int cy = baseY + baseH/2;

        // If image is present, draw scaled & faded image centered on baseX/baseY.
        if (image != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            AffineTransform at = new AffineTransform();
            at.translate(cx, cy);
            at.scale(scale, scale);
            at.translate(-baseW/2.0, -baseH/2.0);
            g2.drawImage(image, at, null);
        } else {
            // fallback: draw a visible magenta box so it's clear something happened
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha + 0.2f)));
            g2.setColor(Color.MAGENTA);
            int w = Math.max(4, (int)Math.round(baseW * scale));
            int h = Math.max(4, (int)Math.round(baseH * scale));
            g2.fillRect(cx - w/2, cy - h/2, w, h);
        }

        // particles (draw on top)
        for (Particle p : particles) {
            if (p.life <= 0f) continue;
            float pa = p.life * alpha;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pa));
            g2.setColor(p.color);
            int size = Math.max(2, (int)(Math.round(6 * p.life)));
            g2.fillOval((int)(p.x - size/2f), (int)(p.y - size/2f), size, size);
        }

        // popup text; floats upward slightly and fades
        if (popupText != null && popupTicksRemaining > 0) {
            float popupProgress = 1f - (float)popupTicksRemaining / (float)totalTicks;
            float popupAlpha = Math.max(0f, 1f - popupProgress);
            int yOffset = (int)(-24 * popupProgress); // move up to ~24px
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, popupAlpha));
            g2.setFont(new Font("Arial", Font.BOLD, Math.max(12, baseH/3)));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (int)(baseX + baseW/2f - fm.stringWidth(popupText)/2f);
            int ty = (int)(baseY + baseH/2f + yOffset);
            g2.setColor(Color.BLACK);
            g2.drawString(popupText, tx+1, ty+1); // shadow
            g2.setColor(Color.YELLOW);
            g2.drawString(popupText, tx, ty);
        }

        // Restore
        g2.setTransform(oldTx);
        g2.setComposite(oldComp);
        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
        g2.setFont(oldFont);
    }
}
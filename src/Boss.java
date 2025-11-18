import java.awt.Image;
import java.util.Random;

/**
 * Represents a Boss enemy. Extends Actor to be a movable entity.
 * It manages its own state, including lives and attack modes (normal and reflect).
 * The main game loop should call updateAI() and performLongRangeAttack().
 */
public class Boss extends Actor {

    // --- Boss State ---
    private int lives;
    public static final int STARTING_LIVES = 3;

    private boolean isReflecting;
    private int modeTimer; // Timer to switch modes
    private int attackCooldown; // Timer for long-range attacks

    private Random random = new Random();

    // --- Mode Timings (in game ticks) ---
    // At 20fps (50ms timer), 200 ticks = 10 seconds
    private static final int NORMAL_MODE_DURATION = 200;
    private static final int REFLECT_MODE_DURATION = 100; // 5 seconds
    private static final int ATTACK_COOLDOWN_TICKS = 40; // 2 seconds

    /**
     * Constructor for the Boss.
     * @param image The starting image for the boss.
     * @param x Starting X position.
     * @param y Starting Y position.
     * @param width Entity width.
     * @param height Entity height.
     * @param speed Movement speed.
     */
    public Boss(Image image, int x, int y, int width, int height, int speed) {
        super(image, x, y, width, height, speed);
        this.lives = STARTING_LIVES;
        this.isReflecting = false;
        this.modeTimer = NORMAL_MODE_DURATION;
        this.attackCooldown = 0;
    }

    // --- Public State Getters ---

    /**
     * @return The current number of lives the boss has.
     */
    public int getLives() {
        return lives;
    }

    /**
     * @return true if the boss is currently in its "reflect damage" mode, false otherwise.
     */
    public boolean isReflecting() {
        return isReflecting;
    }

    // --- Core Logic Methods ---

    /**
     * Updates the boss's internal state, timers, and modes.
     * This should be called once per game tick from the main game loop (e.g., in PacMan.updateGame()).
     */
    public void updateAI() {
        updateMode();
        updateAttackCooldown();
    }

    /**
     * Ticks down the mode timer and switches between normal and reflect modes.
     */
    private void updateMode() {
        modeTimer--;
        if (modeTimer <= 0) {
            isReflecting = !isReflecting;

            if (isReflecting) {
                this.modeTimer = REFLECT_MODE_DURATION;
                // You would change the boss's image here to give a visual cue
                // (This logic should be in PacMan.java to access AssetManager)
            } else {
                this.modeTimer = NORMAL_MODE_DURATION;
                // Change image back to normal here
            }
        }
    }

    /**
     * Ticks down the attack cooldown timer.
     */
    private void updateAttackCooldown() {
        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }

    /**
     * The boss takes one unit of damage.
     * If in reflect mode, it reflects the damage (logic must be handled by CollisionManager).
     *
     * @return true if the boss is still alive, false if it is defeated.
     */
    public boolean takeDamage() {
        if (isReflecting) {
            return true; // Damage is reflected, boss is unharmed
        }

        this.lives--;
        return this.lives > 0;
    }

    /**
     * Attempts to perform a long-range attack.
     * This method creates a new projectile (as an Actor) that the main game must
     * add to a list to be rendered and updated.
     *
     * @param target The PacMan actor to fire at.
     * @param projectileImage The image to use for the projectile (from AssetManager).
     * @return A new Actor (projectile) if fired, otherwise null.
     */
    public Actor performLongRangeAttack(Actor target, Image projectileImage) {
        // Can only attack in normal mode and if cooldown is ready
        if (isReflecting || attackCooldown > 0) {
            return null;
        }

        // Reset cooldown
        this.attackCooldown = ATTACK_COOLDOWN_TICKS;

        // Create the projectile
        int projSpeed = this.speed + 2; // Projectile is slightly faster than the boss
        int projSize = 16; // Example size, you can adjust this

        // Start projectile from boss center
        int startX = this.x + this.width / 2 - projSize / 2;
        int startY = this.y + this.height / 2 - projSize / 2;

        Actor projectile = new Actor(projectileImage, startX, startY, projSize, projSize, projSpeed);

        // Calculate direction vector towards target's center
        double targetCenterX = target.x + target.width / 2.0;
        double targetCenterY = target.y + target.height / 2.0;
        double startCenterX = this.x + this.width / 2.0;
        double startCenterY = this.y + this.height / 2.0;

        double dx = targetCenterX - startCenterX;
        double dy = targetCenterY - startCenterY;
        double magnitude = Math.sqrt(dx * dx + dy * dy);

        if (magnitude == 0) {
            // Failsafe in case boss is on top of player
            projectile.velocityX = 0;
            projectile.velocityY = projSpeed; // Fire down by default
        } else {
            // Set velocity based on normalized direction vector
            projectile.velocityX = (int) Math.round(dx / magnitude * projSpeed);
            projectile.velocityY = (int) Math.round(dy / magnitude * projSpeed);
        }

        // We can use the 'direction' field as a flag to identify projectiles
        // in the collision and movement managers.
        projectile.direction = 'P'; // 'P' for Projectile

        return projectile;
    }
}
import java.awt.Image;

public class Boss extends Actor {

    // --- Configuration ---
    private static final int STARTING_LIVES = 3;
    private static final int PROJECTILE_SIZE = 16;
    private static final int SPEED_BONUS = 2;

    // State Definitions (Duration, CanTakeDamage, CanAttack)
    private enum BossState {
        NORMAL(200, true, true),    // 10 seconds
        REFLECT(100, false, false); // 5 seconds

        final int duration;
        final boolean vulnerable;
        final boolean canAttack;

        BossState(int duration, boolean vulnerable, boolean canAttack) {
            this.duration = duration;
            this.vulnerable = vulnerable;
            this.canAttack = canAttack;
        }
    }

    // --- Variables ---
    private int lives = STARTING_LIVES;
    private BossState currentState;
    private int stateTimer;
    private int attackCooldown;

    public Boss(Image image, int x, int y, int width, int height, int speed) {
        super(image, x, y, width, height, speed);
        transitionTo(BossState.NORMAL);
    }

    // --- Main Loop ---
    public void updateAI() {
        // 1. Handle State Timer
        if (--stateTimer <= 0) {
            toggleState();
        }
        // 2. Handle Attack Cooldown
        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }

    // --- State Management ---
    private void transitionTo(BossState newState) {
        this.currentState = newState;
        this.stateTimer = newState.duration;
    }

    private void toggleState() {
        transitionTo(currentState == BossState.NORMAL ? BossState.REFLECT : BossState.NORMAL);
    }

    // --- Game Interactions ---
    public boolean takeDamage() {
        if (!currentState.vulnerable) return true; // Reflected

        lives--;
        return lives > 0; // Return true if still alive
    }

    public boolean isReflecting() {
        return currentState == BossState.REFLECT;
    }

    public int getLives() {
        return lives;
    }

    // --- Combat Logic ---
    public Actor performLongRangeAttack(Actor target, Image projectileImg) {
        if (!currentState.canAttack || attackCooldown > 0) return null;

        attackCooldown = 40; // Reset cooldown (2 seconds)
        return spawnProjectile(target, projectileImg);
    }

    private Actor spawnProjectile(Actor target, Image img) {
        int pSpeed = this.speed + SPEED_BONUS;

        // Center projectile on Boss
        int px = this.x + (this.width - PROJECTILE_SIZE) / 2;
        int py = this.y + (this.height - PROJECTILE_SIZE) / 2;

        Actor proj = new Actor(img, px, py, PROJECTILE_SIZE, PROJECTILE_SIZE, pSpeed);
        proj.direction = 'P'; // 'P' for Projectile

        // Aim at target
        setHomingVelocity(proj, target, pSpeed);
        return proj;
    }

    private void setHomingVelocity(Actor proj, Actor target, int speed) {
        double dx = (target.x + target.width / 2.0) - (proj.x + proj.width / 2.0);
        double dy = (target.y + target.height / 2.0) - (proj.y + proj.height / 2.0);
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 0) {
            proj.velocityX = (int) ((dx / dist) * speed);
            proj.velocityY = (int) ((dy / dist) * speed);
        } else {
            proj.velocityY = speed; // Fallback if positions match
        }
    }
}
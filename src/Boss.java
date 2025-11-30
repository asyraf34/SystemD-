import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

public class Boss extends Actor {
    // --- Configuration ---
    private static final int STARTING_LIVES = GameConstants.BOSS_LIVES;
    private static final int PROJECTILE_SIZE = 16;

    // State Definitions (Duration, CanTakeDamage, CanAttack)
    private enum BossState {
        NORMAL(GameConstants.TIMER_BOSS_NORMAL, true, true),
        REFLECT(GameConstants.TIMER_BOSS_REFLECT, false, false);

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
    public List<Actor> performLongRangeAttack(Actor target, Image projectileImg) {
        if (!currentState.canAttack || attackCooldown > 0) return null;

        attackCooldown = GameConstants.TIMER_BOSS_ATTACK; // Reset cooldown

        List<Actor> projectiles = new ArrayList<>();
        // Fire 3 projectiles: Center, Left (-20 deg), Right (+20 deg)
        projectiles.add(spawnProjectile(target, projectileImg, 0));
        projectiles.add(spawnProjectile(target, projectileImg, Math.toRadians(-20)));
        projectiles.add(spawnProjectile(target, projectileImg, Math.toRadians(20)));
        /**projectiles.add(spawnProjectile(target, projectileImg, Math.toRadians(-40)));
        projectiles.add(spawnProjectile(target, projectileImg, Math.toRadians(40)));
        projectiles.add(spawnProjectile(target, projectileImg, Math.toRadians(-60)));
        projectiles.add(spawnProjectile(target, projectileImg, Math.toRadians(60)));*/


        return projectiles;
    }

    private Actor spawnProjectile(Actor target, Image img, double angleOffset) {
        int pSpeed = this.speed + GameConstants.SPEED_PROJECTILE_BONUS;
        // Center projectile on Boss
        int px = this.x + (this.width - PROJECTILE_SIZE) / 2;
        int py = this.y + (this.height - PROJECTILE_SIZE) / 2;

        Actor proj = new Actor(img, px, py, PROJECTILE_SIZE, PROJECTILE_SIZE, pSpeed);
        proj.direction = Direction.NONE;

        // Aim at target with offset
        double dx = (target.x + target.width / 2.0) - (proj.x + proj.width / 2.0);
        double dy = (target.y + target.height / 2.0) - (proj.y + proj.height / 2.0);
        double baseAngle = Math.atan2(dy, dx);

        double finalAngle = baseAngle + angleOffset;

        proj.velocityX = (int) (pSpeed * Math.cos(finalAngle));
        proj.velocityY = (int) (pSpeed * Math.sin(finalAngle));

        return proj;
    }
}
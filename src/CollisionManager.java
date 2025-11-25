import java.util.Iterator;
import java.util.logging.Logger;

public class CollisionManager {

    public static final int GHOST_COLLISION_NONE = 0;
    public static final int GHOST_COLLISION_LIFE_LOST = 1;
    public static final int GHOST_COLLISION_GHOST_KILLED = 2;
    private static final Logger LOGGER = Logger.getLogger(CollisionManager.class.getSimpleName());

    // --- 1. Simple Collisions (Food & Knife) ---

    public void checkFoodCollisions(GameState state, SoundManager soundManager) {
        Iterator<Entity> it = state.foods.iterator();
        while (it.hasNext()) {
            if (state.pacman.collidesWith(it.next())) {
                it.remove();
                state.score += 10;
                soundManager.playEffect("audio/food.wav");
                return; // Eat only one per frame
            }
        }
    }

    public boolean checkKnifeCollisions(GameState state) {
        Iterator<Entity> it = state.knives.iterator();
        while (it.hasNext()) {
            if (state.pacman.collidesWith(it.next())) {
                it.remove();
                state.hasWeapon = true;
                state.knifeCount++;
                return true;
            }
        }
        return false;
    }

    // --- 2. Entity Collisions (Ghost, Boss, Projectile) ---

    public int checkGhostCollisions(GameState state, SoundManager soundManager) {
        Iterator<Actor> it = state.ghosts.iterator();
        while (it.hasNext()) {
            Actor ghost = it.next();
            if (state.pacman.collidesWith(ghost)) {
                LOGGER.info( "hasWeapon = " + state.hasWeapon + ", " + "police collides with mafia = " + state.pacman.collidesWith(ghost));
                // CASE A: Pac-Man has weapon -> Kill Ghost
                if (state.hasWeapon && state.knifeCount > 0) {
                    LOGGER.info( "hasWeapon = " + state.hasWeapon + ", " + "police collides with mafia = " + state.pacman.collidesWith(ghost));
                    consumeWeapon(state);
                    triggerDeathAnimation(state, ghost);
                    it.remove();
                    soundManager.playEffect("audio/kill.wav");
                    return GHOST_COLLISION_GHOST_KILLED;
                }

                // CASE B: No weapon -> Pac-Man dies
                LOGGER.info( "hasWeapon = " + state.hasWeapon + ", " + "police collides with mafia = " + state.pacman.collidesWith(ghost));
                return handleLifeLost(state, soundManager);
            }
        }
        return GHOST_COLLISION_NONE;
    }

    public int checkBossCollisions(GameState state, SoundManager soundManager) {
        if (state.boss == null || !state.pacman.collidesWith(state.boss)) {
            return GHOST_COLLISION_NONE;
        }

        // CASE A: No Weapon -> Instant Death
        if (!state.hasWeapon || state.knifeCount <= 0) {
            return handleLifeLost(state, soundManager);
        }

        // CASE B: Has Weapon (Knife)

        // 1. If Boss is Reflecting -> Pac-Man takes damage (but knife is NOT consumed)
        if (state.boss.isReflecting()) {
            return handleLifeLost(state, soundManager);
        }

        // 2. Boss is Vulnerable -> Boss takes damage (Knife IS consumed)
        consumeWeapon(state);

        // Apply damage. takeDamage() returns true if boss is still alive, false if defeated.
        if (!state.boss.takeDamage()) {
            state.score += 1000;
            state.boss = null; // Boss defeated
        }
        state.pacman.reset();

        // Successful hit on a vulnerable boss. Player does NOT lose life.
        return GHOST_COLLISION_GHOST_KILLED;
    }

    public int checkProjectileCollisions(GameState state, SoundManager soundManager) {
        if (state.projectiles == null) return GHOST_COLLISION_NONE;

        Iterator<Actor> it = state.projectiles.iterator();
        while (it.hasNext()) {
            if (state.pacman.collidesWith(it.next())) {
                it.remove();
                return handleLifeLost(state, soundManager);
            }
        }
        return GHOST_COLLISION_NONE;
    }

    // --- 3. Private Helper Methods (Reduces Duplication) ---

    private int handleLifeLost(GameState state, SoundManager sound) {
        state.lives--;
        sound.playEffect("audio/life_lost.wav");
        if (state.lives <= 0) {
            state.gameOver = true;
        }
        return GHOST_COLLISION_LIFE_LOST;
    }

    private void consumeWeapon(GameState state) {
        state.knifeCount--;
        if (state.knifeCount <= 0) {
            state.hasWeapon = false;
        }
    }

    private void triggerDeathAnimation(GameState state, Actor ghost) {
        try {
            state.animations.add(new DeathAnimation(
                    ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, 30, "~"
            ));
        } catch (Exception e) {
            // Ignore animation errors, game must go on
        }
    }
}
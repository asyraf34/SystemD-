import java.util.Iterator;

public class CollisionManager {

    public static final int GHOST_COLLISION_NONE = 0;
    public static final int GHOST_COLLISION_LIFE_LOST = 1;
    public static final int GHOST_COLLISION_GHOST_KILLED = 2;

    // --- 1. Simple Collisions (Food & Knife) ---

    public void checkFoodCollisions(PacMan.GameState state, SoundManager soundManager) {
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

    public boolean checkKnifeCollisions(PacMan.GameState state) {
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

    public int checkGhostCollisions(PacMan.GameState state, SoundManager soundManager) {
        Iterator<Actor> it = state.ghosts.iterator();
        while (it.hasNext()) {
            Actor ghost = it.next();
            if (state.pacman.collidesWith(ghost)) {

                // CASE A: Pac-Man has weapon -> Kill Ghost
                if (state.hasWeapon && state.knifeCount > 0) {
                    consumeWeapon(state);
                    triggerDeathAnimation(state, ghost);
                    it.remove();
                    soundManager.playEffect("audio/kill.wav");
                    return GHOST_COLLISION_GHOST_KILLED;
                }

                // CASE B: No weapon -> Pac-Man dies
                return handleLifeLost(state, soundManager);
            }
        }
        return GHOST_COLLISION_NONE;
    }

    public int checkBossCollisions(PacMan.GameState state, SoundManager soundManager) {
        if (state.boss == null || !state.pacman.collidesWith(state.boss)) {
            return GHOST_COLLISION_NONE;
        }

        // CASE A: No Weapon -> Instant Death
        if (!state.hasWeapon || state.knifeCount <= 0) {
            return handleLifeLost(state, soundManager);
        }

        // CASE B: Has Weapon (Always consumes knife on contact)
        consumeWeapon(state);

        // If Boss is Reflecting -> Pac-Man takes damage
        if (state.boss.isReflecting()) {
            return handleLifeLost(state, soundManager);
        }

        // If Boss is Vulnerable -> Boss takes damage
        if (!state.boss.takeDamage()) {
            state.score += 1000;
            state.boss = null; // Boss defeated
        }
        return GHOST_COLLISION_GHOST_KILLED; // Successful hit
    }

    public int checkProjectileCollisions(PacMan.GameState state, SoundManager soundManager) {
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

    private int handleLifeLost(PacMan.GameState state, SoundManager sound) {
        state.lives--;
        sound.playEffect("audio/life_lost.wav");
        if (state.lives <= 0) {
            state.gameOver = true;
        }
        return GHOST_COLLISION_LIFE_LOST;
    }

    private void consumeWeapon(PacMan.GameState state) {
        state.knifeCount--;
        if (state.knifeCount <= 0) {
            state.hasWeapon = false;
        }
    }

    private void triggerDeathAnimation(PacMan.GameState state, Actor ghost) {
        try {
            state.animations.add(new DeathAnimation(
                    ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, 30, "~"
            ));
        } catch (Exception e) {
            // Ignore animation errors, game must go on
        }
    }
}
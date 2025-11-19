import java.util.HashSet;

/**
 * Manages all collision detection for the game.
 * This is a stateless "service" class that operates on the GameState.
 */
public class CollisionManager {

    // Public constants to return from checkGhostCollisions
    public static final int GHOST_COLLISION_NONE = 0;
    public static final int GHOST_COLLISION_LIFE_LOST = 1;
    public static final int GHOST_COLLISION_GHOST_KILLED = 2;

    /**
     * Checks for collisions between Pac-Man and food pellets.
     */
    public void checkFoodCollisions(PacMan.GameState state, SoundManager soundManager) {
        Entity foodEaten = null;
        for (Entity food : state.foods) {
            if (state.pacman.collidesWith(food)) {
                foodEaten = food;
                state.score += 10;
                soundManager.playEffect("audio/food.wav");
                break; // Eat one per frame
            }
        }
        if (foodEaten != null) {
            state.foods.remove(foodEaten);
        }
    }

    /**
     * Checks for collisions between Pac-Man and knives.
     * @return true if a knife was picked up (image update needed)
     */
    public boolean checkKnifeCollisions(PacMan.GameState state) {
        Entity knifeCollected = null;
        for (Entity knife : state.knives) {
            if (state.pacman.collidesWith(knife)) {
                knifeCollected = knife;
                state.hasWeapon = true;
                state.knifeCount++;
                break;
            }
        }
        if (knifeCollected != null) {
            state.knives.remove(knifeCollected);
            return true; // Knife was picked up
        }
        return false; // No knife picked up
    }

    /**
     * Checks for collisions between Pac-Man and ghosts.
     * @return an integer constant: 0 (none), 1 (life lost), or 2 (ghost killed)
     */
    public int checkGhostCollisions(PacMan.GameState state, SoundManager soundManager) {
        // logger
        System.out.println(
                "Boss collision check: boss=" + (state.boss != null) +
                        " collides=" + (state.boss != null && state.pacman.collidesWith(state.boss)) +
                        " hasWeapon=" + state.hasWeapon +
                        " knifeCount=" + state.knifeCount
        );

        Iterator<Actor> it = state.ghosts.iterator();
        while (it.hasNext()) {
            Actor ghost = it.next();
            if (state.pacman.collidesWith(ghost)) {
                if (state.hasWeapon && state.knifeCount > 0) {
                    // Pac-Man wins
                    state.knifeCount--;
                    ghostToRemove = ghost;
                    if (state.knifeCount == 0) {
                        state.hasWeapon = false;
                    }
                    // Don't break; Pac-Man can kill multiple ghosts in one frame
                    if (ghostToRemove != null) {
                        state.ghosts.remove(ghostToRemove);
                    }
                    return GHOST_COLLISION_GHOST_KILLED; // Ghost was killed
                } else {
                    // Ghost wins
                    state.lives--;
                    soundManager.playEffect("audio/life_lost.wav");
                    if (state.lives <= 0) {
                        state.gameOver = true;
                    }
                    return GHOST_COLLISION_LIFE_LOST; // Life was lost
                }
            }
        }
        return GHOST_COLLISION_NONE; // No collision
    }

    /**
     * Checks for collisions between Pac-Man and the Boss.
     * @return an integer constant: 0 (none), 1 (life lost), or 2 (boss hit)
     */
    public int checkBossCollisions(PacMan.GameState state, SoundManager soundManager) {
        // debugging lines
        if (state.boss != null && state.pacman.collidesWith(state.boss)){
            System.out.println("Boss collided");
        }
        System.out.println(
                "Boss collision check: boss=" + (state.boss != null) +
                        " collides=" + (state.boss != null && state.pacman.collidesWith(state.boss)) +
                        " hasWeapon=" + state.hasWeapon +
                        " knifeCount=" + state.knifeCount
        );
        if (state.hasWeapon){
            System.out.println("Reflect mode actived");
            System.out.println("Boss collision: isReflecting=" + state.boss.isReflecting());
        }
        System.out.println("Boss collision: isReflecting=" + state.boss.isReflecting());

        if (state.boss == null || !state.pacman.collidesWith(state.boss)) {
            return GHOST_COLLISION_NONE; // No collision
        }

        if (state.hasWeapon && state.knifeCount > 0) {
            // Pac-Man attacked the boss
            if (state.boss.isReflecting()) {
                // Boss reflects damage! Pac-Man gets hurt.
                state.lives--;
                soundManager.playEffect("audio/life_lost.wav");

                // Consume the knife on reflect
                state.knifeCount--;
                if (state.knifeCount == 0) state.hasWeapon = false;

                if (state.lives <= 0) {
                    state.gameOver = true;
                }
                return GHOST_COLLISION_LIFE_LOST; // Life lost

            } else {
                // Boss takes damage
                state.knifeCount--;
                if (state.knifeCount == 0) state.hasWeapon = false;

                boolean bossAlive = state.boss.takeDamage();
                if (!bossAlive) {
                    state.score += 1000; // Boss defeated bonus
                    state.boss = null; // Remove boss
                }
                return GHOST_COLLISION_GHOST_KILLED; // Boss was hit
            }
        } else {
            // Pac-Man touched boss without weapon, life lost
            state.lives--;
            soundManager.playEffect("audio/life_lost.wav");
            if (state.lives <= 0) {
                state.gameOver = true;
            }
            return GHOST_COLLISION_LIFE_LOST; // Life lost
        }
    }

    /**
     * Checks for collisions between Pac-Man and projectiles.
     * @return an integer constant: 0 (none) or 1 (life lost)
     */
    public int checkProjectileCollisions(PacMan.GameState state, SoundManager soundManager) {
        if (state.projectiles == null || state.projectiles.isEmpty()) {
            return GHOST_COLLISION_NONE;
        }

        Actor projectileHit = null;
        for (Actor proj : state.projectiles) {
            if (state.pacman.collidesWith(proj)) {
                projectileHit = proj;
                break; // Found a hit
            }
        }

        if (projectileHit != null) {
            state.projectiles.remove(projectileHit);
            state.lives--;
            soundManager.playEffect("audio/life_lost.wav");

            if (state.lives <= 0) {
                state.gameOver = true;
            }
            return GHOST_COLLISION_LIFE_LOST; // Life lost
        }

        return GHOST_COLLISION_NONE; // No collision
    }
}
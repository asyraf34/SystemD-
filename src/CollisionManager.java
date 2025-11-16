import java.util.HashSet;
import java.util.Iterator;
import java.awt.Image;

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
     *
     * NOTE: use Iterator here to safely remove items while iterating.
     */
    public boolean checkKnifeCollisions(PacMan.GameState state) {
        Iterator<Entity> it = state.knives.iterator();
        while (it.hasNext()) {
            Entity knife = it.next();
            if (state.pacman.collidesWith(knife)) {
                it.remove();
                state.hasWeapon = true;
                state.knifeCount++;
                return true; // Knife was picked up
            }
        }
        return false; // No knife picked up
    }

    /**
     * Checks for collisions between Pac-Man and ghosts.
     * @return an integer constant: 0 (none), 1 (life lost), or 2 (ghost killed)
     *
     * Uses an Iterator to safely remove a ghost while iterating and creates a DeathAnimation.
     */
    public int checkGhostCollisions(PacMan.GameState state, SoundManager soundManager) {
        Iterator<Actor> it = state.ghosts.iterator();
        while (it.hasNext()) {
            Actor ghost = it.next();
            if (state.pacman.collidesWith(ghost)) {

                if (state.hasWeapon && state.knifeCount > 0) {
                    // Pac-Man wins: consume knife
                    state.knifeCount--;
                    if (state.knifeCount == 0) {
                        state.hasWeapon = false;
                    }

                    // Snapshot position + image and create visual animation (non-blocking)
                    Image snap = ghost.image;
                    int tx = ghost.x;
                    int ty = ghost.y;
                    int tw = ghost.width;
                    int th = ghost.height;
                    int totalTicks = 30; //
                    String scoreText = "+100";

                    try {
                        DeathAnimation anim = new DeathAnimation(snap, tx, ty, tw, th, totalTicks, scoreText);
                        if (state.animations != null) {
                            state.animations.add(anim);
                        }
                    } catch (Throwable t) {
                        // If DeathAnimation isn't available or fails, ignore; still remove the ghost.
                        System.err.println("DeathAnimation creation failed: " + t.getMessage());
                    }

                    // remove from ghosts safely
                    it.remove();

                    // play kill sound
                    soundManager.playEffect("audio/kill.wav");

                    return GHOST_COLLISION_GHOST_KILLED;
                } else {
                    // Ghost wins
                    state.lives--;
                    soundManager.playEffect("audio/life_lost.wav");
                    if (state.lives == 0) {
                        state.gameOver = true;
                    }
                    return GHOST_COLLISION_LIFE_LOST;
                }
            }
        }
        return GHOST_COLLISION_NONE; // No collision
    }
}
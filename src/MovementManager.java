import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Random;

/**
 * Manages all actor movement and player input.
 * This is a stateless "service" class; it does not hold any game state itself,
 * but operates on the GameState object passed to it.
 */
public class MovementManager {

    // A copy of the directions, passed from PacMan
    private final char[] directions;
    private final Random random = new Random();

    public MovementManager(char[] directions) {
        this.directions = directions;
    }

    /**
     * Main update method to move all actors.
     * @return true if Pac-Man's direction or state changed (image update needed)
     */
    public boolean updateActorPositions(PacMan.GameState state, InputHandler input, GameMap map, SoundManager sound, int tileSize) {
        // HandlePlayerInput returns true if a move was successfully started
        boolean moveStarted = handlePlayerInput(state, input, sound, tileSize);
        updatePacmanPosition(state, tileSize);
        checkPacmanBounds(state, map, tileSize);
        moveGhosts(state, map, tileSize);
        moveBoss(state, map, tileSize);
        moveProjectiles(state);

        return moveStarted;
    }

    /**
     * Checks for player input and attempts to start a move.
     * @return true if a new move was successfully initiated
     */
    private boolean handlePlayerInput(PacMan.GameState state, InputHandler input, SoundManager sound, int tileSize) {
        if (state.pacman.isMoving) return false;

        if (input.isUpPressed()) {
            return attemptMove(state, 'U', 0, -tileSize, sound, tileSize);
        } else if (input.isDownPressed()) {
            return attemptMove(state, 'D', 0, tileSize, sound, tileSize);
        } else if (input.isLeftPressed()) {
            return attemptMove(state, 'L', -tileSize, 0, sound, tileSize);
        } else if (input.isRightPressed()) {
            return attemptMove(state, 'R', tileSize, 0, sound, tileSize);
        }
        return false;
    }

    /**
     * Attempts to set Pac-Man's target tile in a new direction.
     * @return true if the move is valid and direction changed
     */
    private boolean attemptMove(PacMan.GameState state, char dir, int dx, int dy, SoundManager sound, int tileSize) {
        int newX = state.pacman.x + dx;
        int newY = state.pacman.y + dy;

        // Check for wall collision at the target *tile*
        Entity tempTarget = new Entity(null, newX, newY, state.pacman.width, state.pacman.height);
        for (Entity wall : state.walls) {
            if (tempTarget.collidesWith(wall)) {
                return false; // Blocked
            }
        }

        state.pacman.direction = dir;
        state.pacman.targetX = newX;
        state.pacman.targetY = newY;
        state.pacman.isMoving = true;
        sound.playEffect("audio/move.wav"); // Play sound on move start
        return true; // Direction changed
    }

    /**
     * Handles the smooth interpolation of Pac-Man's movement.
     */
    private void updatePacmanPosition(PacMan.GameState state, int tileSize) {
        if (!state.pacman.isMoving) return;

        int moveSpeed = 8; // Interpolation speed
        Actor pacman = state.pacman;

        int nextX = pacman.x;
        int nextY = pacman.y;

        // Move towards target
        if (pacman.x < pacman.targetX) nextX += moveSpeed;
        else if (pacman.x > pacman.targetX) nextX -= moveSpeed;
        else if (pacman.y < pacman.targetY) nextY += moveSpeed;
        else if (pacman.y > pacman.targetY) nextY -= moveSpeed;

        // Collision guard during interpolation
        Entity nextPos = new Entity(null, nextX, nextY, pacman.width, pacman.height);
        boolean blocked = false;
        for (Entity wall : state.walls) {
            if (nextPos.collidesWith(wall)) {
                blocked = true;
                break;
            }
        }

        if (!blocked) {
            pacman.x = nextX;
            pacman.y = nextY;
        } else {
            pacman.isMoving = false;
            pacman.targetX = pacman.x; // Snap back
            pacman.targetY = pacman.y;
        }

        // Check if arrived at target tile
        if (Math.abs(pacman.x - pacman.targetX) < moveSpeed &&
                Math.abs(pacman.y - pacman.targetY) < moveSpeed) {
            pacman.x = pacman.targetX;
            pacman.y = pacman.targetY;
            pacman.isMoving = false;
        }
    }

    /**
     * Keeps Pac-Man within the screen boundaries.
     */
    private void checkPacmanBounds(PacMan.GameState state, GameMap map, int tileSize) {
        int boardWidth = map.getColumnCount() * tileSize;
        int boardHeight = map.getRowCount() * tileSize;
        Actor pacman = state.pacman;

        if (pacman.x < 0) pacman.x = 0;
        if (pacman.y < 0) pacman.y = 0;
        if (pacman.x + pacman.width > boardWidth) pacman.x = boardWidth - pacman.width;
        if (pacman.y + pacman.height > boardHeight) pacman.y = boardHeight - pacman.height;

        if (!pacman.isMoving) {
            pacman.targetX = pacman.x;
            pacman.targetY = pacman.y;
        }
    }

    /**
     * Handles ghost AI movement and wall collisions.
     */
    private void moveGhosts(PacMan.GameState state, GameMap map, int tileSize) {
        int boardWidth = map.getColumnCount() * tileSize;
        int boardHeight = map.getRowCount() * tileSize;

        for (Actor ghost : state.ghosts) {
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            boolean collided = false;

            // Wall collision
            for (Entity wall : state.walls) {
                if (ghost.collidesWith(wall)) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    collided = true;
                    break;
                }
            }

            // Screen boundary collision
            if (ghost.x < 0 || ghost.x + ghost.width > boardWidth ||
                    ghost.y < 0 || ghost.y + ghost.height > boardHeight) {
                ghost.x = Math.max(0, Math.min(ghost.x, boardWidth - ghost.width));
                ghost.y = Math.max(0, Math.min(ghost.y, boardHeight - ghost.height));
                collided = true;
            }

            // Pick a new random direction on collision
            if (collided) {
                ghost.direction = directions[random.nextInt(directions.length)];
                ghost.updateVelocity();
            }
        }
    }

    private void moveBoss(PacMan.GameState state, GameMap map, int tileSize) {
        int boardWidth = map.getColumnCount() * tileSize;
        int boardHeight = map.getRowCount() * tileSize;

        for (Actor ghost : state.ghosts) {
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            boolean collided = false;

            // Wall collision
            for (Entity wall : state.walls) {
                if (ghost.collidesWith(wall)) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    collided = true;
                    break;
                }
            }

            // Screen boundary collision
            if (ghost.x < 0 || ghost.x + ghost.width > boardWidth ||
                    ghost.y < 0 || ghost.y + ghost.height > boardHeight) {
                ghost.x = Math.max(0, Math.min(ghost.x, boardWidth - ghost.width));
                ghost.y = Math.max(0, Math.min(ghost.y, boardHeight - ghost.height));
                collided = true;
            }

            // Pick a new random direction on collision
            if (collided) {
                ghost.direction = directions[random.nextInt(directions.length)];
                ghost.updateVelocity();
            }
        }
    }

    private void moveProjectiles(PacMan.GameState state) {
        HashSet<Actor> projectilesToRemove = new HashSet<>();
        for (Actor proj : state.projectiles) {
            proj.x += proj.velocityX;
            proj.y += proj.velocityY;

            // Check for wall collision
            for (Entity wall : state.walls) {
                if (proj.collidesWith(wall)) {
                    projectilesToRemove.add(proj);
                    break;
                }
            }
        }
        state.projectiles.removeAll(projectilesToRemove);
    }
}
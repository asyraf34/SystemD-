import java.util.HashSet;
import java.util.Random;

public class MovementManager {

    private final Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
    private final Random random = new Random();

    public MovementManager() {}

    public boolean updateActorPositions(GameState state, InputHandler input, GameMap map, SoundManager sound, int tileSize) {
        boolean moveStarted = handlePlayerInput(state, input, sound, tileSize);

        updatePacmanPosition(state);
        checkPacmanBounds(state, map, tileSize);

        moveAiActors(state.ghosts, state.walls, map, tileSize);

        if (state.boss != null) {
            HashSet<Actor> bossSet = new HashSet<>();
            bossSet.add(state.boss);
            moveAiActors(bossSet, state.walls, map, tileSize);
        }

        moveProjectiles(state);

        return moveStarted;
    }

    private boolean handlePlayerInput(GameState state, InputHandler input, SoundManager sound, int tileSize) {
        if (state.pacman.isMoving) return false;

        Direction nextDir = input.getDirection();
        if (nextDir != Direction.NONE) {
            return attemptMove(state, nextDir, sound, tileSize);
        }
        return false;
    }

    private boolean attemptMove(GameState state, Direction dir, SoundManager sound, int tileSize) {
        int dx = dir.getDx(tileSize);
        int dy = dir.getDy(tileSize);

        int newX = state.pacman.x + dx;
        int newY = state.pacman.y + dy;

        // Check collision at the target TILE
        Entity tempTarget = new Entity(null, newX, newY, state.pacman.width, state.pacman.height);
        for (Entity wall : state.walls) {
            if (tempTarget.collidesWith(wall)) return false;
        }

        state.pacman.direction = dir;
        state.pacman.targetX = newX;
        state.pacman.targetY = newY;
        state.pacman.isMoving = true;
        sound.playEffect(GameConstants.SOUND_MOVE);
        return true;
    }

    private void updatePacmanPosition(GameState state) {
        if (!state.pacman.isMoving) return;

        Actor pacman = state.pacman;
        int moveSpeed = 8; // Interpolation speed

        // Move pixels towards target
        if (pacman.x < pacman.targetX) pacman.x += moveSpeed;
        else if (pacman.x > pacman.targetX) pacman.x -= moveSpeed;
        else if (pacman.y < pacman.targetY) pacman.y += moveSpeed;
        else if (pacman.y > pacman.targetY) pacman.y -= moveSpeed;

        // Check if arrived
        if (Math.abs(pacman.x - pacman.targetX) < moveSpeed && Math.abs(pacman.y - pacman.targetY) < moveSpeed) {
            pacman.x = pacman.targetX;
            pacman.y = pacman.targetY;
            pacman.isMoving = false;
        }
    }

    private void checkPacmanBounds(GameState state, GameMap map, int tileSize) {
        int boardW = map.getColumnCount() * tileSize;
        int boardH = map.getRowCount() * tileSize;
        Actor p = state.pacman;

        if (p.x < 0) p.x = 0;
        if (p.y < 0) p.y = 0;
        if (p.x + p.width > boardW) p.x = boardW - p.width;
        if (p.y + p.height > boardH) p.y = boardH - p.height;
    }

    // Unified method for Ghosts and Boss
    private void moveAiActors(HashSet<Actor> actors, HashSet<Entity> walls, GameMap map, int tileSize) {
        int boardW = map.getColumnCount() * tileSize;
        int boardH = map.getRowCount() * tileSize;

        for (Actor actor : actors) {
            actor.x += actor.velocityX;
            actor.y += actor.velocityY;

            boolean collided = false;

            // Wall Collision
            for (Entity wall : walls) {
                if (actor.collidesWith(wall)) {
                    collided = true;
                    break;
                }
            }

            // Bounds Collision
            if (!collided) {
                if (actor.x < 0 || actor.x + actor.width > boardW ||
                        actor.y < 0 || actor.y + actor.height > boardH) {
                    collided = true;
                }
            }

            if (collided) {
                // Revert move
                actor.x -= actor.velocityX;
                actor.y -= actor.velocityY;

                // Pick random new direction
                actor.direction = directions[random.nextInt(directions.length)];
                actor.updateVelocity();
            }
        }
    }

    private void moveProjectiles(GameState state) {
        HashSet<Actor> toRemove = new HashSet<>();
        for (Actor proj : state.projectiles) {
            proj.x += proj.velocityX;
            proj.y += proj.velocityY;

            for (Entity wall : state.walls) {
                if (proj.collidesWith(wall)) {
                    toRemove.add(proj);
                    break;
                }
            }
        }
        state.projectiles.removeAll(toRemove);
    }
}
public class GameLogic {
    private final GameState state;
    private final GameMap gameMap;
    private final InputHandler inputHandler;
    private final MovementManager movementManager;
    private final CollisionManager collisionManager;
    private final SoundManager soundManager;
    private final AssetManager assetManager;

    public GameLogic(GameState state, GameMap map, InputHandler input,
                     SoundManager sound, AssetManager assets) {
        this.state = state;
        this.gameMap = map;
        this.inputHandler = input;
        this.soundManager = sound;
        this.assetManager = assets;
        this.movementManager = new MovementManager();
        this.collisionManager = new CollisionManager();
    }

    public void update() {
        // 1. Clean up dead animations
        if (!state.animations.isEmpty()) {
            state.animations.removeIf(da -> !da.tick());
        }

        // 2. Pause for Game Over / Win
        if (state.gameOver || state.gameWon) {
            if (state.restartDebounceTicks > 0) state.restartDebounceTicks--;
            return;
        }

        // 3. Inter-Level Logic (Wait for timer)
        if (state.interLevel) {
            state.interLevelTicks--;
            if (state.interLevelTicks <= 0) {
                state.interLevel = false;
                state.currentLevel = state.nextLevelToStart;
            }
            return;
        }

        // 4. Boss Logic
        if (state.boss != null) {
            state.boss.updateAI();
            // Update Boss Image
            if (state.boss.isReflecting()) state.boss.image = assetManager.getBossReflectImage();
            else state.boss.image = assetManager.getBossImage();

            // Boss Attack
            Actor proj = state.boss.performLongRangeAttack(state.pacman, assetManager.getProjectileImage());
            if (proj != null) state.projectiles.add(proj);
        }

        // 5. Movement
        boolean moveStarted = movementManager.updateActorPositions(state, inputHandler, gameMap, soundManager, GameConstants.TILE_SIZE);

        // 6. Collisions
        collisionManager.checkFoodCollisions(state, soundManager);
        boolean knifePicked = collisionManager.checkKnifeCollisions(state);
        if (knifePicked) soundManager.playEffect("audio/knife_pick.wav");

        int bossRes = (state.boss != null) ? collisionManager.checkBossCollisions(state, soundManager) : CollisionManager.GHOST_COLLISION_NONE;
        int projRes = (state.boss != null && !state.projectiles.isEmpty()) ? collisionManager.checkProjectileCollisions(state, soundManager) : CollisionManager.GHOST_COLLISION_NONE;
        int ghostRes = collisionManager.checkGhostCollisions(state, soundManager);

        // 7. Update Pacman Image
        boolean ghostKilled = (ghostRes == CollisionManager.GHOST_COLLISION_GHOST_KILLED || bossRes == CollisionManager.GHOST_COLLISION_GHOST_KILLED);
        if (moveStarted || knifePicked || ghostKilled) {
            updatePacmanImage();
        }

        // 8. Check Life Lost
        boolean lifeLost = (ghostRes == CollisionManager.GHOST_COLLISION_LIFE_LOST ||
                bossRes == CollisionManager.GHOST_COLLISION_LIFE_LOST ||
                projRes == CollisionManager.GHOST_COLLISION_LIFE_LOST);

        if (lifeLost) {
            if (state.lives <= 0) {
                state.gameOver = true;
                inputHandler.clear();
                state.restartDebounceTicks = GameConstants.TIMER_RESTART;
            } else {
                state.pacman.reset();
            }
        }

        // 9. Check Win
        if (state.foods.isEmpty() && !state.gameWon && !state.interLevel) {
            state.nextLevelToStart = state.currentLevel + 1;
            if (state.nextLevelToStart > gameMap.getLevelCount()) {
                state.gameWon = true;
                inputHandler.clear();
                state.restartDebounceTicks = GameConstants.TIMER_RESTART;
            } else {
                state.interLevel = true;
                state.interLevelTicks = GameConstants.TIMER_INTERLEVEL;
                inputHandler.clear();
            }
        }
    }

    private void updatePacmanImage() {
        boolean hasKnife = (state.hasWeapon && state.knifeCount > 0);
        switch (state.pacman.direction) {
            case UP: state.pacman.image = hasKnife ? assetManager.getPacmanUpKnifeImage() : assetManager.getPacmanUpImage(); break;
            case DOWN: state.pacman.image = hasKnife ? assetManager.getPacmanDownKnifeImage() : assetManager.getPacmanDownImage(); break;
            case LEFT: state.pacman.image = hasKnife ? assetManager.getPacmanLeftKnifeImage() : assetManager.getPacmanLeftImage(); break;
            case RIGHT: case NONE: state.pacman.image = hasKnife ? assetManager.getPacmanRightKnifeImage() : assetManager.getPacmanRightImage(); break;
        }
    }
}
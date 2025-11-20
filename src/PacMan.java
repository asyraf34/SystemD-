import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
/**
 * Main game class. Acts as the "Conductor" for the game.
 * It owns the game state and coordinates the other managers (Renderer,
 * InputHandler, MovementManager, CollisionManager) to run the game.
 * Its complexity is now very low, as all logic is delegated.
 */
public class PacMan extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final Direction[] directions = { Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT };

    // --- Core Components ---
    private final AssetManager assetManager;
    private final GameMap gameMap;
    private final SoundManager soundManager;
    private final Timer gameLoop;
    private final Random random = new Random();

    // --- New Delegated Managers ---
    private final InputHandler inputHandler;
    private final Renderer renderer;
    private final MovementManager movementManager;
    private final CollisionManager collisionManager;

    // --- Game State (Inner Class) ---
    /**
     * A "data class" to hold the complete game state,
     * making it easy to pass to the managers.
     */
    public class GameState {
        public int score = 0;
        public int lives = 3;
        public boolean gameOver = false;
        public boolean gameWon = false;
        public int currentLevel = 1;
        public int knifeCount = 0;
        public boolean hasWeapon = false;
        public Boss boss;
        public HashSet<Actor> projectiles;
        public HashSet<Entity> walls;
        public HashSet<Entity> foods;
        public HashSet<Entity> knives;
        public HashSet<Actor> ghosts;
        public Actor pacman;
        public boolean interLevel = false;
        public int interLevelTicks = 0;
        public int nextLevelToStart = 0;
        public int restartDebounceTicks = 0; // prevent auto-restart caused by key

        public List<DeathAnimation> animations = new ArrayList<>();
    }

    private final GameState state;

    PacMan() {
        // --- Initialize State ---
        state = new GameState();
        state.currentLevel = 1;

        // --- Initialize Core Components ---
        assetManager = new AssetManager(GameConstants.TILE_SIZE);
        gameMap = new GameMap();
        soundManager = new SoundManager();

        // --- Initialize New Managers ---
        inputHandler = new InputHandler();
        renderer = new Renderer(assetManager, gameMap, GameConstants.TILE_SIZE);
        movementManager = new MovementManager();
        collisionManager = new CollisionManager();

        // --- Board Setup ---
        int mapW = gameMap.getColumnCount() * GameConstants.TILE_SIZE;
        int mapH = gameMap.getRowCount() * GameConstants.TILE_SIZE;
        int topBarH = Math.max(32, GameConstants.TILE_SIZE);
        int bottomBarH = Math.max(40, (int)(GameConstants.TILE_SIZE * 1.2));
        setPreferredSize(new Dimension(mapW, topBarH + mapH + bottomBarH));
        setBackground(Color.LIGHT_GRAY);

        // --- Load Level 1 ---
        loadMap();
        spawnKnives(GameConstants.STARTING_KNIVES);
        // --- Start Game ---
        soundManager.playBackgroundLoop(GameConstants.SOUND_BG);
        gameLoop = new Timer(50, this); // 20fps
        gameLoop.start();

        // --- Input Setup ---
        addKeyListener(inputHandler); // Delegate listener
        setFocusable(true);
        requestFocusInWindow();
    }

    /**
     * Loads all entities for the current level.
     */
    public void loadMap() {
        state.walls = new HashSet<>();
        state.foods = new HashSet<>();
        state.ghosts = new HashSet<>();
        state.knives = new HashSet<>();
        state.projectiles = new HashSet<>();
        state.boss = null;

        state.animations.clear();

        boolean[][] wallMatrix = new boolean[gameMap.getRowCount()][gameMap.getColumnCount()];
        String[] currentMap = gameMap.getMapData(state.currentLevel);

        for (int r = 0; r < gameMap.getRowCount(); r++) {
            String row = currentMap[r];
            for (int c = 0; c < gameMap.getColumnCount(); c++) {
                char tileChar = row.charAt(c);
                int x = c * GameConstants.TILE_SIZE;
                int y = r * GameConstants.TILE_SIZE;

                switch (tileChar) {
                    case 'B':
                        state.boss = new Boss(assetManager.getBossImage(), x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE, GameConstants.SPEED_BOSS);
                        break;
                    case 'X':
                        wallMatrix[r][c] = true;
                        break;
                    case 'P':
                        state.pacman = new Actor(assetManager.getPacmanRightImage(), x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE, GameConstants.SPEED_PACMAN);
                        break;
                    case ' ':
                        int foodX = x + (GameConstants.TILE_SIZE - assetManager.getFoodWidth()) / 2;
                        int foodY = y + (GameConstants.TILE_SIZE - assetManager.getFoodHeight()) / 2;
                        state.foods.add(new Entity(assetManager.getFoodImage(), foodX, foodY, assetManager.getFoodWidth(), assetManager.getFoodHeight()));
                        break;
                }
            }
        }

        spawnGhosts(currentMap);

        // Create textured wall images using the Renderer
        for (int r = 0; r < gameMap.getRowCount(); r++) {
            for (int c = 0; c < gameMap.getColumnCount(); c++) {
                if (wallMatrix[r][c]) {
                    int x = c * GameConstants.TILE_SIZE;
                    int y = r * GameConstants.TILE_SIZE;
                    Image wallImg = renderer.createWallTexture(wallMatrix, r, c);
                    state.walls.add(new Entity(wallImg, x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE));
                }
            }
        }
    }

    /**
     * Spawns (or respawns) ghosts based on the map.
     */
    private void spawnGhosts(String[] currentMap) {
        state.ghosts.clear();
        int speed = (state.currentLevel == 3) ? GameConstants.SPEED_BOSS : GameConstants.SPEED_GHOST;

        for (int r = 0; r < gameMap.getRowCount(); r++) {
            String row = currentMap[r];
            for (int c = 0; c < gameMap.getColumnCount(); c++) {
                char tileChar = row.charAt(c);
                int x = c * GameConstants.TILE_SIZE;
                int y = r * GameConstants.TILE_SIZE;

                Image ghostImage = null;
                if (tileChar == 'b') ghostImage = assetManager.getBlueGhostImage();
                else if (tileChar == 'o') ghostImage = assetManager.getOrangeGhostImage();
                else if (tileChar == 'p') ghostImage = assetManager.getPinkGhostImage();
                else if (tileChar == 'r') ghostImage = assetManager.getRedGhostImage();

                if (ghostImage != null) {
                    Actor ghost = new Actor(ghostImage, x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE, speed);

                    ghost.direction = directions[random.nextInt(directions.length)];

                    ghost.updateVelocity();
                    state.ghosts.add(ghost);
                }
            }
        }
    }

    /**
     * Spawns a number of knives by replacing random food pellets.
     */
    public void spawnKnives(int count) {
        state.knives.clear();
        Entity[] foodArray = state.foods.toArray(new Entity[0]);
        count = Math.min(count, foodArray.length);
        int created = 0;

        while (created < count && foodArray.length > 0) {
            int index = random.nextInt(foodArray.length);
            Entity chosenFood = foodArray[index];

            if (state.foods.contains(chosenFood)) {
                int knifeX = chosenFood.x + (chosenFood.width - (GameConstants.TILE_SIZE / 2)) / 2;
                int knifeY = chosenFood.y + (chosenFood.height - (GameConstants.TILE_SIZE / 2)) / 2;
                state.knives.add(new Entity(assetManager.getKnifeImage(), knifeX, knifeY, GameConstants.TILE_SIZE / 2, GameConstants.TILE_SIZE / 2));
                state.foods.remove(chosenFood);
                created++;
            }
        }
    }

    /**
     * Resets Pac-Man, ghosts, and boss to their starting positions.
     * Also clears player input.
     */
    public void resetPositions() {
        state.pacman.reset();
        updatePacmanImage(); // Reset to default image

        String[] currentMap = gameMap.getMapData(state.currentLevel);
        spawnGhosts(currentMap);

        if (state.boss != null) {
            state.boss.reset(); // Reset boss to its start position
        }
        inputHandler.clear(); //Stop player from moving immediately
    }

    // --- Main Game Loop ---

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();  // always tick; updateGame() pauses when needed
        repaint();
    }

    /**
     * Main game logic update method. Just coordinates the managers.
     */
    private void updateGame() {

        // --- Tick active death animations ---
        if (state.animations != null && !state.animations.isEmpty()) {
            java.util.Iterator<DeathAnimation> it = state.animations.iterator();
            while (it.hasNext()) {
                DeathAnimation da = it.next();
                boolean alive = da.tick();   // update fade / scale / particles / popup
                if (!alive) {
                    it.remove();            // finished → remove from list
                }
            }
        }
        // ====== Game Over / Win pause & restart ======
        if (state.gameOver || state.gameWon) {
            // Count debounce
            if (state.restartDebounceTicks > 0) {
                state.restartDebounceTicks--;
                repaint();
                return; // Don't move in debounce
            }

            // After debounce, wait for key press
            if (inputHandler.anyKeyPressed()) {
                restartGame();
            }

            repaint();
            return;     // block movement
        }

        // If inter-level banner is active, tick down and wait
        if (state.interLevel) {
            state.interLevelTicks--;
            if (state.interLevelTicks <= 0) {
                state.interLevel = false;
                state.currentLevel = state.nextLevelToStart;

                // Safety: if player finished the last level during banner
                if (state.currentLevel > gameMap.getLevelCount()) {
                    state.gameWon = true;
                } else {
                    loadMap();        // rebuild entities for the new level
                    resetPositions(); // put sprites at their starts
                    spawnKnives(3);   // re-place knives for the new level
                }
            }

            // Still showing the banner this frame: draw only, skip physics
            repaint();
            return;
        }

        if (state.boss != null) {
            state.boss.updateAI();

            // Check if boss image needs to change
            updateBossImage();

            // Attempt to fire projectile
            Actor newProjectile = state.boss.performLongRangeAttack(state.pacman, assetManager.getProjectileImage());
            if (newProjectile != null) {
                state.projectiles.add(newProjectile);
            }
        }

        // --- 1. Handle Movement ---
        // moveStarted is true if Pac-Man just started a new move
        boolean moveStarted = movementManager.updateActorPositions(state, inputHandler, gameMap, soundManager, GameConstants.TILE_SIZE);

        // --- 2. Handle Collisions ---
        collisionManager.checkFoodCollisions(state, soundManager);
        boolean knifePickedUp = collisionManager.checkKnifeCollisions(state);
        if (knifePickedUp){
            soundManager.playEffect("audio/knife_pick.wav");
        }

        int bossResult = CollisionManager.GHOST_COLLISION_NONE;
        int projectileResult = CollisionManager.GHOST_COLLISION_NONE;

        // Only check boss collisions if the boss exists
        if (state.boss != null) {
            bossResult = collisionManager.checkBossCollisions(state, soundManager);
            // Only check projectile collisions if projectiles exist (or boss exists)
            if (state.projectiles != null && !state.projectiles.isEmpty()) {
                projectileResult = collisionManager.checkProjectileCollisions(state, soundManager);
            }
        }

        int ghostResult = collisionManager.checkGhostCollisions(state, soundManager);

        // --- 3. React to Collisions ---

        // Check if we need to update Pac-Man's image (moved, got knife, or used knife)
        boolean usedKnife = (ghostResult == CollisionManager.GHOST_COLLISION_GHOST_KILLED ||
                bossResult == CollisionManager.GHOST_COLLISION_GHOST_KILLED);

        if (moveStarted || knifePickedUp || usedKnife) {
            updatePacmanImage();
        }

        // Check if a life was lost from *any* source
        boolean lifeLost = (ghostResult == CollisionManager.GHOST_COLLISION_LIFE_LOST ||
                bossResult == CollisionManager.GHOST_COLLISION_LIFE_LOST ||
                projectileResult == CollisionManager.GHOST_COLLISION_LIFE_LOST);

        // reset if the player successfully hits the boss.
        // acts as a "knockback" and prevents an immediate death on the next frame.
        boolean resetFromBossHit = (bossResult == CollisionManager.GHOST_COLLISION_GHOST_KILLED);


        if (lifeLost || resetFromBossHit) {
            // Check for Game Over *only if a life was lost*(A boss hit shouldn't trigger game over, just a reset)
            if (lifeLost && state.lives <= 0) {
                state.gameOver = true;
                inputHandler.clear();
                state.restartDebounceTicks = GameConstants.TIMER_RESTART;
            } else {
                resetPositions();
            }
            return; // Stop update for this frame
        }

        // --- 4. Check for Level Win ---
        checkLevelCompletion();
    }

    private void updateBossImage() {
        if (state.boss == null) return;

        if (state.boss.isReflecting()) {
            state.boss.image = assetManager.getBossReflectImage();
        } else {
            state.boss.image = assetManager.getBossImage();
        }
    }

    /**
     * Checks if all food is eaten and advances the level.
     */
    private void checkLevelCompletion() {
        if (state.foods.isEmpty() && !state.gameWon && !state.interLevel) {
            state.nextLevelToStart = state.currentLevel + 1;

            // If exceed total levels, just mark won and show the win HUD
            if (state.nextLevelToStart > gameMap.getLevelCount()) {
                state.gameWon = true;
                inputHandler.clear();
                state.restartDebounceTicks = GameConstants.TIMER_RESTART;
                return;
            }

            // Enter inter-level mode
            state.interLevel = true;
            state.interLevelTicks = GameConstants.TIMER_INTERLEVEL;
            // clear any held keys so mafia doesn't move right away
            inputHandler.clear();
        }
    }
    private void restartGame() {
        // Reset
        state.currentLevel = 1;
        state.score = 0;
        state.lives = GameConstants.MAX_LIVES;
        state.hasWeapon = false;
        state.knifeCount = 0;
        state.gameOver = false;
        state.gameWon = false;
        state.interLevel = false;
        state.interLevelTicks = 0;

        // Clear input so that it wouldn't move
        inputHandler.clear();

        // Recreate level
        loadMap();
        resetPositions();
        spawnKnives(GameConstants.STARTING_KNIVES);
    }

    // --- Rendering (Delegated) ---
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Just one line! Tell the renderer to draw the current state.
        renderer.drawGame(g, this, state);
    }

    // --- Utility Methods (Kept in Mafia) ---

    /**
     * Updates Pac-Man's image based on his direction and weapon status.
     * This is kept in PacMan because it directly modifies PacMan's actor state.
     */
    private void updatePacmanImage() {
        Actor pacman = state.pacman;
        boolean hasKnife = (state.hasWeapon && state.knifeCount > 0);

        // Now using the Enum in the switch statement
        switch (pacman.direction) {
            case UP:
                pacman.image = hasKnife ? assetManager.getPacmanUpKnifeImage() : assetManager.getPacmanUpImage();
                break;
            case DOWN:
                pacman.image = hasKnife ? assetManager.getPacmanDownKnifeImage() : assetManager.getPacmanDownImage();
                break;
            case LEFT:
                pacman.image = hasKnife ? assetManager.getPacmanLeftKnifeImage() : assetManager.getPacmanLeftImage();
                break;
            case RIGHT:
            case NONE: // Handle default/start case
                pacman.image = hasKnife ? assetManager.getPacmanRightKnifeImage() : assetManager.getPacmanRightImage();
                break;
        }
    }
}

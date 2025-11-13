import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

/**
 * Main game class. Acts as the "Conductor" for the game.
 * It owns the game state and coordinates the other managers (Renderer,
 * InputHandler, MovementManager, CollisionManager) to run the game.
 * Its complexity is now very low, as all logic is delegated.
 */
public class PacMan extends JPanel implements ActionListener {

    // --- Game Configuration ---
    private final int tileSize = 32;
    private final int pacmanSpeed;
    private final int ghostSpeed;
    private final int bossSpeed;
    private final char[] directions = {'U', 'D', 'L', 'R'};

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

    // === Inter-level banner state ===
    private static final int INTERLEVEL_TICKS = 15; // ~3s if 50ms per tick

    // === Game Won & Game Over banner state ===
    private static final int RESTART_DEBOUNCE_TICKS = 10;

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
        public HashSet<Entity> walls;
        public HashSet<Entity> foods;
        public HashSet<Entity> knives;
        public HashSet<Actor> ghosts;
        public Actor pacman;
        public boolean interLevel = false;
        public int interLevelTicks = 0;
        public int nextLevelToStart = 0;
        public int restartDebounceTicks = 0; // prevent auto-restart caused by key
    }

    private final GameState state;

    PacMan() {
        // --- Initialize State ---
        state = new GameState();
        state.currentLevel = 1;

        // --- Initialize Core Components ---
        assetManager = new AssetManager(tileSize);
        gameMap = new GameMap();
        soundManager = new SoundManager();

        // --- Initialize New Managers ---
        inputHandler = new InputHandler();
        renderer = new Renderer(assetManager, gameMap, tileSize);
        movementManager = new MovementManager(directions); // Pass directions
        collisionManager = new CollisionManager();

        // --- Board Setup ---
        int mapW = gameMap.getColumnCount() * tileSize;
        int mapH = gameMap.getRowCount() * tileSize;
        int topBarH = Math.max(32, tileSize);
        int bottomBarH = Math.max(40, (int)(tileSize * 1.2));
        setPreferredSize(new Dimension(mapW, topBarH + mapH + bottomBarH));
        setBackground(Color.LIGHT_GRAY);

        // --- Entity Speeds ---
        pacmanSpeed = tileSize / 4;
        ghostSpeed = tileSize / 4;
        bossSpeed = tileSize / 3;

        // --- Load Level 1 ---
        loadMap();
        spawnKnives(3);

        // --- Start Game ---
        soundManager.playBackgroundLoop("audio/background.wav");
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

        boolean[][] wallMatrix = new boolean[gameMap.getRowCount()][gameMap.getColumnCount()];
        String[] currentMap = gameMap.getMapData(state.currentLevel);

        for (int r = 0; r < gameMap.getRowCount(); r++) {
            String row = currentMap[r];
            for (int c = 0; c < gameMap.getColumnCount(); c++) {
                char tileChar = row.charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                switch (tileChar) {
                    case 'X':
                        wallMatrix[r][c] = true;
                        break;
                    case 'P':
                        state.pacman = new Actor(assetManager.getPacmanRightImage(), x, y, tileSize, tileSize, pacmanSpeed);
                        break;
                    case ' ':
                        int foodX = x + (tileSize - assetManager.getFoodWidth()) / 2;
                        int foodY = y + (tileSize - assetManager.getFoodHeight()) / 2;
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
                    int x = c * tileSize;
                    int y = r * tileSize;
                    Image wallImg = renderer.createWallTexture(wallMatrix, r, c);
                    state.walls.add(new Entity(wallImg, x, y, tileSize, tileSize));
                }
            }
        }
    }

    /**
     * Spawns (or respawns) ghosts based on the map.
     */
    private void spawnGhosts(String[] currentMap) {
        state.ghosts.clear();
        int speed = (state.currentLevel == 3) ? bossSpeed : ghostSpeed;

        for (int r = 0; r < gameMap.getRowCount(); r++) {
            String row = currentMap[r];
            for (int c = 0; c < gameMap.getColumnCount(); c++) {
                char tileChar = row.charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                Image ghostImage = null;
                if (tileChar == 'b') ghostImage = assetManager.getBlueGhostImage();
                else if (tileChar == 'o') ghostImage = assetManager.getOrangeGhostImage();
                else if (tileChar == 'p') ghostImage = assetManager.getPinkGhostImage();
                else if (tileChar == 'r') ghostImage = assetManager.getRedGhostImage();

                if (ghostImage != null) {
                    Actor ghost = new Actor(ghostImage, x, y, tileSize, tileSize, speed);
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
                int knifeX = chosenFood.x + (chosenFood.width - (tileSize / 2)) / 2;
                int knifeY = chosenFood.y + (chosenFood.height - (tileSize / 2)) / 2;
                state.knives.add(new Entity(assetManager.getKnifeImage(), knifeX, knifeY, tileSize / 2, tileSize / 2));
                state.foods.remove(chosenFood);
                created++;
            }
        }
    }

    /**
     * Resets Pac-Man and ghosts to their starting positions.
     */
    public void resetPositions() {
        state.pacman.reset();
        updatePacmanImage(); // Reset to default image

        String[] currentMap = gameMap.getMapData(state.currentLevel);
        spawnGhosts(currentMap);
    }

    // --- Main Game Loop ---

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();  // always tick; updateGame() pauses when needed
        repaint();
    }

    /**
     * Main game logic update method. Now just coordinates the managers.
     */
    private void updateGame() {

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

        // --- 1. Handle Movement ---
        // moveStarted is true if Pac-Man just started a new move
        boolean moveStarted = movementManager.updateActorPositions(state, inputHandler, gameMap, soundManager, tileSize);

        // --- 2. Handle Collisions ---
        collisionManager.checkFoodCollisions(state, soundManager);
        boolean knifePickedUp = collisionManager.checkKnifeCollisions(state);
        int ghostResult = collisionManager.checkGhostCollisions(state, soundManager);

        // --- 3. React to Collisions ---

        // Update Mafia's image if he moved, picked up a knife, or used a knife
        if (moveStarted || knifePickedUp || ghostResult == CollisionManager.GHOST_COLLISION_GHOST_KILLED) {
            updatePacmanImage();
        }

        // Check if a life was lost
        if (ghostResult == CollisionManager.GHOST_COLLISION_LIFE_LOST) {
            if (state.lives == 0) {
                state.gameOver = true;
                inputHandler.clear();
                state.restartDebounceTicks = RESTART_DEBOUNCE_TICKS;

            } else {
                resetPositions();
            }
            return; // Stop update for this frame
        }

        // --- 4. Check for Level Win ---
        checkLevelCompletion();
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
                state.restartDebounceTicks = RESTART_DEBOUNCE_TICKS;

                return;
            }

            // Enter inter-level mode
            state.interLevel = true;
            state.interLevelTicks = INTERLEVEL_TICKS;

            // clear any held keys so mafia doesn't move right away
            inputHandler.clear();
        }
    }
    private void restartGame() {
        // Reset
        state.currentLevel = 1;
        state.score = 0;
        state.lives = 3;
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
        spawnKnives(3);
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
        if (state.hasWeapon && state.knifeCount > 0) {
            switch (pacman.direction) {
                case 'U': pacman.image = assetManager.getPacmanUpKnifeImage(); break;
                case 'D': pacman.image = assetManager.getPacmanDownKnifeImage(); break;
                case 'L': pacman.image = assetManager.getPacmanLeftKnifeImage(); break;
                case 'R': pacman.image = assetManager.getPacmanRightKnifeImage(); break;
                default:  pacman.image = assetManager.getPacmanRightKnifeImage(); break;
            }
        } else {
            switch (pacman.direction) {
                case 'U': pacman.image = assetManager.getPacmanUpImage(); break;
                case 'D': pacman.image = assetManager.getPacmanDownImage(); break;
                case 'L': pacman.image = assetManager.getPacmanLeftImage(); break;
                case 'R': pacman.image = assetManager.getPacmanRightImage(); break;
                default:  pacman.image = assetManager.getPacmanRightImage(); break;
            }
        }
    }
}
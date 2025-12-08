import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Random;

public class PacMan extends JPanel {
    // Core Parts
    private final GameState state;
    private final GameLogic logic;
    private final GameView view;

    // Helpers
    private final AssetManager assetManager;
    private final GameMap gameMap;
    private final InputHandler inputHandler;
    private final Renderer renderer;

    // Track the active mode for this game instance
    private GameMode currentMode;

    public PacMan() {
        this(GameMode.PLAY);
    }

    // Keep existing signature if any code constructs PacMan() — we keep both constructors for compatibility.
    public PacMan(GameMode initialMode) {
        this.currentMode = (initialMode == null) ? ModeManager.getSelectedMode() : initialMode;

        setLayout(new BorderLayout());

        // 1. Initialize Data & Tools
        state = new GameState();
        gameMap = new GameMap();
        assetManager = new AssetManager(GameConstants.TILE_SIZE);
        // Use the singleton SoundManager so that MenuPanel and game share the same instance
        SoundManager soundManager = SoundManager.getInstance();
        inputHandler = new InputHandler();
        renderer = new Renderer(assetManager, gameMap, GameConstants.TILE_SIZE);

        // 2. Initialize Logic & View
        logic = new GameLogic(state, gameMap, inputHandler, soundManager, assetManager);
        addKeyListener(inputHandler);
        setFocusable(true);

        // Listen for focus so we can detect start after Menu selection (App shows the game and requests focus).
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                applySelectedModeIfNeeded();
            }
        });

        int mapW = gameMap.getColumnCount() * GameConstants.TILE_SIZE;
        int mapH = gameMap.getRowCount() * GameConstants.TILE_SIZE;
        int topBarH = Math.max(32, GameConstants.TILE_SIZE);
        int bottomBarH = Math.max(40, (int)(GameConstants.TILE_SIZE * 1.2));

        view = new GameView(renderer, state, mapW, topBarH + mapH + bottomBarH, inputHandler);
        add(view, BorderLayout.CENTER);

        // Set initial lives depending on the selected/current mode
        state.lives = (currentMode == GameMode.DEMO) ? 5 : 3;

        // 3. Load Initial Level
        loadLevel();

        // 4. Start Loop
        // Check for Level Transition completion
        // Check for Restart
        Timer gameLoop = new Timer(50, e -> {
            // Check for Level Transition completion
            // NEW: Check if flag is set and timer is finished
            if (state.interLevel && state.interLevelTicks <= 0) {
                state.currentLevel = state.nextLevelToStart; // 1. Update Level Index
                loadLevel();                                 // 2. Load New Map & Entities
                state.interLevel = false;                    // 3. Clear Transition Flag
            }

            // Check for Restart
            if ((state.gameOver || state.gameWon) && state.restartDebounceTicks == 0 && inputHandler.anyKeyPressed()) {
                restartGame();
            }

            logic.update();
            view.repaint();
        });
        gameLoop.start();
    }
    public void startGameMusic() {
        SoundManager.getInstance().playBackgroundLoop(GameConstants.SOUND_GAME);
    }


    // --- Apply mode if it's changed (called when the panel gains focus) ---
    private void applySelectedModeIfNeeded() {
        GameMode selected = ModeManager.getSelectedMode();
        if (selected == null) selected = GameMode.PLAY;
        if (selected != this.currentMode) {
            this.currentMode = selected;
            // Apply lives and reload the level so the mode takes effect immediately.
            state.lives = (currentMode == GameMode.DEMO) ? 5 : 3;
            state.hasWeapon = false;
            state.knifeCount = 0;
            state.currentLevel = 1; // reset to first level when switching mode
            loadLevel();
        }
    }

    // --- Setup Methods ---

    private void loadLevel() {
        state.walls.clear();
        state.foods.clear();
        state.ghosts.clear();
        state.knives.clear();
        state.projectiles.clear();
        state.boss = null;
        state.animations.clear();
        state.sprintActive = false;
        state.sprintTicksRemaining = 0;
        state.sprintCooldownTicks = 0;

        String[] currentMap = gameMap.getMapData(state.currentLevel);
        boolean[][] wallMatrix = new boolean[gameMap.getRowCount()][gameMap.getColumnCount()];
        boolean[][] walkableGrid = new boolean[gameMap.getRowCount()][gameMap.getColumnCount()];

        for (int r = 0; r < gameMap.getRowCount(); r++) {
            String row = currentMap[r];
            for (int c = 0; c < gameMap.getColumnCount(); c++) {
                char tileChar = row.charAt(c);
                int x = c * GameConstants.TILE_SIZE;
                int y = r * GameConstants.TILE_SIZE;

                boolean isWall = (tileChar == 'X');
                wallMatrix[r][c] = isWall;
                walkableGrid[r][c] = !isWall;

                switch (tileChar) {
                    case 'B':
                        state.boss = new Boss(assetManager.getBossImage(), x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE, GameConstants.SPEED_BOSS);
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

        state.walkableGrid = walkableGrid;

        // Generate Walls
        for (int r = 0; r < gameMap.getRowCount(); r++) {
            for (int c = 0; c < gameMap.getColumnCount(); c++) {
                if (wallMatrix[r][c]) {
                    Image wallImg = renderer.createWallTexture(wallMatrix, r, c);
                    state.walls.add(new Entity(wallImg, c * GameConstants.TILE_SIZE, r * GameConstants.TILE_SIZE, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE));
                }
            }
        }

        spawnGhosts(currentMap);
        // Use the mode to decide how many knives to spawn per level
        int knivesToSpawn = (currentMode == GameMode.DEMO) ? 5 : 3;
        spawnKnives(knivesToSpawn);
    }

    private void spawnGhosts(String[] currentMap) {
        state.ghosts.clear();
        int speed = (state.currentLevel == 3) ? GameConstants.SPEED_BOSS : GameConstants.SPEED_GHOST;
        Random random = new Random();
        Direction[] directions = { Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT };

        for (int r = 0; r < gameMap.getRowCount(); r++) {
            String row = currentMap[r];
            for (int c = 0; c < gameMap.getColumnCount(); c++) {
                char tileChar = row.charAt(c);
                if ("bopr".indexOf(tileChar) >= 0) {
                    Image ghostImage = null;
                    if (tileChar == 'b') ghostImage = assetManager.getBlueGhostImage();
                    else if (tileChar == 'o') ghostImage = assetManager.getOrangeGhostImage();
                    else if (tileChar == 'p') ghostImage = assetManager.getPinkGhostImage();
                    else if (tileChar == 'r') ghostImage = assetManager.getRedGhostImage();

                    Actor ghost = new Actor(ghostImage, c * GameConstants.TILE_SIZE, r * GameConstants.TILE_SIZE, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE, speed);

                    // split movement into SMART and RANDOM; even index->SMART, odd index->RANDOM
                    ghost.movementType = (state.ghosts.size() % 2 == 0) ? MovementType.SMART : MovementType.RANDOM;
                    ghost.direction = directions[random.nextInt(directions.length)];
                    ghost.updateVelocity();
                    state.ghosts.add(ghost);
                }
            }
        }
    }

    private void spawnKnives(int count) {
        state.knives.clear();
        Entity[] foodArray = state.foods.toArray(new Entity[0]);
        if (foodArray.length == 0) return;

        count = Math.min(count, foodArray.length);
        Random random = new Random();
        int created = 0;

        while (created < count) {
            int index = random.nextInt(foodArray.length);
            Entity chosenFood = foodArray[index];
            if (state.foods.contains(chosenFood)) {
                int knifeSize = Math.max(1, (int) Math.round(GameConstants.TILE_SIZE * 0.7));
                int tileX = chosenFood.x - (GameConstants.TILE_SIZE - assetManager.getFoodWidth()) / 2;
                int tileY = chosenFood.y - (GameConstants.TILE_SIZE - assetManager.getFoodHeight()) / 2;
                int knifeX = tileX + (GameConstants.TILE_SIZE - knifeSize) / 2;
                int knifeY = tileY + (GameConstants.TILE_SIZE - knifeSize) / 2;
                state.knives.add(new Entity(assetManager.getKnifeImage(), knifeX, knifeY, knifeSize, knifeSize));
                state.foods.remove(chosenFood);
                created++;
            }
        }
    }

    private void restartGame() {
        state.currentLevel = 1;
        state.score = 0;
        // Use mode-aware lives on restart
        state.lives = (currentMode == GameMode.DEMO) ? 5 : 3;
        state.hasWeapon = false;
        state.knifeCount = 0;
        state.gameOver = false;
        state.gameWon = false;
        inputHandler.clear();
        loadLevel();
    }
}
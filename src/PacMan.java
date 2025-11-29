import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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

    public PacMan() {
        setLayout(new BorderLayout());

        // 1. Initialize Data & Tools
        state = new GameState();
        gameMap = new GameMap();
        assetManager = new AssetManager(GameConstants.TILE_SIZE);

        // Use the singleton SoundManager so UI and logic share the same audio instance
        SoundManager soundManager = SoundManager.getInstance();

        // Pass soundManager into InputHandler (constructor injection)
        inputHandler = new InputHandler(soundManager);

        // Pass soundManager into Renderer so it can draw & modify volume UI
        renderer = new Renderer(assetManager, gameMap, GameConstants.TILE_SIZE, soundManager);

        // 2. Initialize Logic & View
        logic = new GameLogic(state, gameMap, inputHandler, soundManager, assetManager);
        addKeyListener(inputHandler);
        setFocusable(true);

        int mapW = gameMap.getColumnCount() * GameConstants.TILE_SIZE;
        int mapH = gameMap.getRowCount() * GameConstants.TILE_SIZE;
        int topBarH = Math.max(32, GameConstants.TILE_SIZE);
        int bottomBarH = Math.max(40, (int)(GameConstants.TILE_SIZE * 1.2));

        view = new GameView(renderer, state, mapW, topBarH + mapH + bottomBarH, inputHandler);
        add(view, BorderLayout.CENTER);

        // Forward mouse events from view to renderer for volume control interaction
        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                renderer.onVolumeMousePressed(e.getX(), e.getY());
                view.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                renderer.onVolumeMouseReleased(e.getX(), e.getY());
                view.repaint();
            }
        });
        view.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                renderer.onVolumeMouseDragged(e.getX(), e.getY());
                view.repaint();
            }
        });

        // 3. Load Initial Level
        loadLevel();

        // 4. Start Loop
        soundManager.playBackgroundLoop(GameConstants.SOUND_BG);

        Timer gameLoop = new Timer(50, e -> {
            // Level transition handling
            if (state.interLevel && state.interLevelTicks <= 0) {
                state.currentLevel = state.nextLevelToStart;
                loadLevel();
                state.interLevel = false;
            }

            // Restart detection
            if ((state.gameOver || state.gameWon) && state.restartDebounceTicks == 0 && inputHandler.anyKeyPressed()) {
                restartGame();
            }

            logic.update();
            view.repaint();
        });
        gameLoop.start();
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
        spawnKnives(GameConstants.STARTING_KNIVES);
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
                int knifeX = chosenFood.x + (chosenFood.width - (GameConstants.TILE_SIZE / 2)) / 2;
                int knifeY = chosenFood.y + (chosenFood.height - (GameConstants.TILE_SIZE / 2)) / 2;
                state.knives.add(new Entity(assetManager.getKnifeImage(), knifeX, knifeY, GameConstants.TILE_SIZE / 2, GameConstants.TILE_SIZE / 2));
                state.foods.remove(chosenFood);
                created++;
            }
        }
    }

    private void restartGame() {
        state.currentLevel = 1;
        state.score = 0;
        state.lives = GameConstants.MAX_LIVES;
        state.hasWeapon = false;
        state.knifeCount = 0;
        state.gameOver = false;
        state.gameWon = false;
        inputHandler.clear();
        loadLevel();
    }
}
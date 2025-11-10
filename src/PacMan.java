import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;
// No longer needs BufferedImage

public class PacMan extends JPanel implements ActionListener { // No KeyListener

    // --- Game Configuration ---
    private final int tileSize = 32;
    private final int pacmanSpeed;
    private final int ghostSpeed;
    private final int bossSpeed;

    // --- Core Components ---
    private final AssetManager assetManager;
    private final GameMap gameMap;
    private final SoundManager soundManager;
    private final Timer gameLoop;
    private final Random random = new Random();
    private final InputHandler inputHandler; // NEW
    private final Renderer renderer;         // NEW

    // --- Game State (Now in its own inner class for passing) ---
    /**
     * A "data class" to hold the complete game state,
     * making it easy to pass to the Renderer.
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
    }

    private final GameState state;
    private final char[] directions = {'U', 'D', 'L', 'R'};

    PacMan() {
        // --- Initialize State ---
        state = new GameState();
        state.currentLevel = 1;

        // --- Initialize Core Components ---
        assetManager = new AssetManager(tileSize);
        gameMap = new GameMap();
        soundManager = new SoundManager();
        inputHandler = new InputHandler(); // NEW
        renderer = new Renderer(assetManager, gameMap, tileSize); // NEW

        // --- Board Setup ---
        int boardWidth = gameMap.getColumnCount() * tileSize;
        int boardHeight = gameMap.getRowCount() * tileSize;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
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
        addKeyListener(inputHandler); // Delegate listener to inputHandler
        setFocusable(true);
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
                    // Ask the renderer to create the image
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
        updatePacmanImage();

        String[] currentMap = gameMap.getMapData(state.currentLevel);
        spawnGhosts(currentMap);
    }

    // --- Main Game Loop ---

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!state.gameOver && !state.gameWon) {
            updateGame();
        }
        repaint();
        if (state.gameOver || state.gameWon) {
            gameLoop.stop();
        }
    }

    /**
     * Main game logic update method. (Formerly 'move()')
     */
    private void updateGame() {
        handlePlayerInput();
        updatePacmanPosition();
        checkPacmanBounds();

        checkFoodCollisions();
        checkKnifeCollisions();

        if (checkGhostCollisions()) { // Returns true if life was lost
            return; // Stop update for this frame
        }

        moveGhosts();
        checkLevelCompletion();
    }

    // --- `updateGame()` Helper Methods ---

    private void handlePlayerInput() {
        if (state.pacman.isMoving) return;

        // Ask the InputHandler for the state
        if (inputHandler.isUpPressed()) {
            attemptMove('U', 0, -tileSize);
        } else if (inputHandler.isDownPressed()) {
            attemptMove('D', 0, tileSize);
        } else if (inputHandler.isLeftPressed()) {
            attemptMove('L', -tileSize, 0);
        } else if (inputHandler.isRightPressed()) {
            attemptMove('R', tileSize, 0);
        }
    }

    private void attemptMove(char dir, int dx, int dy) {
        int newX = state.pacman.x + dx;
        int newY = state.pacman.y + dy;

        Entity tempTarget = new Entity(null, newX, newY, state.pacman.width, state.pacman.height);
        for (Entity wall : state.walls) {
            if (tempTarget.collidesWith(wall)) {
                return; // Blocked
            }
        }

        state.pacman.direction = dir;
        state.pacman.targetX = newX;
        state.pacman.targetY = newY;
        state.pacman.isMoving = true;
        updatePacmanImage();
        soundManager.playEffect("audio/move.wav"); // Play sound on move *start*
    }

    private void updatePacmanPosition() {
        if (!state.pacman.isMoving) return;

        int moveSpeed = 8;
        Actor pacman = state.pacman; //Shorthand

        int nextX = pacman.x;
        int nextY = pacman.y;

        if (pacman.x < pacman.targetX) nextX += moveSpeed;
        else if (pacman.x > pacman.targetX) nextX -= moveSpeed;
        else if (pacman.y < pacman.targetY) nextY += moveSpeed;
        else if (pacman.y > pacman.targetY) nextY -= moveSpeed;

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
            pacman.targetX = pacman.x;
            pacman.targetY = pacman.y;
        }

        if (Math.abs(pacman.x - pacman.targetX) < moveSpeed &&
                Math.abs(pacman.y - pacman.targetY) < moveSpeed) {
            pacman.x = pacman.targetX;
            pacman.y = pacman.targetY;
            pacman.isMoving = false;
        }
    }

    private void checkPacmanBounds() {
        int boardWidth = gameMap.getColumnCount() * tileSize;
        int boardHeight = gameMap.getRowCount() * tileSize;
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

    private void checkFoodCollisions() {
        Entity foodEaten = null;
        for (Entity food : state.foods) {
            if (state.pacman.collidesWith(food)) {
                foodEaten = food;
                state.score += 10;
                soundManager.playEffect("audio/food.wav");
                break;
            }
        }
        if (foodEaten != null) {
            state.foods.remove(foodEaten);
        }
    }

    private void checkKnifeCollisions() {
        Entity knifeCollected = null;
        for (Entity knife : state.knives) {
            if (state.pacman.collidesWith(knife)) {
                knifeCollected = knife;
                state.hasWeapon = true;
                state.knifeCount++;
                updatePacmanImage();
                break;
            }
        }
        if (knifeCollected != null) {
            state.knives.remove(knifeCollected);
        }
    }

    private boolean checkGhostCollisions() {
        Actor ghostToRemove = null;
        for (Actor ghost : state.ghosts) {
            if (state.pacman.collidesWith(ghost)) {
                if (state.hasWeapon && state.knifeCount > 0) {
                    state.knifeCount--;
                    ghostToRemove = ghost;
                    if (state.knifeCount == 0) {
                        state.hasWeapon = false;
                        updatePacmanImage();
                    }
                    break;
                } else {
                    state.lives--;
                    soundManager.playEffect("audio/life_lost.wav");
                    if (state.lives == 0) {
                        state.gameOver = true;
                    } else {
                        resetPositions();
                    }
                    return true; // Life was lost
                }
            }
        }
        if (ghostToRemove != null) {
            state.ghosts.remove(ghostToRemove);
        }
        return false; // No life lost
    }

    private void moveGhosts() {
        int boardWidth = gameMap.getColumnCount() * tileSize;
        int boardHeight = gameMap.getRowCount() * tileSize;

        for (Actor ghost : state.ghosts) {
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            boolean collided = false;

            for (Entity wall : state.walls) {
                if (ghost.collidesWith(wall)) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    collided = true;
                    break;
                }
            }

            if (ghost.x < 0 || ghost.x + ghost.width > boardWidth ||
                    ghost.y < 0 || ghost.y + ghost.height > boardHeight) {
                ghost.x = Math.max(0, Math.min(ghost.x, boardWidth - ghost.width));
                ghost.y = Math.max(0, Math.min(ghost.y, boardHeight - ghost.height));
                collided = true;
            }

            if (collided) {
                ghost.direction = directions[random.nextInt(directions.length)];
                ghost.updateVelocity();
            }
        }
    }

    private void checkLevelCompletion() {
        if (state.foods.isEmpty() && !state.gameWon) {
            state.currentLevel++;
            if (state.currentLevel > gameMap.getLevelCount()) {
                state.gameWon = true;
            } else {
                loadMap();
                resetPositions();
                spawnKnives(3);
            }
        }
    }

    // --- Rendering (Now delegated) ---

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.drawGame(g, this, state); // Just one line!
    }

    // --- Utility Methods (Still needed by PacMan) ---

    private void updatePacmanImage() {
        Actor pacman = state.pacman;
        if (state.hasWeapon && state.knifeCount > 0) {
            switch (pacman.direction) {
                case 'U': pacman.image = assetManager.getPacmanUpKnifeImage(); break;
                case 'D': pacman.image = assetManager.getPacmanDownKnifeImage(); break;
                case 'L': pacman.image = assetManager.getPacmanLeftKnifeImage(); break;
                case 'R': pacman.image = assetManager.getPacmanRightKnifeImage(); break;
            }
        } else {
            switch (pacman.direction) {
                case 'U': pacman.image = assetManager.getPacmanUpImage(); break;
                case 'D': pacman.image = assetManager.getPacmanDownImage(); break;
                case 'L': pacman.image = assetManager.getPacmanLeftImage(); break;
                case 'R': pacman.image = assetManager.getPacmanRightImage(); break;
                default: pacman.image = assetManager.getPacmanRightImage(); break;
            }
        }
    }

    // --- Input Handlers (Now Empty) ---
    // (We removed KeyListener, so these are gone)
}
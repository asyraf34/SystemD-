import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;
import java.awt.image.BufferedImage;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int scoreboardHeight = tileSize * 2;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize + scoreboardHeight;


    private Image backgroundImage;
    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    //X = wall, O = skip, P = pac man, ' ' = food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        //load images
        backgroundImage = new ImageIcon(getClass().getResource("/background.png")).getImage();
        wallImage = new ImageIcon(getClass().getResource("/wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("/blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("/orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("/pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("/redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("/pacmanRight.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        //how long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();

    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        boolean[][] wallMatrix = new boolean[rowCount][columnCount];

        for (int r = 0 ; r < rowCount ; r++) {
            for (int c = 0 ; c < columnCount ; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = scoreboardHeight + r*tileSize;

                if (tileMapChar == 'X') {
                    wallMatrix[r][c] = true;
                }
                else if (tileMapChar == 'b') {
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') {
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') {
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') {
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') {
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') {
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
        for (int r = 0 ; r < rowCount ; r++) {
            for (int c = 0 ; c < columnCount ; c++) {
                if (!wallMatrix[r][c]) {
                    continue;
                }

                int x = c*tileSize;
                int y = scoreboardHeight + r*tileSize;
                Image connectedWallImage = createWallTexture(wallMatrix, r, c);
                Block wall = new Block(connectedWallImage, x, y, tileSize, tileSize);
                walls.add(wall);
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {

        Graphics2D graphics2D = (Graphics2D) g.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawScoreboard(graphics2D);

        for (Block wall : walls) {
            graphics2D.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }
        graphics2D.setColor(new Color(120, 180, 255));
        for (Block food : foods) {
            graphics2D.fillRoundRect(food.x, food.y, food.width, food.height, food.width, food.height);
        }
        for (Block ghost : ghosts){
            graphics2D.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        //for score
        graphics2D.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);


        if (gameOver) {
            drawGameOverOverlay(graphics2D);
        }

        graphics2D.dispose();
    }

    private void drawScoreboard(Graphics2D graphics2D) {
        int padding = tileSize / 3;
        int scoreboardX = padding;
        int scoreboardY = padding / 2;
        int scoreboardWidth = boardWidth - padding * 2;
        int scoreboardBodyHeight = scoreboardHeight - padding;

        graphics2D.setColor(new Color(10, 5, 30, 230));
        graphics2D.fillRect(0, 0, boardWidth, scoreboardHeight);

        GradientPaint backgroundPaint = new GradientPaint(
                scoreboardX,
                scoreboardY,
                new Color(20, 10, 60, 220),
                scoreboardX,
                scoreboardY + scoreboardBodyHeight,
                new Color(60, 30, 120, 200)
        );
        graphics2D.setPaint(backgroundPaint);
        graphics2D.fillRoundRect(scoreboardX, scoreboardY, scoreboardWidth, scoreboardBodyHeight, tileSize, tileSize);

        graphics2D.setStroke(new BasicStroke(Math.max(2, tileSize / 12f)));
        graphics2D.setColor(new Color(150, 130, 255, 220));
        graphics2D.drawRoundRect(scoreboardX, scoreboardY, scoreboardWidth, scoreboardBodyHeight, tileSize, tileSize);

        Font scoreFont = new Font("SansSerif", Font.BOLD, Math.max(18, scoreboardBodyHeight / 2));
        graphics2D.setFont(scoreFont);
        FontMetrics metrics = graphics2D.getFontMetrics();
        int textBaseline = scoreboardY + (scoreboardBodyHeight + metrics.getAscent() - metrics.getDescent()) / 2;

        String scoreLabel = String.format("SCORE %04d", Math.max(0, score));
        graphics2D.setColor(Color.WHITE);
        graphics2D.drawString(scoreLabel, scoreboardX + padding, textBaseline);

        String livesLabel = "LIVES";
        Font livesFont = scoreFont.deriveFont(Font.PLAIN, scoreFont.getSize() * 0.85f);
        FontMetrics livesMetrics = graphics2D.getFontMetrics(livesFont);
        graphics2D.setFont(livesFont);
        int lifeIconSize = scoreboardBodyHeight - padding * 2;
        lifeIconSize = Math.max(tileSize, lifeIconSize);
        int lifeSpacing = tileSize / 4;
        int livesLabelWidth = livesMetrics.stringWidth(livesLabel);
        int livesIconsWidth = lives * lifeIconSize + Math.max(0, lives - 1) * lifeSpacing;
        int totalLivesWidth = livesLabelWidth + lifeSpacing + livesIconsWidth;
        int livesStartX = scoreboardX + (scoreboardWidth - totalLivesWidth) / 2;

        graphics2D.setColor(new Color(210, 200, 255));
        graphics2D.drawString(livesLabel, livesStartX, textBaseline);

        int iconsStartX = livesStartX + livesLabelWidth + lifeSpacing;
        int iconY = scoreboardY + (scoreboardBodyHeight - lifeIconSize) / 2;
        for (int i = 0; i < lives; i++) {
            graphics2D.drawImage(pacmanRightImage, iconsStartX + i * (lifeIconSize + lifeSpacing), iconY, lifeIconSize, lifeIconSize, null);
        }

        String multiplierLabel = "x" + lives;
        int multiplierX = iconsStartX + livesIconsWidth + lifeSpacing / 2;
        graphics2D.setFont(scoreFont.deriveFont(Font.BOLD, scoreFont.getSize() * 0.8f));
        graphics2D.setColor(new Color(255, 230, 120));
        graphics2D.drawString(multiplierLabel, multiplierX, textBaseline);
    }

    private void drawGameOverOverlay(Graphics2D graphics2D) {
        int overlayWidth = boardWidth - tileSize * 4;
        int overlayHeight = tileSize * 6;
        int overlayX = (boardWidth - overlayWidth) / 2;
        int overlayY = scoreboardHeight + (rowCount * tileSize - overlayHeight) / 2;

        graphics2D.setColor(new Color(0, 0, 0, 180));
        graphics2D.fillRoundRect(overlayX, overlayY, overlayWidth, overlayHeight, tileSize, tileSize);

        graphics2D.setColor(new Color(255, 220, 0));
        graphics2D.setStroke(new BasicStroke(4f));
        graphics2D.drawRoundRect(overlayX, overlayY, overlayWidth, overlayHeight, tileSize, tileSize);

        Font titleFont = new Font("SansSerif", Font.BOLD, tileSize * 2);
        graphics2D.setFont(titleFont);
        FontMetrics titleMetrics = graphics2D.getFontMetrics();
        String title = "GAME OVER";
        int titleX = overlayX + (overlayWidth - titleMetrics.stringWidth(title)) / 2;
        int titleY = overlayY + titleMetrics.getAscent() + tileSize / 2;
        graphics2D.drawString(title, titleX, titleY);

        Font infoFont = new Font("SansSerif", Font.PLAIN, tileSize);
        graphics2D.setFont(infoFont);
        FontMetrics infoMetrics = graphics2D.getFontMetrics();
        String scoreSummary = "Final Score: " + score;
        int scoreX = overlayX + (overlayWidth - infoMetrics.stringWidth(scoreSummary)) / 2;
        int scoreY = titleY + tileSize * 2;
        graphics2D.setColor(Color.WHITE);
        graphics2D.drawString(scoreSummary, scoreX, scoreY);

        String restartHint = "Press any key to restart";
        int hintX = overlayX + (overlayWidth - infoMetrics.stringWidth(restartHint)) / 2;
        int hintY = scoreY + tileSize * 3 / 2;
        graphics2D.drawString(restartHint, hintX, hintY);
    }

    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        //check wall collisions
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        //check ghost collisions
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
            }

            if (ghost.y == scoreboardHeight + tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }
        //check food collision
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }
    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        // System.out.println("KeyEvent: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }

        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        }
        else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        }
        else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        }
        else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }
    }
    private Image createWallTexture(boolean[][] wallMatrix, int row, int column) {
        BufferedImage texture = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = texture.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color baseColor = new Color(18, 9, 38);
        Color outlineColor = new Color(200, 00, 00);
        Color glowColor = new Color(192, 160, 255, 180);

        if (wallImage != null) {
            graphics2D.drawImage(wallImage, 0, 0, tileSize, tileSize, null);
        }

        graphics2D.setColor(baseColor);
        graphics2D.fillRect(0, 0, tileSize, tileSize);

        int borderThickness = Math.max(2, tileSize / 8);
        int glowThickness = Math.max(1, borderThickness / 2);
        int cornerDiameter = borderThickness * 2;

        boolean hasTop = row > 0 && wallMatrix[row - 1][column];
        boolean hasBottom = row < rowCount - 1 && wallMatrix[row + 1][column];
        boolean hasLeft = column > 0 && wallMatrix[row][column - 1];
        boolean hasRight = column < columnCount - 1 && wallMatrix[row][column + 1];

        // draw base fill to soften interior seams
        graphics2D.setColor(new Color(36, 18, 72));
        int innerWidth = Math.max(0, tileSize - borderThickness * 2);
        int innerHeight = Math.max(0, tileSize - borderThickness * 2);
        if (innerWidth > 0 && innerHeight > 0) {
            graphics2D.fillRect(borderThickness, borderThickness, innerWidth, innerHeight);
        }

        graphics2D.setColor(outlineColor);
        if (!hasTop) {
            graphics2D.fillRect(0, 0, tileSize, borderThickness);
        }
        if (!hasBottom) {
            graphics2D.fillRect(0, tileSize - borderThickness, tileSize, borderThickness);
        }
        if (!hasLeft) {
            graphics2D.fillRect(0, 0, borderThickness, tileSize);
        }
        if (!hasRight) {
            graphics2D.fillRect(tileSize - borderThickness, 0, borderThickness, tileSize);
        }

        // add glow that bleeds slightly into the pathways for visibility
        graphics2D.setColor(glowColor);
        int horizontalGlowWidth = Math.max(0, tileSize - borderThickness * 2);
        int verticalGlowHeight = Math.max(0, tileSize - borderThickness * 2);

        if (!hasTop && horizontalGlowWidth > 0) {
            graphics2D.fillRect(borderThickness, borderThickness - glowThickness, horizontalGlowWidth, glowThickness);
        }
        if (!hasBottom && horizontalGlowWidth > 0) {
            graphics2D.fillRect(borderThickness, tileSize - borderThickness, horizontalGlowWidth, glowThickness);
        }
        if (!hasLeft && verticalGlowHeight > 0) {
            graphics2D.fillRect(borderThickness - glowThickness, borderThickness, glowThickness, verticalGlowHeight);
        }
        if (!hasRight && verticalGlowHeight > 0) {
            graphics2D.fillRect(tileSize - borderThickness, borderThickness, glowThickness, verticalGlowHeight);
        }

        graphics2D.setColor(outlineColor);
        if (!hasTop && !hasLeft) {
            graphics2D.fillArc(0, 0, cornerDiameter, cornerDiameter, 180, 90);
        }
        if (!hasTop && !hasRight) {
            graphics2D.fillArc(tileSize - cornerDiameter, 0, cornerDiameter, cornerDiameter, 270, 90);
        }
        if (!hasBottom && !hasLeft) {
            graphics2D.fillArc(0, tileSize - cornerDiameter, cornerDiameter, cornerDiameter, 90, 90);
        }
        if (!hasBottom && !hasRight) {
            graphics2D.fillArc(tileSize - cornerDiameter, tileSize - cornerDiameter, cornerDiameter, cornerDiameter, 0, 90);
        }

        graphics2D.dispose();
        return texture;
    }
}
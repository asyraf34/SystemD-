import java.awt.Image;
import javax.swing.ImageIcon;

/**
 * Loads and provides access to all game image assets.
 */
public class AssetManager {

    // All images are private
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
    private Image foodImage;
    private Image knifeImage;
    private Image pacmanUpKnifeImage;
    private Image pacmanDownKnifeImage;
    private Image pacmanLeftKnifeImage;
    private Image pacmanRightKnifeImage;
    private Image invertboss;
    private Image projectile;

    // Scaled food dimensions
    private int foodWidth;
    private int foodHeight;

    private static final String FOOD_IMAGE_RESOURCE = "/goldFood.png";

    public AssetManager(int tileSize) {
        loadImages();
        scaleFoodImage(tileSize);
    }

    private void loadImages() {
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
        knifeImage = new ImageIcon(getClass().getResource("/knife.png")).getImage();
        pacmanUpKnifeImage = new ImageIcon(getClass().getResource("/pacmanUp-with-knife.png")).getImage();
        pacmanDownKnifeImage = new ImageIcon(getClass().getResource("/pacmanDown-with-knife.png")).getImage();
        pacmanLeftKnifeImage = new ImageIcon(getClass().getResource("/pacmanLeft-with-knife.png")).getImage();
        pacmanRightKnifeImage = new ImageIcon(getClass().getResource("/pacmanRight-with-knife.png")).getImage();
        invertboss = new ImageIcon(getClass().getResource("/invertboss.png")).getImage();
        projectile = new ImageIcon(getClass().getResource("/projectile.png")).getImage();

        // Food is loaded separately for scaling
        ImageIcon foodIcon = new ImageIcon(getClass().getResource(FOOD_IMAGE_RESOURCE));
        foodImage = foodIcon.getImage();
        foodWidth = foodIcon.getIconWidth();
        foodHeight = foodIcon.getIconHeight();
    }

    private void scaleFoodImage(int tileSize) {
        double maxFoodCoverage = 0.6;
        int maxFoodWidth = (int) Math.round(tileSize * maxFoodCoverage);
        int maxFoodHeight = (int) Math.round(tileSize * maxFoodCoverage);
        double widthScale = (double) maxFoodWidth / foodWidth;
        double heightScale = (double) maxFoodHeight / foodHeight;
        double scale = Math.min(1.0, Math.min(widthScale, heightScale));

        if (scale < 1.0) {
            foodWidth = Math.max(1, (int) Math.round(foodWidth * scale));
            foodHeight = Math.max(1, (int) Math.round(foodHeight * scale));
            foodImage = foodImage.getScaledInstance(foodWidth, foodHeight, Image.SCALE_SMOOTH);
        }
    }

    // --- Public Getters ---

    public Image getBackgroundImage() { return backgroundImage; }
    public Image getWallImage() { return wallImage; }
    public Image getBlueGhostImage() { return blueGhostImage; }
    public Image getOrangeGhostImage() { return orangeGhostImage; }
    public Image getPinkGhostImage() { return pinkGhostImage; }
    public Image getRedGhostImage() { return redGhostImage; }
    public Image getPacmanUpImage() { return pacmanUpImage; }
    public Image getPacmanDownImage() { return pacmanDownImage; }
    public Image getPacmanLeftImage() { return pacmanLeftImage; }
    public Image getPacmanRightImage() { return pacmanRightImage; }
    public Image getFoodImage() { return foodImage; }
    public Image getKnifeImage() { return knifeImage; }
    public Image getPacmanUpKnifeImage() { return pacmanUpKnifeImage; }
    public Image getPacmanDownKnifeImage() { return pacmanDownKnifeImage; }
    public Image getPacmanLeftKnifeImage() { return pacmanLeftKnifeImage; }
    public Image getPacmanRightKnifeImage() { return pacmanRightKnifeImage; }
    public Image getInvertBoss() { return invertboss; }
    public Image getProjectile() { return projectile; }

    public int getFoodWidth() { return foodWidth; }
    public int getFoodHeight() { return foodHeight; }
}
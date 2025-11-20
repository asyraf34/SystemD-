import java.awt.Image;
import java.util.Objects;
import javax.swing.ImageIcon;

/**
 * Loads and provides access to all game image assets.
 */
public class AssetManager {

    // All images are private
    private Image backgroundImage, wallImage;
    private Image blueGhostImage, orangeGhostImage, pinkGhostImage, redGhostImage;
    private Image pacmanUpImage, pacmanDownImage, pacmanLeftImage, pacmanRightImage;
    private Image pacmanUpKnifeImage, pacmanDownKnifeImage, pacmanLeftKnifeImage, pacmanRightKnifeImage;
    private Image knifeImage, foodImage;
    private Image BossImage, BossReflectImage, ProjectileImage;

    // Scaled food dimensions
    private int foodWidth;
    private int foodHeight;

    private static final String FOOD_IMAGE_RESOURCE = "/goldFood.png";

    public AssetManager(int tileSize) {
        loadImages();
        scaleFoodImage(tileSize);
    }

    private void loadImages() {
        // 1. Load standard images using the helper method
        backgroundImage       = loadImage("/background.png");
        wallImage             = loadImage("/wall.png");

        blueGhostImage        = loadImage("/blueGhost.png");
        orangeGhostImage      = loadImage("/orangeGhost.png");
        pinkGhostImage        = loadImage("/pinkGhost.png");
        redGhostImage         = loadImage("/redGhost.png");

        pacmanUpImage         = loadImage("/pacmanUp.png");
        pacmanDownImage       = loadImage("/pacmanDown.png");
        pacmanLeftImage       = loadImage("/pacmanLeft.png");
        pacmanRightImage      = loadImage("/pacmanRight.png");

        knifeImage            = loadImage("/knife.png");

        pacmanUpKnifeImage    = loadImage("/pacmanUp-with-knife.png");
        pacmanDownKnifeImage  = loadImage("/pacmanDown-with-knife.png");
        pacmanLeftKnifeImage  = loadImage("/pacmanLeft-with-knife.png");
        pacmanRightKnifeImage = loadImage("/pacmanRight-with-knife.png");

        BossImage             = loadImage("/BossImage.png");
        BossReflectImage      = loadImage("/BossReflectImage.png");
        ProjectileImage       = loadImage("/ProjectileImage.png");

        // 2. Food is a special case (we need the dimensions from the Icon)
        ImageIcon foodIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource(FOOD_IMAGE_RESOURCE)));
        foodImage = foodIcon.getImage();
        foodWidth = foodIcon.getIconWidth();
        foodHeight = foodIcon.getIconHeight();
    }

    private Image loadImage(String path) {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource(path))).getImage();
    }

    private void scaleFoodImage(int tileSize) {
        double maxFoodCoverage = 0.6;
        int maxFoodWidth = (int) Math.round(tileSize * maxFoodCoverage);
        int maxFoodHeight = (int) Math.round(tileSize * maxFoodCoverage);

        // Calculate scale to fit within max bounds while maintaining aspect ratio
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
    public Image getBossImage() { return BossImage; }
    public Image getBossReflectImage() { return BossReflectImage; }
    public Image getProjectileImage() { return ProjectileImage; }

    public int getFoodWidth() { return foodWidth; }
    public int getFoodHeight() { return foodHeight; }
}
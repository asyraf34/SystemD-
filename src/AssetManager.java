import java.awt.Image;
import java.net.URL;
import java.util.Objects;
import javax.swing.ImageIcon;

/**
 * Loads and provides access to all game image assets.
 *
 * Added: optional decorative icons (mute/volume).
 * - These are loaded via loadOptionalImage so missing icons won't crash the game.
 * - Put icons on the classpath at /icons/mute.png and /icons/volume.png (recommended)
 *   or package them with your resources (e.g., src/main/resources/icons/...).
 */
public class AssetManager {
    // All images are private
    private Image backgroundImage, wallImage;
    private Image blueGhostImage, orangeGhostImage, pinkGhostImage, redGhostImage;
    private Image pacmanUpImage, pacmanDownImage, pacmanLeftImage, pacmanRightImage;
    private Image pacmanUpKnifeImage, pacmanDownKnifeImage, pacmanLeftKnifeImage, pacmanRightKnifeImage;
    private Image knifeImage, foodImage;
    private Image BossImage, BossReflectImage, ProjectileImage;

    // Optional decorative icons
    private Image iconMuteImage;
    private Image iconVolumeImage;

    // Scaled food dimensions
    private int foodWidth;
    private int foodHeight;

    private static final String FOOD_IMAGE_RESOURCE = "/goldFood.png";

    public AssetManager(int tileSize) {
        loadImages();
        scaleFoodImage(tileSize);
    }

    private void loadImages() {
        // 1. Load standard images using the helper method (these are required)
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

        // 3. Optional decorative icons -- load safely (do not throw if missing)
        iconMuteImage   = loadOptionalImage("/mute.png");
        iconVolumeImage = loadOptionalImage("/volume.png");
    }

    // Existing helper: will throw if the required resource is missing (keeps prior behavior)
    private Image loadImage(String path) {
        URL res = getClass().getResource(path);
        return new ImageIcon(Objects.requireNonNull(res)).getImage();
    }

    // New helper: returns null if the resource is not available (optional icons)
    private Image loadOptionalImage(String path) {
        try {
            URL res = getClass().getResource(path);
            if (res == null) return null;
            return new ImageIcon(res).getImage();
        } catch (Throwable t) {
            return null;
        }
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

    // Optional decorative icon getters (may return null if not provided)
    public Image getIconMuteImage() { return iconMuteImage; }
    public Image getIconVolumeImage() { return iconVolumeImage; }

    public int getFoodWidth() { return foodWidth; }
    public int getFoodHeight() { return foodHeight; }
}
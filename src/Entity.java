import java.awt.Image;

/**
 * Represents a static game object with position, dimensions, and an image.
 * Used for walls, food, and knives.
 */
public class Entity {
    public int x;
    public int y;
    public int width;
    public int height;
    public Image image;

    public Entity(Image image, int x, int y, int width, int height) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Basic AABB collision detection.
     */
    public boolean collidesWith(Entity other) {
        return this.x < other.x + other.width &&
                this.x + this.width > other.x &&
                this.y < other.y + other.height &&
                this.y + this.height > other.y;
    }
}
import java.awt.Image;

/**
 * Represents a movable game object (an "actor") like Pac-Man or a Ghost.
 * Extends Entity and adds movement, direction, and state.
 */
public class Actor extends Entity {

    public final int startX;
    public final int startY;

    // CHANGED: Now uses the Enum
    public Direction direction = Direction.NONE;

    public int velocityX = 0;
    public int velocityY = 0;
    public int speed;

    public boolean isMoving = false;
    public int targetX, targetY;

    public Actor(Image image, int x, int y, int width, int height, int speed) {
        super(image, x, y, width, height);
        this.startX = x;
        this.startY = y;
        this.speed = speed;
        this.targetX = x;
        this.targetY = y;
    }

    /**
     * Updates the actor's velocity based on its current direction.
     */
    public void updateVelocity() {
        // CHANGED: Much simpler using the helper method in Direction
        this.velocityX = direction.getDx(speed);
        this.velocityY = direction.getDy(speed);
    }

    /**
     * Resets the actor to its starting position.
     */
    public void reset() {
        this.x = this.startX;
        this.y = this.startY;
        this.targetX = this.startX;
        this.targetY = this.startY;
        this.velocityX = 0;
        this.velocityY = 0;
        this.direction = Direction.NONE; // CHANGED
        this.isMoving = false;
    }
}
import java.awt.Image;

/**
 * Represents a movable game object (an "actor") like Pac-Man or a Ghost.
 * Extends Entity and adds movement, direction, and state.
 */
public class Actor extends Entity {

    public final int startX;
    public final int startY;

    public char direction = 0; // U D L R
    public int velocityX = 0;
    public int velocityY = 0;
    public int speed;

    // For smooth tile-based movement
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
        if (this.direction == 'U') {
            this.velocityX = 0;
            this.velocityY = -speed;
        } else if (this.direction == 'D') {
            this.velocityX = 0;
            this.velocityY = speed;
        } else if (this.direction == 'L') {
            this.velocityX = -speed;
            this.velocityY = 0;
        } else if (this.direction == 'R') {
            this.velocityX = speed;
            this.velocityY = 0;
        }
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
        this.direction = 0;
        this.isMoving = false;
    }
}
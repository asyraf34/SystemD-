import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

enum Direction { UP, DOWN, LEFT, RIGHT }

class MafiaMovement {
    int x, y;
    int speed = 1;
    boolean canMove = true;
    boolean collidedWithWall = false;

    MafiaMovement(int x, int y) { this.x = x; this.y = y; }

    void move(Direction dir) {
        if (!canMove || collidedWithWall) return;
        switch (dir) {
            case UP:    y -= speed; break;
            case DOWN:  y += speed; break;
            case LEFT:  x -= speed; break;
            case RIGHT: x += speed; break;
        }
    }
}

public class MafiaMovementTest {
    MafiaMovement mafia;

    @BeforeEach
    void setup() {
        mafia = new MafiaMovement(5, 5);
        mafia.speed = 2; // easy assertion of movement
    }

    @Test
    void testMoveUp() {
        mafia.move(Direction.UP);
        assertEquals(5, mafia.x);
        assertEquals(3, mafia.y); // 5-2
    }

    @Test
    void testMoveDown() {
        mafia.move(Direction.DOWN);
        assertEquals(5, mafia.x);
        assertEquals(7, mafia.y); // 5+2
    }

    @Test
    void testMoveLeft() {
        mafia.move(Direction.LEFT);
        assertEquals(3, mafia.x); // 5-2
        assertEquals(5, mafia.y);
    }

    @Test
    void testMoveRight() {
        mafia.move(Direction.RIGHT);
        assertEquals(7, mafia.x); // 5+2
        assertEquals(5, mafia.y);
    }

    @Test
    void testMovementBlocked_cannotMove() {
        mafia.canMove = false;
        mafia.move(Direction.UP);
        assertEquals(5, mafia.x);
        assertEquals(5, mafia.y); // unchanged
    }

    @Test
    void testMovementBlocked_byWall() {
        mafia.collidedWithWall = true;
        mafia.move(Direction.RIGHT);
        assertEquals(5, mafia.x);
        assertEquals(5, mafia.y); // unchanged
    }


}
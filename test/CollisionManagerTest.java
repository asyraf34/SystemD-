import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.awt.Image;

class Entity {
    int x, y, width, height;
    public boolean collidesWith(Entity o) { return false; }
}

class Actor extends Entity {
    boolean collideFlag = false;
    int speed;
    Image image;
    @Override
    public boolean collidesWith(Entity o) { return collideFlag; }
}

class Boss extends Actor {
    private boolean reflecting = false;
    private int lives;
    public Boss(int lives) { this.lives = lives; }
    public void setReflecting(boolean r) { reflecting = r; }
    public boolean isReflecting() { return reflecting; }
    public boolean takeDamage() { if(reflecting) return true; lives--; return lives > 0; }
}

class SoundManager {
    void playEffect(String s) {}
}

class GameState {
    Actor pacman = new Actor();
    Boss boss = null;
    HashSet<Actor> projectiles = new HashSet<>(); // FIXED type!
    boolean hasWeapon = false;
    int knifeCount = 0;
    int score = 0;
    int lives = 3;
    boolean gameOver = false;
    List<Object> animations = new ArrayList<>();
}

public class CollisionManagerTest {
    private CollisionManager collisionManager;
    private GameState state;
    private SoundManager soundManager;

    @BeforeEach
    void setup() {
        collisionManager = new CollisionManager();
        soundManager = new SoundManager();
        state = new GameState();
        state.pacman = new Actor();
    }

    @Test
    void testBossCollision_none() {
        state.boss = null;
        int result = collisionManager.checkBossCollisions(state, soundManager);
        assertEquals(CollisionManager.GHOST_COLLISION_NONE, result);
    }

    @Test
    void testBossCollision_instantDeath() {
        state.boss = new Boss(1);
        state.boss.image = null;
        state.hasWeapon = false;
        state.knifeCount = 0;
        state.pacman.collideFlag = true;
        int result = collisionManager.checkBossCollisions(state, soundManager);
        assertEquals(CollisionManager.GHOST_COLLISION_LIFE_LOST, result);
    }

    @Test
    void testBossCollision_reflecting() {
        state.boss = new Boss(1);
        state.boss.image = null;
        state.hasWeapon = true;
        state.knifeCount = 1;
        state.boss.setReflecting(true);
        state.pacman.collideFlag = true;
        int result = collisionManager.checkBossCollisions(state, soundManager);
        assertEquals(CollisionManager.GHOST_COLLISION_LIFE_LOST, result);
    }

    @Test
    void testProjectileCollision_none() {
        int result = collisionManager.checkProjectileCollisions(state, soundManager);
        assertEquals(CollisionManager.GHOST_COLLISION_NONE, result);
    }

    @Test
    void testProjectileCollision_hit() {
        Actor projectile = new Actor();
        projectile.collideFlag = false;
        projectile.image = null;
        state.projectiles.add(projectile); // HashSet<Actor>
        state.pacman.collideFlag = true;
        int result = collisionManager.checkProjectileCollisions(state, soundManager);
        assertEquals(CollisionManager.GHOST_COLLISION_LIFE_LOST, result);
        assertTrue(state.projectiles.isEmpty());
    }
}
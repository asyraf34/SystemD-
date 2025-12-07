import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;


public class GameState {
    public int score = 0;
    public int lives = 3;
    public boolean gameOver = false;
    public boolean gameWon = false;
    public int currentLevel = 1;
    public int knifeCount = 0;
    public boolean hasWeapon = false;
    public Boss boss;
    public boolean bossState = true;

    // Sprint State
    public boolean sprintActive = false;
    public int sprintTicksRemaining = 0;
    public int sprintCooldownTicks = 0;

    // Entities
    public HashSet<Actor> projectiles = new HashSet<>();
    public HashSet<Entity> walls = new HashSet<>();
    public HashSet<Entity> foods = new HashSet<>();
    public HashSet<Entity> knives = new HashSet<>();
    public HashSet<Actor> ghosts = new HashSet<>();
    public Actor pacman;

    // create grid on the game map for heuristic
    public boolean[][] walkableGrid;

    // Level Transition State
    public boolean interLevel = false;
    public int interLevelTicks = 0;
    public int nextLevelToStart = 0;
    public int restartDebounceTicks = 0;

    public List<DeathAnimation> animations = new ArrayList<>();

    //false = no kill route
    //true = kill route
    public boolean route = false;
    public boolean ghostKill = false; //check ghost kill
    public boolean gameEndProcessed = false;

}
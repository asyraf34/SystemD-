public class GameConstants {
    // --- Dimensions ---
    public static final int TILE_SIZE = 32;
    public static final int SCREEN_WIDTH = 19 * TILE_SIZE;  // Based on map columns
    public static final int SCREEN_HEIGHT = 21 * TILE_SIZE; // Based on map rows

    // --- Game Speed Balance ---
    public static final int SPEED_PACMAN = TILE_SIZE / 1;
    public static final int SPEED_PACMAN_SPRINT = SPEED_PACMAN * 2;
    public static final int SPEED_GHOST = TILE_SIZE / 3;
    public static final int SPEED_BOSS = TILE_SIZE / 3;
    public static final int SPEED_PROJECTILE_BONUS = 2;

    // --- Gameplay Settings (legacy defaults kept for compatibility) ---
    // Legacy defaults (kept for backward compatibility). Prefer using startingKnives(mode) and maxLives(mode).
    public static final int MAX_LIVES = 3;
    public static final int BOSS_LIVES = 3;
    public static final int STARTING_KNIVES = 5;

    // --- Timers (in Game Ticks) ---
    public static final int TIMER_INTERLEVEL = 15;   // Time between levels
    public static final int TIMER_RESTART = 10;      // Delay before restart allowed
    public static final int TIMER_BOSS_NORMAL = 200; // 10 seconds (at 20fps)
    public static final int TIMER_BOSS_REFLECT = 100;// 5 seconds
    public static final int TIMER_BOSS_ATTACK = 40;  // 2 seconds
    public static final int TIMER_SPRINT_DURATION = 30; // 1.5 seconds at 20 updates per second
    public static final int TIMER_SPRINT_COOLDOWN = 100; // 5 seconds cooldown (legacy default)

    // --- Asset Paths ---
    public static final String SOUND_MENU = "audio/menu_music.wav";
    public static final String SOUND_MENU_SIREN = "audio/police_siren.wav";
    public static final String SOUND_START = "audio/start.wav";
    public static final String SOUND_GAME = "audio/background.wav";
    public static final String SOUND_MOVE = "audio/move.wav";
    public static final String SOUND_KNIFE = "audio/knife_pick.wav";

    // --- Mode-aware helpers ---
    // Play mode: 3 knives, 3 lives
    // Demo mode: 5 knives, 5 lives
    public static int startingKnives(GameMode mode) {
        if (mode == null) return STARTING_KNIVES;
        return (mode == GameMode.DEMO) ? 5 : 3;
    }

    public static int maxLives(GameMode mode) {
        if (mode == null) return MAX_LIVES;
        return (mode == GameMode.DEMO) ? 5 : 3;
    }

    private static final int DEMO_COOLDOWN_TICKS = 60; // shorter cooldown for demo

    public static int sprintCooldown(GameMode mode) {
        if (mode == GameMode.DEMO) return DEMO_COOLDOWN_TICKS;
        return TIMER_SPRINT_COOLDOWN;
    }
}
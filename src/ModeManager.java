/**
 * Small helper to store the user's selected game mode.
 * Default is PLAY.
 */
public final class ModeManager {
    private static volatile GameMode selectedMode = GameMode.PLAY;

    private ModeManager() {}

    public static GameMode getSelectedMode() {
        return selectedMode;
    }

    public static void setSelectedMode(GameMode mode) {
        selectedMode = (mode == null) ? GameMode.PLAY : mode;
    }
}
import java.awt.image.BufferedImage;

/**
 * Simple singleton to hold pause state and snapshot.
 * Renderer will set snapshot when pausing; clear when unpausing.
 */
public class PauseManager {
    private static final PauseManager INSTANCE = new PauseManager();

    private volatile boolean paused = false;
    private volatile BufferedImage pauseSnapshot = null;

    private PauseManager() {}

    public static PauseManager getInstance() {
        return INSTANCE;
    }

    public synchronized void setPaused(boolean p) {
        if (this.paused == p) return;
        this.paused = p;
        if (p) {
            // entering pause: clear old snapshot so renderer captures a fresh one
            this.pauseSnapshot = null;
        } else {
            // leaving pause: remove snapshot so it won't be reused
            this.pauseSnapshot = null;
        }
    }

    public synchronized void togglePaused() {
        setPaused(!paused);
    }

    public boolean isPaused() {
        return paused;
    }

    public synchronized void setPauseSnapshot(BufferedImage img) {
        this.pauseSnapshot = img;
    }

    public synchronized BufferedImage getPauseSnapshot() {
        return pauseSnapshot;
    }

    public synchronized void clearPauseSnapshot() {
        this.pauseSnapshot = null;
    }
}
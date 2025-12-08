import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PauseManager {
    private boolean paused = false;

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }
}

public class PauseTest {
    PauseManager pauseManager;

    @BeforeEach
    void setup() {
        pauseManager = new PauseManager();
    }

    @Test
    void testSetPausedTrue() {
        pauseManager.setPaused(true);
        assertTrue(pauseManager.isPaused(), "Pause state should be true after setPaused(true)");
    }

    @Test
    void testSetPausedFalse() {
        pauseManager.setPaused(true); // Pause first
        pauseManager.setPaused(false); // Then unpause
        assertFalse(pauseManager.isPaused(), "Pause state should be false after setPaused(false)");
    }
}
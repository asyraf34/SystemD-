import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;

public class InputHandler implements KeyListener {

    private final HashSet<Integer> pressedKeys = new HashSet<>();
    private boolean pHeld = false;
    private final SoundManager soundManager; // injected

    // volume step for +/- keys
    private static final float VOLUME_STEP = 0.05f;

    public InputHandler(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    public Direction getDirection() {
        if (pressedKeys.contains(KeyEvent.VK_W) || pressedKeys.contains(KeyEvent.VK_UP)) return Direction.UP;
        if (pressedKeys.contains(KeyEvent.VK_S) || pressedKeys.contains(KeyEvent.VK_DOWN)) return Direction.DOWN;
        if (pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT)) return Direction.LEFT;
        if (pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT)) return Direction.RIGHT;
        return Direction.NONE;
    }

    public boolean isSprintPressed() {
        return pressedKeys.contains(KeyEvent.VK_SPACE);
    }
    public void clear() { pressedKeys.clear(); }
    public boolean anyKeyPressed() { return !pressedKeys.isEmpty(); }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int kc = e.getKeyCode();

        // Pause toggle (P)
        if (kc == KeyEvent.VK_P) {
            if (!pHeld) {
                PauseManager.getInstance().togglePaused();
                pHeld = true;
            }
            return; // P is a control key; don't add to pressedKeys
        }

        // Volume keyboard shortcuts (use injected soundManager if available)
        if (soundManager != null) {
            // Increase volume: + or = (shift+'=')
            if (kc == KeyEvent.VK_EQUALS || e.getKeyChar() == '+') {
                soundManager.changeBackgroundVolumeBy(VOLUME_STEP);
                return;
            }
            // Numpad add (+)
            if (kc == KeyEvent.VK_ADD) {
                soundManager.changeBackgroundVolumeBy(VOLUME_STEP);
                return;
            }
            // Decrease volume: -
            if (kc == KeyEvent.VK_MINUS || e.getKeyChar() == '-') {
                soundManager.changeBackgroundVolumeBy(-VOLUME_STEP);
                return;
            }
            if (kc == KeyEvent.VK_SUBTRACT) {
                soundManager.changeBackgroundVolumeBy(-VOLUME_STEP);
                return;
            }
        }

        // existing behavior: movement / sprint keys
        pressedKeys.add(kc);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int kc = e.getKeyCode();
        if (kc == KeyEvent.VK_P) {
            pHeld = false;
            return;
        }
        pressedKeys.remove(kc);
    }
}
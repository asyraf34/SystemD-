import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;

/**
 * Manages all keyboard input for the game.
 * It listens for key presses and releases and provides simple
 * methods to check the current state of movement keys.
 */
public class InputHandler implements KeyListener {

    private final HashSet<Integer> pressedKeys = new HashSet<>();
    private String lastMoveSound = "audio/move.wav"; // Assuming this is your sound

    // Public flags that the game can check
    public boolean isUpPressed() {
        return pressedKeys.contains(KeyEvent.VK_UP);
    }

    public boolean isDownPressed() {
        return pressedKeys.contains(KeyEvent.VK_DOWN);
    }

    public boolean isLeftPressed() {
        return pressedKeys.contains(KeyEvent.VK_LEFT);
    }

    public boolean isRightPressed() {
        return pressedKeys.contains(KeyEvent.VK_RIGHT);
    }

    /**
     * Optional: A way for the game to ask if a sound should be played.
     * This is just an example of moving logic here.
     * You would call this and reset() from your game loop.
     */
    public boolean didMoveKeyRelease(SoundManager soundManager) {
        return false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());

    }
    public void clear() {
        pressedKeys.clear();
    }
    public boolean anyKeyPressed() {
        return !pressedKeys.isEmpty();
    }

}
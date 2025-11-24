import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;

public class InputHandler implements KeyListener {

    private final HashSet<Integer> pressedKeys = new HashSet<>();
    private boolean pHeld = false;

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
    //@Override
    //public void keyPressed(KeyEvent e) { pressedKeys.add(e.getKeyCode()); }
    //@Override
    //public void keyReleased(KeyEvent e) { pressedKeys.remove(e.getKeyCode()); }

    @Override
    public void keyPressed(KeyEvent e) {
        int kc = e.getKeyCode();

        // Handle pause toggle on 'P' (case-insensitive)
        if (kc == KeyEvent.VK_P) {
            if (!pHeld) {
                PauseManager.getInstance().togglePaused(); // ensure PauseManager class is present
                pHeld = true;
            }
            // don't add 'P' into pressedKeys (it is a control key)
            return;
        }

        // existing behavior for movement / sprint etc.
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
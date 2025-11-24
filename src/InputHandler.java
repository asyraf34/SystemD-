import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;

public class InputHandler implements KeyListener {

    private final HashSet<Integer> pressedKeys = new HashSet<>();

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
    public void keyPressed(KeyEvent e) { pressedKeys.add(e.getKeyCode()); }
    @Override
    public void keyReleased(KeyEvent e) { pressedKeys.remove(e.getKeyCode()); }
}

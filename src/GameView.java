import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.KeyListener;


public class GameView extends JPanel {
    private final Renderer renderer;
    private final GameState state;

    public GameView(Renderer renderer, GameState state, int width, int height, KeyListener input) {
        this.renderer = renderer;
        this.state = state;
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.LIGHT_GRAY);
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(input);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (renderer != null && state != null) {
            renderer.drawGame(g, this, state);
        }
    }
}
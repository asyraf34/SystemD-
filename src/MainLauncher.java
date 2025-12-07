import javax.swing.*;

import java.awt.*;

/**
 * Launcher that works with the existing MenuPanel(Runnable) constructor.
 * MenuPanel is expected to set ModeManager before invoking the Runnable.
 */
public class MainLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MAN - HUNT");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            // Use Runnable constructor so types match your MenuPanel(Runnable).
            MenuPanel menu = new MenuPanel(() -> {
                // MenuPanel should have stored the selected mode in ModeManager.
                GameMode mode = ModeManager.getSelectedMode();

                // Create game with the selected mode so PacMan uses correct lives/knives.
                PacMan game = new PacMan(mode);

                frame.getContentPane().removeAll();
                frame.getContentPane().add(game, BorderLayout.CENTER);
                frame.revalidate();
                frame.repaint();

                // Give focus to the game so input works immediately
                game.requestFocusInWindow();
            });

            frame.getContentPane().add(menu, BorderLayout.CENTER);
            frame.setSize(800, 720);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // give focus to menu for key handling
            menu.requestFocusInWindow();
        });
    }
}
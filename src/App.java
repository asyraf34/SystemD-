import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) throws Exception {
        int rowCount = 21;
        int columnCount = 19;
        int tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int scoreboardHeight = tileSize * 2;
        int boardHeight = rowCount * tileSize + scoreboardHeight;

        JFrame frame = new JFrame("Pac Man");
        // frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardLayout cardLayout = new CardLayout();
        JPanel menuPanel  = new JPanel(cardLayout);
        PacMan pacmanGame = new PacMan();

        MenuPanel menu = new MenuPanel(() -> {
            cardLayout.show(menuPanel, "GAME");
            pacmanGame.setFocusable(true);
            pacmanGame.requestFocusInWindow();
        });

        menuPanel.add(menu, "MENU");
        menuPanel.add(pacmanGame, "GAME");

        frame.add(menuPanel);
        frame.pack();
        frame.setVisible(true);

        cardLayout.show(menuPanel, "MENU");
    }
}

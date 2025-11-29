import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        int rowCount = 21;
        int columnCount = 19;
        int tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int scoreboardHeight = tileSize * 2;
        int boardHeight = rowCount * tileSize + scoreboardHeight;

        JFrame frame = new JFrame("Pac Man");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardLayout cardLayout = new CardLayout();
        JPanel menuPanel = new JPanel(cardLayout);

        String[][] story = ReadStoryFile.getStorySets();
        PacMan pacmanGame = new PacMan();  // Create upfront

        // Menu -> Cutscene -> Game flow
        MenuPanel menu = new MenuPanel(() -> {
            CutscenePanel cutscene = new CutscenePanel(story, () -> {
                cardLayout.show(menuPanel, "GAME");
                pacmanGame.requestFocusInWindow();
            });

            menuPanel.add(cutscene, "CUTSCENE");
            cardLayout.show(menuPanel, "CUTSCENE");
            cutscene.requestFocusInWindow();
        });

        // Add ALL panels upfront
        menuPanel.add(menu, "MENU");
        menuPanel.add(pacmanGame, "GAME");

        frame.add(menuPanel);
        frame.pack();
        frame.setVisible(true);

        cardLayout.show(menuPanel, "MENU");
    }
}
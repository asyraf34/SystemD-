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

        // Card container
        CardLayout cardLayout = new CardLayout();
        JPanel cards = new JPanel(cardLayout);

        String story = ReadStoryFile.getStoryText();

        // MENU PANEL
        MenuPanel menu = new MenuPanel(() -> {
            // When user presses ENTER on the menu, go to the cutscene
            CutscenePanel cutscene = new CutscenePanel(story, () -> {
                // This runs when ENTER is pressed inside CutscenePanel
                PacMan pacmanGame = new PacMan();
                cards.add(pacmanGame, "GAME");
                cardLayout.show(cards, "GAME");
                pacmanGame.requestFocusInWindow();
            });

            cards.add(cutscene, "CUTSCENE");
            cardLayout.show(cards, "CUTSCENE");
            cutscene.requestFocusInWindow();
        });

        // Add menu card
        cards.add(menu, "MENU");

        frame.add(cards);
        frame.setVisible(true);

        // Start on the menu
        cardLayout.show(cards, "MENU");
        menu.requestFocusInWindow();
    }
}

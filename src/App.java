import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        int rowCount = 21;
        int columnCount = 19;
        int tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int scoreboardHeight = tileSize * 2;
        int boardHeight = rowCount * tileSize + scoreboardHeight;
        try {
            LOGGER.info("Loading assets and initializing game components.");

            JFrame frame = new JFrame("Pac Man");
            frame.setSize(boardWidth, boardHeight);
            frame.setLocationRelativeTo(null);
            frame.setResizable(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            LOGGER.info("Creating game view and menu panels.");
            CardLayout cardLayout = new CardLayout();
            JPanel menuPanel  = new JPanel(cardLayout);

        String story = ReadStoryFile.getStoryText();

        // MENU PANEL
        MenuPanel menu = new MenuPanel(() -> {
            // When user presses ENTER on the menu, go to the cutscene
            CutscenePanel cutscene = new CutscenePanel(story, () -> {
                // This runs when ENTER is pressed inside CutscenePanel
                PacMan pacmanGame = new PacMan();
                menuPanel.add(pacmanGame, "GAME");
                cardLayout.show(menuPanel, "GAME");
                pacmanGame.requestFocusInWindow();
            });

            menuPanel.add(cutscene, "CUTSCENE");
            cardLayout.show(menuPanel, "CUTSCENE");
            cutscene.requestFocusInWindow();
        });

        menuPanel.add(menu, "MENU");

        frame.add(menuPanel);
        frame.setVisible(true);

            LOGGER.info("Starting game loop.");
        cardLayout.show(cards, "MENU");
        menu.requestFocusInWindow();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Uncaught exception during application startup.", e);
            System.exit(1);
        }

    }
}

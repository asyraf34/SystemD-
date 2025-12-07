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

            JFrame frame = new JFrame("Man-Hunt");
            frame.setSize(boardWidth, boardHeight);
            frame.setLocationRelativeTo(null);
            frame.setResizable(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardLayout cardLayout = new CardLayout();
        JPanel menuPanel = new JPanel(cardLayout);
        ReadStoryFile readStoryFile = ReadStoryFile.fromFile("res/storyText.txt");
        String[][] story = readStoryFile.getStorySets();
        PacMan pacmanGame = new PacMan();  // Create upfront

        // Menu -> Cutscene -> Game flow
        MenuPanel menu = new MenuPanel(() -> {
            CutscenePanel cutscene = new CutscenePanel(story, () -> {
                cardLayout.show(menuPanel, "GAME");
                pacmanGame.requestFocusInWindow();
                pacmanGame.startGameMusic();
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

            LOGGER.info("Starting game loop.");
            SoundManager.getInstance().playBackgroundLoops(
                    GameConstants.SOUND_MENU,
                    GameConstants.SOUND_MENU_SIREN
            );
            cardLayout.show(menuPanel, "MENU");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Uncaught exception during application startup.", e);
            System.exit(1);
        }
    }
}
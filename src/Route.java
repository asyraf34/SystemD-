import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Route {
    public static void showEndingCutscene(boolean routeChoice, JPanel container,
                                          CardLayout layout, String cardName,
                                          Runnable afterCutscene) {
        String[][] storyRoute;
        try {
            ReadStoryFile storyFile;

            // Fixed: Now actually using different files for different routes
            if(routeChoice)
                storyFile = ReadStoryFile.fromFile("res/storyRouteMercy.txt");
            else
                storyFile = ReadStoryFile.fromFile("res/storyRouteJustice.txt"); // Fixed filename

            storyRoute = storyFile.getStorySets();
            CutscenePanel cutscene = new CutscenePanel(storyRoute, afterCutscene);
            container.add(cutscene, cardName);
            layout.show(container, cardName);
            cutscene.requestFocusInWindow();

        } catch (IOException e) {
            e.printStackTrace();
            // Fallback to menu or show error
            if (afterCutscene != null) {
                afterCutscene.run();
            }
        }
    }
}
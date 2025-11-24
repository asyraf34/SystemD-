import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadStoryFile {

    private static final String filePath = "res/storyText.txt";
    private static String fileContent;

    // Load once at startup
    static {
        try {
            Path path = Paths.get(filePath);
            fileContent = Files.readString(path);
        } catch (IOException e) {
            e.printStackTrace();
            fileContent = ""; // fail-safe
        }
    }

    public static String getStoryText() {
        return fileContent;
    }
}

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadStoryFile {

    private static final String filePath = "res/storyText.txt";
    private static String[][] storySets;

    static {
        try {
            Path path = Paths.get(filePath);
            String fileContent = Files.readString(path);

            // Normalize line endings (Windows/Linux/Mac)
            fileContent = fileContent.replace("\r\n", "\n").replace("\r", "\n");

            // Split into sets using double newlines
            String[] rawSets = fileContent.split("\n\\s*\n");

            storySets = new String[rawSets.length][];

            for (int i = 0; i < rawSets.length; i++) {
                // Split sentences inside each set by newline
                storySets[i] = rawSets[i].trim().split("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
            storySets = new String[][]{{"Error: Could not load story file."}};
        }
    }

    public static String[][] getStorySets() {
        return storySets;
    }
}

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadStoryFile {

    private static final String filePath = "res/storyText.txt";
    private static String[][] storySets;  // ← NEW

    static {
        try {
            Path path = Paths.get(filePath);
            String content = Files.readString(path);

            storySets = parseToSets(content);

        } catch (IOException e) {
            e.printStackTrace();
            storySets = new String[0][0]; // fail-safe
        }
    }

    public static String[][] getStorySets() {
        return storySets;
    }

    // --------------------------------------------------------------
    // Converts file text → String[][] sets
    // Blank line separates sets
    // Sentences split by . ? !
    // --------------------------------------------------------------
    private static String[][] parseToSets(String text) {

        // 1. Split into sets by blank lines
        String[] rawSets = text.split("\n\\s*\n"); // two or more newlines

        String[][] result = new String[rawSets.length][];

        for (int i = 0; i < rawSets.length; i++) {
            String paragraph = rawSets[i].trim();

            // 2. Split sentences properly
            String[] sentences = paragraph.split("(?<=[.!?])\\s+");

            result[i] = sentences;
        }

        return result;
    }
}

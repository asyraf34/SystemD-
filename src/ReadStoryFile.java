import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadStoryFile {

    private String[][] storySets;

    // Private constructor
    private ReadStoryFile(String filePath) throws IOException {
        loadStoryFile(filePath);
    }

    private void loadStoryFile(String filePath) throws IOException {
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
    }

    // Factory method
    public static ReadStoryFile fromFile(String filePath) throws IOException {
        return new ReadStoryFile(filePath);
    }

    // Overloaded factory method with default error handling
    public static ReadStoryFile fromFileOrDefault(String filePath) {
        try {
            return new ReadStoryFile(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            ReadStoryFile instance = new ReadStoryFile();
            instance.storySets = new String[][]{{"Error: Could not load story file."}};
            return instance;
        }
    }

    // Private constructor for error case
    private ReadStoryFile() {
        // For error handling use only
    }

    public String[][] getStorySets() {
        return storySets;
    }
}

// Usage:
// ReadStoryFile storyFile = ReadStoryFile.fromFile("res/storyText.txt");
// String[][] storySets = storyFile.getStorySets();
    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;

    public class ReadStoryFile {

        String filePath = "res/storyText.txt";
        static String fileContent;
        ReadStoryFile() {
            try {
                Path path = Paths.get(filePath);
                this.fileContent = Files.readString(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static String getStoryText() {
            return fileContent;
        }
    }

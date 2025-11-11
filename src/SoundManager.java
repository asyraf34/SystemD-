import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SoundManager {
    private Clip backgroundClip;

    public void playBackgroundLoop(String resourcePath) {
        stopBackground();
        backgroundClip = loadClip(resourcePath);
        if (backgroundClip != null) {
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopBackground() {
        if (backgroundClip != null) {
            backgroundClip.stop();
            backgroundClip.close();
            backgroundClip = null;
        }
    }

    public void playEffect(String resourcePath) {
        Clip clip = loadClip(resourcePath);
        if (clip != null) {
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.start();
        }
    }

    private Clip loadClip(String resourcePath) {
        AudioInputStream audioStream = resolveAudioStream(resourcePath);
        if (audioStream == null) {
            return null;
        }
        try (AudioInputStream ignored = audioStream) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            return clip;
        } catch (IOException | LineUnavailableException exception) {
            System.err.println("Failed to load audio resource " + resourcePath + ": " + exception.getMessage());
            return null;
        }
    }

    private AudioInputStream resolveAudioStream(String resourcePath) {
        URL resource = SoundManager.class.getResource(resourcePath);
        if (resource != null) {
            try {
                return AudioSystem.getAudioInputStream(resource);
            } catch (UnsupportedAudioFileException | IOException exception) {
                System.err.println("Failed to read bundled audio resource " + resourcePath + ": " + exception.getMessage());
                return null;
            }
        }

        File candidate = toCandidateFile(resourcePath);
        if (candidate != null && candidate.isFile()) {
            try {
                return AudioSystem.getAudioInputStream(candidate);
            } catch (UnsupportedAudioFileException | IOException exception) {
                System.err.println("Failed to read audio file " + candidate.getPath() + ": " + exception.getMessage());
            }
        } else {
            System.err.println("Audio resource not found: " + resourcePath);
        }
        return null;
    }

    private File toCandidateFile(String resourcePath) {
        String trimmedPath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;

        File file = new File(resourcePath);
        if (file.isFile()) {
            return file;
        }

        file = new File(trimmedPath);
        if (file.isFile()) {
            return file;
        }

        file = new File("res", resourcePath);
        if (file.isFile()) {
            return file;
        }

        file = new File("res", trimmedPath);
        if (file.isFile()) {
            return file;
        }

        return null;
    }
}
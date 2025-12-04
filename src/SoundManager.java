import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.prefs.Preferences;

public class SoundManager {
    private static final String PREF_KEY = "bg_volume";
    private static final int DEFAULT_VOLUME = 100; // 0..100

    private static final SoundManager INSTANCE = new SoundManager();

    private Clip backgroundClip;
    private FloatControl bgGainControl;
    private final Preferences prefs = Preferences.userNodeForPackage(SoundManager.class);

    private SoundManager() { }

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    public void playBackgroundLoop(String resourcePath) {
        stopBackground();
        backgroundClip = loadClip(resourcePath);
        if (backgroundClip != null) {
            // get gain control if available and apply saved volume
            if (backgroundClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                try {
                    bgGainControl = (FloatControl) backgroundClip.getControl(FloatControl.Type.MASTER_GAIN);
                    // apply saved volume
                    setBackgroundVolume(getSavedVolume());
                } catch (IllegalArgumentException ex) {
                    bgGainControl = null;
                }
            } else {
                bgGainControl = null;
            }
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopBackground() {
        if (backgroundClip != null) {
            backgroundClip.stop();
            backgroundClip.close();
            backgroundClip = null;
            bgGainControl = null;
        }
    }

    /**
     * Set background music volume (0..100). Persists the value.
     * Does NOT change sound effects playback.
     */
    public void setBackgroundVolume(int percent) {
        int clamped = Math.max(0, Math.min(100, percent));
        prefs.putInt(PREF_KEY, clamped);

        if (bgGainControl != null) {
            float min = bgGainControl.getMinimum();
            float max = bgGainControl.getMaximum();
            // map linear slider [0..100] to control's [min..max]
            float gain = min + (max - min) * (clamped / 100f);
            bgGainControl.setValue(gain);
        }
    }

    /**
     * Read saved volume (0..100)
     */
    public int getSavedVolume() {
        return prefs.getInt(PREF_KEY, DEFAULT_VOLUME);
    }

    public void playEffect(String resourcePath) {
        // one-shot effects are NOT affected by setBackgroundVolume
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
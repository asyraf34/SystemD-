import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class SoundManager {
    private static final String PREF_KEY = "bg_volume";
    private static final int DEFAULT_VOLUME = 100; // 0..100

    private static final SoundManager INSTANCE = new SoundManager();

    private final List<Clip> backgroundClips = new ArrayList<>();
    private final List<FloatControl> bgGainControls = new ArrayList<>();
    private final Preferences prefs = Preferences.userNodeForPackage(SoundManager.class);

    private SoundManager() { }

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    public void playBackgroundLoop(String resourcePath) {
        playBackgroundLoops(resourcePath);
    }

    public void playBackgroundLoops(String... resourcePaths) {
        stopBackground();
        if (resourcePaths == null || resourcePaths.length == 0) {
            return;
        }

        int savedVolume = getSavedVolume();

        for (String resourcePath : resourcePaths) {
            Clip clip = loadClip(resourcePath);
            if (clip == null) {
                continue;
            }

            FloatControl gainControl = null;
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                try {
                    gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                } catch (IllegalArgumentException ex) {
                    gainControl = null;
                }

            }

            backgroundClips.add(clip);
            if (gainControl != null) {
                bgGainControls.add(gainControl);
            }

            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }

        setBackgroundVolume(savedVolume);
    }

    public void stopBackground() {
        for (Clip backgroundClip : backgroundClips) {
            backgroundClip.stop();
            backgroundClip.close();
        }
        backgroundClips.clear();
        bgGainControls.clear();
    }

    /**
     * Set background music volume (0..100). Persists the value.
     * Does NOT change sound effects playback.
     */
    public void setBackgroundVolume(int percent) {
        int clamped = Math.max(0, Math.min(100, percent));
        prefs.putInt(PREF_KEY, clamped);

        for (FloatControl gainControl : bgGainControls) {
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            // map linear slider [0..100] to control's [min..max]
            float gain = min + (max - min) * (clamped / 100f);
            gainControl.setValue(gain);
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
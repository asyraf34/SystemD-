import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * SoundManager (singleton)
 *
 * - Background music volume is controlled by setBackgroundVolume / getBackgroundVolume and is affected by mute.
 * - Sound effects are played at effectsVolume (separate) and are NOT affected by the background volume slider or mute.
 * - playEffect(...) applies effectsVolume while playBackgroundLoop(...) applies backgroundVolume.
 *
 * Notes:
 * - If you want a separate UI control for effects later, use setEffectsVolume/getEffectsVolume.
 */
public class SoundManager {
    private static final SoundManager INSTANCE = new SoundManager();

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    private Clip backgroundClip;

    // volume state
    private float backgroundVolume = 1.0f; // linear 0.0 .. 1.0 (controlled by slider)
    private float effectsVolume = 1.0f;    // linear 0.0 .. 1.0 (keeps effects independent)
    private boolean muted = false;         // mutes background music only

    public SoundManager() {
    }

    // -----------------------
    // Background music API
    // -----------------------
    public void playBackgroundLoop(String resourcePath) {
        stopBackground();
        backgroundClip = loadClip(resourcePath);
        if (backgroundClip != null) {
            applyVolume(backgroundClip, computeEffectiveBackgroundVolume());
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

    // -----------------------
    // Sound effects API
    // -----------------------
    public void playEffect(String resourcePath) {
        Clip clip = loadClip(resourcePath);
        if (clip != null) {
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            // Apply effectsVolume (NOT background volume and NOT muted)
            applyVolume(clip, computeEffectiveEffectsVolume());
            clip.start();
        }
    }

    // -----------------------
    // Background Volume / Mute API
    // -----------------------
    public synchronized void setBackgroundVolume(float v) {
        backgroundVolume = clamp01(v);
        if (backgroundClip != null) {
            applyVolume(backgroundClip, computeEffectiveBackgroundVolume());
        }
        System.out.println("SoundManager: setBackgroundVolume -> " + backgroundVolume);
    }

    public synchronized float getBackgroundVolume() {
        return backgroundVolume;
    }

    public synchronized void changeBackgroundVolumeBy(float delta) {
        setBackgroundVolume(backgroundVolume + delta);
    }

    /**
     * Mute toggles only the background music. Effects are still audible.
     */
    public synchronized void setMuted(boolean m) {
        muted = m;
        if (backgroundClip != null) {
            applyVolume(backgroundClip, computeEffectiveBackgroundVolume());
        }
        System.out.println("SoundManager: muted -> " + muted);
    }

    public synchronized void toggleMute() {
        setMuted(!muted);
    }

    public synchronized boolean isMuted() {
        return muted;
    }

    private float computeEffectiveBackgroundVolume() {
        return muted ? 0f : backgroundVolume;
    }

    // -----------------------
    // Effects Volume API (separate)
    // -----------------------
    public synchronized void setEffectsVolume(float v) {
        effectsVolume = clamp01(v);
        System.out.println("SoundManager: setEffectsVolume -> " + effectsVolume);
    }

    public synchronized float getEffectsVolume() {
        return effectsVolume;
    }

    public synchronized void changeEffectsVolumeBy(float delta) {
        setEffectsVolume(effectsVolume + delta);
    }

    private float computeEffectiveEffectsVolume() {
        return effectsVolume;
    }

    // -----------------------
    // Helpers
    // -----------------------
    private void applyVolume(Clip clip, float vol01) {
        if (clip == null) return;
        try {
            if (clip.isOpen()) {
                try {
                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        float dB;
                        if (vol01 <= 0f) {
                            dB = gain.getMinimum(); // silence
                        } else {
                            dB = (float) (20.0 * Math.log10(vol01)); // linear -> dB
                            dB = Math.max(dB, gain.getMinimum());
                            dB = Math.min(dB, gain.getMaximum());
                        }
                        gain.setValue(dB);
                        // Debug log (optional)
                        // System.out.println("SoundManager: applied volume=" + vol01 + " -> dB=" + dB);
                    } else {
                        // MASTER_GAIN not supported; ignore gracefully
                        // System.out.println("SoundManager: MASTER_GAIN not supported for this clip");
                    }
                } catch (IllegalArgumentException iae) {
                    // Control not available
                    // System.out.println("SoundManager: MASTER_GAIN control not available: " + iae.getMessage());
                }
            }
        } catch (Throwable t) {
            System.err.println("Warning: failed to apply volume to clip: " + t.getMessage());
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

    private static float clamp01(float v) {
        if (Float.isNaN(v)) return 0f;
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }
}
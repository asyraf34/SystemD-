import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;

/**
 * MenuPanel with a top-centered volume slider.
 * - slider is slightly shorter (220px)
 * - slider track is rendered in yellow via a custom BasicSliderUI
 * - mute.png at left end of the slider, volume.png at right end
 * - volume percentage label shown to the immediate right of the volume icon
 *
 * Place /images/mute.png and /images/volume.png on the classpath.
 *
 * NOTE: This class only modifies the menu UI and does not change any in-game HUDs.
 */
public class MenuPanel extends JPanel implements ActionListener {
    @Serial
    private static final long serialVersionUID = 1L;
    private boolean showPressStart = true;    // blinking text
    private final Timer blinkTimer = new Timer(500, this); // blink every 0.5 sec

    Font customFont;

    // slider UI
    private final JSlider volumeSlider;
    private final JLabel leftIcon;
    private final JLabel rightIcon;
    private final JLabel percentLabel;

    public MenuPanel(Runnable startGameCallback) {
        setFocusable(true);
        setBackground(Color.BLACK);

        // use BorderLayout so we can put the slider at the top (NORTH)
        setLayout(new BorderLayout());

        try (InputStream is = getClass().getResourceAsStream("/04B_03__.ttf")) {
            if (is != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            } else {
                // fallback to default
                customFont = new Font("SansSerif", Font.PLAIN, 12);
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            customFont = new Font("SansSerif", Font.PLAIN, 12);
        }

        blinkTimer.start();

        // ---------- top-centered volume slider (only in MenuPanel) ----------
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        topPanel.setOpaque(false);
        topPanel.setPreferredSize(new Dimension(0, 56)); // give some vertical space at top

        // load icons from resources (place /images/mute.png and /images/volume.png on classpath)
        ImageIcon muteIc = null;
        ImageIcon volIc = null;
        try {
            java.net.URL murl = getClass().getResource("mute.png");
            java.net.URL vurl = getClass().getResource("volume.png");
            if (murl != null) muteIc = new ImageIcon(murl);
            if (vurl != null) volIc = new ImageIcon(vurl);
        } catch (Exception ex) {
            System.err.println("Failed to load volume icons: " + ex.getMessage());
        }

        // Optionally scale icons to a consistent display size (24x24). If icon not found, use empty label.
        final int ICON_SIZE = 24;
        leftIcon = new JLabel();
        if (muteIc != null && muteIc.getIconWidth() > 0) {
            Image img = muteIc.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            leftIcon.setIcon(new ImageIcon(img));
        } else {
            leftIcon.setText(""); // keep empty so layout spacing remains consistent
        }

        rightIcon = new JLabel();
        if (volIc != null && volIc.getIconWidth() > 0) {
            Image img = volIc.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            rightIcon.setIcon(new ImageIcon(img));
        } else {
            rightIcon.setText("");
        }

        // initial slider value comes from SoundManager saved preference (0..100)
        int initial = SoundManager.getInstance().getSavedVolume();

        // Slightly shorter slider: width 220
        volumeSlider = new JSlider(0, 100, initial);
        volumeSlider.setPreferredSize(new Dimension(220, 22));
        volumeSlider.setOpaque(false);
        volumeSlider.setFocusable(false);
        volumeSlider.setPaintTicks(false);
        volumeSlider.setPaintLabels(false);
        volumeSlider.setToolTipText(initial + "%");

        // use a custom UI to paint the track in yellow
        volumeSlider.setUI(new YellowTrackSliderUI(volumeSlider));

        // percentage label (0% .. 100%) shown to the right of the volume icon
        percentLabel = new JLabel(initial + "%");
        percentLabel.setForeground(new Color(207, 10, 10)); // yellow to match track
        percentLabel.setFont(customFont.deriveFont(Font.BOLD, 14f));
        percentLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));

        // set listener to change background music volume only and update percent text
        volumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int v = volumeSlider.getValue(); // guaranteed 0..100
                // update UI
                percentLabel.setText(v + "%");
                volumeSlider.setToolTipText(v + "%");
                // apply to SoundManager
                SoundManager.getInstance().setBackgroundVolume(v);
            }
        });

        // Add in order: left (mute) icon, slider, right (volume) icon, percent label
        topPanel.add(leftIcon);
        topPanel.add(volumeSlider);
        topPanel.add(rightIcon);
        topPanel.add(percentLabel);

        add(topPanel, BorderLayout.NORTH);
        // ------------------------------------------------------------------------

        // Keep original key handling behavior
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    SoundManager.getInstance().playEffect(GameConstants.SOUND_START);
                    blinkTimer.stop();
                    startGameCallback.run();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Title
        g2.setColor(Color.YELLOW);
        g2.setFont(customFont.deriveFont(Font.BOLD, 72));
        String title = "MAN-HUNT";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        int deltaY = 30;
        g2.drawString(title, (getWidth() - titleWidth) / 2, 285 + deltaY);

        // Blinking text
        if (showPressStart) {
            g2.setColor(Color.WHITE);
            g2.setFont(customFont.deriveFont(Font.BOLD,32));
            String msg = "PRESS ENTER TO START";
            int msgWidth = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (getWidth() - msgWidth) / 2, 350 + deltaY);
        }

        // Instructions
        g2.setFont(customFont.deriveFont(Font.PLAIN, 20));
        g2.setColor(Color.GRAY);
        String escMsg = "Press ESC to Exit";
        int escWidth = g2.getFontMetrics().stringWidth(escMsg);
        g2.drawString(escMsg, (getWidth() - escWidth) / 2, 400 + deltaY);

        // Pause hint
        g2.setFont(customFont.deriveFont(Font.PLAIN, 20));
        g2.setColor(Color.GRAY);
        String pauseMsg = "Press P to Pause";
        int pauseWidth = g2.getFontMetrics().stringWidth(pauseMsg);
        g2.drawString(pauseMsg, (getWidth() - pauseWidth) / 2, 430 + deltaY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        showPressStart = !showPressStart; // toggle blinking
        repaint();
    }

    /**
     * Custom BasicSliderUI that draws the slider track as a yellow line.
     * Thumb painting is delegated to the superclass.
     */
    private static class YellowTrackSliderUI extends BasicSliderUI {
        private static final Color TRACK_COLOR = new Color(207, 10, 10); // yellow (golden)
        private static final Stroke TRACK_STROKE = new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        public YellowTrackSliderUI(JSlider b) {
            super(b);
            // make sure the thumb is visible (default)
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TRACK_COLOR);
            g2.setStroke(TRACK_STROKE);

            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                int y = trackRect.y + trackRect.height / 2;
                int x1 = trackRect.x;
                int x2 = trackRect.x + trackRect.width;
                g2.drawLine(x1, y, x2, y);
            } else {
                int x = trackRect.x + trackRect.width / 2;
                int y1 = trackRect.y;
                int y2 = trackRect.y + trackRect.height;
                g2.drawLine(x, y1, x, y2);
            }

            g2.dispose();
        }

        // keep default thumb painting; optionally make the thumb color match the track:
        @Override
        public void paintThumb(Graphics g) {
            super.paintThumb(g);
        }
    }
}
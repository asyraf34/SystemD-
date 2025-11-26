import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CutscenePanel extends JPanel {

    private String[][] textSets;       // ← NEW: multiple sentence sets
    private int currentSet = 0;        // ← NEW: current set index

    private String fullText = "";      // text for current set
    private String displayedText = "";
    private int index = 0;

    private Timer typeTimer;
    private Runnable onCutsceneEnd;
    private boolean typing = false;    // ← NEW: to know if typing is ongoing

    public CutscenePanel(String[][] textSets, Runnable onCutsceneEnd) {
        this.textSets = textSets;
        this.onCutsceneEnd = onCutsceneEnd;

        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        setFocusable(true);

        loadSet(currentSet);   // load first set

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                    if (typing) {
                        // Skip typing → instantly show full text of this set
                        typeTimer.stop();
                        displayedText = fullText;
                        typing = false;
                        repaint();
                    } else {
                        // Finished set → go to next
                        currentSet++;

                        if (currentSet >= textSets.length) {
                            onCutsceneEnd.run();   // no more sets
                        } else {
                            loadSet(currentSet);
                        }
                    }
                }
            }
        });
    }

    private void loadSet(int setIndex) {
        // join all sentences into one block
        fullText = String.join(" ", textSets[setIndex]);

        displayedText = "";
        index = 0;

        startTyping();
    }

    private void startTyping() {
        typing = true;

        typeTimer = new Timer(50, e -> {
            if (index < fullText.length()) {
                displayedText += fullText.charAt(index);
                index++;
                repaint();
            } else {
                typing = false;
                typeTimer.stop();
            }
        });

        typeTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.BOLD, 28));

        int x = 50;
        int y = 100;
        int lineHeight = 35;
        int maxWidth = getWidth() - 100;

        String[] words = displayedText.split(" ");
        StringBuilder line = new StringBuilder();
        int lineCount = 0;

        for (String word : words) {
            String testLine = line + (line.length() == 0 ? "" : " ") + word;
            int lineWidth = g.getFontMetrics().stringWidth(testLine);

            if (lineWidth > maxWidth) {
                g.drawString(line.toString(), x, y + lineCount * lineHeight);
                line = new StringBuilder(word);
                lineCount++;
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }

        g.drawString(line.toString(), x, y + lineCount * lineHeight);
    }
}

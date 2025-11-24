import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CutscenePanel extends JPanel {

    private String fullText;
    private String displayedText = "";
    private int index = 0;
    private Timer typeTimer;
    private Runnable onCutsceneEnd;

    public CutscenePanel(String text, Runnable onCutsceneEnd) {
        this.fullText = text;
        this.onCutsceneEnd = onCutsceneEnd;
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        setFocusable(true);

        // Timer for typewriter effect (50 ms per character)
        typeTimer = new Timer(50, e -> {
            if (index < fullText.length()) {
                displayedText += fullText.charAt(index);
                index++;
                repaint();
            } else {
                typeTimer.stop();
            }
        });
        typeTimer.start();

        // Press ENTER to skip text or go to next screen
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    typeTimer.stop();  // stop typing if still typing
                    onCutsceneEnd.run();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.BOLD, 28));

        // Draw text with word wrap
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

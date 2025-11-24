import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;

public class MenuPanel extends JPanel implements ActionListener {
    @Serial
    private static final long serialVersionUID = 1L;
    private boolean showPressStart = true;    // blinking text
    private final Timer blinkTimer = new Timer(500, this); // blink every 0.5 sec

    Font customFont;

    public MenuPanel(Runnable startGameCallback) {
        setFocusable(true);
        setBackground(Color.BLACK);

        try (InputStream is = getClass().getResourceAsStream("/04B_03__.ttf")) {
            if (is != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

        blinkTimer.start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
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
        String title = "MAN - HUNT";
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
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        showPressStart = !showPressStart; // toggle blinking
        repaint();
    }
}
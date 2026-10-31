import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main entry point for the Call of Duty Zombies 2D game.
 * Initializes the JFrame and starts the game loop.
 */
public class Game {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    public static final String TITLE = "Call of Duty Zombies 2D";
    public static final int FPS = 60;

    private JFrame frame;
    private GamePanel gamePanel;

    public Game() {
        frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        gamePanel = new GamePanel();
        frame.add(gamePanel);
        frame.pack();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Ensure GamePanel gets focus after window is visible
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                gamePanel.requestFocusInWindow();
            }
        });

        gamePanel.startGameLoop();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Game());
    }
}
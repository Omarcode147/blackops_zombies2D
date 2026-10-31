import java.awt.event.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles keyboard and mouse input for the game.
 * Thread-safe for use with Swing EDT and game loop thread.
 */
public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {
    public static final int MOUSE_LEFT = 1;
    public static final int MOUSE_RIGHT = 3;
    public static final int MOUSE_MIDDLE = 2;

    // Thread-safe sets for cross-thread access (EDT -> game loop)
    private final Set<Integer> pressedKeys = Collections.synchronizedSet(new HashSet<>());
    private final Set<Integer> pressedKeysThisFrame = Collections.synchronizedSet(new HashSet<>());
    private final Set<Integer> releasedKeysThisFrame = Collections.synchronizedSet(new HashSet<>());

    private int mouseX = 0;
    private int mouseY = 0;
    private final Set<Integer> pressedMouseButtons = Collections.synchronizedSet(new HashSet<>());
    private final Set<Integer> pressedMouseButtonsThisFrame = Collections.synchronizedSet(new HashSet<>());
    private final Set<Integer> releasedMouseButtonsThisFrame = Collections.synchronizedSet(new HashSet<>());

    // Key state
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        synchronized (pressedKeys) {
            if (!pressedKeys.contains(keyCode)) {
                pressedKeysThisFrame.add(keyCode);
            }
            pressedKeys.add(keyCode);
        }
    }

    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        synchronized (pressedKeys) {
            pressedKeys.remove(keyCode);
            releasedKeysThisFrame.add(keyCode);
        }
    }

    public void keyTyped(KeyEvent e) {
        // Not used
    }

    // Mouse state
    public void mousePressed(MouseEvent e) {
        int button = e.getButton();
        mouseX = e.getX();
        mouseY = e.getY();
        synchronized (pressedMouseButtons) {
            if (!pressedMouseButtons.contains(button)) {
                pressedMouseButtonsThisFrame.add(button);
            }
            pressedMouseButtons.add(button);
        }
    }

    public void mouseReleased(MouseEvent e) {
        int button = e.getButton();
        mouseX = e.getX();
        mouseY = e.getY();
        synchronized (pressedMouseButtons) {
            pressedMouseButtons.remove(button);
            releasedMouseButtonsThisFrame.add(button);
        }
    }

    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    // Update called each frame to clear frame-specific states
    public void update() {
        synchronized (pressedKeys) {
            pressedKeysThisFrame.clear();
            releasedKeysThisFrame.clear();
        }
        synchronized (pressedMouseButtons) {
            pressedMouseButtonsThisFrame.clear();
            releasedMouseButtonsThisFrame.clear();
        }
    }

    // Key queries
    public boolean isKeyPressed(int keyCode) {
        synchronized (pressedKeys) {
            return pressedKeys.contains(keyCode);
        }
    }

    public boolean wasKeyPressed(int keyCode) {
        synchronized (pressedKeys) {
            return pressedKeysThisFrame.contains(keyCode);
        }
    }

    public boolean wasKeyReleased(int keyCode) {
        synchronized (pressedKeys) {
            return releasedKeysThisFrame.contains(keyCode);
        }
    }

    // Mouse queries
    public boolean isMousePressed(int button) {
        synchronized (pressedMouseButtons) {
            return pressedMouseButtons.contains(button);
        }
    }

    public boolean wasMousePressed(int button) {
        synchronized (pressedMouseButtons) {
            return pressedMouseButtonsThisFrame.contains(button);
        }
    }

    public boolean wasMouseReleased(int button) {
        synchronized (pressedMouseButtons) {
            return releasedMouseButtonsThisFrame.contains(button);
        }
    }

    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }

    // Convenience methods for common keys
    public boolean isUpPressed() { return isKeyPressed(KeyEvent.VK_W) || isKeyPressed(KeyEvent.VK_UP); }
    public boolean isDownPressed() { return isKeyPressed(KeyEvent.VK_S) || isKeyPressed(KeyEvent.VK_DOWN); }
    public boolean isLeftPressed() { return isKeyPressed(KeyEvent.VK_A) || isKeyPressed(KeyEvent.VK_LEFT); }
    public boolean isRightPressed() { return isKeyPressed(KeyEvent.VK_D) || isKeyPressed(KeyEvent.VK_RIGHT); }
    public boolean isShiftPressed() { return isKeyPressed(KeyEvent.VK_SHIFT); }
    public boolean isSpacePressed() { return isKeyPressed(KeyEvent.VK_SPACE); }
    public boolean isEscapePressed() { return wasKeyPressed(KeyEvent.VK_ESCAPE); }
    public boolean isEnterPressed() { return wasKeyPressed(KeyEvent.VK_ENTER); }
}
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Window represents a virtual window opening in a room wall.
 * Windows block player movement but zombies can pass through them freely.
 * Each room has 2-3 windows for zombies to enter while keeping players contained.
 */
public class Window {
    private double x, y;
    private double width, height;
    private Rectangle2D.Double bounds;

    public Window(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        updateBounds();
    }

    private void updateBounds() {
        bounds = new Rectangle2D.Double(x, y, width, height);
    }

    public void render(Graphics2D g) {
        // Window frame
        g.setColor(new Color(60, 50, 40));
        g.fillRect((int) x, (int) y, (int) width, (int) height);

        // Window glass (dark blue tinted)
        g.setColor(new Color(30, 40, 60, 180));
        g.fillRect((int) (x + 4), (int) (y + 4), (int) (width - 8), (int) (height - 8));

        // Window cross pane
        g.setColor(new Color(80, 70, 60));
        g.drawLine((int) (x + width / 2), (int) y, (int) (x + width / 2), (int) (y + height));
        g.drawLine((int) x, (int) (y + height / 2), (int) (x + width), (int) (y + height / 2));

        // Outline
        g.setColor(new Color(100, 90, 80));
        g.drawRect((int) x, (int) y, (int) width, (int) height);
    }

    public Rectangle2D.Double getBounds() { return bounds; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getCenterX() { return x + width / 2; }
    public double getCenterY() { return y + height / 2; }
}

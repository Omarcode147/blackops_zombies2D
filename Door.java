import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Door represents a purchasable doorway that players can open by spending points.
 * Doors start closed and block movement. Once purchased, they open permanently.
 * This mimics the classic Call of Duty Zombies door buy mechanic.
 */
public class Door {
    private double x, y;
    private double width, height;
    private Rectangle2D.Double bounds;
    private int cost;
    private boolean opened = false;
    private String label;

    public Door(double x, double y, double width, double height, int cost, String label) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.cost = cost;
        this.label = label;
        updateBounds();
    }

    private void updateBounds() {
        bounds = new Rectangle2D.Double(x, y, width, height);
    }

    public boolean isPlayerInRange(Player player) {
        double dx = player.getX() - (x + width / 2);
        double dy = player.getY() - (y + height / 2);
        return Math.sqrt(dx * dx + dy * dy) < 70;
    }

    public void attemptPurchase(Player player) {
        if (opened) return;

        if (player.canAfford(cost)) {
            player.spendPoints(cost);
            opened = true;
            System.out.println("Opened " + label + " door for " + cost + " points!");
        }
    }

    public void render(Graphics2D g) {
        if (opened) {
            // Draw opened door - just a faint outline
            g.setColor(new Color(100, 100, 100, 80));
            g.drawRect((int) x, (int) y, (int) width, (int) height);
            return;
        }

        // Door frame
        g.setColor(new Color(80, 60, 40));
        g.fillRect((int) x, (int) y, (int) width, (int) height);

        // Door panels
        g.setColor(new Color(120, 90, 60));
        double panelWidth = width / 2.0 - 4;
        g.fillRect((int) (x + 2), (int) (y + 4), (int) panelWidth, (int) (height - 8));
        g.fillRect((int) (x + width / 2.0 + 2), (int) (y + 4), (int) panelWidth, (int) (height - 8));

        // Door handles
        g.setColor(new Color(180, 160, 100));
        g.fillOval((int) (x + width / 2.0 - 4), (int) (y + height / 2.0 - 4), 8, 8);

        // Cost label
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        String costText = "[" + cost + "]";
        int costX = (int) (x + width / 2.0 - fm.stringWidth(costText) / 2);
        int costY = (int) (y - 10);
        g.drawString(costText, costX, costY);

        // Interaction prompt
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        fm = g.getFontMetrics();
        String prompt = "Hold F to open";
        int promptX = (int) (x + width / 2.0 - fm.stringWidth(prompt) / 2);
        int promptY = (int) (y + height + 20);
        g.setColor(new Color(255, 255, 255, 180));
        g.drawString(prompt, promptX, promptY);

        // Door outline
        g.setColor(new Color(160, 120, 80));
        g.drawRect((int) x, (int) y, (int) width, (int) height);
    }

    public Rectangle2D.Double getBounds() { return bounds; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public int getCost() { return cost; }
    public String getLabel() { return label; }
    public boolean isOpened() { return opened; }
    public double getCenterX() { return x + width / 2; }
    public double getCenterY() { return y + height / 2; }
}

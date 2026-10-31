import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Pack-a-Punch machine - upgrades weapons for 5000 points.
 */
public class PackAPunch {
    private double x, y;
    private double width = 120;
    private double height = 180;
    private Rectangle2D.Double bounds;
    private int cost = 5000;
    private double glowPhase = 0;
    private double particleTimer = 0;

    public PackAPunch(double x, double y) {
        this.x = x;
        this.y = y;
        updateBounds();
    }

    private void updateBounds() {
        bounds = new Rectangle2D.Double(x - width/2, y - height/2, width, height);
    }

    public void update(Player player) {
        glowPhase += 0.08;
        particleTimer += 1.0 / 60.0;
    }

    public boolean isPlayerInRange(Player player) {
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        return Math.sqrt(dx*dx + dy*dy) < 100;
    }

    public void attemptUpgrade(Player player) {
        Weapon weapon = player.getCurrentWeapon();
        if (weapon == null) return;
        if (weapon.isPackAPunched()) return; // Already upgraded
        if (!player.canAfford(cost)) return;

        player.spendPoints(cost);
        player.upgradeCurrentWeapon();
        System.out.println("Pack-a-Punched " + weapon.getName() + " for " + cost + " points!");
    }

    public void render(Graphics2D g) {
        // Machine base
        g.setColor(new Color(30, 10, 40));
        g.fillRect((int)(x - width/2), (int)(y - height/2), (int)width, (int)height);

        // Machine body - purple gradient effect
        for (int i = 0; i < height - 10; i++) {
            float ratio = (float)(i / (height - 10));
            int r = (int)(50 + ratio * 100);
            int b = (int)(100 + ratio * 100);
            g.setColor(new Color(r, 20, b));
            g.drawLine((int)(x - width/2 + 5), (int)(y - height/2 + 5 + i),
                      (int)(x + width/2 - 5), (int)(y - height/2 + 5 + i));
        }

        // Glowing pulsing effect
        float glowAlpha = (float)(0.3 + Math.sin(glowPhase) * 0.3);
        g.setColor(new Color(255, 100, 255, (int)(glowAlpha * 255)));
        g.fillRect((int)(x - width/2 + 10), (int)(y - height/2 + 10), (int)(width - 20), (int)(height - 20));

        // Pack-a-Punch symbol
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g.getFontMetrics();
        String pap = "PAP";
        int papX = (int)(x - fm.stringWidth(pap) / 2);
        int papY = (int)(y - 30 + fm.getAscent() / 2);
        g.drawString(pap, papX, papY);

        // Cost
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        fm = g.getFontMetrics();
        String costText = "[" + cost + "]";
        int costX = (int)(x - fm.stringWidth(costText) / 2);
        int costY = (int)(y + 20);
        g.drawString(costText, costX, costY);

        // Description
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        fm = g.getFontMetrics();
        String desc = "Upgrade Weapon";
        int descX = (int)(x - fm.stringWidth(desc) / 2);
        int descY = (int)(y + 45);
        g.setColor(new Color(200, 200, 200));
        g.drawString(desc, descX, descY);

        // Interaction prompt
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        fm = g.getFontMetrics();
        String prompt = "Hold F to upgrade";
        int promptX = (int)(x - fm.stringWidth(prompt) / 2);
        int promptY = (int)(y + height/2 + 20);
        g.setColor(new Color(255, 255, 255, 180));
        g.drawString(prompt, promptX, promptY);

        // Outline
        g.setColor(new Color(255, 100, 255));
        g.drawRect((int)(x - width/2), (int)(y - height/2), (int)width, (int)height);

        // Decorative particles
        if (particleTimer > 0.1) {
            particleTimer = 0;
            // Particles would be spawned in GamePanel
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public Rectangle2D.Double getBounds() { return bounds; }
}
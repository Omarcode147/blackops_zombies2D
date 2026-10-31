import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;

/**
 * Power-up drops that spawn from killed zombies.
 * Includes: Insta-Kill, Nuke, Max Ammo, Double Points, Fire Sale, Bonfire Sale.
 */
public class PowerUp {
    public enum Type {
        INSTA_KILL("Insta-Kill", new Color(255, 50, 50), 30, "💀"),
        NUKE("Nuke", new Color(255, 150, 0), 0, "☢"),
        MAX_AMMO("Max Ammo", new Color(50, 255, 50), 0, "📦"),
        DOUBLE_POINTS("Double Points", new Color(255, 255, 50), 30, "✕2"),
        FIRE_SALE("Fire Sale", new Color(255, 100, 255), 30, "🔥"),
        BONFIRE_SALE("Bonfire Sale", new Color(255, 50, 255), 60, "GIFT"),
        DEATH_MACHINE("Death Machine", new Color(200, 200, 50), 60, "💪");

        public final String name;
        public final Color color;
        public final int duration; // seconds, 0 = instant
        public final String symbol;

        Type(String name, Color color, int duration, String symbol) {
            this.name = name;
            this.color = color;
            this.duration = duration;
            this.symbol = symbol;
        }
    }

    private double x, y;
    private Type powerUpType;
    private double width = 40;
    private double height = 40;
    private double lifeTime = 0;
    private double maxLifeTime = 30.0; // seconds before despawn
    private boolean collected = false;
    private Rectangle2D.Double bounds;
    private Random random = new Random();
    private double bobOffset = 0;
    private double bobSpeed = 2.0;
    private double rotation = 0;

    public PowerUp(double x, double y, Type type) {
        this.x = x;
        this.y = y;
        this.powerUpType = type;
        this.bobOffset = random.nextDouble() * Math.PI * 2;
        updateBounds();
    }

    private void updateBounds() {
        bounds = new Rectangle2D.Double(x - width/2, y - height/2, width, height);
    }

    public void update() {
        lifeTime += 1.0 / 60.0;
        bobOffset += bobSpeed / 60.0;
        rotation += 0.02;

        // Bobbing animation
        double bobY = Math.sin(bobOffset) * 5;
        bounds.y = y - height/2 + bobY;
    }

    public void collect(Player player, GamePanel gamePanel) {
        if (collected) return;
        collected = true;

        // Apply power-up effect
        applyEffect(player, gamePanel);

        // Award points for collecting
        player.addPoints(50);
    }

    private void applyEffect(Player player, GamePanel gamePanel) {
        switch (powerUpType) {
            case INSTA_KILL:
                gamePanel.activateInstaKill(powerUpType.duration);
                break;
            case NUKE:
                gamePanel.nukeAllZombies();
                break;
            case MAX_AMMO:
                gamePanel.giveMaxAmmo(player);
                break;
            case DOUBLE_POINTS:
                gamePanel.activateDoublePoints(powerUpType.duration);
                break;
            case FIRE_SALE:
                gamePanel.activateFireSale(powerUpType.duration);
                break;
            case BONFIRE_SALE:
                gamePanel.activateBonfireSale(powerUpType.duration);
                break;
            case DEATH_MACHINE:
                gamePanel.giveDeathMachine(player, powerUpType.duration);
                break;
        }
    }

    public void render(Graphics2D g) {
        if (collected) return;

        // Pulsing effect based on remaining time
        double pulseScale = 1.0 + Math.sin(lifeTime * 10) * 0.1;
        if (lifeTime > maxLifeTime - 5) {
            // Flash when about to expire
            pulseScale = 1.0 + Math.sin(lifeTime * 20) * 0.2;
        }

        int drawSize = (int)(width * pulseScale);
        int drawX = (int)(x - drawSize/2);
        int drawY = (int)(bounds.y - (drawSize - height)/2);

        // Background circle
        int r = powerUpType.color.getRed();
        int gr = powerUpType.color.getGreen();
        int b = powerUpType.color.getBlue();
        g.setColor(new Color(r, gr, b, 100));
        g.fillOval(drawX - 5, drawY - 5, drawSize + 10, drawSize + 10);

        // Main icon background
        g.setColor(powerUpType.color);
        g.fillOval(drawX, drawY, drawSize, drawSize);

        // Border
        g.setColor(Color.WHITE);
        g.drawOval(drawX, drawY, drawSize, drawSize);

        // Symbol text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        int textX = (int)(x - fm.stringWidth(powerUpType.symbol) / 2);
        int textY = (int)(y + fm.getAscent() / 2);
        g.drawString(powerUpType.symbol, textX, textY);

        // Name label
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        fm = g.getFontMetrics();
        textX = (int)(x - fm.stringWidth(powerUpType.name) / 2);
        textY = (int)(y + height/2 + 15);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(textX - 2, textY - fm.getAscent() - 2, fm.stringWidth(powerUpType.name) + 4, fm.getHeight() + 4);
        g.setColor(Color.WHITE);
        g.drawString(powerUpType.name, textX, textY);

        // Timer bar for timed power-ups
        if (powerUpType.duration > 0) {
            // This would be rendered on the active power-up display, not on the pickup
        }
    }

    // Getters
    public Type getType() { return powerUpType; }
    public double getX() { return x; }
    public double getY() { return y; }
    public Color getColor() { return powerUpType.color; }
    public Rectangle2D.Double getBounds() { return bounds; }
    public boolean isCollected() { return collected; }
    public boolean isExpired() { return lifeTime >= maxLifeTime; }
    public double getLifeTime() { return lifeTime; }
    public double getMaxLifeTime() { return maxLifeTime; }
}
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Perk machines that players can buy for permanent upgrades.
 * All 8 classic CoD Zombies perks included.
 */
public class Perk {
    public enum Type {
        JUGGERNOG("Juggernog", new Color(255, 50, 50), 2500,
                "Increases max health to 200", "🔴"),
        SPEED_COLA("Speed Cola", new Color(100, 200, 255), 3000,
                "Reload 2x faster", "🔵"),
        DOUBLE_TAP("Double Tap Root Beer", new Color(255, 255, 100), 2000,
                "Fire rate doubled", "🟡"),
        QUICK_REVIVE("Quick Revive", new Color(100, 255, 100), 500,
                "Self-revive when downed (Solo), One use only", "🟢"),
        STAMIN_UP("Stamin-Up", new Color(255, 150, 50), 2000,
                "Move 50% faster, sprint longer", "🟠"),
        DEADSHOT_DAIQUIRI("Deadshot Daiquiri", new Color(200, 100, 255), 2500,
                "Auto-aim at heads, 2x headshot damage", "🟣"),
        MULE_KICK("Mule Kick", new Color(150, 100, 50), 4000,
                "Carry 3 weapons instead of 2", "🟤"),
        PH_FLOPPER("PhD Flopper", new Color(100, 100, 100), 2000,
                "No self-damage from explosives, dolphin dive creates explosion", "⚫");

        public final String name;
        public final Color color;
        public final int cost;
        public final String description;
        public final String symbol;

        Type(String name, Color color, int cost, String description, String symbol) {
            this.name = name;
            this.color = color;
            this.cost = cost;
            this.description = description;
            this.symbol = symbol;
        }
    }

    private double x, y;
    private Type type;
    private double width = 80;
    private double height = 120;
    private Rectangle2D.Double bounds;
    private double glowIntensity = 0;
    private double glowDirection = 1;
    private boolean purchased = false;

    public Perk(double x, double y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
        updateBounds();
    }

    private void updateBounds() {
        bounds = new Rectangle2D.Double(x - width/2, y - height/2, width, height);
    }

    public void update(Player player) {
        // Glow animation
        glowIntensity += glowDirection * 0.02;
        if (glowIntensity >= 1.0) {
            glowIntensity = 1.0;
            glowDirection = -1;
        } else if (glowIntensity <= 0.0) {
            glowIntensity = 0.0;
            glowDirection = 1;
        }
    }

    public boolean isPlayerInRange(Player player) {
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        return Math.sqrt(dx*dx + dy*dy) < 80;
    }

    public void attemptPurchase(Player player) {
        // Allow re-purchase if player doesn't have the perk
        if (player.hasPerk(type)) return;

        if (player.canAfford(type.cost)) {
            player.spendPoints(type.cost);
            player.givePerk(this);
            // Don't set purchased = true so it can be bought again if lost
            System.out.println("Purchased " + type.name + " for " + type.cost + " points!");
        }
    }

    public void render(Graphics2D g) {
        // Always render the machine, but show different state if owned
        boolean owned = false;
        // We can't easily check player here, so just always render

        // Machine base
        g.setColor(new Color(40, 40, 40));
        g.fillRect((int)(x - width/2), (int)(y - height/2), (int)width, (int)height);

        // Machine body with perk color
        g.setColor(type.color);
        g.fillRect((int)(x - width/2 + 5), (int)(y - height/2 + 5), (int)(width - 10), (int)(height - 10));

        // Glow effect
        float alpha = (float)(0.3 + glowIntensity * 0.4);
        g.setColor(new Color(type.color.getRed(), type.color.getGreen(), type.color.getBlue(), (int)(alpha * 255)));
        g.fillRect((int)(x - width/2 + 5), (int)(y - height/2 + 5), (int)(width - 10), (int)(height - 10));

        // Perk symbol/icon
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();
        int symbolX = (int)(x - fm.stringWidth(type.symbol) / 2);
        int symbolY = (int)(y - 10 + fm.getAscent() / 2);
        g.drawString(type.symbol, symbolX, symbolY);

        // Perk name
        g.setFont(new Font("Arial", Font.BOLD, 12));
        fm = g.getFontMetrics();
        String shortName = getShortName();
        int nameX = (int)(x - fm.stringWidth(shortName) / 2);
        int nameY = (int)(y + 25);
        g.drawString(shortName, nameX, nameY);

        // Cost
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        fm = g.getFontMetrics();
        String costText = "[" + type.cost + "]";
        int costX = (int)(x - fm.stringWidth(costText) / 2);
        int costY = (int)(y + 45);
        g.setColor(Color.YELLOW);
        g.drawString(costText, costX, costY);

        // Interaction prompt
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        fm = g.getFontMetrics();
        String prompt = "Hold F to buy";
        int promptX = (int)(x - fm.stringWidth(prompt) / 2);
        int promptY = (int)(y + height/2 + 20);
        g.setColor(new Color(255, 255, 255, 180));
        g.drawString(prompt, promptX, promptY);

        // Outline
        g.setColor(type.color.brighter());
        g.drawRect((int)(x - width/2), (int)(y - height/2), (int)width, (int)height);
    }

    private String getShortName() {
        switch (type) {
            case JUGGERNOG: return "JUGGERNOG";
            case SPEED_COLA: return "SPEED COLA";
            case DOUBLE_TAP: return "DOUBLE TAP";
            case QUICK_REVIVE: return "QUICK REVIVE";
            case STAMIN_UP: return "STAMIN-UP";
            case DEADSHOT_DAIQUIRI: return "DEADSHOT";
            case MULE_KICK: return "MULE KICK";
            case PH_FLOPPER: return "PH.D FLOPPER";
            default: return type.name;
        }
    }

    // Getters
    public Type getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public Color getColor() { return type.color; }
    public int getCost() { return type.cost; }
    public String getName() { return type.name; }
    public String getDescription() { return type.description; }
    public String getSymbol() { return type.symbol; }
    public Rectangle2D.Double getBounds() { return bounds; }
    public boolean isPurchased() { return purchased; }
}
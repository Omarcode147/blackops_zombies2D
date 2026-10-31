import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * WallGun represents a weapon mounted on a wall that players can purchase for points.
 * Once purchased, the wall gun slot is emptied (the weapon is taken by the player).
 * This mimics the classic Call of Duty Zombies wall buy mechanic.
 */
public class WallGun {
    private double x, y;
    private double width = 60;
    private double height = 80;
    private Rectangle2D.Double bounds;
    private Weapon.Type weaponType;
    private int cost;
    private boolean purchased = false;
    private double glowIntensity = 0;
    private double glowDirection = 1;

    public WallGun(double x, double y, Weapon.Type weaponType) {
        this.x = x;
        this.y = y;
        this.weaponType = weaponType;
        this.cost = weaponType.pointsCost;
        updateBounds();
    }

    private void updateBounds() {
        bounds = new Rectangle2D.Double(x - width / 2, y - height / 2, width, height);
    }

    public void update() {
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
        return Math.sqrt(dx * dx + dy * dy) < 70;
    }

    public void attemptPurchase(Player player) {
        // Check if player already has this weapon type (including third weapon slot from Mule Kick)
        boolean hasWeapon = player.getPrimaryWeapon() != null && player.getPrimaryWeapon().getType() == weaponType ||
                           player.getSecondaryWeapon() != null && player.getSecondaryWeapon().getType() == weaponType ||
                           player.hasMuleKick() && player.getThirdWeapon() != null && player.getThirdWeapon().getType() == weaponType;

        if (hasWeapon) {
            // Buy ammo for existing weapon
            int ammoCost = cost / 4; // Ammo costs 1/4 of weapon price
            if (player.canAfford(ammoCost)) {
                player.spendPoints(ammoCost);
                // Add ammo to the matching weapon
                Weapon targetWeapon = null;
                if (player.getPrimaryWeapon() != null && player.getPrimaryWeapon().getType() == weaponType) {
                    targetWeapon = player.getPrimaryWeapon();
                } else if (player.getSecondaryWeapon() != null && player.getSecondaryWeapon().getType() == weaponType) {
                    targetWeapon = player.getSecondaryWeapon();
                }
                if (targetWeapon != null) {
                    targetWeapon.addAmmo(targetWeapon.getMaxAmmo() / 2); // Give half max ammo
                    System.out.println("Bought ammo for " + weaponType.name + " for " + ammoCost + " points!");
                }
            }
        } else {
            // Buy the weapon (or re-buy if lost)
            if (player.canAfford(cost)) {
                Weapon weapon = Weapon.createWallWeapon(weaponType);
                player.giveWeapon(weapon);
                player.spendPoints(cost);
                System.out.println("Purchased " + weaponType.name + " from wall for " + cost + " points!");
            }
        }
    }

    public void render(Graphics2D g) {
        if (purchased) {
            // Draw empty slot / faint outline after purchase
            g.setColor(new Color(60, 60, 60));
            g.drawRect((int) (x - width / 2), (int) (y - height / 2), (int) width, (int) height);
            g.setColor(new Color(100, 100, 100, 100));
            g.fillRect((int) (x - width / 2 + 5), (int) (y - height / 2 + 5), (int) (width - 10), (int) (height - 10));
            return;
        }

        // Weapon mount bracket
        g.setColor(new Color(80, 70, 60));
        g.fillRect((int) (x - 8), (int) (y - height / 2 - 10), 16, 10);

        // Weapon body
        g.setColor(weaponType.color);
        g.fillRect((int) (x - width / 2 + 5), (int) (y - height / 2 + 5), (int) (width - 10), (int) (height - 10));

        // Glow effect
        float alpha = (float) (0.3 + glowIntensity * 0.4);
        g.setColor(new Color(weaponType.color.getRed(), weaponType.color.getGreen(), weaponType.color.getBlue(), (int) (alpha * 255)));
        g.fillRect((int) (x - width / 2 + 5), (int) (y - height / 2 + 5), (int) (width - 10), (int) (height - 10));

        // Weapon name
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        String name = weaponType.name;
        int nameX = (int) (x - fm.stringWidth(name) / 2);
        int nameY = (int) (y + height / 2 + 15);
        g.drawString(name, nameX, nameY);

        // Cost
        g.setFont(new Font("Arial", Font.BOLD, 12));
        fm = g.getFontMetrics();
        String costText = "[" + cost + "]";
        int costX = (int) (x - fm.stringWidth(costText) / 2);
        int costY = (int) (y + height / 2 + 30);
        g.setColor(Color.YELLOW);
        g.drawString(costText, costX, costY);

        // Interaction prompt
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        fm = g.getFontMetrics();
        String prompt = "Hold F to buy";
        int promptX = (int) (x - fm.stringWidth(prompt) / 2);
        int promptY = (int) (y + height / 2 + 45);
        g.setColor(new Color(255, 255, 255, 180));
        g.drawString(prompt, promptX, promptY);

        // Outline
        g.setColor(weaponType.color.brighter());
        g.drawRect((int) (x - width / 2), (int) (y - height / 2), (int) width, (int) height);
    }

    public Rectangle2D.Double getBounds() { return bounds; }
    public double getX() { return x; }
    public double getY() { return y; }
    public Weapon.Type getWeaponType() { return weaponType; }
    public int getCost() { return cost; }
    public boolean isPurchased() { return purchased; }
}

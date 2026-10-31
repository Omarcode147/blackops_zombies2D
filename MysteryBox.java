import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;

/**
 * Mystery Box - random weapon for 950 points.
 * Moves to a new location after a certain number of uses.
 */
public class MysteryBox {
    private double x, y;
    private double width = 100;
    private double height = 150;
    private Rectangle2D.Double bounds;
    private int cost = 950;
    private int usesUntilMove = 3;
    private int usesCount = 0;
    private boolean open = false;
    private double openTimer = 0;
    private double openDuration = 3.0; // seconds
    private double weaponRevealDuration = 5.0; // seconds to take weapon after reveal
    private Weapon currentWeapon = null;
    private Random random = new Random();
    private double glowPhase = 0;
    private boolean weaponSpinning = false;
    private double spinTimer = 0;
    private Weapon[] spinWeapons;
    private int spinIndex = 0;
    private boolean weaponRevealed = false;

    // Possible locations for the box to move to
    private Point[] locations = {
        new Point(800, 300),
        new Point(2000, 300),
        new Point(800, 1500),
        new Point(2000, 1500),
        new Point(1400, 900),
        new Point(500, 900),
        new Point(2300, 900)
    };

    public MysteryBox(double x, double y) {
        this.x = x;
        this.y = y;
        updateBounds();
    }

    private void updateBounds() {
        bounds = new Rectangle2D.Double(x - width/2, y - height/2, width, height);
    }

    public void update(Player player) {
        glowPhase += 0.05;

        if (weaponSpinning) {
            spinTimer -= 1.0 / 60.0;
            openTimer -= 1.0 / 60.0; // Decrement timer during spinning too
            if (spinTimer <= 0) {
                spinIndex = (spinIndex + 1) % spinWeapons.length;
                spinTimer = 0.1; // Spin speed
                if (spinTimer * spinWeapons.length * 2 > openDuration) {
                    // Slow down near end
                    spinTimer *= 1.1;
                }
            }

            if (openTimer <= 0) {
                weaponSpinning = false;
                // Pick a truly random weapon instead of relying on spinIndex
                currentWeapon = spinWeapons[random.nextInt(spinWeapons.length)];
                weaponRevealed = true;
                openTimer = weaponRevealDuration; // Reset timer for weapon reveal
            }
        } else if (weaponRevealed) {
            // Weapon is revealed, give player time to take it
            openTimer -= 1.0 / 60.0;
            if (openTimer <= 0) {
                weaponRevealed = false;
                open = false;
                currentWeapon = null;
            }
        }
    }

    public boolean isPlayerInRange(Player player) {
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        return Math.sqrt(dx*dx + dy*dy) < 100;
    }

    public void attemptPurchase(Player player) {
        if (open || weaponSpinning) return;
        if (!player.canAfford(cost)) return;

        player.spendPoints(cost);
        openBox(player);
        usesCount++;

        if (usesCount >= usesUntilMove) {
            moveToRandomLocation();
            usesCount = 0;
        }
    }

    private void openBox(Player player) {
        open = true;
        openTimer = openDuration;
        weaponSpinning = true;
        spinTimer = 0.05;
        weaponRevealed = false;

        // Create array of possible weapons for spin animation
        Weapon.Type[] weaponTypes = {
            Weapon.Type.RayGun, Weapon.Type.Thundergun, Weapon.Type.WintersHowl,
            Weapon.Type.Scavenger, Weapon.Type.Python, Weapon.Type.Crossbow,
            Weapon.Type.BallisticKnife, Weapon.Type.MP40, Weapon.Type.MP5,
            Weapon.Type.AK74u, Weapon.Type.M16, Weapon.Type.Olympia,
            Weapon.Type.Stakeout, Weapon.Type.RPK, Weapon.Type.HK21,
            Weapon.Type.WunderwaffeDG2, Weapon.Type.ThunderGunUpgraded,
            Weapon.Type.L96A1, Weapon.Type.PSG1,
            Weapon.Type.PM63, Weapon.Type.Spectre, Weapon.Type.MP5K,
            Weapon.Type.Commando, Weapon.Type.FAL, Weapon.Type.G11, Weapon.Type.AUG,
            Weapon.Type.HS10, Weapon.Type.SPAS12, Weapon.Type.Striker,
            Weapon.Type.M60, Weapon.Type.HK21Upgraded,
            Weapon.Type.M1911Upgraded, Weapon.Type.FiveSeven
        };

        spinWeapons = new Weapon[weaponTypes.length];
        for (int i = 0; i < weaponTypes.length; i++) {
            spinWeapons[i] = new Weapon(weaponTypes[i]);
        }
        spinIndex = 0;
    }

    private void moveToRandomLocation() {
        Point newLoc = locations[random.nextInt(locations.length)];
        x = newLoc.x;
        y = newLoc.y;
        updateBounds();
        System.out.println("Mystery Box moved to " + x + ", " + y);
    }

    public void giveWeaponToPlayer(Player player) {
        if (currentWeapon != null) {
            player.giveWeapon(currentWeapon);
            currentWeapon = null;
        }
    }

    public void render(Graphics2D g) {
        // Box base
        g.setColor(new Color(80, 50, 20));
        g.fillRect((int)(x - width/2), (int)(y - height/2), (int)width, (int)height);

        // Box body
        g.setColor(new Color(100, 70, 40));
        g.fillRect((int)(x - width/2 + 5), (int)(y - height/2 + 5), (int)(width - 10), (int)(height - 10));

        // Glowing question marks
        float glowAlpha = (float)(0.5 + Math.sin(glowPhase) * 0.3);
        g.setColor(new Color(255, 255, 100, (int)(glowAlpha * 255)));
        g.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g.getFontMetrics();
        String qm = "?";
        int qmX = (int)(x - fm.stringWidth(qm) / 2);
        int qmY = (int)(y - height/4 + fm.getAscent() / 2);
        g.drawString(qm, qmX, qmY);

        // Cost
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        fm = g.getFontMetrics();
        String costText = "[" + cost + "]";
        int costX = (int)(x - fm.stringWidth(costText) / 2);
        int costY = (int)(y + height/2 - 30);
        g.drawString(costText, costX, costY);

        // Interaction prompt
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        fm = g.getFontMetrics();
        String prompt = "Hold F to try";
        int promptX = (int)(x - fm.stringWidth(prompt) / 2);
        int promptY = (int)(y + height/2 + 15);
        g.setColor(new Color(255, 255, 255, 180));
        g.drawString(prompt, promptX, promptY);

        // Open animation - spinning weapon
        if (weaponSpinning && spinWeapons != null && spinIndex < spinWeapons.length) {
            Weapon w = spinWeapons[spinIndex];
            g.setColor(w.getColor());
            g.setFont(new Font("Arial", Font.BOLD, 14));
            fm = g.getFontMetrics();
            String wName = w.getName();
            int wX = (int)(x - fm.stringWidth(wName) / 2);
            int wY = (int)(y + fm.getAscent() / 2);
            g.drawString(wName, wX, wY);
        } else if ((open || weaponRevealed) && currentWeapon != null) {
            // Show won weapon
            g.setColor(currentWeapon.getColor());
            g.setFont(new Font("Arial", Font.BOLD, 16));
            fm = g.getFontMetrics();
            String wName = currentWeapon.getName() + "!";
            int wX = (int)(x - fm.stringWidth(wName) / 2);
            int wY = (int)(y + fm.getAscent() / 2);
            g.drawString(wName, wX, wY);

            // Press F to take
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            fm = g.getFontMetrics();
            String takePrompt = "Press F to take";
            int takeX = (int)(x - fm.stringWidth(takePrompt) / 2);
            int takeY = (int)(y + height/2 + 15);
            g.setColor(Color.GREEN);
            g.drawString(takePrompt, takeX, takeY);
        }

        // Outline
        g.setColor(new Color(150, 100, 50));
        g.drawRect((int)(x - width/2), (int)(y - height/2), (int)width, (int)height);
    }

    public Weapon getCurrentWeapon() { return currentWeapon; }
    public void clearCurrentWeapon() { currentWeapon = null; }
    public boolean isOpen() { return open || weaponRevealed; }
    public double getX() { return x; }
    public double getY() { return y; }
    public Rectangle2D.Double getBounds() { return bounds; }

    // Close the box immediately (after taking weapon)
    public void closeBox() {
        open = false;
        weaponSpinning = false;
        weaponRevealed = false;
        openTimer = 0;
        currentWeapon = null;
    }
}
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

/**
 * Heads-Up Display showing player stats, weapon info, wave, points, and active power-ups.
 */
public class HUD {
    private Player player;
    private WaveManager waveManager;
    private GamePanel gamePanel;

    // Active power-up timers
    private double instaKillTimer = 0;
    private double doublePointsTimer = 0;
    private double fireSaleTimer = 0;
    private double deathMachineTimer = 0;
    private boolean hasDeathMachine = false;

    public HUD(Player player, WaveManager waveManager) {
        this.player = player;
        this.waveManager = waveManager;
    }

    public void setGamePanel(GamePanel panel) {
        this.gamePanel = panel;
    }

    public void render(Graphics2D g) {
        renderPoints(g);
        renderWave(g);
        renderHealth(g);
        renderWeaponInfo(g);
        renderPerks(g);
        renderActivePowerUps(g);
        renderMinimap(g);
    }

    private void renderPoints(Graphics2D g) {
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.setColor(Color.YELLOW);
        String pointsText = "" + player.getPoints();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(pointsText, 30, 60);

        // Points icon
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("PTS", 30, 85);
    }

    private void renderWave(Graphics2D g) {
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(Color.WHITE);
        String waveText = "WAVE " + waveManager.getCurrentWave();
        FontMetrics fm = g.getFontMetrics();
        int x = Game.WIDTH - fm.stringWidth(waveText) - 30;
        g.drawString(waveText, x, 50);

        // Zombies remaining
        if (waveManager.isWaveInProgress()) {
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            String remainingText = "Zombies: " + (waveManager.getZombiesRemaining() + gamePanel.getZombies().size());
            fm = g.getFontMetrics();
            x = Game.WIDTH - fm.stringWidth(remainingText) - 30;
            g.drawString(remainingText, x, 75);
        } else if (waveManager.getWaveTransitionTimeRemaining() > 0) {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.setColor(Color.GREEN);
            String nextText = "NEXT WAVE IN " + (int)Math.ceil(waveManager.getWaveTransitionTimeRemaining());
            fm = g.getFontMetrics();
            x = Game.WIDTH - fm.stringWidth(nextText) - 30;
            g.drawString(nextText, x, 75);
        }
    }

    private void renderHealth(Graphics2D g) {
        int barWidth = 300;
        int barHeight = 30;
        int x = 30;
        int y = Game.HEIGHT - 80;

        // Background
        g.setColor(new Color(100, 0, 0, 200));
        g.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

        // Health
        double healthPercent = player.getHealthPercent();
        Color healthColor = healthPercent > 0.5 ? new Color(0, 255, 0) :
                           healthPercent > 0.25 ? new Color(255, 200, 0) : new Color(255, 50, 50);
        g.setColor(healthColor);
        g.fillRoundRect(x, y, (int)(barWidth * healthPercent), barHeight, 10, 10);

        // Border
        g.setColor(Color.WHITE);
        g.drawRoundRect(x, y, barWidth, barHeight, 10, 10);

        // Health text
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        String healthText = (int)player.getHealth() + " / " + player.getMaxHealth();
        int textX = x + (barWidth - fm.stringWidth(healthText)) / 2;
        int textY = y + (barHeight + fm.getAscent()) / 2 - 2;
        g.setColor(Color.WHITE);
        g.drawString(healthText, textX, textY);

        // Downed indicator
        if (player.isDowned()) {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.setColor(Color.RED);
            String downedText = "DOWNED - BLEEDING OUT";
            fm = g.getFontMetrics();
            textX = (Game.WIDTH - fm.stringWidth(downedText)) / 2;
            g.drawString(downedText, textX, Game.HEIGHT / 2 - 50);
        }
    }

    private void renderWeaponInfo(Graphics2D g) {
        Weapon weapon = player.getCurrentWeapon();
        if (weapon == null) return;

        int x = Game.WIDTH - 350;
        int y = 100; // Moved to top right, above the map area
        int width = 320;
        int height = 120;
        FontMetrics fm;

        // Background
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(x, y, width, height, 10, 10);
        g.setColor(weapon.getColor());
        g.drawRoundRect(x, y, width, height, 10, 10);

        // Weapon name
        g.setFont(new Font("Arial", Font.BOLD, 20));
        fm = g.getFontMetrics();
        String name = weapon.getName() + (weapon.isPackAPunched() ? " (PAP)" : "");
        g.setColor(Color.WHITE);
        g.drawString(name, x + 15, y + 30);

        // Ammo
        g.setFont(new Font("Arial", Font.BOLD, 36));
        String ammoText = weapon.getCurrentAmmo() + " / " + weapon.getReserveAmmo();
        fm = g.getFontMetrics();
        g.drawString(ammoText, x + 15, y + 75);

        // Reload indicator
        if (weapon.isReloading()) {
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.setColor(Color.YELLOW);
            double progress = weapon.getReloadProgress();
            String reloadText = "RELOADING " + (int)(progress * 100) + "%";
            g.drawString(reloadText, x + 15, y + 100);

            // Reload bar
            g.setColor(new Color(100, 100, 100));
            g.fillRect(x + 15, y + 105, width - 30, 8);
            g.setColor(Color.YELLOW);
            g.fillRect(x + 15, y + 105, (int)((width - 30) * progress), 8);
        } else if (weapon.getCurrentAmmo() == 0 && weapon.getReserveAmmo() > 0) {
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.setColor(Color.RED);
            g.drawString("PRESS R TO RELOAD", x + 15, y + 100);
        } else if (weapon.getCurrentAmmo() == 0 && weapon.getReserveAmmo() == 0) {
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.setColor(Color.RED);
            g.drawString("OUT OF AMMO!", x + 15, y + 100);
        }

        // Weapon class icon
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(Color.GRAY);
        String classText = "[" + weapon.getWeaponClass() + "]";
        g.drawString(classText, x + width - 15 - fm.stringWidth(classText), y + 25);

        // Fire mode indicator
        if (weapon.getFireRate() > 800) {
            g.setColor(Color.CYAN);
            g.drawString("AUTO", x + width - 15 - fm.stringWidth("AUTO"), y + 45);
        } else if (weapon.getWeaponClass() == Weapon.WeaponClass.SHOTGUN) {
            g.setColor(Color.ORANGE);
            g.drawString("PUMP", x + width - 15 - fm.stringWidth("PUMP"), y + 45);
        } else {
            g.setColor(Color.GREEN);
            g.drawString("SEMI", x + width - 15 - fm.stringWidth("SEMI"), y + 45);
        }
    }

    private void renderPerks(Graphics2D g) {
        int startX = 30;
        int startY = 130;
        int iconSize = 36;
        int spacing = 42;
        FontMetrics fm;

        for (int i = 0; i < player.getActivePerks().size(); i++) {
            Perk perk = player.getActivePerks().get(i);
            int px = startX;
            int py = startY + i * spacing;

            // Perk icon background
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRoundRect(px - 5, py - 5, iconSize + 10, iconSize + 10, 8, 8);

            // Perk color
            g.setColor(perk.getColor());
            g.fillRoundRect(px, py, iconSize, iconSize, 6, 6);

            // Perk symbol
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            fm = g.getFontMetrics();
            int symX = px + (iconSize - fm.stringWidth(perk.getSymbol())) / 2;
            int symY = py + (iconSize + fm.getAscent()) / 2 - 2;
            g.drawString(perk.getSymbol(), symX, symY);

            // Perk name
            g.setFont(new Font("Arial", Font.PLAIN, 11));
            fm = g.getFontMetrics();
            String name = getPerkShortName(perk.getType());
            g.drawString(name, px + iconSize + 8, py + (iconSize + fm.getAscent()) / 2);
        }
    }

    private String getPerkShortName(Perk.Type type) {
        switch (type) {
            case JUGGERNOG: return "Juggernog (+Health)";
            case SPEED_COLA: return "Speed Cola (Fast Reload)";
            case DOUBLE_TAP: return "Double Tap (2x Fire Rate)";
            case QUICK_REVIVE: return "Quick Revive (Self Revive)";
            case STAMIN_UP: return "Stamin-Up (Speed+)";
            case DEADSHOT_DAIQUIRI: return "Deadshot (Headshots)";
            case MULE_KICK: return "Mule Kick (3 Weapons)";
            case PH_FLOPPER: return "PhD Flopper (No Self Dmg)";
            default: return type.name();
        }
    }

    private void renderActivePowerUps(Graphics2D g) {
        int x = Game.WIDTH / 2 - 150;
        int y = 30;
        int iconSize = 40;
        int spacing = 50;
        int index = 0;

        // Insta-Kill
        if (instaKillTimer > 0) {
            renderPowerUpIcon(g, x + index * spacing, y, iconSize, "INSTA-KILL",
                    new Color(255, 50, 50), instaKillTimer, 30);
            index++;
        }

        // Double Points
        if (doublePointsTimer > 0) {
            renderPowerUpIcon(g, x + index * spacing, y, iconSize, "2X POINTS",
                    new Color(255, 255, 50), doublePointsTimer, 30);
            index++;
        }

        // Fire Sale
        if (fireSaleTimer > 0) {
            renderPowerUpIcon(g, x + index * spacing, y, iconSize, "FIRE SALE",
                    new Color(255, 100, 255), fireSaleTimer, 30);
            index++;
        }

        // Death Machine
        if (deathMachineTimer > 0) {
            renderPowerUpIcon(g, x + index * spacing, y, iconSize, "DEATH MACHINE",
                    new Color(200, 200, 50), deathMachineTimer, 60);
            index++;
        }
    }

    private void renderPowerUpIcon(Graphics2D g, int x, int y, int size, String name,
                                   Color color, double timeLeft, double maxTime) {
        // Background
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x, y, size, size, 6, 6);

        // Icon
        g.setColor(color);
        g.fillRoundRect(x + 2, y + 2, size - 4, size - 4, 4, 4);

        // Timer ring
        double progress = timeLeft / maxTime;
        g.setColor(new Color(255, 255, 255, 100));
        g.drawRoundRect(x, y, size, size, 6, 6);

        // Progress arc (simplified as a bar at bottom)
        g.setColor(Color.WHITE);
        g.fillRect(x, y + size - 4, (int)(size * progress), 4);

        // Name tooltip on hover would go here
    }

    public void activateInstaKill(double duration) {
        instaKillTimer = duration;
    }

    public void activateDoublePoints(double duration) {
        doublePointsTimer = duration;
    }

    public void activateFireSale(double duration) {
        fireSaleTimer = duration;
    }

    public void activateBonfireSale(double duration) {
        // Similar to fire sale but for mystery box
        fireSaleTimer = Math.max(fireSaleTimer, duration);
    }

    public void giveDeathMachine(Player player, double duration) {
        deathMachineTimer = duration;
        hasDeathMachine = true;
        // Give player a temporary Death Machine weapon (replaces current weapon)
        Weapon dm = new Weapon(Weapon.Type.MINIGUN);
        // Death Machine is already powerful, don't pack-a-punch it
        // But we can mark it as special
        player.giveWeapon(dm);
    }

    public void nukeAllZombies() {
        if (gamePanel != null) {
            for (Zombie zombie : gamePanel.getZombies()) {
                zombie.takeDamage(9999, false);
                player.addPoints(100); // Reduced points for nuke kills
            }
        }
    }

    public void giveMaxAmmo(Player player) {
        for (Weapon w : new Weapon[]{player.getPrimaryWeapon(), player.getSecondaryWeapon()}) {
            if (w != null) {
                w.setAmmo(w.getMagazineSize(), w.getMaxAmmo());
            }
        }
    }

    public void update(double deltaTime) {
        if (instaKillTimer > 0) instaKillTimer -= deltaTime;
        if (doublePointsTimer > 0) doublePointsTimer -= deltaTime;
        if (fireSaleTimer > 0) fireSaleTimer -= deltaTime;
        if (deathMachineTimer > 0) {
            deathMachineTimer -= deltaTime;
            if (deathMachineTimer <= 0) {
                hasDeathMachine = false;
                // Remove death machine weapon
            }
        }
    }

    public boolean isInstaKillActive() { return instaKillTimer > 0; }
    public boolean isDoublePointsActive() { return doublePointsTimer > 0; }
    public boolean isFireSaleActive() { return fireSaleTimer > 0; }
    public boolean hasDeathMachine() { return hasDeathMachine; }

    private void renderMinimap(Graphics2D g) {
        if (gamePanel == null || gamePanel.getMap() == null) return;

        int mapSize = 200;
        int x = Game.WIDTH - mapSize - 20;
        int y = Game.HEIGHT - mapSize - 20;

        // Background
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(x, y, mapSize, mapSize, 10, 10);
        g.setColor(new Color(100, 100, 100));
        g.drawRoundRect(x, y, mapSize, mapSize, 10, 10);

        // Map scale
        double scaleX = (double)mapSize / Map.WIDTH;
        double scaleY = (double)mapSize / Map.HEIGHT;

        // Draw walls (simplified)
        g.setColor(new Color(80, 80, 80));
        for (Rectangle2D.Double wall : gamePanel.getMap().getWalls()) {
            int wx = x + (int)(wall.x * scaleX);
            int wy = y + (int)(wall.y * scaleY);
            int ww = Math.max(1, (int)(wall.width * scaleX));
            int wh = Math.max(1, (int)(wall.height * scaleY));
            g.fillRect(wx, wy, ww, wh);
        }

        // Draw player
        int px = x + (int)(player.getX() * scaleX);
        int py = y + (int)(player.getY() * scaleY);
        g.setColor(Color.GREEN);
        g.fillOval(px - 3, py - 3, 6, 6);

        // Draw zombies
        g.setColor(Color.RED);
        for (Zombie zombie : gamePanel.getZombies()) {
            int zx = x + (int)(zombie.getX() * scaleX);
            int zy = y + (int)(zombie.getY() * scaleY);
            g.fillOval(zx - 2, zy - 2, 4, 4);
        }

        // Draw perks
        g.setColor(Color.CYAN);
        for (Perk perk : gamePanel.getMap().getPerks()) {
            if (!perk.isPurchased()) {
                int pkx = x + (int)(perk.getX() * scaleX);
                int pky = y + (int)(perk.getY() * scaleY);
                g.fillOval(pkx - 2, pky - 2, 4, 4);
            }
        }

        // Draw mystery box
        if (gamePanel.getMysteryBox() != null) {
            int bx = x + (int)(gamePanel.getMysteryBox().getX() * scaleX);
            int by = y + (int)(gamePanel.getMysteryBox().getY() * scaleY);
            g.setColor(Color.YELLOW);
            g.fillOval(bx - 3, by - 3, 6, 6);
        }

        // Draw pack-a-punch
        if (gamePanel.getPackAPunch() != null) {
            int px2 = x + (int)(gamePanel.getPackAPunch().getX() * scaleX);
            int py2 = y + (int)(gamePanel.getPackAPunch().getY() * scaleY);
            g.setColor(new Color(255, 100, 255));
            g.fillOval(px2 - 3, py2 - 3, 6, 6);
        }
    }
}
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Player class representing the player character.
 * Handles movement, shooting, weapons, perks, and stats.
 */
public class Player extends Entity {
    // Movement
    private static final double MOVE_SPEED = 250;
    private static final double DIAGONAL_SPEED = MOVE_SPEED * 0.7071;

    // Shooting
    private double lastShotTime = 0;
    private double shootCooldown = 0;

    // Stats
    private int points = 500;
    private int maxHealth = 200; // With Juggernog
    private int baseMaxHealth = 100;
    private double healthRegenTimer = 0; // Seconds since last hit for health regen
    private double damageMultiplier = 1.0;
    private double fireRateMultiplier = 1.0;
    private double reloadSpeedMultiplier = 1.0;
    private double moveSpeedMultiplier = 1.0;

    // Weapons
    private Weapon primaryWeapon;
    private Weapon secondaryWeapon;
    private Weapon thirdWeapon; // Third weapon for Mule Kick
    private Weapon currentWeapon;
    private int weaponSlot = 0; // 0 = primary, 1 = secondary, 2 = third (Mule Kick)

    // Perks
    private List<Perk> activePerks = new ArrayList<>();
    private boolean hasJuggernog = false;
    private boolean hasSpeedCola = false;
    private boolean hasDoubleTap = false;
    private boolean hasQuickRevive = false;
    private boolean hasStaminUp = false;
    private boolean hasDeadshot = false;
    private boolean hasMuleKick = false;
    private boolean hasPhD = false;

    // Downed state (for solo/co-op)
    private boolean isDowned = false;
    private double downedTime = 0;
    private static final double DOWNED_BLEED_OUT_TIME = 30.0; // seconds
    private double quickReviveTimer = 0; // Timer for Quick Revive auto-revive
    private static final double QUICK_REVIVE_REVIVE_TIME = 4.0; // seconds to self-revive

    // Input reference
    private GamePanel gamePanel;

    public Player(double x, double y, Map map) {
        super(x, y, 32, 32, 100);
        this.maxHealth = baseMaxHealth;
        this.health = maxHealth;

        // Start with a pistol
        primaryWeapon = new Weapon(Weapon.Type.M1911);
        secondaryWeapon = null;
        thirdWeapon = null;
        currentWeapon = primaryWeapon;
    }

    public void update(InputHandler input, GamePanel panel) {
        this.gamePanel = panel;

        if (isDowned) {
            updateDowned();
            return;
        }

        handleMovement(input);
        handleShooting(input);
        handleWeaponSwitch(input);
        handleInteractions(input);

        // Update current weapon
        currentWeapon.update(this);

        // Regenerate health 10% per second after taking damage
        if (health < maxHealth) {
            healthRegenTimer += 1.0 / 60.0;
            if (healthRegenTimer >= 1.0) {
                int regenAmount = (int) Math.ceil(maxHealth * 0.10);
                health = Math.min(health + regenAmount, maxHealth);
                healthRegenTimer = 0;
            }
        }
    }

    private void handleMovement(InputHandler input) {
        double vx = 0, vy = 0;
        double speed = MOVE_SPEED * moveSpeedMultiplier;

        if (input.isKeyPressed(KeyEvent.VK_W) || input.isKeyPressed(KeyEvent.VK_UP)) vy -= 1;
        if (input.isKeyPressed(KeyEvent.VK_S) || input.isKeyPressed(KeyEvent.VK_DOWN)) vy += 1;
        if (input.isKeyPressed(KeyEvent.VK_A) || input.isKeyPressed(KeyEvent.VK_LEFT)) vx -= 1;
        if (input.isKeyPressed(KeyEvent.VK_D) || input.isKeyPressed(KeyEvent.VK_RIGHT)) vx += 1;

        // Normalize diagonal movement
        if (vx != 0 && vy != 0) {
            vx *= 0.7071;
            vy *= 0.7071;
        }

        velocityX = vx * speed;
        velocityY = vy * speed;

        // Apply movement with collision detection
        // Use fixed timestep from GamePanel
        double dt = 1.0 / 60.0;
        double newX = x + velocityX * dt;
        double newY = y + velocityY * dt;

        // Check X collision
        if (!mapCollides(newX, y)) {
            x = newX;
        }
        // Check Y collision
        if (!mapCollides(x, newY)) {
            y = newY;
        }

        updateBounds();
    }

    private boolean mapCollides(double checkX, double checkY) {
        // Check collision with map walls
        if (gamePanel == null || gamePanel.getMap() == null) return false;
        Rectangle2D.Double testBounds = new Rectangle2D.Double(
            checkX - width / 2, checkY - height / 2, width, height
        );
        if (gamePanel.getMap().checkCollision(testBounds)) return true;
        // Windows block players but zombies can pass through them
        return gamePanel.getMap().checkWindowCollision(testBounds);
    }

    private void handleShooting(InputHandler input) {
        if (input.isMousePressed(InputHandler.MOUSE_LEFT) && currentWeapon.canShoot()) {
            shoot(input.getMouseX(), input.getMouseY());
        }

        // Reload
        if (input.isKeyPressed(KeyEvent.VK_R)) {
            currentWeapon.startReload();
        }
    }

    private void shoot(double mouseX, double mouseY) {
        // Convert screen coordinates to world coordinates
        double worldMouseX = mouseX + gamePanel.getCameraX();
        double worldMouseY = mouseY + gamePanel.getCameraY();

        double angle = Math.atan2(worldMouseY - y, worldMouseX - x);

        // Calculate bullet spawn position (at gun barrel)
        double barrelOffset = 20;
        double bulletX = x + Math.cos(angle) * barrelOffset;
        double bulletY = y + Math.sin(angle) * barrelOffset;

        List<Bullet> bullets = currentWeapon.shoot(bulletX, bulletY, angle, this);
        for (Bullet bullet : bullets) {
            gamePanel.addBullet(bullet);
        }
        if (!bullets.isEmpty()) {
            gamePanel.createMuzzleFlash(bulletX, bulletY, angle);
        }
    }

    private void handleWeaponSwitch(InputHandler input) {
        if (input.wasKeyPressed(KeyEvent.VK_1)) {
            switchWeapon(0);
        } else if (input.wasKeyPressed(KeyEvent.VK_2)) {
            switchWeapon(1);
        } else if (input.wasKeyPressed(KeyEvent.VK_3)) {
            switchWeapon(2);
        } else if (input.wasKeyPressed(KeyEvent.VK_Q)) {
            // Quick switch between available weapons
            if (hasMuleKick && secondaryWeapon != null) {
                // With Mule Kick, cycle through all 3 weapons
                int nextSlot = (weaponSlot + 1) % 3;
                if (nextSlot == 2 && secondaryWeapon == null) nextSlot = 0;
                switchWeapon(nextSlot);
            } else {
                // Without Mule Kick, quick switch between primary and secondary
                switchWeapon(weaponSlot == 0 ? 1 : 0);
            }
        }
    }

    private void switchWeapon(int slot) {
        if (slot == 0 && primaryWeapon != null) {
            weaponSlot = 0;
            currentWeapon = primaryWeapon;
        } else if (slot == 1 && secondaryWeapon != null) {
            weaponSlot = 1;
            currentWeapon = secondaryWeapon;
        } else if (slot == 2 && hasMuleKick && thirdWeapon != null) {
            weaponSlot = 2;
            currentWeapon = thirdWeapon;
        }
    }

    private void handleInteractions(InputHandler input) {
        if (input.wasKeyPressed(KeyEvent.VK_E) || input.wasKeyPressed(KeyEvent.VK_F)) {
            MysteryBox mysteryBox = gamePanel.getMysteryBox();
            // If mystery box has a weapon revealed, take it (don't charge again)
            if (mysteryBox.isPlayerInRange(this) && mysteryBox.getCurrentWeapon() != null) {
                mysteryBox.giveWeaponToPlayer(this);
                mysteryBox.clearCurrentWeapon();
                // Close the box immediately after taking weapon
                mysteryBox.closeBox();
            }
            // Try to buy from mystery box (only if closed)
            else if (mysteryBox.isPlayerInRange(this) && !mysteryBox.isOpen()) {
                mysteryBox.attemptPurchase(this);
            }
            // Try to use pack-a-punch
            else if (gamePanel.getPackAPunch().isPlayerInRange(this)) {
                gamePanel.getPackAPunch().attemptUpgrade(this);
            }
            // Try to buy perk
            else {
                for (Perk perk : gamePanel.getMap().getPerks()) {
                    if (perk.isPlayerInRange(this) && !hasPerk(perk.getType())) {
                        perk.attemptPurchase(this);
                    }
                }
            }
            // Try to buy wall gun
            for (WallGun wallGun : gamePanel.getMap().getWallGuns()) {
                if (wallGun.isPlayerInRange(this) && !wallGun.isPurchased()) {
                    wallGun.attemptPurchase(this);
                }
            }
            // Try to open door
            for (Door door : gamePanel.getMap().getDoors()) {
                if (door.isPlayerInRange(this) && !door.isOpened()) {
                    door.attemptPurchase(this);
                }
            }
        }
    }

    private void updateDowned() {
        downedTime -= 1.0 / 60.0;

        // Quick Revive solo: auto-revive after a few seconds
        if (hasQuickRevive) {
            quickReviveTimer -= 1.0 / 60.0;
            if (quickReviveTimer <= 0) {
                // Self-revive with half health and push zombies away
                if (gamePanel != null) {
                    revive(gamePanel.getZombies());
                } else {
                    revive();
                }
                return;
            }
        }

        if (downedTime <= 0) {
            // Bleed out - game over
            health = 0;
            alive = false;
        }
    }

    public void takeDamage(double damage) {
        if (isDowned) return;

        healthRegenTimer = 0;
        health -= damage;
        if (health <= 0) {
            if (hasQuickRevive && !isDowned) {
                // Quick Revive self-revive (solo)
                goDown();
            } else {
                health = 0;
                alive = false;
            }
        }
        gamePanel.addScreenShake(3, 0.15);
    }

    private void goDown() {
        isDowned = true;
        downedTime = DOWNED_BLEED_OUT_TIME;
        health = 1; // Minimum health while downed
        // Lose perks on down (except Quick Revive)
        // In real CoD, you lose all perks. Here we'll keep Quick Revive for solo.

        // Give M1911 pistol for self-defense while downed (Quick Revive solo)
        if (hasQuickRevive) {
            // Replace current weapon with M1911 for downed state
            currentWeapon = new Weapon(Weapon.Type.M1911);
            currentWeapon.setAmmo(8, 40); // Full ammo for defense
            weaponSlot = 0;
            quickReviveTimer = QUICK_REVIVE_REVIVE_TIME; // Start auto-revive timer
        }
    }

    public void revive() {
        isDowned = false;
        health = maxHealth / 2; // Revive with half health
    }

    /**
     * Revive the player and push nearby zombies away.
     * Quick Revive is one-use - removed after revive.
     * @param zombies List of zombies to push away from the player
     */
    public void revive(List<Zombie> zombies) {
        revive();
        // Quick Revive is one-use - remove it after reviving
        if (hasQuickRevive) {
            removePerk(Perk.Type.QUICK_REVIVE);
        }
        if (zombies != null) {
            pushZombiesAway(zombies, 150.0); // Push radius of 150 pixels
        }
    }

    /**
     * Push all zombies within radius away from the player.
     */
    private void pushZombiesAway(List<Zombie> zombies, double radius) {
        double radiusSq = radius * radius;
        for (Zombie zombie : zombies) {
            if (!zombie.isAlive()) continue;
            double dx = zombie.getX() - x;
            double dy = zombie.getY() - y;
            double distSq = dx * dx + dy * dy;
            if (distSq < radiusSq && distSq > 0) {
                double dist = Math.sqrt(distSq);
                // Push zombie away - stronger force
                double pushForce = (radius - dist) * 2.0; // Increased from 0.5 to 2.0
                double pushX = (dx / dist) * pushForce;
                double pushY = (dy / dist) * pushForce;
                zombie.setX(zombie.getX() + pushX);
                zombie.setY(zombie.getY() + pushY);
                zombie.updateBounds();
            }
        }
    }

    public void addPoints(int amount) {
        points += amount;
    }

    public void spendPoints(int amount) {
        points = Math.max(0, points - amount);
    }

    public boolean canAfford(int cost) {
        return points >= cost;
    }

    // Perk management
    public void givePerk(Perk perk) {
        if (!hasPerk(perk.getType())) {
            activePerks.add(perk);
            applyPerkEffects(perk);
        }
    }

    public boolean hasPerk(Perk.Type type) {
        for (Perk p : activePerks) {
            if (p.getType() == type) return true;
        }
        return false;
    }

    /**
     * Remove a perk from the player.
     * Used for one-use perks like Quick Revive.
     */
    public void removePerk(Perk.Type type) {
        activePerks.removeIf(p -> p.getType() == type);
        // Reset perk effects
        switch (type) {
            case JUGGERNOG:
                hasJuggernog = false;
                baseMaxHealth = 100;
                maxHealth = 100;
                health = Math.min(health, maxHealth);
                break;
            case SPEED_COLA:
                hasSpeedCola = false;
                reloadSpeedMultiplier = 1.0;
                break;
            case DOUBLE_TAP:
                hasDoubleTap = false;
                fireRateMultiplier = 1.0;
                break;
            case QUICK_REVIVE:
                hasQuickRevive = false;
                break;
            case STAMIN_UP:
                hasStaminUp = false;
                moveSpeedMultiplier = 1.0;
                break;
            case DEADSHOT_DAIQUIRI:
                hasDeadshot = false;
                damageMultiplier = 1.0;
                break;
            case MULE_KICK:
                hasMuleKick = false;
                break;
            case PH_FLOPPER:
                hasPhD = false;
                break;
        }
    }

    private void applyPerkEffects(Perk perk) {
        switch (perk.getType()) {
            case JUGGERNOG:
                hasJuggernog = true;
                baseMaxHealth = 200;
                maxHealth = 200;
                health = maxHealth;
                break;
            case SPEED_COLA:
                hasSpeedCola = true;
                reloadSpeedMultiplier = 2.0;
                break;
            case DOUBLE_TAP:
                hasDoubleTap = true;
                fireRateMultiplier = 2.0;
                break;
            case QUICK_REVIVE:
                hasQuickRevive = true;
                break;
            case STAMIN_UP:
                hasStaminUp = true;
                moveSpeedMultiplier = 1.5;
                break;
            case DEADSHOT_DAIQUIRI:
                hasDeadshot = true;
                damageMultiplier = 1.5; // Headshot damage bonus handled in bullet
                break;
            case MULE_KICK:
                hasMuleKick = true;
                // Third weapon slot handled in weapon switching
                break;
            case PH_FLOPPER:
                hasPhD = true;
                // Explosion immunity handled in damage calculation
                break;
        }
    }

    public void giveWeapon(Weapon weapon) {
        if (primaryWeapon == null) {
            primaryWeapon = weapon;
            currentWeapon = primaryWeapon;
            weaponSlot = 0;
        } else if (secondaryWeapon == null) {
            secondaryWeapon = weapon;
        } else if (hasMuleKick && thirdWeapon == null) {
            // Third weapon slot available with Mule Kick
            thirdWeapon = weapon;
        } else if (hasMuleKick) {
            // All three slots filled with Mule Kick - replace current weapon
            if (weaponSlot == 0) {
                primaryWeapon = weapon;
            } else if (weaponSlot == 1) {
                secondaryWeapon = weapon;
            } else if (weaponSlot == 2) {
                thirdWeapon = weapon;
            }
            currentWeapon = weapon;
        } else {
            // No Mule Kick - replace current weapon (primary or secondary)
            if (weaponSlot == 0) {
                primaryWeapon = weapon;
            } else {
                secondaryWeapon = weapon;
            }
            currentWeapon = weapon;
        }
    }

    public void upgradeCurrentWeapon() {
        if (currentWeapon != null) {
            currentWeapon.packAPunch();
        }
    }

    @Override
    public void update() {
        // Called from GamePanel, actual update is in update(InputHandler, GamePanel)
    }

    @Override
    public void render(Graphics2D g) {
        // Player body
        g.setColor(new Color(80, 180, 80));
        g.fillOval((int)(x - width/2), (int)(y - height/2), (int)width, (int)height);

        // Player direction indicator
        double mouseX = x + 100;
        double mouseY = y;
        if (gamePanel != null && gamePanel.getInputHandler() != null) {
            mouseX = gamePanel.getInputHandler().getMouseX() + gamePanel.getCameraX();
            mouseY = gamePanel.getInputHandler().getMouseY() + gamePanel.getCameraY();
        }
        double angle = Math.atan2(mouseY - y, mouseX - x);

        g.setColor(Color.WHITE);
        int noseX = (int)(x + Math.cos(angle) * 20);
        int noseY = (int)(y + Math.sin(angle) * 20);
        g.drawLine((int)x, (int)y, noseX, noseY);

        // Health bar above player
        renderHealthBar(g);

        // Downed indicator
        if (isDowned) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("DOWNED - " + (int)downedTime + "s", (int)(x - 40), (int)(y - 40));
        }
    }

    private void renderHealthBar(Graphics2D g) {
        double barWidth = 40;
        double barHeight = 5;
        double barX = x - barWidth / 2;
        double barY = y - height / 2 - 10;

        g.setColor(new Color(100, 0, 0));
        g.fillRect((int)barX, (int)barY, (int)barWidth, (int)barHeight);

        g.setColor(new Color(0, 255, 0));
        g.fillRect((int)barX, (int)barY, (int)(barWidth * getHealthPercent()), (int)barHeight);

        g.setColor(Color.WHITE);
        g.drawRect((int)barX, (int)barY, (int)barWidth, (int)barHeight);
    }

    private void renderPerkIcons(Graphics2D g) {
        int iconSize = 16;
        int startX = (int)(x - width/2 - 2);
        int startY = (int)(y - height/2 - 25);
        int index = 0;

        for (Perk perk : activePerks) {
            Color perkColor = perk.getColor();
            g.setColor(perkColor);
            g.fillOval(startX - iconSize, startY - index * (iconSize + 2), iconSize, iconSize);
            g.setColor(Color.WHITE);
            g.drawOval(startX - iconSize, startY - index * (iconSize + 2), iconSize, iconSize);
            index++;
        }
    }

    @Override
    protected void onDeath() {
        // Player death handled in GamePanel
    }

    // Getters
    public int getPoints() { return points; }
    public Weapon getCurrentWeapon() { return currentWeapon; }
    public Weapon getPrimaryWeapon() { return primaryWeapon; }
    public Weapon getSecondaryWeapon() { return secondaryWeapon; }
    public Weapon getThirdWeapon() { return thirdWeapon; }
    public int getWeaponSlot() { return weaponSlot; }
    public boolean isDowned() { return isDowned; }
    public List<Perk> getActivePerks() { return activePerks; }
    public boolean hasJuggernog() { return hasJuggernog; }
    public boolean hasSpeedCola() { return hasSpeedCola; }
    public boolean hasDoubleTap() { return hasDoubleTap; }
    public boolean hasQuickRevive() { return hasQuickRevive; }
    public boolean hasStaminUp() { return hasStaminUp; }
    public boolean hasDeadshot() { return hasDeadshot; }
    public boolean hasMuleKick() { return hasMuleKick; }
    public boolean hasPhD() { return hasPhD; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public double getFireRateMultiplier() { return fireRateMultiplier; }
    public double getReloadSpeedMultiplier() { return reloadSpeedMultiplier; }
    public double getMoveSpeedMultiplier() { return moveSpeedMultiplier; }
    public double getMaxHealth() { return maxHealth; }
}
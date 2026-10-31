import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Weapon class representing all guns in the game.
 * Includes stats, firing mechanics, reloading, and Pack-a-Punch upgrades.
 */
public class Weapon {
    public enum Type {
        // Starting pistols
        M1911("M1911", 35, 12, 72, 450, 1.8, 2.0, 25, WeaponClass.PISTOL,
                new Color(100, 100, 100), 500),

        // Wall weapons (50% reduced prices)
        MP40("MP40", 40, 32, 128, 750, 2.0, 2.5, 30, WeaponClass.SMG,
                new Color(80, 80, 90), 1250),
        MP5("MP5", 35, 30, 120, 800, 2.0, 2.5, 30, WeaponClass.SMG,
                new Color(70, 70, 80), 1500),
        AK74u("AK-74u", 45, 30, 120, 700, 2.2, 3.0, 35, WeaponClass.ASSAULT,
                new Color(90, 80, 70), 2000),
        M16("M16", 55, 30, 120, 600, 2.5, 3.0, 40, WeaponClass.ASSAULT,
                new Color(100, 90, 80), 2000),
        Olympia("Olympia", 100, 2, 12, 120, 1.5, 3.5, 8, WeaponClass.SHOTGUN,
                new Color(120, 100, 80), 1750),
        Stakeout("Stakeout", 80, 6, 18, 150, 2.0, 3.0, 10, WeaponClass.SHOTGUN,
                new Color(110, 90, 70), 1500),
        RPK("RPK", 50, 100, 200, 650, 3.5, 5.0, 50, WeaponClass.LMG,
                new Color(80, 90, 80), 2000),
        HK21("HK21", 55, 80, 160, 600, 4.0, 5.5, 55, WeaponClass.LMG,
                new Color(70, 80, 70), 2000),

        // Mystery box weapons
        RayGun("Ray Gun", 80, 20, 40, 400, 1.0, 1.5, 100, WeaponClass.WONDER,
                new Color(100, 255, 100), 950),
        Thundergun("Thundergun", 200, 1, 4, 100, 3.0, 4.0, 50, WeaponClass.WONDER,
                new Color(100, 100, 255), 950),
        WintersHowl("Winter's Howl", 30, 30, 60, 300, 0.5, 1.0, 200, WeaponClass.WONDER,
                new Color(100, 200, 255), 950),
        Scavenger("Scavenger", 60, 4, 12, 200, 2.5, 3.0, 100, WeaponClass.WONDER,
                new Color(255, 100, 100), 950),
        Python("Python", 130, 6, 36, 200, 1.8, 2.5, 80, WeaponClass.PISTOL,
                new Color(150, 100, 50), 950),
        Crossbow("Crossbow", 150, 1, 5, 100, 2.0, 2.0, 150, WeaponClass.SPECIAL,
                new Color(180, 150, 100), 950),
        BallisticKnife("Ballistic Knife", 200, 1, 5, 150, 1.0, 1.5, 100, WeaponClass.SPECIAL,
                new Color(150, 150, 150), 950),
        // Additional wonder weapons
        WunderwaffeDG2("Wunderwaffe DG-2", 100, 3, 12, 150, 2.0, 2.0, 150, WeaponClass.WONDER,
                new Color(100, 255, 200), 950),
        ThunderGunUpgraded("Zeus Cannon", 300, 1, 3, 80, 3.5, 4.0, 60, WeaponClass.WONDER,
                new Color(100, 100, 255), 950),
        // Sniper rifles
        L96A1("L96A1", 180, 5, 30, 40, 2.5, 3.0, 120, WeaponClass.SNIPER,
                new Color(80, 80, 80), 950),
        PSG1("PSG-1", 200, 5, 25, 35, 2.2, 2.8, 130, WeaponClass.SNIPER,
                new Color(90, 90, 90), 950),
        // More SMGs
        PM63("PM63 RAK", 38, 25, 100, 700, 1.8, 2.2, 28, WeaponClass.SMG,
                new Color(85, 85, 95), 950),
        Spectre("Spectre", 32, 30, 120, 850, 2.0, 2.5, 30, WeaponClass.SMG,
                new Color(75, 75, 85), 950),
        MP5K("MP5K", 34, 25, 100, 900, 1.8, 2.2, 28, WeaponClass.SMG,
                new Color(70, 70, 80), 950),
        // More Assault Rifles
        Commando("Commando", 52, 30, 120, 650, 2.3, 3.0, 38, WeaponClass.ASSAULT,
                new Color(100, 90, 80), 950),
        FAL("FAL", 60, 20, 80, 500, 2.5, 3.0, 40, WeaponClass.ASSAULT,
                new Color(95, 85, 75), 950),
        G11("G11", 48, 50, 150, 800, 2.2, 2.8, 42, WeaponClass.ASSAULT,
                new Color(110, 100, 90), 950),
        AUG("AUG", 50, 30, 120, 680, 2.4, 2.8, 38, WeaponClass.ASSAULT,
                new Color(105, 95, 85), 950),
        // More Shotguns
        HS10("HS10", 85, 4, 16, 130, 1.8, 3.2, 9, WeaponClass.SHOTGUN,
                new Color(115, 95, 75), 950),
        SPAS12("SPAS-12", 90, 7, 21, 180, 2.0, 3.5, 10, WeaponClass.SHOTGUN,
                new Color(110, 90, 70), 950),
        Striker("Striker", 70, 12, 36, 240, 2.2, 3.0, 12, WeaponClass.SHOTGUN,
                new Color(120, 100, 80), 950),
        // More LMGs
        M60("M60", 55, 100, 200, 600, 4.2, 5.5, 55, WeaponClass.LMG,
                new Color(85, 95, 85), 950),
        HK21Upgraded("HK21E", 58, 80, 160, 650, 3.8, 5.0, 52, WeaponClass.LMG,
                new Color(75, 85, 75), 950),
        // More pistols
        M1911Upgraded("Mustang & Sally", 45, 8, 32, 400, 1.5, 1.8, 60, WeaponClass.PISTOL,
                new Color(150, 150, 100), 950),
        FiveSeven("Five-Seven", 40, 20, 60, 500, 1.8, 2.0, 35, WeaponClass.PISTOL,
                new Color(120, 120, 130), 950),
        // Death Machine (Power-up weapon)
        MINIGUN("Minigun", 60, 200, 600, 1200, 4.0, 0.5, 0, WeaponClass.LMG,
                new Color(200, 200, 50), 0),

        // Pack-a-Punched variants (stats are base * multiplier)
        // These are created dynamically from base weapons
        ;

        public final String name;
        public final double baseDamage;
        public final int magazineSize;
        public final int maxAmmo;
        public final int fireRate; // RPM
        public final double reloadTime;
        public final double adsTime;
        public final int pointsCost;
        public final WeaponClass weaponClass;
        public final Color color;
        public final int mysteryBoxCost;

        Type(String name, double baseDamage, int magazineSize, int maxAmmo,
             int fireRate, double reloadTime, double adsTime, int pointsCost,
             WeaponClass weaponClass, Color color, int mysteryBoxCost) {
            this.name = name;
            this.baseDamage = baseDamage;
            this.magazineSize = magazineSize;
            this.maxAmmo = maxAmmo;
            this.fireRate = fireRate;
            this.reloadTime = reloadTime;
            this.adsTime = adsTime;
            this.pointsCost = pointsCost;
            this.weaponClass = weaponClass;
            this.color = color;
            this.mysteryBoxCost = mysteryBoxCost;
        }
    }

    public enum WeaponClass {
        PISTOL, SMG, ASSAULT, SHOTGUN, LMG, SNIPER, WONDER, SPECIAL
    }

    // Current stats
    private Type type;
    private double damage;
    private int magazineSize;
    private int maxAmmo;
    private int currentAmmo;
    private int reserveAmmo;
    private int fireRate; // RPM
    private double reloadTime;
    private double adsTime;
    private int pointsCost;
    private WeaponClass weaponClass;
    private Color color;
    private boolean packAPunched = false;
    private String customName;

    // State
    private double lastShotTime = 0;
    private double shootCooldown = 0;
    private boolean reloading = false;
    private double reloadTimer = 0;
    private Random random = new Random();

    // Player reference for perk effects
    private Player player;

    public Weapon(Type type) {
        this.type = type;
        this.customName = type.name;
        initializeStats();
        currentAmmo = magazineSize;
        reserveAmmo = maxAmmo - magazineSize;
    }

    private void initializeStats() {
        this.damage = type.baseDamage;
        this.magazineSize = type.magazineSize;
        this.maxAmmo = type.maxAmmo;
        this.fireRate = type.fireRate;
        this.reloadTime = type.reloadTime;
        this.adsTime = type.adsTime;
        this.pointsCost = type.pointsCost;
        this.weaponClass = type.weaponClass;
        this.color = type.color;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void update(Player player) {
        this.player = player;
        if (shootCooldown > 0) {
            shootCooldown -= 1.0 / 60.0;
        }
        if (reloading) {
            reloadTimer -= 1.0 / 60.0;
            if (reloadTimer <= 0) {
                finishReload();
            }
        }
    }

    public boolean canShoot() {
        return !reloading && shootCooldown <= 0 && currentAmmo > 0;
    }

    public List<Bullet> shoot(double x, double y, double angle, Player shooter) {
        if (!canShoot()) return new ArrayList<>();

        // Apply player perks
        double finalDamage = damage;
        double finalFireRate = fireRate;
        double finalReloadTime = reloadTime;

        if (player != null) {
            finalDamage *= player.getDamageMultiplier();
            finalFireRate *= player.getFireRateMultiplier();
            finalReloadTime /= player.getReloadSpeedMultiplier();
        }

        // Calculate actual fire rate delay
        double fireDelay = 60.0 / finalFireRate; // seconds per shot
        shootCooldown = fireDelay;

        currentAmmo--;

        // Calculate spread based on weapon class
        double spread = getSpread();
        double actualAngle = angle + (random.nextDouble() - 0.5) * spread;

        // Determine if critical (Deadshot Daiquiri)
        boolean critical = false;
        if (player != null && player.hasDeadshot() && random.nextDouble() < 0.15) {
            critical = true;
            finalDamage *= 2.0;
        }

        // Create bullet(s)
        List<Bullet> bullets = new ArrayList<>();
        Color bulletColor = packAPunched ? new Color(255, 100, 255) : color;

        if (weaponClass == WeaponClass.SHOTGUN) {
            // Shotguns fire multiple pellets
            int pellets = type == Type.Olympia ? 6 : 8;
            for (int i = 0; i < pellets; i++) {
                double pelletAngle = actualAngle + (random.nextDouble() - 0.5) * spread;
                Bullet bullet = new Bullet(x, y, pelletAngle, getBulletSpeed(), finalDamage / pellets,
                        bulletColor, false, critical);
                bullets.add(bullet);
            }
        } else {
            Bullet bullet = new Bullet(x, y, actualAngle, getBulletSpeed(), finalDamage,
                    bulletColor, false, critical,
                    packAPunched && weaponClass != WeaponClass.SHOTGUN, // Piercing for PaP non-shotguns
                    packAPunched ? 2 : 0);
            bullets.add(bullet);
        }

        lastShotTime = System.currentTimeMillis() / 1000.0;
        return bullets;
    }

    private double getSpread() {
        switch (weaponClass) {
            case PISTOL: return 0.05;
            case SMG: return 0.08;
            case ASSAULT: return 0.06;
            case SHOTGUN: return 0.15;
            case LMG: return 0.1;
            case SNIPER: return 0.01;
            case WONDER: return 0.03;
            case SPECIAL: return 0.05;
            default: return 0.05;
        }
    }

    private double getBulletSpeed() {
        switch (weaponClass) {
            case PISTOL: return 800;
            case SMG: return 900;
            case ASSAULT: return 1000;
            case SHOTGUN: return 600;
            case LMG: return 950;
            case SNIPER: return 1500;
            case WONDER: return 1200;
            case SPECIAL: return 700;
            default: return 800;
        }
    }

    public void startReload() {
        if (reloading || currentAmmo == magazineSize || reserveAmmo == 0) return;

        reloading = true;
        double finalReloadTime = reloadTime;
        if (player != null) {
            finalReloadTime /= player.getReloadSpeedMultiplier();
        }
        reloadTimer = finalReloadTime;
    }

    private void finishReload() {
        int needed = magazineSize - currentAmmo;
        int toLoad = Math.min(needed, reserveAmmo);
        currentAmmo += toLoad;
        reserveAmmo -= toLoad;
        reloading = false;
    }

    public void packAPunch() {
        if (packAPunched) return;

        packAPunched = true;
        customName = type.name + " (PAP)";

        // Pack-a-Punch upgrades
        damage *= 2.5;
        magazineSize = (int)(magazineSize * 1.5);
        maxAmmo = (int)(maxAmmo * 2);
        fireRate = (int)(fireRate * 1.2);
        reloadTime *= 0.8;
        color = new Color(255, 100, 255); // Purple/pink tint

        // Refill ammo
        currentAmmo = magazineSize;
        reserveAmmo = maxAmmo - magazineSize;
    }

    public void addAmmo(int amount) {
        reserveAmmo = Math.min(reserveAmmo + amount, maxAmmo - currentAmmo);
    }

    public void setAmmo(int current, int reserve) {
        this.currentAmmo = Math.min(current, magazineSize);
        this.reserveAmmo = Math.min(reserve, maxAmmo - this.currentAmmo);
    }

    // Getters
    public Type getType() { return type; }
    public String getName() { return customName; }
    public double getDamage() { return damage; }
    public int getMagazineSize() { return magazineSize; }
    public int getMaxAmmo() { return maxAmmo; }
    public int getCurrentAmmo() { return currentAmmo; }
    public int getReserveAmmo() { return reserveAmmo; }
    public int getFireRate() { return fireRate; }
    public double getReloadTime() { return reloadTime; }
    public double getAdsTime() { return adsTime; }
    public int getPointsCost() { return pointsCost; }
    public WeaponClass getWeaponClass() { return weaponClass; }
    public Color getColor() { return color; }
    public boolean isPackAPunched() { return packAPunched; }
    public boolean isReloading() { return reloading; }
    public double getReloadProgress() { return reloading ? 1.0 - (reloadTimer / reloadTime) : 1.0; }

    public static Weapon createFromMysteryBox() {
        Type[] boxWeapons = {
            Type.RayGun, Type.Thundergun, Type.WintersHowl, Type.Scavenger,
            Type.Python, Type.Crossbow, Type.BallisticKnife,
            Type.MP40, Type.MP5, Type.AK74u, Type.M16,
            Type.Olympia, Type.Stakeout, Type.RPK, Type.HK21,
            Type.WunderwaffeDG2, Type.ThunderGunUpgraded,
            Type.L96A1, Type.PSG1,
            Type.PM63, Type.Spectre, Type.MP5K,
            Type.Commando, Type.FAL, Type.G11, Type.AUG,
            Type.HS10, Type.SPAS12, Type.Striker,
            Type.M60, Type.HK21Upgraded,
            Type.M1911Upgraded, Type.FiveSeven
        };
        Type randomType = boxWeapons[new Random().nextInt(boxWeapons.length)];
        return new Weapon(randomType);
    }

    public static Weapon createWallWeapon(Type type) {
        return new Weapon(type);
    }
}
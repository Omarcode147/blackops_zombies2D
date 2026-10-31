import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Bullet/Projectile class for player and zombie weapons.
 */
public class Bullet extends Entity {
    private double damage;
    private double angle;
    private double speed;
    private double lifeTime;
    private double maxLifeTime = 3.0; // seconds
    private boolean fromZombie = false;
    private boolean critical = false;
    private Color color;
    private boolean piercing = false;
    private int pierceCount = 0;
    private int maxPierce = 0;

    public Bullet(double x, double y, double angle, double speed, double damage,
                  Color color, boolean fromZombie, boolean critical) {
        super(x, y, 6, 6, 1); // Small bullet, health not used
        this.angle = angle;
        this.speed = speed;
        this.damage = damage;
        this.color = color;
        this.fromZombie = fromZombie;
        this.critical = critical;
        this.velocityX = Math.cos(angle) * speed;
        this.velocityY = Math.sin(angle) * speed;
    }

    public Bullet(double x, double y, double angle, double speed, double damage,
                  Color color, boolean fromZombie, boolean critical, boolean piercing, int maxPierce) {
        this(x, y, angle, speed, damage, color, fromZombie, critical);
        this.piercing = piercing;
        this.maxPierce = maxPierce;
    }

    @Override
    public void update() {
        if (!alive) return;

        x += velocityX / 60.0;
        y += velocityY / 60.0;
        lifeTime += 1.0 / 60.0;
        updateBounds();

        if (lifeTime >= maxLifeTime) {
            alive = false;
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!alive) return;

        g.setColor(color);
        // Draw bullet as a small circle with trail
        int xInt = (int)x;
        int yInt = (int)y;
        int size = (int)width;

        // Trail effect
        for (int i = 1; i <= 3; i++) {
            double trailX = x - velocityX * i / 60.0;
            double trailY = y - velocityY * i / 60.0;
            float alpha = 1.0f - i * 0.3f;
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255)));
            g.fillOval((int)(trailX - size/2), (int)(trailY - size/2), size, size);
        }

        // Main bullet
        g.setColor(color);
        g.fillOval(xInt - size/2, yInt - size/2, size, size);

        // Critical hit indicator
        if (critical) {
            g.setColor(Color.YELLOW);
            g.drawOval(xInt - size/2 - 1, yInt - size/2 - 1, size + 2, size + 2);
        }
    }

    public void deactivate() {
        alive = false;
    }

    public boolean isActive() {
        return alive;
    }

    public void onHit() {
        if (piercing) {
            pierceCount++;
            if (pierceCount >= maxPierce) {
                alive = false;
            }
        } else {
            alive = false;
        }
    }

    // Getters
    public double getDamage() { return damage; }
    public double getAngle() { return angle; }
    public double getSpeed() { return speed; }
    public boolean isFromZombie() { return fromZombie; }
    public boolean isCritical() { return critical; }
    public boolean isPiercing() { return piercing; }
    public Color getColor() { return color; }
}
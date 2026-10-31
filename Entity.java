import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Base class for all game entities.
 * Provides common properties like position, velocity, bounds, and health.
 */
public abstract class Entity {
    protected double x, y;
    protected double velocityX, velocityY;
    protected double width, height;
    protected double maxHealth;
    protected double health;
    protected boolean alive = true;
    protected Rectangle2D.Double bounds;

    public Entity(double x, double y, double width, double height, double health) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxHealth = health;
        this.health = health;
        updateBounds();
    }

    public void updateBounds() {
        bounds = new Rectangle2D.Double(x - width / 2, y - height / 2, width, height);
    }

    public Rectangle2D.Double getBounds() {
        return bounds;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; updateBounds(); }
    public void setY(double y) { this.y = y; updateBounds(); }
    public void setPosition(double x, double y) { this.x = x; this.y = y; updateBounds(); }

    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public void setVelocity(double vx, double vy) { this.velocityX = vx; this.velocityY = vy; }
    public void addVelocity(double vx, double vy) { this.velocityX += vx; this.velocityY += vy; }

    public double getWidth() { return width; }
    public double getHeight() { return height; }

    public double getHealth() { return health; }
    public double getMaxHealth() { return maxHealth; }
    public double getHealthPercent() { return health / maxHealth; }

    public boolean isAlive() { return alive; }

    public void takeDamage(double damage) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            alive = false;
            onDeath();
        }
    }

    public void heal(double amount) {
        health = Math.min(health + amount, maxHealth);
    }

    protected void onDeath() {}

    public abstract void update();
    public abstract void render(Graphics2D g);
}
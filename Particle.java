import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Particle effects for hits, deaths, muzzle flashes, power-ups, etc.
 */
public class Particle {
    private double x, y;
    private double velocityX, velocityY;
    private Color color;
    private int colorR, colorG, colorB, colorA; // Cached color components
    private double lifeTime = 0;
    private double maxLifeTime;
    private double size;
    private double startSize;
    private boolean alive = true;
    private ParticleType type;

    public enum ParticleType {
        BLOOD, SPARK, SMOKE, MUZZLE_FLASH, POWER_UP, EXPLOSION
    }

    public Particle(double x, double y, double velocityX, double velocityY,
                    Color color, double maxLifeTime) {
        this(x, y, velocityX, velocityY, color, maxLifeTime, ParticleType.SPARK);
    }

    public Particle(double x, double y, double velocityX, double velocityY,
                    Color color, double maxLifeTime, ParticleType type) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.color = color;
        this.colorR = color.getRed();
        this.colorG = color.getGreen();
        this.colorB = color.getBlue();
        this.colorA = color.getAlpha();
        this.maxLifeTime = maxLifeTime;
        this.type = type;
        this.startSize = 4 + Math.random() * 4;
        this.size = startSize;
    }

    public void update() {
        if (!alive) return;

        // Physics
        x += velocityX / 60.0;
        y += velocityY / 60.0;

        // Gravity for blood/sparks
        if (type == ParticleType.BLOOD || type == ParticleType.SPARK) {
            velocityY += 200 / 60.0; // Gravity
        }

        // Drag
        velocityX *= 0.98;
        velocityY *= 0.98;

        // Size fade
        lifeTime += 1.0 / 60.0;
        double progress = lifeTime / maxLifeTime;
        size = startSize * (1.0 - progress);

        if (lifeTime >= maxLifeTime || size <= 0.5) {
            alive = false;
        }
    }

    public void render(Graphics2D g) {
        if (!alive) return;

        double progress = lifeTime / maxLifeTime;
        float alpha = (float)(1.0 - progress);
        int alphaInt = (int)(alpha * 255);
        int finalAlpha = Math.min(alphaInt, colorA);

        g.setColor(new Color(colorR, colorG, colorB, finalAlpha));

        int xInt = (int)x;
        int yInt = (int)y;
        int sizeInt = (int)size;

        switch (type) {
            case BLOOD:
                // Blood droplet
                g.fillOval(xInt - sizeInt/2, yInt - sizeInt/2, sizeInt, sizeInt);
                break;
            case SPARK:
                // Spark with trail
                g.fillOval(xInt - sizeInt/2, yInt - sizeInt/2, sizeInt, sizeInt);
                // Trail
                for (int i = 1; i <= 3; i++) {
                    double tx = x - velocityX * i / 60.0;
                    double ty = y - velocityY * i / 60.0;
                    float tAlpha = (float)(alpha * (1.0 - i * 0.3));
                    int trailAlpha = (int)(tAlpha * 255);
                    g.setColor(new Color(colorR, colorG, colorB, trailAlpha));
                    g.fillOval((int)(tx - sizeInt/2), (int)(ty - sizeInt/2), sizeInt, sizeInt);
                }
                break;
            case SMOKE:
                // Expanding smoke ring
                g.drawOval(xInt - sizeInt, yInt - sizeInt, sizeInt * 2, sizeInt * 2);
                break;
            case MUZZLE_FLASH:
                // Bright flash
                g.fillOval(xInt - sizeInt, yInt - sizeInt, sizeInt * 2, sizeInt * 2);
                // Cross shape
                g.drawLine(xInt - sizeInt, yInt, xInt + sizeInt, yInt);
                g.drawLine(xInt, yInt - sizeInt, xInt, yInt + sizeInt);
                break;
            case POWER_UP:
                // Rising sparkle
                g.fillOval(xInt - sizeInt/2, yInt - sizeInt/2, sizeInt, sizeInt);
                // Star shape
                g.drawLine(xInt, yInt - sizeInt, xInt, yInt + sizeInt);
                g.drawLine(xInt - sizeInt, yInt, xInt + sizeInt, yInt);
                break;
            case EXPLOSION:
                // Expanding ring
                g.drawOval(xInt - sizeInt, yInt - sizeInt, sizeInt * 2, sizeInt * 2);
                g.drawOval(xInt - sizeInt/2, yInt - sizeInt/2, sizeInt, sizeInt);
                break;
        }
    }

    public boolean isAlive() { return alive; }
    public double getX() { return x; }
    public double getY() { return y; }
    public Color getColor() { return color; }

    // Factory methods for common particle effects
    public static Particle createBlood(double x, double y) {
        double angle = Math.random() * Math.PI * 2;
        double speed = 50 + Math.random() * 150;
        return new Particle(x, y,
            Math.cos(angle) * speed,
            Math.sin(angle) * speed,
            new Color(180, 0, 0),
            0.5 + Math.random() * 0.5,
            ParticleType.BLOOD);
    }

    public static Particle createSpark(double x, double y, Color color) {
        double angle = Math.random() * Math.PI * 2;
        double speed = 100 + Math.random() * 200;
        return new Particle(x, y,
            Math.cos(angle) * speed,
            Math.sin(angle) * speed,
            color,
            0.3 + Math.random() * 0.4,
            ParticleType.SPARK);
    }

    public static Particle createMuzzleFlash(double x, double y, double angle) {
        double spread = (Math.random() - 0.5) * 0.5;
        double speed = 200 + Math.random() * 300;
        return new Particle(x, y,
            Math.cos(angle + spread) * speed,
            Math.sin(angle + spread) * speed,
            new Color(255, 255, 100, 200),
            0.1 + Math.random() * 0.1,
            ParticleType.MUZZLE_FLASH);
    }

    public static Particle createPowerUp(double x, double y, Color color) {
        double angle = Math.random() * Math.PI * 2;
        double speed = 30 + Math.random() * 80;
        return new Particle(x, y,
            Math.cos(angle) * speed,
            Math.sin(angle) * speed - 50, // Upward bias
            color,
            1.0 + Math.random() * 1.0,
            ParticleType.POWER_UP);
    }

    public static Particle createExplosion(double x, double y) {
        double angle = Math.random() * Math.PI * 2;
        double speed = 50 + Math.random() * 200;
        return new Particle(x, y,
            Math.cos(angle) * speed,
            Math.sin(angle) * speed,
            new Color(255, 150 + (int)(Math.random() * 100), 0),
            0.5 + Math.random() * 0.5,
            ParticleType.EXPLOSION);
    }
}
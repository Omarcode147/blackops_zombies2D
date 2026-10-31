import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Random;

/**
 * Zombie enemy class with different types and AI behavior.
 */
public class Zombie extends Entity {
    public enum Type {
        // Damage reduced by 25% from original values
        NORMAL(100, 4.5, 1.0, 50, new Color(60, 120, 60), 1.0),
        FAST(60, 3.75, 1.8, 30, new Color(80, 160, 80), 1.2),
        TANK(300, 9, 0.6, 100, new Color(40, 100, 40), 0.8),
        CRAWLER(40, 3, 0.8, 20, new Color(100, 80, 60), 0.9),
        BOSS(1000, 13.5, 0.8, 500, new Color(150, 30, 30), 0.7);

        public final double health;
        public final double damage;
        public final double speedMultiplier;
        public final int pointsValue;
        public final Color color;
        public final double sizeMultiplier;

        Type(double health, double damage, double speedMultiplier, int pointsValue, Color color, double sizeMultiplier) {
            this.health = health;
            this.damage = damage;
            this.speedMultiplier = speedMultiplier;
            this.pointsValue = pointsValue;
            this.color = color;
            this.sizeMultiplier = sizeMultiplier;
        }
    }

    private static final double BASE_SPEED = 80;
    private static final double ATTACK_RANGE = 35;
    private static final double ATTACK_COOLDOWN = 1.0; // seconds

    private Type type;
    private double speed;
    private double attackTimer = 0;
    private Random random = new Random();

    // Pathfinding
    private double stuckTimer = 0;
    private double lastX, lastY;

    // Player reference for movement
    private final Player playerRef;

    public Zombie(double x, double y, Type type, int waveNumber, Player player) {
        super(x, y,
            32 * type.sizeMultiplier,
            32 * type.sizeMultiplier,
            type.health * (1.0 + (waveNumber - 1) * 0.1));

        this.type = type;
        this.speed = BASE_SPEED * type.speedMultiplier;
        double healthMult = 1.0 + (waveNumber - 1) * 0.1;
        this.maxHealth = type.health * healthMult;
        this.health = this.maxHealth;
        this.playerRef = player;
    }

    public void update(Player player, Map map, List<Zombie> allZombies) {
        if (!alive) return;

        attackTimer -= 1.0 / 60.0;

        // Move towards player
        moveTowardsTarget(map, allZombies);

        // Attack player if in range
        attackPlayer(player);

        updateBounds();
        lastX = x;
        lastY = y;
    }

    private void moveTowardsTarget(Map map, List<Zombie> allZombies) {
        if (playerRef == null) return;

        double targetX = playerRef.getX();
        double targetY = playerRef.getY();

        double dx = targetX - x;
        double dy = targetY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 0) {
            dx /= dist;
            dy /= dist;

            // Separation from other zombies
            double sepX = 0, sepY = 0;
            int neighbors = 0;
            for (Zombie other : allZombies) {
                if (other != this && other.isAlive()) {
                    double odx = x - other.x;
                    double ody = y - other.y;
                    double odist = Math.sqrt(odx * odx + ody * ody);
                    if (odist < 40 && odist > 0) {
                        sepX += odx / odist;
                        sepY += ody / odist;
                        neighbors++;
                    }
                }
            }
            if (neighbors > 0) {
                sepX /= neighbors;
                sepY /= neighbors;
                dx += sepX * 0.5;
                dy += sepY * 0.5;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len > 0) {
                    dx /= len;
                    dy /= len;
                }
            }

            // Check if direct path to player is blocked by walls
            boolean directPathBlocked = !map.hasLineOfSight(x, y, targetX, targetY);

            // If direct path is blocked, try to find a window or open door to path through
            double waypointX = targetX;
            double waypointY = targetY;
            Window targetWindow = null;
            Door targetDoor = null;

            if (directPathBlocked) {
                // Find nearest window or open door that gets us closer to player
                Window bestWindow = findBestWindow(map, targetX, targetY);
                Door bestDoor = findBestDoor(map, targetX, targetY);

                // Prefer doors if open, otherwise windows
                if (bestDoor != null) {
                    targetDoor = bestDoor;
                    waypointX = bestDoor.getCenterX();
                    waypointY = bestDoor.getCenterY();
                } else if (bestWindow != null) {
                    targetWindow = bestWindow;
                    // For windows, aim for a point PAST the window on the player's side
                    // This helps the zombie move THROUGH the window instead of getting stuck at the center
                    double wx = bestWindow.getCenterX();
                    double wy = bestWindow.getCenterY();
                    double windowToPlayerDx = targetX - wx;
                    double windowToPlayerDy = targetY - wy;
                    double windowToPlayerDist = Math.hypot(windowToPlayerDx, windowToPlayerDy);
                    if (windowToPlayerDist > 0) {
                        // Aim 50px past the window towards the player
                        waypointX = wx + (windowToPlayerDx / windowToPlayerDist) * 50;
                        waypointY = wy + (windowToPlayerDy / windowToPlayerDist) * 50;
                    } else {
                        waypointX = wx;
                        waypointY = wy;
                    }
                }
            }

            // Recalculate direction to waypoint (player or window/door)
            dx = waypointX - x;
            dy = waypointY - y;
            dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 0) {
                dx /= dist;
                dy /= dist;

                // If targeting a window and we're close to the wall, try to move straight through the gap
                if (targetWindow != null && dist < 120) {
                    // Check if we can move straight through the window gap
                    double windowDx = targetWindow.getCenterX() - x;
                    double windowDy = targetWindow.getCenterY() - y;
                    double windowDist = Math.hypot(windowDx, windowDy);

                    if (windowDist < 80) {
                        // We're near the window - determine wall orientation and move perpendicular through gap
                        // Check if window is horizontal (top/bottom wall) or vertical (left/right wall)
                        boolean isHorizontal = targetWindow.getWidth() > targetWindow.getHeight();
                        double moveThroughX = 0, moveThroughY = 0;

                        if (isHorizontal) {
                            // Window on horizontal wall - move vertically through gap
                            moveThroughY = (targetY > y) ? 1 : -1;
                        } else {
                            // Window on vertical wall - move horizontally through gap
                            moveThroughX = (targetX > x) ? 1 : -1;
                        }

                        double newX = x + moveThroughX * speed / 60.0;
                        double newY = y + moveThroughY * speed / 60.0;

                        // Try to move through the window gap
                        if (!map.checkCollision(new Rectangle2D.Double(newX - width/2, newY - height/2, width, height))) {
                            x = newX;
                            y = newY;
                            // Successfully moved through window, skip normal movement
                        } else {
                            // Normal movement
                            moveNormally(map, dx, dy, speed, targetWindow, targetDoor);
                        }
                    } else {
                        // Normal movement towards waypoint
                        moveNormally(map, dx, dy, speed, targetWindow, targetDoor);
                    }
                } else {
                    // Normal movement towards waypoint
                    moveNormally(map, dx, dy, speed, targetWindow, targetDoor);
                }

                // Stuck detection
                double movedDist = Math.sqrt((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY));
                if (movedDist < 1) {
                    stuckTimer += 1.0 / 60.0;
                } else {
                    stuckTimer = 0;
                }

                // If stuck for a while, try more aggressive movement
                if (stuckTimer > 1.5) {
                    // Try to move towards any nearby window or door
                    Window nearbyWindow = findNearestWindow(map);
                    Door nearbyDoor = findNearestOpenDoor(map);

                    if (nearbyDoor != null) {
                        double doorDx = nearbyDoor.getCenterX() - x;
                        double doorDy = nearbyDoor.getCenterY() - y;
                        double doorDist = Math.sqrt(doorDx * doorDx + doorDy * doorDy);
                        if (doorDist > 0) {
                            x += (doorDx / doorDist) * speed / 60.0 * 1.5;
                            y += (doorDy / doorDist) * speed / 60.0 * 1.5;
                        }
                    } else if (nearbyWindow != null) {
                        double winDx = nearbyWindow.getCenterX() - x;
                        double winDy = nearbyWindow.getCenterY() - y;
                        double winDist = Math.sqrt(winDx * winDx + winDy * winDy);
                        if (winDist > 0) {
                            x += (winDx / winDist) * speed / 60.0 * 1.5;
                            y += (winDy / winDist) * speed / 60.0 * 1.5;
                        }
                    } else {
                        // Fallback: random movement with bias towards player
                        double randomAngle = random.nextDouble() * Math.PI * 2;
                        double biasAngle = Math.atan2(targetY - y, targetX - x);
                        double finalAngle = (randomAngle * 0.5 + biasAngle * 0.5);
                        x += Math.cos(finalAngle) * speed / 60.0;
                        y += Math.sin(finalAngle) * speed / 60.0;
                    }
                    stuckTimer = 0;
                }
            }
        }
    }

    /**
     * Normal movement with collision checking and wall sliding.
     */
    private void moveNormally(Map map, double dx, double dy, double speed, Window targetWindow, Door targetDoor) {
        double newX = x + dx * speed / 60.0;
        double newY = y + dy * speed / 60.0;

        // Try move X
        boolean movedX = false;
        if (!map.checkCollision(new Rectangle2D.Double(newX - width/2, y - height/2, width, height))) {
            x = newX;
            movedX = true;
        }

        // Try move Y
        boolean movedY = false;
        if (!map.checkCollision(new Rectangle2D.Double(x - width/2, newY - height/2, width, height))) {
            y = newY;
            movedY = true;
        }

        // If both X and Y blocked, try sliding along walls
        if (!movedX && !movedY) {
            // If we have a target window or door, try sliding TOWARDS it
            if (targetWindow != null) {
                double windowCx = targetWindow.getCenterX();
                double windowCy = targetWindow.getCenterY();
                double toWindowDx = windowCx - x;
                double toWindowDy = windowCy - y;
                double toWindowDist = Math.hypot(toWindowDx, toWindowDy);

                if (toWindowDist > 0) {
                    // Try sliding towards the window center
                    double slideX = (toWindowDx / toWindowDist) * speed / 60.0;
                    double slideY = (toWindowDy / toWindowDist) * speed / 60.0;

                    if (!map.checkCollision(new Rectangle2D.Double((x + slideX) - width/2, y - height/2, width, height))) {
                        x += slideX;
                        return;
                    }
                    if (!map.checkCollision(new Rectangle2D.Double(x - width/2, (y + slideY) - height/2, width, height))) {
                        y += slideY;
                        return;
                    }
                }
            } else if (targetDoor != null) {
                double doorCx = targetDoor.getCenterX();
                double doorCy = targetDoor.getCenterY();
                double toDoorDx = doorCx - x;
                double toDoorDy = doorCy - y;
                double toDoorDist = Math.hypot(toDoorDx, toDoorDy);

                if (toDoorDist > 0) {
                    // Try sliding towards the door center
                    double slideX = (toDoorDx / toDoorDist) * speed / 60.0;
                    double slideY = (toDoorDy / toDoorDist) * speed / 60.0;

                    if (!map.checkCollision(new Rectangle2D.Double((x + slideX) - width/2, y - height/2, width, height))) {
                        x += slideX;
                        return;
                    }
                    if (!map.checkCollision(new Rectangle2D.Double(x - width/2, (y + slideY) - height/2, width, height))) {
                        y += slideY;
                        return;
                    }
                }
            }

            // Fallback: try perpendicular sliding
            double slideAngle = Math.atan2(dy, dx) + Math.PI / 2;
            double slideX = Math.cos(slideAngle) * speed / 60.0;
            double slideY = Math.sin(slideAngle) * speed / 60.0;

            // Try both perpendicular directions
            if (!map.checkCollision(new Rectangle2D.Double((x + slideX) - width/2, y - height/2, width, height))) {
                x += slideX;
            } else if (!map.checkCollision(new Rectangle2D.Double((x - slideX) - width/2, y - height/2, width, height))) {
                x -= slideX;
            } else if (!map.checkCollision(new Rectangle2D.Double(x - width/2, (y + slideY) - height/2, width, height))) {
                y += slideY;
            } else if (!map.checkCollision(new Rectangle2D.Double(x - width/2, (y - slideY) - height/2, width, height))) {
                y -= slideY;
            }
        }
    }

    /**
     * Find the best window to path through to reach the target.
     * Returns the window that minimizes total distance (zombie -> window -> target).
     */
    private Window findBestWindow(Map map, double targetX, double targetY) {
        Window bestWindow = null;
        double bestDist = Double.MAX_VALUE;

        for (Window window : map.getWindows()) {
            double wx = window.getCenterX();
            double wy = window.getCenterY();

            // Distance from zombie to window + window to target
            double distToWindow = Math.hypot(wx - x, wy - y);
            double distWindowToTarget = Math.hypot(wx - targetX, wy - targetY);
            double totalDist = distToWindow + distWindowToTarget;

            // Only consider windows that are roughly in the right direction
            // (window should be between zombie and target, or at least not too far off)
            double directDist = Math.hypot(targetX - x, targetY - y);
            if (totalDist < directDist * 1.5 && totalDist < bestDist) { // Allow some detour
                bestDist = totalDist;
                bestWindow = window;
            }
        }
        return bestWindow;
    }

    /**
     * Find the best open door to path through to reach the target.
     */
    private Door findBestDoor(Map map, double targetX, double targetY) {
        Door bestDoor = null;
        double bestDist = Double.MAX_VALUE;

        for (Door door : map.getDoors()) {
            if (!door.isOpened()) continue; // Only use open doors

            double dx = door.getCenterX();
            double dy = door.getCenterY();

            double distToDoor = Math.hypot(dx - x, dy - y);
            double distDoorToTarget = Math.hypot(dx - targetX, dy - targetY);
            double totalDist = distToDoor + distDoorToTarget;

            double directDist = Math.hypot(targetX - x, targetY - y);
            if (totalDist < directDist * 1.5 && totalDist < bestDist) {
                bestDist = totalDist;
                bestDoor = door;
            }
        }
        return bestDoor;
    }

    /**
     * Find nearest window (for stuck recovery).
     */
    private Window findNearestWindow(Map map) {
        Window nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Window window : map.getWindows()) {
            double dist = Math.hypot(window.getCenterX() - x, window.getCenterY() - y);
            if (dist < nearestDist && dist < 400) { // Only consider windows within 400px
                nearestDist = dist;
                nearest = window;
            }
        }
        return nearest;
    }

    /**
     * Find nearest open door (for stuck recovery).
     */
    private Door findNearestOpenDoor(Map map) {
        Door nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Door door : map.getDoors()) {
            if (!door.isOpened()) continue;
            double dist = Math.hypot(door.getCenterX() - x, door.getCenterY() - y);
            if (dist < nearestDist && dist < 400) {
                nearestDist = dist;
                nearest = door;
            }
        }
        return nearest;
    }

    private void attackPlayer(Player player) {
        double dist = distanceTo(player.getX(), player.getY());
        if (dist <= ATTACK_RANGE && attackTimer <= 0) {
            player.takeDamage(getDamage());
            attackTimer = ATTACK_COOLDOWN;
        }
    }

    private double distanceTo(double tx, double ty) {
        double dx = tx - x;
        double dy = ty - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public boolean takeDamage(double damage, boolean isCritical) {
        double finalDamage = damage;
        if (isCritical) {
            finalDamage *= 2.0; // Critical hit = 2x damage
        }
        health -= finalDamage;
        if (health <= 0) {
            health = 0;
            alive = false;
            return true; // Killed
        }
        return false;
    }

    @Override
    public void update() {
        // Actual update is in update(Player, Map, List<Zombie>)
    }

    @Override
    public void render(Graphics2D g) {
        // Zombie body
        g.setColor(type.color);
        g.fillOval((int)(x - width/2), (int)(y - height/2), (int)width, (int)height);

        // Zombie outline
        g.setColor(type.color.darker());
        g.drawOval((int)(x - width/2), (int)(y - height/2), (int)width, (int)height);

        // Eyes (red)
        g.setColor(Color.RED);
        int eyeSize = (int)(width / 6);
        int eyeOffsetX = (int)(width / 4);
        int eyeOffsetY = (int)(height / 5);
        g.fillOval((int)(x - eyeOffsetX), (int)(y - eyeOffsetY), eyeSize, eyeSize);
        g.fillOval((int)(x + eyeOffsetX - eyeSize), (int)(y - eyeOffsetY), eyeSize, eyeSize);

        // Health bar
        renderHealthBar(g);

        // Type indicator for special zombies
        if (type != Type.NORMAL) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString(type.name(), (int)(x - 15), (int)(y - height/2 - 5));
        }
    }

    private void renderHealthBar(Graphics2D g) {
        double barWidth = width;
        double barHeight = 4;
        double barX = x - barWidth / 2;
        double barY = y - height / 2 - 8;

        g.setColor(new Color(100, 0, 0));
        g.fillRect((int)barX, (int)barY, (int)barWidth, (int)barHeight);

        g.setColor(new Color(255, 50, 50));
        g.fillRect((int)barX, (int)barY, (int)(barWidth * getHealthPercent()), (int)barHeight);
    }

    // Getters
    public Type getType() { return type; }
    public double getDamage() { return type.damage; }
    public int getPointsValue() { return type.pointsValue; }
    public Color getColor() { return type.color; }
}
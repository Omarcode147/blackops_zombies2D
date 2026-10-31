import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Game map with walls, floors, barriers, spawn points, perks, wall guns, doors, and windows.
 *
 * Layout: 4 corner rooms + 1 central room connecting them all.
 * Each room contains:
 *   - A perk machine to buy
 *   - A wall gun to buy
 *   - A door to buy to enter
 *   - 2-3 windows (zombies can pass through, players cannot)
 *
 * Corner rooms:
 *   Top-left, Top-right, Bottom-left, Bottom-right
 *
 * Central room connects to all 4 corner rooms via doors.
 */
public class Map {
    public static final int WIDTH = 3000;
    public static final int HEIGHT = 2000;
    public static final int TILE_SIZE = 64;

    private List<Rectangle2D.Double> walls = new ArrayList<>();
    private List<Rectangle2D.Double> floorTiles = new ArrayList<>();
    private List<Perk> perks = new ArrayList<>();
    private List<WallGun> wallGuns = new ArrayList<>();
    private List<Door> doors = new ArrayList<>();
    private List<Window> windows = new ArrayList<>();
    private List<Point> zombieSpawnPoints = new ArrayList<>();
    private List<Point> playerSpawnPoints = new ArrayList<>();
    private Random random = new Random();

    public Map() {
        generateMap();
    }

    private void generateMap() {
        // Generate floor tiles across the entire map
        for (int x = 0; x < WIDTH; x += TILE_SIZE) {
            for (int y = 0; y < HEIGHT; y += TILE_SIZE) {
                floorTiles.add(new Rectangle2D.Double(x, y, TILE_SIZE, TILE_SIZE));
            }
        }

        // Create walls around the map border
        addWall(0, 0, WIDTH, 50);      // Top
        addWall(0, 0, 50, HEIGHT);     // Left
        addWall(0, HEIGHT - 50, WIDTH, 50); // Bottom
        addWall(WIDTH - 50, 0, 50, HEIGHT); // Right

        // Create the 4 corner rooms + central room
        createCornerRooms();
        createCentralRoom();

        // Create perk locations (one per room)
        createPerks();

        // Create wall guns (one per room)
        createWallGuns();

        // Create doors (one entrance per corner room + central connections)
        createDoors();

        // Create windows (2-3 per room, zombies pass through, players blocked)
        createWindows();

        // Create zombie spawn points
        createZombieSpawns();

        // Create player spawn points
        createPlayerSpawns();
    }

    /**
     * Creates the 4 corner rooms with walls (leaving gaps for doors).
     * Each room is 600x600.
     */
    private void createCornerRooms() {
        // ----- Top-Left Room (x: 200-800, y: 200-800) -----
        // Top wall (split for window at x=290-370, width=80)
        addWall(200, 200, 90, 50);      // Left of window (x=200-290)
        addWall(370, 200, 430, 50);     // Right of window (x=370-800)
        // Left wall (split for window at y=350-430, height=80)
        addWall(200, 200, 50, 150);     // Above window (y=200-350)
        addWall(200, 430, 50, 370);     // Below window (y=430-800)
        // Right wall (split for door to central at y=450-550, AND window at y=300-380)
        addWall(750, 200, 50, 100);     // Above window (y=200-300)
        addWall(750, 380, 50, 70);      // Between window and door (y=380-450)
        addWall(750, 550, 50, 250);     // Lower part (y=550-800)
        // Bottom wall (split for entrance door)
        addWall(200, 750, 250, 50);     // Left of door
        addWall(550, 750, 250, 50);     // Right of door

        // ----- Top-Right Room (x: 2200-2800, y: 200-800) -----
        // Top wall (split for window at x=2390-2470, width=80)
        addWall(2200, 200, 190, 50);    // Left of window (x=2200-2390)
        addWall(2470, 200, 330, 50);    // Right of window (x=2470-2800)
        // Right wall (split for window at y=350-430, height=80)
        addWall(2750, 200, 50, 150);    // Above window (y=200-350)
        addWall(2750, 430, 50, 370);    // Below window (y=430-800)
        // Left wall (split for door to central at y=450-550, AND window at y=300-380)
        addWall(2200, 200, 50, 100);    // Above window (y=200-300)
        addWall(2200, 380, 50, 70);     // Between window and door (y=380-450)
        addWall(2200, 550, 50, 250);    // Lower part (y=550-800)
        // Bottom wall (split for entrance door at x=2450, width=100)
        addWall(2200, 750, 250, 50);    // Left of door (x=2200-2450)
        addWall(2550, 750, 250, 50);    // Right of door (x=2550-2800)

        // ----- Bottom-Left Room (x: 200-800, y: 1200-1800) -----
        // Top wall (split for entrance door at x=450, width=100)
        addWall(200, 1150, 250, 50);    // Left of door (x=200-450)
        addWall(550, 1150, 250, 50);    // Right of door (x=550-800)
        // Left wall (split for window at y=1450-1530, height=80)
        addWall(200, 1200, 50, 250);    // Above window (y=1200-1450)
        addWall(200, 1530, 50, 270);    // Below window (y=1530-1800)
        // Right wall (split for door to central at y=1450-1550, AND window at y=1300-1380)
        addWall(750, 1200, 50, 100);    // Above window (y=1200-1300)
        addWall(750, 1380, 50, 70);     // Between window and door (y=1380-1450)
        addWall(750, 1550, 50, 250);    // Lower part (y=1550-1800)
        // Bottom wall (split for window at x=290-370, width=80)
        addWall(200, 1750, 90, 50);     // Left of window (x=200-290)
        addWall(370, 1750, 430, 50);    // Right of window (x=370-800)

        // ----- Bottom-Right Room (x: 2200-2800, y: 1200-1800) -----
        // Top wall (split for entrance door at x=2450, width=100)
        addWall(2200, 1150, 250, 50);   // Left of door (x=2200-2450)
        addWall(2550, 1150, 250, 50);   // Right of door (x=2550-2800)
        // Right wall (split for window at y=1450-1530, height=80)
        addWall(2750, 1200, 50, 250);   // Above window (y=1200-1450)
        addWall(2750, 1530, 50, 270);   // Below window (y=1530-1800)
        // Left wall (split for door to central at y=1450-1550, AND window at y=1300-1380)
        addWall(2200, 1200, 50, 100);   // Above window (y=1200-1300)
        addWall(2200, 1380, 50, 70);    // Between window and door (y=1380-1450)
        addWall(2200, 1550, 50, 250);   // Lower part (y=1550-1800)
        // Bottom wall (split for window at x=2390-2470, width=80)
        addWall(2200, 1750, 190, 50);   // Left of window (x=2200-2390)
        addWall(2470, 1750, 330, 50);   // Right of window (x=2470-2800)
    }

    /**
     * Creates the central room connecting all 4 corner rooms.
     * Central room is 800x1000 (x: 1100-1900, y: 500-1500) - bigger than before.
     */
    private void createCentralRoom() {
        // Top wall (split for window at x=1360-1440, width=80)
        addWall(1100, 500, 260, 50);    // Left of window (x=1100-1360)
        addWall(1440, 500, 460, 50);    // Right of window (x=1440-1900)
        // Left wall (split for doors to TL at y=750 and BL at y=1150, plus window at y=940-1020)
        addWall(1100, 500, 50, 250);    // Above TL door (y=500-750)
        addWall(1100, 850, 50, 90);     // Between TL door and window (y=850-940)
        addWall(1100, 1020, 50, 130);   // Between window and BL door (y=1020-1150)
        addWall(1100, 1250, 50, 250);   // Below BL door (y=1250-1500)
        // Right wall (split for doors to TR at y=750 and BR at y=1150, plus window at y=1000-1080)
        addWall(1850, 500, 50, 250);    // Above TR door (y=500-750)
        addWall(1850, 850, 50, 150);    // Between TR door and window (y=850-1000)
        addWall(1850, 1080, 50, 70);    // Between window and BR door (y=1080-1150)
        addWall(1850, 1250, 50, 250);   // Below BR door (y=1250-1500)
        // Bottom wall (split for window at x=1500-1580, width=80)
        addWall(1100, 1450, 400, 50);   // Left of window (x=1100-1500)
        addWall(1580, 1450, 320, 50);   // Right of window (x=1580-1900)
    }

    /**
     * Creates perk machines - two perks in each room for more variety.
     */
    private void createPerks() {
        // Top-Left Room perks
        perks.add(new Perk(400, 400, Perk.Type.JUGGERNOG));       // Top-left corner
        perks.add(new Perk(600, 600, Perk.Type.DEADSHOT_DAIQUIRI)); // Near center

        // Top-Right Room perks
        perks.add(new Perk(2400, 400, Perk.Type.SPEED_COLA));     // Top-right corner
        perks.add(new Perk(2600, 600, Perk.Type.MULE_KICK));      // Near center

        // Bottom-Left Room perks
        perks.add(new Perk(400, 1400, Perk.Type.DOUBLE_TAP));     // Bottom-left corner
        perks.add(new Perk(600, 1600, Perk.Type.PH_FLOPPER));     // Near center

        // Bottom-Right Room perks
        perks.add(new Perk(2400, 1400, Perk.Type.QUICK_REVIVE));  // Bottom-right corner
        perks.add(new Perk(2600, 1600, Perk.Type.STAMIN_UP));     // Near center

        // Central Room perks
        perks.add(new Perk(1300, 900, Perk.Type.STAMIN_UP));      // Central west
        perks.add(new Perk(1700, 1100, Perk.Type.DEADSHOT_DAIQUIRI)); // Central east
    }

    /**
     * Creates wall guns - one in each room.
     * Players can purchase these weapons for points (increased costs).
     */
    private void createWallGuns() {
        // Corner room wall guns (increased costs)
        wallGuns.add(new WallGun(500, 700, Weapon.Type.MP40));      // Top-left - 2500 points
        wallGuns.add(new WallGun(2500, 700, Weapon.Type.MP5));      // Top-right - 3000 points
        wallGuns.add(new WallGun(500, 1300, Weapon.Type.AK74u));    // Bottom-left - 4000 points
        wallGuns.add(new WallGun(2500, 1300, Weapon.Type.M16));     // Bottom-right - 4000 points

        // Central room wall gun
        wallGuns.add(new WallGun(1500, 900, Weapon.Type.Olympia));  // Central - 3500 points
    }

    /**
     * Creates doors that players can purchase to enter rooms.
     * Each corner room has an entrance door + a door connecting to central.
     * Central room has doors connecting to each corner room.
     */
    private void createDoors() {
        // ----- Corner room entrance doors (from outside into the room) -----
        doors.add(new Door(450, 750, 100, 50, 750, "Top-Left Entrance"));      // TL bottom wall
        doors.add(new Door(2450, 750, 100, 50, 750, "Top-Right Entrance"));     // TR bottom wall
        doors.add(new Door(450, 1150, 100, 50, 750, "Bottom-Left Entrance"));   // BL top wall
        doors.add(new Door(2450, 1150, 100, 50, 750, "Bottom-Right Entrance")); // BR top wall

        // ----- Corner room doors connecting to central room -----
        doors.add(new Door(750, 450, 50, 100, 750, "Top-Left to Central"));     // TL right wall
        doors.add(new Door(2200, 450, 50, 100, 750, "Top-Right to Central"));    // TR left wall
        doors.add(new Door(750, 1450, 50, 100, 750, "Bottom-Left to Central"));  // BL right wall
        doors.add(new Door(2200, 1450, 50, 100, 750, "Bottom-Right to Central"));// BR left wall

        // ----- Central room doors connecting to corner rooms -----
        doors.add(new Door(1100, 750, 50, 100, 750, "Central to Top-Left"));    // Central left wall (upper)
        doors.add(new Door(1850, 750, 50, 100, 750, "Central to Top-Right"));   // Central right wall (upper)
        doors.add(new Door(1100, 1150, 50, 100, 750, "Central to Bottom-Left")); // Central left wall (lower)
        doors.add(new Door(1850, 1150, 50, 100, 750, "Central to Bottom-Right"));// Central right wall (lower)
    }

    /**
     * Creates windows (virtual windows in room walls).
     * Zombies can pass through them, players cannot.
     * Windows match wall thickness (50px) and are 80px wide/tall for easy passage.
     * 4 windows per corner room (one on each wall), 4 in central room.
     */
    private void createWindows() {
        // ----- Top-Left Room windows (4) -----
        // Top wall: gap at x=310-350 (center 330), window 80px wide, 50px tall (match wall)
        windows.add(new Window(290, 200, 80, 50));
        // Left wall: gap at y=370-410 (center 390), window 50px wide (match wall), 80px tall
        windows.add(new Window(200, 350, 50, 80));
        // Right wall (inner): gap at y=300-380 (center 340), window 50px wide (match wall), 80px tall
        windows.add(new Window(750, 300, 50, 80));
        // Bottom wall: right of entrance door (door at 450-550), wall from 550-800, center 675
        windows.add(new Window(635, 750, 80, 50));

        // ----- Top-Right Room windows (4) -----
        // Top wall: gap at x=2410-2450 (center 2430), window 80px wide
        windows.add(new Window(2390, 200, 80, 50));
        // Right wall: gap at y=370-410 (center 390), window 50px wide, 80px tall
        windows.add(new Window(2750, 350, 50, 80));
        // Left wall (inner): gap at y=300-380 (center 340), window 50px wide, 80px tall
        windows.add(new Window(2200, 300, 50, 80));
        // Bottom wall: right of entrance door (door at 2450-2550), wall from 2550-2800, center 2675
        windows.add(new Window(2635, 750, 80, 50));

        // ----- Bottom-Left Room windows (4) -----
        // Top wall: left of entrance door (door at 450-550), wall from 200-450, center 325
        windows.add(new Window(285, 1150, 80, 50));
        // Left wall: gap at y=1470-1510 (center 1490), window 50px wide, 80px tall
        windows.add(new Window(200, 1450, 50, 80));
        // Right wall (inner): gap at y=1300-1380 (center 1340), window 50px wide, 80px tall
        windows.add(new Window(750, 1300, 50, 80));
        // Bottom wall: gap at x=310-350 (center 330), window 80px wide
        windows.add(new Window(290, 1750, 80, 50));

        // ----- Bottom-Right Room windows (4) -----
        // Top wall: right of entrance door (door at 2450-2550), wall from 2550-2800, center 2675
        windows.add(new Window(2635, 1150, 80, 50));
        // Right wall: gap at y=1470-1510 (center 1490), window 50px wide, 80px tall
        windows.add(new Window(2750, 1450, 50, 80));
        // Left wall (inner): gap at y=1300-1380 (center 1340), window 50px wide, 80px tall
        windows.add(new Window(2200, 1300, 50, 80));
        // Bottom wall: gap at x=2410-2450 (center 2430), window 80px wide
        windows.add(new Window(2390, 1750, 80, 50));

        // ----- Central Room windows (4) -----
        // Top wall: gap at x=1360-1440 (center 1400), window 80px wide
        windows.add(new Window(1360, 500, 80, 50));
        // Left wall: gap at y=960-1000 (center 980), window 50px wide, 80px tall
        windows.add(new Window(1100, 940, 50, 80));
        // Right wall: NEW gap at y=1000-1080 (center 1040), window 50px wide, 80px tall
        windows.add(new Window(1850, 1000, 50, 80));
        // Bottom wall: gap at x=1500-1580 (center 1540), window 80px wide
        windows.add(new Window(1500, 1450, 80, 50));
    }

    private void createZombieSpawns() {
        // Spawn points around the map edges
        int spacing = 200;
        // Top edge
        for (int x = 100; x < WIDTH - 100; x += spacing) {
            zombieSpawnPoints.add(new Point(x, 100));
        }
        // Bottom edge
        for (int x = 100; x < WIDTH - 100; x += spacing) {
            zombieSpawnPoints.add(new Point(x, HEIGHT - 100));
        }
        // Left edge
        for (int y = 100; y < HEIGHT - 100; y += spacing) {
            zombieSpawnPoints.add(new Point(100, y));
        }
        // Right edge
        for (int y = 100; y < HEIGHT - 100; y += spacing) {
            zombieSpawnPoints.add(new Point(WIDTH - 100, y));
        }
    }

    private void createPlayerSpawns() {
        // Player spawn points - one in each room + center
        playerSpawnPoints.add(new Point(500, 500));       // Top-left room
        playerSpawnPoints.add(new Point(2500, 500));      // Top-right room
        playerSpawnPoints.add(new Point(500, 1500));      // Bottom-left room
        playerSpawnPoints.add(new Point(2500, 1500));     // Bottom-right room
        playerSpawnPoints.add(new Point(1500, 1000));     // Central room
    }

    private void addWall(double x, double y, double w, double h) {
        walls.add(new Rectangle2D.Double(x, y, w, h));
    }

    /**
     * Checks collision against walls and closed doors.
     * Windows are NOT checked here - zombies can pass through windows,
     * but players are blocked by windows via Player.mapCollides().
     */
    public boolean checkCollision(Rectangle2D.Double bounds) {
        for (Rectangle2D.Double wall : walls) {
            if (wall.intersects(bounds)) {
                return true;
            }
        }
        // Closed doors block movement
        for (Door door : doors) {
            if (!door.isOpened() && door.getBounds().intersects(bounds)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks collision against windows only (players cannot pass through windows).
     */
    public boolean checkWindowCollision(Rectangle2D.Double bounds) {
        for (Window window : windows) {
            if (window.getBounds().intersects(bounds)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasLineOfSight(double x1, double y1, double x2, double y2) {
        // Simple raycast to check if there's a wall between two points
        int steps = (int) (Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)) / 20);
        for (int i = 1; i < steps; i++) {
            double t = (double) i / steps;
            double x = x1 + (x2 - x1) * t;
            double y = y1 + (y2 - y1) * t;
            Rectangle2D.Double pointBounds = new Rectangle2D.Double(x - 5, y - 5, 10, 10);
            if (checkCollision(pointBounds) || checkWindowCollision(pointBounds)) {
                return false;
            }
        }
        return true;
    }

    public Point getRandomZombieSpawn() {
        return zombieSpawnPoints.get(random.nextInt(zombieSpawnPoints.size()));
    }

    public Point getRandomPlayerSpawn() {
        return playerSpawnPoints.get(random.nextInt(playerSpawnPoints.size()));
    }

    public void renderFloor(Graphics2D g) {
        // Draw floor tiles with slight variation
        for (Rectangle2D.Double tile : floorTiles) {
            int tx = (int) (tile.x / TILE_SIZE);
            int ty = (int) (tile.y / TILE_SIZE);
            if ((tx + ty) % 2 == 0) {
                g.setColor(new Color(40, 45, 50));
            } else {
                g.setColor(new Color(35, 40, 45));
            }
            g.fillRect((int) tile.x, (int) tile.y, (int) tile.width, (int) tile.height);
        }
    }

    public void renderWalls(Graphics2D g) {
        g.setColor(new Color(60, 55, 50));
        for (Rectangle2D.Double wall : walls) {
            g.fillRect((int) wall.x, (int) wall.y, (int) wall.width, (int) wall.height);

            // Wall outline
            g.setColor(new Color(40, 35, 30));
            g.drawRect((int) wall.x, (int) wall.y, (int) wall.width, (int) wall.height);
            g.setColor(new Color(60, 55, 50));
        }
    }

    public void renderWindows(Graphics2D g) {
        for (Window window : windows) {
            window.render(g);
        }
    }

    public void renderWallGuns(Graphics2D g) {
        for (WallGun wallGun : wallGuns) {
            wallGun.render(g);
        }
    }

    public void renderDoors(Graphics2D g) {
        for (Door door : doors) {
            door.render(g);
        }
    }

    public void renderPerks(Graphics2D g) {
        for (Perk perk : perks) {
            perk.render(g);
        }
    }

    public void update() {
        for (WallGun wallGun : wallGuns) {
            wallGun.update();
        }
        for (Perk perk : perks) {
            perk.update(null); // Perk update doesn't need player for glow animation
        }
    }

    // Getters
    public List<Rectangle2D.Double> getWalls() {
        return walls;
    }

    public List<Perk> getPerks() {
        return perks;
    }

    public List<WallGun> getWallGuns() {
        return wallGuns;
    }

    public List<Door> getDoors() {
        return doors;
    }

    public List<Window> getWindows() {
        return windows;
    }

    public List<Point> getZombieSpawnPoints() {
        return zombieSpawnPoints;
    }

    public List<Point> getPlayerSpawnPoints() {
        return playerSpawnPoints;
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main game panel that handles the game loop, rendering, and input.
 * Uses a fixed timestep for consistent gameplay across different hardware.
 */
public class GamePanel extends JPanel implements Runnable {
    // Game loop timing
    private static final double NANOSECONDS_PER_SECOND = 1_000_000_000.0;
    private static final double TARGET_FPS = 60.0;
    private static final double TARGET_TIME_PER_FRAME = NANOSECONDS_PER_SECOND / TARGET_FPS;

    // Game objects
    private Player player;
    private Map map;
    private WaveManager waveManager;
    private final List<Bullet> bullets = Collections.synchronizedList(new ArrayList<>());
    private final List<Zombie> zombies = Collections.synchronizedList(new ArrayList<>());
    private final List<PowerUp> powerUps = Collections.synchronizedList(new ArrayList<>());
    private final List<Particle> particles = Collections.synchronizedList(new ArrayList<>());
    private MysteryBox mysteryBox;
    private PackAPunch packAPunch;
    private List<WallGun> wallGuns;
    private List<Door> doors;
    private HUD hud;
    private InputHandler inputHandler;
    private volatile GameState gameState;

    // Threading
    private Thread gameThread;
    private volatile boolean running = false;

    // Camera
    private double cameraX = 0;
    private double cameraY = 0;

    // Screen shake
    private double screenShakeIntensity = 0;
    private double screenShakeDuration = 0;

    // Double buffering
    private BufferedImage backBuffer;
    private Graphics2D backBufferGraphics;

    public GamePanel() {
        setPreferredSize(new Dimension(Game.WIDTH, Game.HEIGHT));
        setBackground(new Color(30, 30, 40));
        setFocusable(true);
        requestFocusInWindow();

        initializeGame();
        initializeInput();
    }

    private void initializeGame() {
        map = new Map();
        player = new Player(Game.WIDTH / 2, Game.HEIGHT / 2, map);
        waveManager = new WaveManager(map, player);
        bullets.clear();
        zombies.clear();
        powerUps.clear();
        particles.clear();
        wallGuns = map.getWallGuns();
        doors = map.getDoors();

        // Initialize special objects
        mysteryBox = new MysteryBox(800, 300);
        packAPunch = new PackAPunch(1000, 500);

        hud = new HUD(player, waveManager);
        hud.setGamePanel(this);
        gameState = GameState.MAIN_MENU;

        // Create back buffer for double buffering
        backBuffer = new BufferedImage(Game.WIDTH, Game.HEIGHT, BufferedImage.TYPE_INT_ARGB);
        backBufferGraphics = backBuffer.createGraphics();
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private void initializeInput() {
        inputHandler = new InputHandler();
        addKeyListener(inputHandler);
        addMouseListener(inputHandler);
        addMouseMotionListener(inputHandler);

        // Ensure focus for KeyListener to work
        setFocusable(true);
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                // Re-request focus when lost (but not if window is closing)
                if (isShowing()) {
                    requestFocusInWindow();
                }
            }
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                // Clear any stuck keys when focus is regained
                inputHandler.update();
            }
        });
    }

    public void startGameLoop() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double lastTime = System.nanoTime();
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        int updates = 0;

        while (running) {
            double now = System.nanoTime();
            delta += (now - lastTime) / TARGET_TIME_PER_FRAME;
            lastTime = now;

            while (delta >= 1) {
                update();
                updates++;
                delta--;
            }

            repaint(); // Use Swing's repaint mechanism instead of getGraphics()
            frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                // System.out.println("FPS: " + frames + " | UPS: " + updates);
                frames = 0;
                updates = 0;
            }
        }
    }

    private void update() {
        // Handle state-specific logic FIRST (check input before clearing frame state)
        if (gameState == GameState.MAIN_MENU) {
            handleMainMenuInput();
            // Clear input state after handling menu input
            inputHandler.update();
            return;
        }

        // For other states, update input at the end of the frame
        // We'll update it after all input checks

        if (gameState != GameState.PLAYING) {
            handlePauseGameOverInput();
            // Clear input state after handling pause/game over input
            inputHandler.update();
            return;
        }

        // Update HUD timers
        updateHUD(1.0 / TARGET_FPS);

        // Update camera to follow player with smooth lerp
        double targetCameraX = player.getX() - Game.WIDTH / 2.0;
        double targetCameraY = player.getY() - Game.HEIGHT / 2.0;
        cameraX += (targetCameraX - cameraX) * 0.1;
        cameraY += (targetCameraY - cameraY) * 0.1;

        // Clamp camera to map bounds
        cameraX = Math.max(0, Math.min(cameraX, Map.WIDTH - Game.WIDTH));
        cameraY = Math.max(0, Math.min(cameraY, Map.HEIGHT - Game.HEIGHT));

        // Update screen shake
        if (screenShakeDuration > 0) {
            screenShakeDuration -= 1.0 / TARGET_FPS;
            if (screenShakeDuration <= 0) {
                screenShakeIntensity = 0;
            }
        }

        // Update player
        player.update(inputHandler, this);

        // Update zombies
        for (int i = zombies.size() - 1; i >= 0; i--) {
            Zombie zombie = zombies.get(i);
            zombie.update(player, map, zombies);
            if (!zombie.isAlive()) {
                player.addPoints(zombie.getPointsValue());
                createDeathParticles(zombie.getX(), zombie.getY(), zombie.getColor());
                zombies.remove(i);
            }
        }

        // Update bullets
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update();
            if (!bullet.isActive()) {
                bullets.remove(i);
            }
        }

        // Check bullet-zombie collisions
        checkBulletZombieCollisions();

        // Check zombie-player collisions
        checkZombiePlayerCollisions();

        // Update power-ups
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            powerUp.update();
            if (powerUp.isCollected() || powerUp.isExpired()) {
                powerUps.remove(i);
            }
        }

        // Check power-up collection
        checkPowerUpCollection();

        // Update particles
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle particle = particles.get(i);
            particle.update();
            if (!particle.isAlive()) {
                particles.remove(i);
            }
        }

        // Update wave manager
        waveManager.update(zombies);

        // Update special objects
        mysteryBox.update(player);
        packAPunch.update(player);

        // Update wall guns
        for (WallGun wallGun : wallGuns) {
            wallGun.update();
        }

        // Update map (perks, etc.)
        map.update();

        // Check game over
        if (player.getHealth() <= 0) {
            gameState = GameState.GAME_OVER;
        }

        // Clear input state at the end of the frame (after all input checks)
        inputHandler.update();
    }

    private void checkBulletZombieCollisions() {
        // Need to use synchronized blocks since we're iterating over synchronized lists
        List<Bullet> bulletsCopy;
        synchronized (bullets) {
            bulletsCopy = new ArrayList<>(bullets);
        }

        List<Zombie> zombiesCopy;
        synchronized (zombies) {
            zombiesCopy = new ArrayList<>(zombies);
        }

        for (Bullet bullet : bulletsCopy) {
            if (!bullet.isActive() || bullet.isFromZombie()) continue;

            for (Zombie zombie : zombiesCopy) {
                if (!zombie.isAlive()) continue;
                if (bullet.getBounds().intersects(zombie.getBounds())) {
                    double damage = bullet.getDamage();

                    // Insta-Kill power-up: instant kill
                    if (isInstaKillActive()) {
                        damage = 99999;
                    }

                    // Double Points power-up: double points from kills
                    int pointsMultiplier = isDoublePointsActive() ? 2 : 1;

                    boolean killed = zombie.takeDamage(damage, bullet.isCritical());
                    bullet.onHit(); // Handle piercing logic

                    if (killed) {
                        createDeathParticles(zombie.getX(), zombie.getY(), zombie.getColor());
                        // Award points with multiplier
                        player.addPoints(zombie.getPointsValue() * pointsMultiplier);
                        // Notify wave manager
                        waveManager.onZombieKilled();
                        // Chance to drop power-up
                        if (Math.random() < 0.05) {
                            spawnRandomPowerUp(zombie.getX(), zombie.getY());
                        }
                    } else {
                        createHitParticles(zombie.getX(), zombie.getY());
                    }

                    // Screen shake on hit
                    addScreenShake(2, 0.1);

                    break;
                }
            }
        }
    }

    private void checkZombiePlayerCollisions() {
        for (Zombie zombie : zombies) {
            if (zombie.getBounds().intersects(player.getBounds())) {
                player.takeDamage(zombie.getDamage());
                addScreenShake(5, 0.2);
                createHitParticles(player.getX(), player.getY());
            }
        }
    }

    private void checkPowerUpCollection() {
        for (PowerUp powerUp : powerUps) {
            if (powerUp.getBounds().intersects(player.getBounds())) {
                powerUp.collect(player, this);
                createPowerUpParticles(powerUp.getX(), powerUp.getY(), powerUp.getColor());
            }
        }
    }

    // Input handling for different game states
    private void handleMainMenuInput() {
        if (inputHandler.isEnterPressed() || inputHandler.wasKeyPressed(KeyEvent.VK_SPACE)) {
            startGame();
        }

        // Check for mouse click on Start button
        if (inputHandler.wasMousePressed(InputHandler.MOUSE_LEFT)) {
            int mouseX = inputHandler.getMouseX();
            int mouseY = inputHandler.getMouseY();
            // Start button bounds (centered)
            int buttonWidth = 300;
            int buttonHeight = 60;
            int buttonX = (Game.WIDTH - buttonWidth) / 2;
            int buttonY = Game.HEIGHT / 2 + 180;
            if (mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
                mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                startGame();
            }
        }
    }

    private void handlePauseGameOverInput() {
        if (gameState == GameState.GAME_OVER) {
            if (inputHandler.isEnterPressed()) {
                // Go back to main menu instead of restarting directly
                gameState = GameState.MAIN_MENU;
            }
        } else if (gameState == GameState.PAUSED) {
            if (inputHandler.isEscapePressed() || inputHandler.wasKeyPressed(KeyEvent.VK_P)) {
                gameState = GameState.PLAYING;
            }
        }
    }

    private void startGame() {
        // Reset game state
        initializeGame();
        gameState = GameState.PLAYING;
    }

    private void render(Graphics2D g) {
        // Clear back buffer
        g.setColor(new Color(30, 30, 40));
        g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);

        if (gameState == GameState.MAIN_MENU) {
            renderMainMenu(g);
            return;
        }

        // Apply screen shake
        double shakeX = 0, shakeY = 0;
        if (screenShakeIntensity > 0) {
            shakeX = (Math.random() - 0.5) * screenShakeIntensity;
            shakeY = (Math.random() - 0.5) * screenShakeIntensity;
        }

        // Translate for camera + shake
        g.translate(-cameraX + shakeX, -cameraY + shakeY);

        // Render map (floor)
        map.renderFloor(g);

        // Render map (walls)
        map.renderWalls(g);

        // Render wall guns
        map.renderWallGuns(g);

        // Render doors
        map.renderDoors(g);

        // Render windows (block players, zombies pass through)
        map.renderWindows(g);

        // Render perk machines
        map.renderPerks(g);

        // Render special objects
        mysteryBox.render(g);
        packAPunch.render(g);

        // Render power-ups
        synchronized (powerUps) {
            for (PowerUp powerUp : powerUps) {
                powerUp.render(g);
            }
        }

        // Render zombies
        synchronized (zombies) {
            for (Zombie zombie : zombies) {
                zombie.render(g);
            }
        }

        // Render bullets
        synchronized (bullets) {
            for (Bullet bullet : bullets) {
                bullet.render(g);
            }
        }

        // Render player
        player.render(g);

        // Render particles
        synchronized (particles) {
            for (Particle particle : particles) {
                particle.render(g);
            }
        }

        // Reset transform for HUD
        g.setTransform(new AffineTransform());

        // Render HUD
        hud.render(g);

        // Render game over screen
        if (gameState == GameState.GAME_OVER) {
            renderGameOverScreen(g);
        } else if (gameState == GameState.PAUSED) {
            renderPauseScreen(g);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backBuffer != null) {
            Graphics2D g2d = backBuffer.createGraphics();
            try {
                render(g2d);
            } finally {
                g2d.dispose();
            }
            g.drawImage(backBuffer, 0, 0, null);
        }
    }

    private void renderGameOverScreen(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 72));
        String gameOver = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        int x = (Game.WIDTH - fm.stringWidth(gameOver)) / 2;
        g.drawString(gameOver, x, Game.HEIGHT / 2 - 50);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 36));
        String waveText = "Waves Survived: " + waveManager.getCurrentWave();
        fm = g.getFontMetrics();
        x = (Game.WIDTH - fm.stringWidth(waveText)) / 2;
        g.drawString(waveText, x, Game.HEIGHT / 2 + 20);

        String scoreText = "Final Score: " + player.getPoints();
        fm = g.getFontMetrics();
        x = (Game.WIDTH - fm.stringWidth(scoreText)) / 2;
        g.drawString(scoreText, x, Game.HEIGHT / 2 + 70);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        String restartText = "Press ENTER to return to Main Menu";
        fm = g.getFontMetrics();
        x = (Game.WIDTH - fm.stringWidth(restartText)) / 2;
        g.drawString(restartText, x, Game.HEIGHT / 2 + 130);
    }

    private void renderPauseScreen(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String paused = "PAUSED";
        FontMetrics fm = g.getFontMetrics();
        int x = (Game.WIDTH - fm.stringWidth(paused)) / 2;
        g.drawString(paused, x, Game.HEIGHT / 2);
    }

    private void renderMainMenu(Graphics2D g) {
        // Dark background
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);

        // Title
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 84));
        FontMetrics fm = g.getFontMetrics();
        String title = "CALL OF DUTY ZOMBIES 2D";
        int x = (Game.WIDTH - fm.stringWidth(title)) / 2;
        g.drawString(title, x, Game.HEIGHT / 2 - 150);

        // Subtitle
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 28));
        String subtitle = "Survive the Undead Horde";
        fm = g.getFontMetrics();
        x = (Game.WIDTH - fm.stringWidth(subtitle)) / 2;
        g.drawString(subtitle, x, Game.HEIGHT / 2 - 80);

        // Instructions
        g.setFont(new Font("Arial", Font.PLAIN, 22));
        String[] instructions = {
            "WASD / Arrow Keys - Move",
            "Mouse - Aim",
            "Left Click - Shoot",
            "R - Reload",
            "E / F - Interact / Buy / Repair",
            "1 / 2 / Q - Switch Weapons",
            "P / ESC - Pause"
        };

        int startY = Game.HEIGHT / 2 - 20;
        for (int i = 0; i < instructions.length; i++) {
            fm = g.getFontMetrics();
            x = (Game.WIDTH - fm.stringWidth(instructions[i])) / 2;
            g.drawString(instructions[i], x, startY + i * 30);
        }

        // Start button
        int buttonWidth = 300;
        int buttonHeight = 60;
        int buttonX = (Game.WIDTH - buttonWidth) / 2;
        int buttonY = Game.HEIGHT / 2 + 180;

        // Check if mouse is hovering over button
        int mouseX = inputHandler.getMouseX();
        int mouseY = inputHandler.getMouseY();
        boolean hovering = mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
                          mouseY >= buttonY && mouseY <= buttonY + buttonHeight;

        // Button background
        g.setColor(hovering ? new Color(0, 150, 0) : new Color(0, 100, 0));
        g.fillRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 10, 10);

        // Button border
        g.setColor(hovering ? Color.YELLOW : Color.GREEN);
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 10, 10);
        g.setStroke(new BasicStroke(1)); // Reset stroke

        // Button text
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(Color.WHITE);
        String startText = "START GAME";
        fm = g.getFontMetrics();
        int textX = buttonX + (buttonWidth - fm.stringWidth(startText)) / 2;
        int textY = buttonY + (buttonHeight + fm.getAscent()) / 2 - 2;
        g.drawString(startText, textX, textY);

        // Keyboard hint below button
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(Color.GRAY);
        String hintText = "Or press ENTER / SPACE";
        fm = g.getFontMetrics();
        x = (Game.WIDTH - fm.stringWidth(hintText)) / 2;
        g.drawString(hintText, x, Game.HEIGHT / 2 + 260);

        // Version info
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(Color.GRAY);
        String version = "v1.0 - Press ESC to quit";
        fm = g.getFontMetrics();
        x = (Game.WIDTH - fm.stringWidth(version)) / 2;
        g.drawString(version, x, Game.HEIGHT - 30);
    }

    // Public methods for other classes to use
    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    public void addZombie(Zombie zombie) {
        zombies.add(zombie);
    }

    public void addPowerUp(PowerUp powerUp) {
        powerUps.add(powerUp);
    }

    public void spawnRandomPowerUp(double x, double y) {
        PowerUp.Type[] types = PowerUp.Type.values();
        PowerUp.Type randomType = types[(int)(Math.random() * types.length)];
        powerUps.add(new PowerUp(x, y, randomType));
    }

    public void addScreenShake(double intensity, double duration) {
        screenShakeIntensity = Math.max(screenShakeIntensity, intensity);
        screenShakeDuration = Math.max(screenShakeDuration, duration);
    }

    public void createHitParticles(double x, double y) {
        for (int i = 0; i < 8; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 50 + Math.random() * 100;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            particles.add(new Particle(x, y, vx, vy, Color.RED, 0.5));
        }
    }

    public void createDeathParticles(double x, double y, Color color) {
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 50 + Math.random() * 200;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            particles.add(new Particle(x, y, vx, vy, color, 1.0));
        }
    }

    public void createPowerUpParticles(double x, double y, Color color) {
        for (int i = 0; i < 15; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 50 + Math.random() * 150;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            particles.add(new Particle(x, y, vx, vy, color, 1.5));
        }
    }

    public void createMuzzleFlash(double x, double y, double angle) {
        for (int i = 0; i < 5; i++) {
            double spreadAngle = angle + (Math.random() - 0.5) * 0.5;
            double speed = 100 + Math.random() * 100;
            double vx = Math.cos(spreadAngle) * speed;
            double vy = Math.sin(spreadAngle) * speed;
            particles.add(new Particle(x, y, vx, vy, new Color(255, 255, 100, 200), 0.2));
        }
    }

    // Getters
    public Player getPlayer() { return player; }
    public Map getMap() { return map; }
    public WaveManager getWaveManager() { return waveManager; }
    public List<Zombie> getZombies() { return zombies; }
    public List<Bullet> getBullets() { return bullets; }
    public GameState getGameState() { return gameState; }
    public void setGameState(GameState state) { this.gameState = state; }
    public InputHandler getInputHandler() { return inputHandler; }
    public double getCameraX() { return cameraX; }
    public double getCameraY() { return cameraY; }
    public MysteryBox getMysteryBox() { return mysteryBox; }
    public PackAPunch getPackAPunch() { return packAPunch; }
    public List<WallGun> getWallGuns() { return wallGuns; }
    public List<Door> getDoors() { return doors; }

    // Power-up activation methods
    public void activateInstaKill(double duration) {
        hud.activateInstaKill(duration);
    }

    public void activateDoublePoints(double duration) {
        hud.activateDoublePoints(duration);
    }

    public void activateFireSale(double duration) {
        hud.activateFireSale(duration);
    }

    public void activateBonfireSale(double duration) {
        hud.activateBonfireSale(duration);
    }

    public void nukeAllZombies() {
        hud.nukeAllZombies();
    }

    public void giveMaxAmmo(Player player) {
        hud.giveMaxAmmo(player);
    }

    public void giveDeathMachine(Player player, double duration) {
        hud.giveDeathMachine(player, duration);
    }

    public boolean isInstaKillActive() {
        return hud.isInstaKillActive();
    }

    public boolean isDoublePointsActive() {
        return hud.isDoublePointsActive();
    }

    public boolean isFireSaleActive() {
        return hud.isFireSaleActive();
    }

    // Update HUD timers
    public void updateHUD(double deltaTime) {
        hud.update(deltaTime);
    }
}
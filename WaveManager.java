import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * Manages zombie waves, spawning, and difficulty scaling.
 */
public class WaveManager {
    private int currentWave = 0;
    private int zombiesRemaining = 0;
    private int zombiesKilledThisWave = 0;
    private double spawnTimer = 0;
    private double spawnInterval = 2.0; // seconds between spawns
    private boolean waveInProgress = false;
    private boolean waveCleared = false;
    private double waveTransitionTimer = 0;
    private static final double WAVE_TRANSITION_TIME = 5.0; // seconds between waves

    private Random random = new Random();
    private Map map;
    private Player player;

    // Wave configuration
    private static final int BASE_ZOMBIES = 20;
    private static final double ZOMBIES_PER_WAVE_MULTIPLIER = 1.35;
    private static final double MAX_SPAWN_INTERVAL = 0.3; // Minimum time between spawns

    public WaveManager(Map map, Player player) {
        this.map = map;
        this.player = player;
    }

    public void update(List<Zombie> zombies) {
        if (waveTransitionTimer > 0) {
            waveTransitionTimer -= 1.0 / 60.0;
            if (waveTransitionTimer <= 0) {
                startNextWave();
            }
            return;
        }

        if (!waveInProgress) {
            // Wait for player to start (first wave starts immediately)
            if (currentWave == 0) {
                startNextWave();
            }
            return;
        }

        // Check if wave is cleared
        if (zombiesRemaining <= 0 && zombies.isEmpty()) {
            endWave();
            return;
        }

        // Spawn zombies
        if (zombiesRemaining > 0) {
            spawnTimer -= 1.0 / 60.0;
            if (spawnTimer <= 0) {
                spawnZombie(zombies);
                spawnTimer = spawnInterval;
            }
        }
    }

    private void startNextWave() {
        currentWave++;
        waveInProgress = true;
        waveCleared = false;
        zombiesKilledThisWave = 0;

        // Calculate zombies for this wave
        int baseZombies = (int)(BASE_ZOMBIES * Math.pow(ZOMBIES_PER_WAVE_MULTIPLIER, currentWave - 1));
        // Cap at reasonable number
        zombiesRemaining = Math.min(baseZombies, 50 + currentWave * 5);

        // Decrease spawn interval (faster spawns) but cap it
        spawnInterval = Math.max(MAX_SPAWN_INTERVAL, 2.0 - (currentWave - 1) * 0.08);
        spawnTimer = 0.5; // Small delay before first spawn

        // Play wave start sound/notification
        System.out.println("Wave " + currentWave + " started! Zombies: " + zombiesRemaining);
    }

    private void spawnZombie(List<Zombie> zombies) {
        if (zombiesRemaining <= 0) return;

        // Get random spawn point
        Point spawnPoint = map.getRandomZombieSpawn();

        // Determine zombie type based on wave
        Zombie.Type type = selectZombieType();

        Zombie zombie = new Zombie(spawnPoint.x, spawnPoint.y, type, currentWave, player);
        zombies.add(zombie);
        zombiesRemaining--;
    }

    private Zombie.Type selectZombieType() {
        double rand = random.nextDouble();
        double cumulative = 0;

        // Wave-based type probabilities
        double normalChance = Math.max(0.3, 0.8 - currentWave * 0.03);
        double fastChance = Math.min(0.4, 0.1 + currentWave * 0.02);
        double tankChance = Math.min(0.2, currentWave * 0.015);
        double crawlerChance = Math.min(0.15, 0.05 + currentWave * 0.01);
        double bossChance = currentWave >= 10 && currentWave % 5 == 0 ? 0.05 : 0;

        cumulative += normalChance;
        if (rand < cumulative) return Zombie.Type.NORMAL;

        cumulative += fastChance;
        if (rand < cumulative) return Zombie.Type.FAST;

        cumulative += tankChance;
        if (rand < cumulative) return Zombie.Type.TANK;

        cumulative += crawlerChance;
        if (rand < cumulative) return Zombie.Type.CRAWLER;

        if (bossChance > 0 && rand < cumulative + bossChance) return Zombie.Type.BOSS;

        return Zombie.Type.NORMAL;
    }

    private void endWave() {
        waveInProgress = false;
        waveCleared = true;
        waveTransitionTimer = WAVE_TRANSITION_TIME;

        // Award bonus points for completing wave
        int bonus = currentWave * 100;
        player.addPoints(bonus);
        System.out.println("Wave " + currentWave + " cleared! Bonus: " + bonus + " points");
    }

    public void onZombieKilled() {
        zombiesKilledThisWave++;
    }

    public void forceNextWave() {
        if (!waveInProgress && waveTransitionTimer <= 0) {
            waveTransitionTimer = 0;
        }
    }

    // Getters
    public int getCurrentWave() { return currentWave; }
    public int getZombiesRemaining() { return zombiesRemaining; }
    public int getZombiesKilledThisWave() { return zombiesKilledThisWave; }
    public boolean isWaveInProgress() { return waveInProgress; }
    public boolean isWaveCleared() { return waveCleared; }
    public double getWaveTransitionProgress() {
        return waveTransitionTimer > 0 ? 1.0 - (waveTransitionTimer / WAVE_TRANSITION_TIME) : 0;
    }
    public double getWaveTransitionTimeRemaining() { return waveTransitionTimer; }
}
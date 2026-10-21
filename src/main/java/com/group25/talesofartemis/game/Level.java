package com.group25.talesofartemis.game;

import com.group25.talesofartemis.model.DifficultyLevel;
import com.group25.talesofartemis.model.Enemy;
import com.group25.talesofartemis.model.PlayerCharacter;
import com.group25.talesofartemis.model.Powerup;
import com.group25.talesofartemis.model.WordManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * One screen worth of combat with who's on it, how hard it is, and the word list for spawning. Enemies share a spawn point
 * and all chase whoever you set with setChaseTarget which GameSession does for you when you hand it the level.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public class Level {

    private final List<Enemy> activeEnemies = new ArrayList<>();
    private final List<Powerup> activePowerups = new ArrayList<>();
    private final DifficultyLevel difficulty;
    private final WordManager wordManager;

    private final double spawnX;
    private final double spawnY;

    /** Total number of enemies to spawn with values from {@link DifficultyLevel#maxSpawns}. */
    private final int maxSpawns;

    /** How many enemies have been spawned so far (via spawnEnemy or addEnemy). */
    private int spawnCount = 0;

    /** Where enemies path toward each frame. */
    private PlayerCharacter chaseTarget;

    /**
     * Constructs a level with the given difficulty, word manager, and spawn point. 
     * The difficulty controls how many enemies spawn, how many powerups drop, and
     * the word manager is used to pull words for new enemies.
     * @param difficulty difficulty level of the world this level is in
     * @param wordManager shared WordManager used to pull words for new enemies
     * @param spawnX X coordinate of the spawn point for enemies
     * @param spawnY Y coordinate of the spawn point for enemies
     */

    public Level(DifficultyLevel difficulty, WordManager wordManager, double spawnX, double spawnY) {
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty");
        this.wordManager = Objects.requireNonNull(wordManager, "wordManager");
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.maxSpawns = difficulty.maxSpawns;
    }

    /** Who the mob AI runs at. Needs to be set before updateEnemies does anything useful. */
    public void setChaseTarget(PlayerCharacter chaseTarget) {
        this.chaseTarget = Objects.requireNonNull(chaseTarget, "chaseTarget");
    }

    /**
     * Returns the number of enemies that will spawn in this level.
     * @return the total number of enemies that will spawn in this level, as determined by the difficulty level
     */
    public int getMaxSpawns() {
        return maxSpawns;
    }

    /**
     * Returns how many enemies have been spawned so far. 
     * @return the number of enemies that have been spawned in this level so far.
     */
    public int getSpawnCount() {
        return spawnCount;
    }

    /**
     * Returns the difficulty level of this level.
     * @return the difficulty level of this level, which determines enemy stats, word lengths, spawn counts, and powerup drops
     */
    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    /**
     * Returns the WordManager used by this level to pull words for new enemies.
     * @return the WordManager which is shared across the whole game session, and used by the level.
     */
    public WordManager getWordManager() {
        return wordManager;
    }

    /**
     * Returns a list of the enemies currently active on this level.
     * @return an unmodifiable list of the enemies currently active on this level, which may be empty if no enemies are present
     */
    public List<Enemy> getActiveEnemies() {
        return Collections.unmodifiableList(activeEnemies);
    }

    /** Action keywords that can be assigned to enemies with elevated probability. */
    private static final String[] ACTION_KEYWORDS = {
        "jump", "duck", "attack", "sword", "shield", "block", "slash", "spell", "arrow", "counter"
    };

    /** Chance that a spawned enemy gets an action keyword instead of a regular word. */
    private static final double ACTION_KEYWORD_CHANCE = 0.35;

    /** Pulls a random word for this difficulty and drops an enemy in at the spawn point with stats that match the tier. */
    public void spawnEnemy() {
        String word;
        if (Math.random() < ACTION_KEYWORD_CHANCE) {
            word = ACTION_KEYWORDS[(int)(Math.random() * ACTION_KEYWORDS.length)];
        } else {
            word = wordManager.getRandomWord(difficulty);
        }
        final int maxHealth;
        final double speed;
        final int attackDamage;
        switch (difficulty) {
            case EASY:
                maxHealth = 30;
                speed = 90.0;
                attackDamage = 6;
                break;
            case MEDIUM:
                maxHealth = 45;
                speed = 130.0;
                attackDamage = 10;
                break;
            case HARD:
                maxHealth = 65;
                speed = 180.0;
                attackDamage = 16;
                break;
            default:
                throw new IllegalStateException("Unhandled difficulty: " + difficulty);
        }
        Enemy enemy = new Enemy(spawnX, spawnY, maxHealth, word, speed, attackDamage);
        activeEnemies.add(enemy);
        spawnCount++;
    }

    /**
     * Tick every enemy, point them at the chase target, and move them for deltaTime seconds. 
     * @param deltaTime time in seconds since the last update, used for frame rate independent movement. This must be non negative.
     */
    public void updateEnemies(double deltaTime) {
        if (chaseTarget == null) {
            throw new IllegalStateException("setChaseTarget must be called before updateEnemies");
        }
        double px = chaseTarget.getX();
        double py = chaseTarget.getY();
        for (Enemy e : activeEnemies) {
            e.setChaseTarget(px, py);
            e.move(deltaTime);
        }
    }

    /** Drop anyone who's already at 0 HP so lists stay small. */
    public void removeDefeatedEnemies() {
        activeEnemies.removeIf(Enemy::isDead);
    }

    /**
     * True when all planned enemies have spawned and been defeated.
     * The spawn limit comes from {@link DifficultyLevel#maxSpawns}.
     * @return true if the spawn count has reached the max spawns for this level and there are no active enemies remaining, false otherwise.
     */
    public boolean isCleared() {
        return spawnCount >= maxSpawns && activeEnemies.isEmpty();
    }

    /**
     * Adds an enemy to the level without pulling a word or setting stats. 
     * @param enemy the enemy to add to the level, which must be non-null. 
     */
    public void addEnemy(Enemy enemy) {
        if (enemy == null) {
            throw new IllegalArgumentException("enemy must be non-null");
        }
        activeEnemies.add(enemy);
        spawnCount++;
    }

    /**
     * Removes the specified enemy from the level's active enemies list.
     * @param enemy the enemy to remove from the level, which must be non-null and currently active in the level.
     * @return true if the enemy was successfully removed, false if not.
     */
    public boolean removeEnemy(Enemy enemy) {
        return activeEnemies.remove(enemy);
    }

    /**
     * Places a powerup into the world so the player can walk into it.
     * @param powerup non-null Powerup (HealthPowerup or SlowTimePowerup) carrying its own world position
     */
    public void addPowerup(Powerup powerup) {
        Objects.requireNonNull(powerup, "powerup");
        activePowerups.add(powerup);
    }

    /**
     * Returns the powerups currently sitting in the world (unmodifiable).
     * @return unmodifiable view of active world powerups in this level
     */
    public List<Powerup> getActivePowerups() {
        return Collections.unmodifiableList(activePowerups);
    }

    /**
     * Removes a powerup from the world after the player picks it up.
     * @param powerup the powerup to remove
     * @return true if it was present and got removed
     */
    public boolean removePowerup(Powerup powerup) {
        return activePowerups.remove(powerup);
    }
}

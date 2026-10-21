package com.group25.talesofartemis.model;

/**
 * Enumeration describing which world difficulty you are in, easy, medium, or hard. 
 * Each constant carries the difficulty specific configuration values used when building 
 * a level so that the numbers live in one place 
 *
 * <p>
 * Used for anything that relies on difficulty like building a level, pulling words from
 * WordManager, and spawning enemies with different stats.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public enum DifficultyLevel {

    /**
     * Easy world difficulty. Will have short words, slow enemies, fewer spawns, generous 
     * powerup drops. Designed for new or younger players.
     */
    EASY(5, 40),

    /**
     * Medium world difficulty. Will have medium-length words, moderate enemy speed, standard 
     * powerup drops. Designed for players who have some experience with the game and want a 
     * balanced challenge.
     */
    MEDIUM(8, 25),

    /**
     * Hard world difficulty. Will have long words, fast enemies, more spawns, smaller powerup 
     * drops. Designed for experienced players looking for a tough challenge and high 
     * replayability.
     */
    HARD(12, 15);

    /**
     * Total number of enemies that spawn in a level of this dificulty.
     * The gameplay loop stops calling spawnEnemy() once this count is reached.
     */
    public final int maxSpawns;

    /**
     * HP restored by a HealthPowerup dropped in a level of this difficulty.
     */
    public final int healthPowerupAmount;

    /**
     * Constructor for DifficultyLevel enum constants.
     * @param maxSpawns fixed number of enemies that will spawn in a level of this difficulty
     * @param healthPowerupAmount fixed HP restored by a HealthPowerup dropped in a level of this difficulty
     */

    DifficultyLevel(int maxSpawns, int healthPowerupAmount) {
        this.maxSpawns = maxSpawns;
        this.healthPowerupAmount = healthPowerupAmount;
    }
}

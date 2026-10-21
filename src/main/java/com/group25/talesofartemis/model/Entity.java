package com.group25.talesofartemis.model;

/**
 * Represents any object in the game world that has a position and health.
 * Both PlayerCharacter and Enemy extend this class.
 *
 * <p>This class provides common functionality such as taking damage,
 * healing, and tracking position. Each subclass must implement its own
 * movement behavior through the move() method.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */

public abstract class Entity {

    private double x;
    private double y;
    private int health;
    private int maxHealth;

     /**
     * Constructs an entity with a given position and maximum health.
     *
     * @param x initial x-coordinate
     * @param y initial y-coordinate
     * @param maxHealth maximum health value (must be non-negative)
     */

    protected Entity(double x, double y, int maxHealth) {
        if (maxHealth < 0) {
            throw new IllegalArgumentException("maxHealth must be non-negative");
        }
        this.x = x;
        this.y = y;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

     /**
     * Reduces the entity's health by the given amount.
     *
     * @param amount damage to apply (negative values treated as 0)
     */

    public void takeDamage(int amount) {
        int dmg = Math.max(0, amount);
        health = Math.max(0, health - dmg);
    }

     /**
     * Restores health to the entity up to its maximum health.
     *
     * @param amount healing amount (negative values treated as 0)
     */

    public void heal(int amount) {
        int h = Math.max(0, amount);
        health = Math.min(maxHealth, health + h);
    }

     /**
     * Checks if the entity has no remaining health.
     *
     * @return true if health is 0 or less, false otherwise
     */

    public boolean isDead() {
        return health <= 0;
    }

    /**
     * Updates the entity’s position based on time progression.
     *
     * @param deltaTime time elapsed since last update (in seconds)
     */

    public abstract void move(double deltaTime);

    /**
     * @return current x-coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * @return current y-coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the x-coordinate.
     *
     * @param x new x position
     */

    protected void setX(double x) {
        this.x = x;
    }

     /**
     * Sets the y-coordinate.
     *
     * @param y new y position
     */

    protected void setY(double y) {
        this.y = y;
    }
     /**
     * @return current health
     */

    public int getHealth() {
        return health;
    }
    
      /**
     * @return maximum health
     */


    protected int getMaxHealth() {
        return maxHealth;
    }


    /**
     * Sets both x and y position at once.
     *
     * @param x new x position
     * @param y new y position
     */

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

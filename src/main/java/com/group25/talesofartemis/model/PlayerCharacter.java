package com.group25.talesofartemis.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the player-controlled character in the game.
 *
 * <p>Handles movement physics, player actions triggered by typing,
 * and active powerups. Movement is influenced by gravity and velocity.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public class PlayerCharacter extends Entity {

    private final List<Powerup> activePowerups = new ArrayList<>();

    private double velocityX;
    private double velocityY;

    private static final double GRAVITY = 1200.0;
    private static final double JUMP_VELOCITY = -520.0;

    /**
     * Constructs the player character.
     *
     * @param x initial x-coordinate
     * @param y initial y-coordinate
     * @param maxHealth maximum health value
     */
    public PlayerCharacter(double x, double y, int maxHealth) {
        super(x, y, maxHealth);
    }

    /**
     * Triggers an action based on a typed command.
     *
     * @param action action to perform (jump, duck, attack, etc.)
     */
    public void triggerAction(ActionType action) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case JUMP -> velocityY = JUMP_VELOCITY;
            case DUCK -> velocityY += 300.0;
            case ATTACK, SWORD, SHIELD, BLOCK, SLASH, SPELL, ARROW, COUNTER -> {
                // handled elsewhere
            }
        }
    }

    /**
     * Prevents the player from falling below the ground.
     *
     * @param groundY y-coordinate of the ground
     */
    public void clampToGround(double groundY) {
        if (getY() >= groundY) {
            setY(groundY);
            velocityY = 0;
        }
    }

    /**
     * Adds a powerup to the active list.
     *
     * @param powerup powerup to add
     */
    public void addPowerup(Powerup powerup) {
        Objects.requireNonNull(powerup, "powerup");
        activePowerups.add(powerup);
    }

    /**
     * Removes a powerup from the active list.
     *
     * @param powerup powerup to remove
     * @return true if removed successfully
     */
    public boolean removePowerup(Powerup powerup) {
        return activePowerups.remove(powerup);
    }

    /**
     * @return list of active powerups (read-only)
     */
    public List<Powerup> getActivePowerups() {
        return Collections.unmodifiableList(activePowerups);
    }

    /**
     * Updates player movement each frame.
     *
     * @param deltaTime time elapsed since last update
     */
    @Override
    public void move(double deltaTime) {
        if (deltaTime < 0) {
            throw new IllegalArgumentException("deltaTime must be non-negative");
        }
        velocityY += GRAVITY * deltaTime;
        setX(getX() + velocityX * deltaTime);
        setY(getY() + velocityY * deltaTime);
        velocityX *= Math.exp(-3.0 * deltaTime);
    }

    @Override
    public double getX() {
        return super.getX();
    }

    @Override
    public double getY() {
        return super.getY();
    }
}
package com.group25.talesofartemis.model;

import com.group25.talesofartemis.game.GameSession;
import java.util.Objects;

/**
 * A powerup that restores health to the player when collected.
 *
 * <p>The amount of healing is fixed and depends on the game
 * difficulty. This class implements the Powerup interface so
 * it can be handled generically by the game system.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public class HealthPowerup implements Powerup {

    private final double x;
    private final double y;
    private final int healAmount;

    /**
     * Creates a health powerup at a specific location.
     *
     * @param x x-coordinate in the game world
     * @param y y-coordinate in the game world
     * @param healAmount amount of health restored (must be non-negative)
     */
    public HealthPowerup(double x, double y, int healAmount) {
        if (healAmount < 0) {
            throw new IllegalArgumentException("healAmount must be non-negative");
        }
        this.x = x;
        this.y = y;
        this.healAmount = healAmount;
    }

    /**
     * @return x-coordinate of the powerup
     */
    @Override
    public double getX() {
        return x;
    }

    /**
     * @return y-coordinate of the powerup
     */
    @Override
    public double getY() {
        return y;
    }

    /**
     * @return amount of health restored
     */
    public int getHealAmount() {
        return healAmount;
    }

    /**
     * @return description shown to the player
     */
    @Override
    public String getDescription() {
        return "Restores " + healAmount + " health.";
    }

    /**
     * Applies the healing effect to the player.
     *
     * @param session the current game session
     */
    @Override
    public void applyEffect(GameSession session) {
        Objects.requireNonNull(session, "session");
        session.getPlayerChar().heal(healAmount);
    }
}

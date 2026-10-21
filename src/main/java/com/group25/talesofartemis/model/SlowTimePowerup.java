package com.group25.talesofartemis.model;

import com.group25.talesofartemis.game.GameSession;
import java.util.Objects;

/**
 * Slows everyone on the level for a configurable duration (0.5 slowFactor = half speed).
 * GameSession.updateGame counts down the timer; when it hits zero enemy speeds are restored and
 * this buff is removed from the player's active powerup list.
 * x/y are the world-space spawn position so the Level can store and render this directly.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public class SlowTimePowerup implements Powerup {

    private final double x;
    private final double y;
    private final double slowFactor;
    private final long durationMs;

    /**
     * @param x          world-space X of the pickup sprite
     * @param y          world-space Y of the pickup sprite
     * @param slowFactor between 0 and 1 — how much to slow enemies (0.5 = half speed)
     * @param durationMs how long the effect lasts in milliseconds
     */
    public SlowTimePowerup(double x, double y, double slowFactor, long durationMs) {
        if (slowFactor <= 0 || slowFactor > 1) {
            throw new IllegalArgumentException("slowFactor must be in (0, 1]");
        }
        if (durationMs <= 0) {
            throw new IllegalArgumentException("durationMs must be positive");
        }
        this.x = x;
        this.y = y;
        this.slowFactor = slowFactor;
        this.durationMs = durationMs;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    public double getSlowFactor() {
        return slowFactor;
    }

    public long getDurationMs() {
        return durationMs;
    }

    @Override
    public String getDescription() {
        return "Slows enemies for " + (durationMs / 1000) + " s.";
    }

    @Override
    public void applyEffect(GameSession session) {
        Objects.requireNonNull(session, "session");
        session.beginSlowTime(this, slowFactor, durationMs / 1000.0);
    }
}

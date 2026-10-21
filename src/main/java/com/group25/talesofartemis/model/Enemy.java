package com.group25.talesofartemis.model;

/**
 * Enemy with the word you have to type. Each frame it steps toward wherever you pointed it which is usually
 * the player. You can increase its health, speed, and damage when you spawn one on a harder world.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public class Enemy extends Entity {

    private final String assignedWord;
    private final double speed;
    private final int attackDamage;

    /** Multiplier on base speed. Slow-time powerups knock this below 1. */
    private double speedMultiplier = 1.0;

    private double targetX = Double.NaN;
    private double targetY = Double.NaN;

    /**
     * @param spawnX       where it appears
     * @param spawnY
     * @param maxHealth    starting HP (and cap)
     * @param assignedWord what the player types to kill it which is non-empty
     * @param speed        how fast it closes in (same units as x/y per second)
     * @param attackDamage hit strength if it gets you
     */
    public Enemy(
            double spawnX,
            double spawnY,
            int maxHealth,
            String assignedWord,
            double speed,
            int attackDamage) {
        super(spawnX, spawnY, maxHealth);
        if (assignedWord == null || assignedWord.isBlank()) {
            throw new IllegalArgumentException("assignedWord must be non-null and non-blank");
        }
        if (speed < 0) {
            throw new IllegalArgumentException("speed must be non-negative");
        }
        if (attackDamage < 0) {
            throw new IllegalArgumentException("attackDamage must be non-negative");
        }
        this.assignedWord = assignedWord.trim();
        this.speed = speed;
        this.attackDamage = attackDamage;
    }

    /**
     * Where to walk toward (player position). Set this before move each tick or it won't go anywhere.
     */
    public void setChaseTarget(double playerX, double playerY) {
        this.targetX = playerX;
        this.targetY = playerY;
    }

    @Override
    public void move(double deltaTime) {
        if (deltaTime < 0) {
            throw new IllegalArgumentException("deltaTime must be non-negative");
        }
        if (Double.isNaN(targetX) || Double.isNaN(targetY)) {
            return;
        }

        double dx = targetX - getX();
        double dy = targetY - getY();
        double dist = Math.hypot(dx, dy);
        if (dist < 1e-9) {
            return;
        }

        double step = speed * speedMultiplier * deltaTime;
        if (step >= dist) {
            setX(targetX);
            setY(targetY);
        } else {
            double nx = dx / dist;
            double ny = dy / dist;
            setX(getX() + nx * step);
            setY(getY() + ny * step);
        }
    }

    public String getAssignedWord() {
        return assignedWord;
    }

    public double getSpeed() {
        return speed;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        if (speedMultiplier < 0) {
            throw new IllegalArgumentException("speedMultiplier must be non-negative");
        }
        this.speedMultiplier = speedMultiplier;
    }

    public int getAttackDamage() {
        return attackDamage;
    }
}

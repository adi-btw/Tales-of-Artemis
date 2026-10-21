package com.group25.talesofartemis.game;

import com.group25.talesofartemis.model.ActionType;
import com.group25.talesofartemis.model.Enemy;
import com.group25.talesofartemis.game.Level;
import com.group25.talesofartemis.model.PlayerCharacter;
import com.group25.talesofartemis.model.SlowTimePowerup;
import com.group25.talesofartemis.model.AccountManager;
import com.group25.talesofartemis.model.PlayerProfile;
import com.group25.talesofartemis.model.TypingManager;
import java.util.Objects;

/**
 * The live match state: who's playing, which level is loaded, and whether slow-mo is running. Other systems (sound,
 * scoring, whatever) can sit next to this later — this class is just the glue for stuff that only exists mid-game.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public class GameSession {

    private final PlayerCharacter playerChar;
    private Level currentLevel;
    private com.group25.talesofartemis.model.AccountManager accountManager;
    private com.group25.talesofartemis.model.PlayerProfile profile;

    /** The typing manager associated with the current game session. */
    private final TypingManager typingManager = new TypingManager();

    /** Slow-time buff we're tracking, if any, plus seconds left before it wears off. */
    private SlowTimePowerup activeSlowPowerup;
    private double slowTimeRemainingSec;

    public GameSession(PlayerCharacter playerChar, com.group25.talesofartemis.model.AccountManager accountManager) {
        this.playerChar = playerChar;
        this.accountManager = accountManager;
        this.profile = (accountManager != null) ? accountManager.getActiveProfile() : null;
    }

    public PlayerCharacter getPlayerChar() {
        return playerChar;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Point the session at a level (or null). When you pass a real level, we wire the player in as the chase target
     * so enemies don't stand around confused.
     */
    public void setCurrentLevel(Level currentLevel) {
        this.currentLevel = currentLevel;
        if (currentLevel != null) {
            currentLevel.setChaseTarget(playerChar);
        }
    }

    /**
     * Player grabbed slow-time, so every enemy's speed is slowed down, the duration is stored, and the powerup is added to
     * the character so we can remove it when the clock runs out. Starting a new slow clears an old one first.
     */
    public void beginSlowTime(SlowTimePowerup powerup, double slowFactor, double durationSec) {
        Objects.requireNonNull(powerup, "powerup");
        if (slowFactor <= 0 || slowFactor > 1) {
            throw new IllegalArgumentException("slowFactor must be in (0, 1]");
        }
        if (durationSec <= 0) {
            throw new IllegalArgumentException("durationSec must be positive");
        }
        cancelActiveSlowTimeIfAny();
        applySlowFactorToActiveEnemies(slowFactor);
        this.activeSlowPowerup = powerup;
        this.slowTimeRemainingSec = durationSec;
        playerChar.addPowerup(powerup);
    }

    /**
     * Feed frame time in seconds. Only really does work while slow-time is active. When the timer drains, enemies snap
     * back to normal speed and we remove the buff from the player.
     */
    public void updateGame(double deltaTimeSec) {
        if (deltaTimeSec < 0) {
            throw new IllegalArgumentException("deltaTimeSec must be non-negative");
        }
        if (activeSlowPowerup == null) {
            return;
        }
        slowTimeRemainingSec -= deltaTimeSec;
        if (slowTimeRemainingSec <= 0) {
            endSlowTimeEffect();
        }
        if (currentLevel != null && currentLevel.isCleared()) {
    saveProgress();
        }
    }

    /** Cancel an in-flight slow without waiting for the timer to run out, used when stacking a new slow on top. */
    private void cancelActiveSlowTimeIfAny() {
        if (activeSlowPowerup == null) {
            return;
        }
        restoreEnemySpeedMultipliers();
        playerChar.removePowerup(activeSlowPowerup);
        activeSlowPowerup = null;
        slowTimeRemainingSec = 0;
    }

    /** When the slow-time timer runs out, speeds back to 1×, buff removed from the player. */
    private void endSlowTimeEffect() {
        restoreEnemySpeedMultipliers();
        if (activeSlowPowerup != null) {
            playerChar.removePowerup(activeSlowPowerup);
            activeSlowPowerup = null;
        }
        slowTimeRemainingSec = 0;
    }

    /** Multiply every live enemy's speed by factor which is between 0 and 1 for slow-mo. */
    private void applySlowFactorToActiveEnemies(double factor) {
        if (currentLevel == null) {
            return;
        }
        for (Enemy e : currentLevel.getActiveEnemies()) {
            e.setSpeedMultiplier(factor);
        }
    }

    /** Put enemy movement back to full speed. */
    private void restoreEnemySpeedMultipliers() {
        if (currentLevel == null) {
            return;
        }
        for (Enemy e : currentLevel.getActiveEnemies()) {
            e.setSpeedMultiplier(1.0);
        }
    }
    /**
     * Checks whether a completed word is a special action keyword (FR-9 / UC-10).
     * Tries to match the word (case-insensitive) to an ActionType enum value. If a match is found,
     * calls playerChar.triggerAction() immediately and returns the ActionType so the view can show
     * visual feedback. Returns null if the word is not a recognised action keyword.
     *
     * @param completedWord the exact word the player just finished typing
     * @return the ActionType that was triggered, or null if the word is not an action keyword
     */
    public ActionType mapWordToAction(String completedWord) {
        if (completedWord == null || completedWord.isEmpty()) return null;
        try {
            ActionType action = ActionType.valueOf(completedWord.toUpperCase());
            playerChar.triggerAction(action);
            return action;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Returns the TypingManager used by this session that tracks the users word progress, WPM, and accuracy.
     * @return the shared TypingManager for this game session
     */
    public TypingManager getTypingManager() {
        return typingManager;
    }

    /**
     * Receives one character typed by the player and forwards it to the TypingManager.
     * Called by the gameplay KeyListener on every keyTyped event. Completes in 50ms or less.
     * @param c the character typed by the player
     * @return true if the character matched the next expected letter, false if otherwise
     */
    public boolean processKeystroke(char c) {
        return typingManager.validateKeystroke(c);
    }

    private void saveProgress() {
        if (profile == null) return;

        int score = 100;
        int playTime = 5;
        double wpm = 50;
        int errors = 2;
        int levelReached = 1;

        profile.updateStats(score, playTime, wpm, errors, levelReached);

        try {
            accountManager.saveProfilesToJSON();
            } catch (Exception e) {
            e.printStackTrace();
            }
    }
}

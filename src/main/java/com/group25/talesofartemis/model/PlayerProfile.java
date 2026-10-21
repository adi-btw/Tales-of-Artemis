package com.group25.talesofartemis.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * One saved player: identity, admin flag, progression, and typing stats.
 *
 * <p>Password security (issue): only a hash is stored (`passwordHash`, SHA-256 hex). Use `hashPassword` when setting
 * or changing a password; `checkPassword` takes plaintext input, hashes it, and compares to the stored digest using
 * `MessageDigest.isEqual` on the raw bytes (no plaintext persisted or compared with string equality).
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public class PlayerProfile {

    private String username;
    private String passwordHash;
    private boolean isAdmin;
    private int maxLevelUnlocked;
    private int topScore;
    private int totalPlayTimeMins;
    private double averageWPM;
    private double peakWPM;
    private int totalErrors;
    private double lastSessionWpm;
    private double lastSessionAccuracy = 100.0;
    private boolean isParent;

    /** Empty constructor so Gson can deserialize from JSON. */
    public PlayerProfile() {}

    /**
     * Constructor for a new player. Username can't be blank and numeric bits get capped at zero so saves don't go
     * negative if someone hands you invalid data.
     */
    public PlayerProfile(
            String username,
            String passwordHash,
            boolean isAdmin,
            int maxLevelUnlocked,
            int topScore,
            int totalPlayTimeMins,
            double averageWPM,
            double peakWPM,
            int totalErrors) {
        this.username = Objects.requireNonNull(username, "username").trim();
        if (this.username.isEmpty()) {
            throw new IllegalArgumentException("username must be non-blank");
        }
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.isAdmin = isAdmin;
        this.maxLevelUnlocked = Math.max(0, maxLevelUnlocked);
        this.topScore = Math.max(0, topScore);
        this.totalPlayTimeMins = Math.max(0, totalPlayTimeMins);
        this.averageWPM = Math.max(0, averageWPM);
        this.peakWPM = Math.max(0, peakWPM);
        this.totalErrors = Math.max(0, totalErrors);
    }

    /** @return true if this account has parental-control privileges. */
    public boolean isParent() {
        return isParent;
    }

    /** @param parent true to grant parental-control privileges to this account. */
    public void setParent(boolean parent) {
        this.isParent = parent;
    }

    /** @return the player's username. */
    public String getUsername() {
        return username;
    }

    /** @param username the new username to assign to this player. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return the SHA-256 hex-encoded password hash stored for this player. */
    public String getPasswordHash() {
        return passwordHash;
    }

    /** @param passwordHash the SHA-256 hex-encoded password hash to store. */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /** @return true if this account has administrator privileges. */
    public boolean isAdmin() {
        return isAdmin;
    }

    /** @param admin true to grant administrator privileges to this account. */
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    /** @return the highest level number this player has unlocked. */
    public int getMaxLevelUnlocked() {
        return maxLevelUnlocked;
    }

    /** @param maxLevelUnlocked the highest level number to record as unlocked. */
    public void setMaxLevelUnlocked(int maxLevelUnlocked) {
        this.maxLevelUnlocked = maxLevelUnlocked;
    }

    /** @return the player's all-time highest score. */
    public int getTopScore() {
        return topScore;
    }

    /** @param topScore the new all-time highest score to store. */
    public void setTopScore(int topScore) {
        this.topScore = topScore;
    }

    /** @return total minutes the player has spent in-game across all sessions. */
    public int getTotalPlayTimeMins() {
        return totalPlayTimeMins;
    }

    /** @param totalPlayTimeMins cumulative play time in minutes to store. */
    public void setTotalPlayTimeMins(int totalPlayTimeMins) {
        this.totalPlayTimeMins = totalPlayTimeMins;
    }

    /** @return the weighted average words-per-minute across all sessions. */
    public double getAverageWPM() {
        return averageWPM;
    }

    /** @param averageWPM the updated weighted average WPM to store. */
    public void setAverageWPM(double averageWPM) {
        this.averageWPM = averageWPM;
    }

    /** @return the highest WPM the player has achieved in any single session. */
    public double getPeakWPM() {
        return peakWPM;
    }

    /** @param peakWPM the new peak WPM record to store. */
    public void setPeakWPM(double peakWPM) {
        this.peakWPM = peakWPM;
    }

    /** @return the cumulative number of typing errors across all sessions. */
    public int getTotalErrors() {
        return totalErrors;
    }

    /** @return the words-per-minute achieved in the most recent session. */
    public double getLastSessionWpm() {
        return lastSessionWpm;
    }

    /** @return the typing accuracy (0-100) achieved in the most recent session. */
    public double getLastSessionAccuracy() {
        return lastSessionAccuracy;
    }

    /** @param accuracy the typing accuracy percentage (0-100) from the most recent session. */
    public void setLastSessionAccuracy(double accuracy) {
        this.lastSessionAccuracy = accuracy;
    }

    /** @param totalErrors the updated cumulative error count to store. */
    public void setTotalErrors(int totalErrors) {
        this.totalErrors = totalErrors;
    }

    /** Resets all gameplay statistics to their default values, keeping the account and credentials intact. */
    public void resetStats() {
        this.maxLevelUnlocked = 1;
        this.topScore = 0;
        this.totalPlayTimeMins = 0;
        this.averageWPM = 0;
        this.peakWPM = 0;
        this.totalErrors = 0;
    }

    /** Resets only the leaderboard-visible fields (top score and max level) without touching typing stats. */
    public void resetLeaderboardStats() {
        this.topScore = 0;
        this.maxLevelUnlocked = 1;
    }

    /**
     * SHA-256 of the password, hex-encoded — suitable for `passwordHash` (bcrypt is a stronger alternative for
     * production; this matches the course SHA-256 option).
     */
    public static String hashPassword(String plaintext) {
        Objects.requireNonNull(plaintext, "plaintext");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plaintext.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Hashes `plaintext` and compares to `passwordHash` without storing the input. Uses constant-time digest equality
     * on decoded bytes, not `String.equals`.
     */
    public boolean checkPassword(String plaintext) {
        if (plaintext == null || passwordHash == null || passwordHash.isEmpty()) {
            return false;
        }
        try {
            byte[] stored = HexFormat.of().parseHex(passwordHash);
            byte[] candidate = HexFormat.of().parseHex(hashPassword(plaintext));
            return MessageDigest.isEqual(stored, candidate);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Call after a session ends. Updates best score and level, adds errors and play time, tracks best WPM for the
     * run, and nudges averageWPM with a weighted average so a long play matters more than a quick burst.
     */
    public void updateStats(
            int sessionScore,
            int sessionPlayTimeMins,
            double sessionWpm,
            int sessionErrors,
            int levelReachedThisSession) {
        if (sessionPlayTimeMins < 0 || sessionWpm < 0 || sessionErrors < 0) {
            throw new IllegalArgumentException("session stats must be non-negative");
        }
        maxLevelUnlocked = Math.max(maxLevelUnlocked, levelReachedThisSession);
        topScore = Math.max(topScore, sessionScore);
        lastSessionWpm = sessionWpm;
        totalErrors += sessionErrors;
        peakWPM = Math.max(peakWPM, sessionWpm);

        int prevMins = totalPlayTimeMins;
        int addMins = sessionPlayTimeMins;
        int newTotalMins = prevMins + addMins;
        if (newTotalMins > 0) {
            averageWPM = (averageWPM * prevMins + sessionWpm * addMins) / (double) newTotalMins;
        } else if (addMins == 0 && prevMins == 0 && sessionWpm > 0) {
            averageWPM = sessionWpm;
        }
        totalPlayTimeMins = newTotalMins;
    }
}

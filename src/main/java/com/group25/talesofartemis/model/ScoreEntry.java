package com.group25.talesofartemis.model;

/**
 * Represents a single score entry in the leaderboard.
 *
 * <p>Stores player username, score, words per minute (WPM),
 * and level reached during a game session. Objects of this
 * class are comparable and sorted in descending order based
 * on score.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public class ScoreEntry implements Comparable<ScoreEntry> {

    private String username;
    private int score;
    private double wpm;
    private int levelReached;

    /**
     * Default constructor used for JSON deserialization.
     */
    public ScoreEntry() {}

    /**
     * Creates a score entry with only username and score.
     *
     * @param username the player's username
     * @param score the score achieved
     */
    public ScoreEntry(String username, int score) {
        this(username, score, 0.0, 0);
    }

    /**
     * Creates a full score entry.
     *
     * @param username the player's username
     * @param score the score achieved
     * @param wpm typing speed (words per minute)
     * @param levelReached highest level reached
     */
    public ScoreEntry(String username, int score, double wpm, int levelReached) {
        this.username = username;
        this.score = score;
        this.wpm = wpm;
        this.levelReached = levelReached;
    }

    /**
     * @return the player's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the score value
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the score.
     *
     * @param score the new score value
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * @return words per minute (WPM)
     */
    public double getWpm() {
        return wpm;
    }

    /**
     * Sets the WPM value.
     *
     * @param wpm the new WPM value
     */
    public void setWpm(double wpm) {
        this.wpm = wpm;
    }

    /**
     * @return the level reached in the game
     */
    public int getLevelReached() {
        return levelReached;
    }

    /**
     * Sets the level reached.
     *
     * @param levelReached the new level value
     */
    public void setLevelReached(int levelReached) {
        this.levelReached = levelReached;
    }

    /**
     * Compares this score entry to another for sorting.
     * Higher scores come first (descending order).
     *
     * @param o the other score entry
     * @return comparison result for sorting
     */
    @Override
    public int compareTo(ScoreEntry o) {
        if (o == null) {
            throw new NullPointerException();
        }
        return Integer.compare(o.score, this.score);
    }
}

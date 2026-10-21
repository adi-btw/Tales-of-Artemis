package com.group25.talesofartemis.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A capped high-score list: we keep scores highest-first, and when you add one too many rows the extras at the bottom
 * disappear.
 *
 * <p>You can save and load with Gson — saveToJSON and loadFromJSON — using a small JSON object with maxSize and an
 * entries array. Every score row keeps username, score, wpm, and levelReached on disk and reads them back in. By
 * default we use data/leaderboard.json, or you can point at any path you like.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public class Leaderboard {

    private static final Path DEFAULT_JSON_PATH = Path.of("data", "leaderboard.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private int maxSize = 10;
    private List<ScoreEntry> entries = new ArrayList<>();

    /**
 * Creates an empty leaderboard with default max size.
 */
    public Leaderboard() {}

    /**
 * @return maximum number of entries allowed
 */
    public int getMaxSize() {
        return maxSize;
    }

    
    /**
 * Sets the maximum number of entries allowed.
 *
 * @param maxSize the new maximum size
 * @throws IllegalArgumentException if maxSize is less than 1
 */
    public void setMaxSize(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("maxSize must be at least 1");
        }
        this.maxSize = maxSize;
        trimToMax();
        sortDescending();
    }

/**
 * Retrieves all leaderboard entries.
 *
 * @return unmodifiable list of entries
 */    
    public List<ScoreEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
 * Sets the list of leaderboard entries.
 *
 * @param entries the new list of entries
 */
    public void setEntries(List<ScoreEntry> entries) {
        this.entries = entries != null ? new ArrayList<>(entries) : new ArrayList<>();
        sortDescending();
        trimToMax();
    }

   /**
 * Adds a score entry to the leaderboard.
 *
 * @param entry the score entry to add
 */

    public void addEntry(ScoreEntry entry) {
        Objects.requireNonNull(entry, "entry");
        // Remove any existing entry for this user so each player appears only once (best score wins)
        entries.removeIf(e -> e.getUsername().equalsIgnoreCase(entry.getUsername()));
        entries.add(
                new ScoreEntry(
                        entry.getUsername(),
                        entry.getScore(),
                        entry.getWpm(),
                        entry.getLevelReached()));
        sortDescending();
        trimToMax();
    }

    /**
 * Adds a score using username and score only.
 *
 * @param username the player's username
 * @param score the score value
 */

    public void addEntry(String username, int score) {
        Objects.requireNonNull(username, "username");
        addEntry(new ScoreEntry(username.trim(), score));
    }

   /**
 * Retrieves top N entries.
 *
 * @param n number of entries
 * @return list of top scores
 */
    public List<ScoreEntry> getTopEntries(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative");
        }
        if (n == 0 || entries.isEmpty()) {
            return List.of();
        }
        int end = Math.min(n, entries.size());
        return List.copyOf(entries.subList(0, end));
    }

    /** Empties the list but leaves the cap alone. */
    public void clear() {
        entries.clear();
    }

    /**
 * Removes all entries for a user.
 *
 * @param username the username
 */
    public void removeByUsername(String username) {
        Objects.requireNonNull(username, "username");
        String u = username.trim();
        if (u.isEmpty()) {
            return;
        }
        entries.removeIf(e -> u.equalsIgnoreCase(e.getUsername()));
    }

   /**
 * Saves leaderboard to default file.
 *
 * @throws IOException if writing fails
 */
    public void saveToJSON() throws IOException {
        saveToJSON(DEFAULT_JSON_PATH);
    }

    /**
 * Saves leaderboard to a specific file.
 *
 * @param path file path
 * @throws IOException if writing fails
 */
    public void saveToJSON(Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String json = GSON.toJson(this);
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    /**
 * Loads leaderboard from default file.
 *
 * @throws IOException if reading fails
 */
    public void loadFromJSON() throws IOException {
        loadFromJSON(DEFAULT_JSON_PATH);
    }

  /**
 * Loads leaderboard from a specific file.
 *
 * @param path file path
 * @throws IOException if reading fails
 */
    public void loadFromJSON(Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        if (!Files.isRegularFile(path)) {
            clear();
            maxSize = 10;
            return;
        }
        String json = Files.readString(path, StandardCharsets.UTF_8);
        Leaderboard parsed = GSON.fromJson(json, Leaderboard.class);
        if (parsed == null) {
            clear();
            maxSize = 10;
            return;
        }
        this.maxSize = parsed.maxSize > 0 ? parsed.maxSize : 10;
        this.entries.clear();
        if (parsed.entries != null) {
            for (ScoreEntry e : parsed.entries) {
                if (e != null) {
                    String u = e.getUsername() != null ? e.getUsername() : "";
                    this.entries.add(new ScoreEntry(u, e.getScore(), e.getWpm(), e.getLevelReached()));
                }
            }
        }
        sortDescending();
        trimToMax();
    }

    private void sortDescending() {
        entries.sort(null);
    }

    private void trimToMax() {
        while (entries.size() > maxSize) {
            entries.remove(entries.size() - 1);
        }
    }
}

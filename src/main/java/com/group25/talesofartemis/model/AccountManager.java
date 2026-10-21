package com.group25.talesofartemis.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.group25.talesofartemis.data.JsonDataRecovery;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Handles accounts and keeps everything in JSON files on disk. It tracks who exists, the leaderboard, and who is
 * currently logged in — plus login, logout, sign-up, delete, and password reset.
 *
 * <p>We save the roster with Gson to data/players.json (or whatever paths you pass in). The file is one JSON object
 * with a players array; see PlayersDocument for the shape. PlayerProfile only stores a password hash (SHA-256 hex), so
 * real passwords never get written out — stick to hashPassword, createAccount, and resetPassword if you want that to
 * stay true.
 *
 * <p>When you build an AccountManager, JsonDataRecovery runs first: broken or missing files get replaced with empty
 * defaults and a quick heads-up (stderr unless you pointed JsonDataRecovery somewhere else), then we load.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */


public class AccountManager {

    private static final Path DEFAULT_PLAYERS_PATH = Path.of("data", "players.json");
    private static final Path DEFAULT_LEADERBOARD_PATH = Path.of("data", "leaderboard.json");

    private final List<PlayerProfile> allProfiles = new ArrayList<>();
    private Leaderboard leaderboard;
    private PlayerProfile activeProfile;

    private final Path playersFilePath;
    private final Path leaderboardFilePath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /** Same as the two-path constructor, but uses the usual data/players.json and data/leaderboard.json locations. */
    public AccountManager() throws IOException {
        this(DEFAULT_PLAYERS_PATH, DEFAULT_LEADERBOARD_PATH);
    }

   /**
 * Creates an AccountManager using default file paths.
 *
 * @throws IOException if files cannot be accessed or created
 */

    public AccountManager(Path playersFilePath, Path leaderboardFilePath) throws IOException {
        this.playersFilePath = Objects.requireNonNull(playersFilePath, "playersFilePath");
        this.leaderboardFilePath = Objects.requireNonNull(leaderboardFilePath, "leaderboardFilePath");
        JsonDataRecovery.ensurePlayersAndLeaderboard(this.playersFilePath, this.leaderboardFilePath);
        loadProfilesFromJSON(this.playersFilePath);
        if (!allProfiles.isEmpty()) {
        allProfiles.get(0).setAdmin(true);
    }
        this.leaderboard = readLeaderboardFile(this.leaderboardFilePath);
    }

    
   /**
 * Initializes the account manager with specific file paths.
 * Repairs missing/corrupt files and loads all profiles and leaderboard data.
 *
 * @param playersFilePath path to the players JSON file
 * @param leaderboardFilePath path to the leaderboard JSON file
 * @throws IOException if files cannot be accessed or read
 */

    public List<PlayerProfile> getAllProfiles() {
        return Collections.unmodifiableList(allProfiles);
    }

    
    /**
     * @return the leaderboard
     */
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    
   /**
 * Gets the currently logged-in user.
 *
 * @return the active player profile, or null if none
 */

    public PlayerProfile getActiveProfile() {
        return activeProfile;
    }

    /**
 * Gets the currently logged-in user.
 *
 * @return the active player profile, or null if none
 */

    public boolean login(String username, String password) {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
        String u = username.trim();
        if (u.isEmpty()) {
            return false;
        }
        Optional<PlayerProfile> found =
                allProfiles.stream().filter(p -> u.equalsIgnoreCase(p.getUsername())).findFirst();
        if (found.isEmpty() || !found.get().checkPassword(password)) {
            return false;
        }
        activeProfile = found.get();
        return true;
    }

    /** Clears the current user. No-op if nobody was signed in. */
    public void logout() {
        activeProfile = null;
    }

   /**
 * Creates a standard user account.
 *
 * @param username the username
 * @param password the password
 */

    public void createAccount(String username, String password) {
        createAccount(username, password, false);
    }

    /**
 * Creates a new user account.
 *
 * @param username the username
 * @param password the password
 * @param isAdmin whether the user has admin privileges
 */
    public void createAccount(String username, String password, boolean isAdmin) {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");

        String u = username.trim();
        if (u.isEmpty()) {
            throw new IllegalArgumentException("username must be non-blank");
        }
        if (password.isEmpty()) {
            throw new IllegalArgumentException("password must be non-blank");
        }

        boolean taken = allProfiles.stream()
                .anyMatch(p -> u.equalsIgnoreCase(p.getUsername()));

        if (taken) {
            throw new IllegalArgumentException("username already taken");
        }

        String hash = PlayerProfile.hashPassword(password);

        PlayerProfile p = new PlayerProfile(u, hash, isAdmin, 0, 0, 0, 0.0, 0.0, 0);

        //  FORCE FIRST USER TO BE ADMIN
        if (allProfiles.size() == 0) {
            p.setAdmin(true);
        }

        allProfiles.add(p);

        //  SAVE IMMEDIATELY (IMPORTANT)
        try {
            saveProfilesToJSON();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
 * Deletes a user profile.
 *
 * @param username the username to delete
 * @return true if deletion was successful
 */

    public boolean deleteProfile(String username) {
        Objects.requireNonNull(username, "username");
        Optional<PlayerProfile> found =
                allProfiles.stream().filter(p -> username.trim().equalsIgnoreCase(p.getUsername())).findFirst();
        if (found.isEmpty()) {
            return false;
        }
        PlayerProfile victim = found.get();
        if (activeProfile == victim) {
            logout();
        }
        allProfiles.remove(victim);
        return true;
    }

   /**
 * Resets a user's password.
 *
 * @param username the username
 * @param newPassword the new password
 */

    public void resetPassword(String username, String newPassword) {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(newPassword, "newPassword");
        if (newPassword.isEmpty()) {
            throw new IllegalArgumentException("newPassword must be non-blank");
        }
        PlayerProfile p =
                allProfiles.stream()
                        .filter(x -> username.trim().equalsIgnoreCase(x.getUsername()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("unknown username"));
        p.setPasswordHash(PlayerProfile.hashPassword(newPassword));
    }

    /**
 * Saves all profiles to the default JSON file.
 *
 * @throws IOException if writing fails
 */

    public void saveProfilesToJSON() throws IOException {
        saveProfilesToJSON(playersFilePath);
    }

    /**
 * Saves profiles to a specified file.
 *
 * @param path file path
 * @throws IOException if writing fails
 */

    public void saveProfilesToJSON(Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        PlayersDocument doc = new PlayersDocument();
        doc.players = new ArrayList<>(allProfiles);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String json = gson.toJson(doc);
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

   /**
 * Loads profiles from the default file.
 *
 * @throws IOException if reading fails
 */
    public void loadProfilesFromJSON() throws IOException {
        loadProfilesFromJSON(playersFilePath);
    }

    /**
 * Loads profiles from a specified file.
 *
 * @param path file path
 * @throws IOException if reading fails
 */

    public void loadProfilesFromJSON(Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        if (!Files.isRegularFile(path)) {
            allProfiles.clear();
            activeProfile = null;
            return;
        }
        String json = Files.readString(path, StandardCharsets.UTF_8);
        PlayersDocument doc = gson.fromJson(json, PlayersDocument.class);
        allProfiles.clear();
        activeProfile = null;
        if (doc != null && doc.players != null) {
            allProfiles.addAll(doc.players);
        }
    }

    private Leaderboard readLeaderboardFile(Path path) throws IOException {
        Leaderboard lb = new Leaderboard();
        if (!Files.isRegularFile(path)) {
            lb.setMaxSize(10);
            return lb;
        }
        lb.loadFromJSON(path);
        return lb;
    }

    /** Gson-friendly wrapper for what we store in players.json: a single players list. */
    private static class PlayersDocument {
        List<PlayerProfile> players;
    }
}

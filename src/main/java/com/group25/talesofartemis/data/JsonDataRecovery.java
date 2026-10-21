package com.group25.talesofartemis.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Small helper for startup: makes sure players.json and leaderboard.json exist and look sane. If something is missing
 * or corrupted, we tell someone (usually stderr), write fresh empty files, and the game can still start. AccountManager
 * calls into this before it loads profiles or scores. Swap in your own listener with setErrorSink if you want UI alerts
 * instead of printing to the console.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public final class JsonDataRecovery {

    private static final String EMPTY_PLAYERS = "{\n  \"players\": []\n}";
    private static final String EMPTY_LEADERBOARD = "{\n  \"maxSize\": 10,\n  \"entries\": []\n}";

    private static Consumer<String> errorSink =
            msg -> System.err.println("[Game data] " + msg);

    private JsonDataRecovery() {}

    /**
     * Where to send messages when we fix bad data — defaults to stderr. Set this if you’d rather show a popup or log
     * somewhere else; do it before you construct AccountManager so early errors use the right sink.
     */
    public static void setErrorSink(Consumer<String> sink) {
        errorSink = sink != null ? sink : msg -> System.err.println("[Game data] " + msg);
    }

    /**
     * Walks both files: creates them with empty defaults if needed, or overwrites them if the JSON doesn’t match what
     * we expect. Users should hear about problems quickly (the course spec wanted feedback within about a second); if
     * the whole check runs longer than that, we add an extra heads-up.
     */
    public static void ensurePlayersAndLeaderboard(Path playersPath, Path leaderboardPath) throws IOException {
        Objects.requireNonNull(playersPath, "playersPath");
        Objects.requireNonNull(leaderboardPath, "leaderboardPath");
        long startMs = System.currentTimeMillis();
        ensurePlayersFile(playersPath);
        ensureLeaderboardFile(leaderboardPath);
        long elapsed = System.currentTimeMillis() - startMs;
        if (elapsed > 1000) {
            errorSink.accept(
                    "Warning: data file checks took " + elapsed + "ms (target was under 1000ms for user feedback).");
        }
    }

    private static void ensurePlayersFile(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            writeDefaults(path, EMPTY_PLAYERS);
            emit("Missing " + path + " — created an empty players list.");
            return;
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            validatePlayersJson(json);
        } catch (IOException e) {
            emit("Could not read " + path + ": " + e.getMessage() + " — replaced with an empty players list.");
            writeDefaults(path, EMPTY_PLAYERS);
        } catch (RuntimeException e) {
            emit("Corrupt or invalid " + path + ": " + e.getMessage() + " — replaced with an empty players list.");
            writeDefaults(path, EMPTY_PLAYERS);
        }
    }

    private static void ensureLeaderboardFile(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            writeDefaults(path, EMPTY_LEADERBOARD);
            emit("Missing " + path + " — created an empty leaderboard.");
            return;
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            validateLeaderboardJson(json);
        } catch (IOException e) {
            emit("Could not read " + path + ": " + e.getMessage() + " — replaced with an empty leaderboard.");
            writeDefaults(path, EMPTY_LEADERBOARD);
        } catch (RuntimeException e) {
            emit("Corrupt or invalid " + path + ": " + e.getMessage() + " — replaced with an empty leaderboard.");
            writeDefaults(path, EMPTY_LEADERBOARD);
        }
    }

    private static void validatePlayersJson(String json) {
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) {
            throw new JsonSyntaxException("root must be an object");
        }
        JsonObject o = root.getAsJsonObject();
        if (!o.has("players")) {
            throw new JsonSyntaxException("missing \"players\"");
        }
        JsonElement p = o.get("players");
        if (!p.isJsonArray()) {
            throw new JsonSyntaxException("\"players\" must be an array");
        }
    }

    private static void validateLeaderboardJson(String json) {
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) {
            throw new JsonSyntaxException("root must be an object");
        }
        JsonObject o = root.getAsJsonObject();
        if (!o.has("maxSize") || !o.get("maxSize").isJsonPrimitive()) {
            throw new JsonSyntaxException("missing numeric \"maxSize\"");
        }
        if (!o.has("entries")) {
            throw new JsonSyntaxException("missing \"entries\"");
        }
        JsonElement e = o.get("entries");
        if (!e.isJsonArray()) {
            throw new JsonSyntaxException("\"entries\" must be an array");
        }
    }

    private static void writeDefaults(Path path, String content) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static void emit(String message) {
        errorSink.accept(message);
    }
}

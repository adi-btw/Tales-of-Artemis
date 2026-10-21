package com.group25.talesofartemis.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Reads a text file with one word per line, sorts words into easy/medium/hard groups by how long they are,
 * then hands out random picks for spawning enemies and the like.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public class WordManager {

    // short words = easy, medium in the middle, long = hard
    private static final int EASY_MAX_LEN = 4;
    private static final int MEDIUM_MAX_LEN = 8;

    private final Path wordFilePath;

    private final List<String> easyWords = new ArrayList<>();
    private final List<String> mediumWords = new ArrayList<>();
    private final List<String> hardWords = new ArrayList<>();

    /**
     * Loads data/words.txt which is relative to where you launched the program (usually the project folder).
     */
    public WordManager() throws IOException {
        this(Path.of("data", "words.txt"));
    }

    public WordManager(Path wordFilePath) throws IOException {
        this.wordFilePath = Objects.requireNonNull(wordFilePath, "wordFilePath");
        loadWordsFromFile();
    }

    /**
     * Read the file again if you swapped the word list on disk.
     */
    public void loadWordsFromFile() throws IOException {
        easyWords.clear();
        mediumWords.clear();
        hardWords.clear();

        List<String> lines = Files.readAllLines(wordFilePath, StandardCharsets.UTF_8);
        for (String line : lines) {
            String w = line.trim().toLowerCase();
            if (w.isEmpty() || !w.chars().allMatch(Character::isLetter)) {
                continue;
            }
            int len = w.length();
            if (len <= EASY_MAX_LEN) {
                easyWords.add(w);
            } else if (len <= MEDIUM_MAX_LEN) {
                mediumWords.add(w);
            } else {
                hardWords.add(w);
            }
        }
    }

    /**
     * One random word from the pool for that difficulty (length band).
     */
    public String getRandomWord(DifficultyLevel difficulty) {
        Objects.requireNonNull(difficulty, "difficulty");
        List<String> pool =
                switch (difficulty) {
                    case EASY -> easyWords;
                    case MEDIUM -> mediumWords;
                    case HARD -> hardWords;
                };
        if (pool.isEmpty()) {
            throw new IllegalStateException("No words loaded for difficulty " + difficulty);
        }
        int i = ThreadLocalRandom.current().nextInt(pool.size());
        return pool.get(i);
    }

    public Path getWordFilePath() {
        return wordFilePath;
    }
}

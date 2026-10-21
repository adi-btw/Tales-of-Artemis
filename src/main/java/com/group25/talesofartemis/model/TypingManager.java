package com.group25.talesofartemis.model;

import java.util.Objects;

/**
 * The bit of the game that tracks the current target word, how far you've gotten, and running counts so
 * you can show WPM/accuracy or send to a profile later. Timer starts on the first real key after a word is
 * set.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public class TypingManager {

    /** What they're supposed to finish typing right now. */
    private String activeWord = "";
    /** Correct prefix built up so far. */
    private final StringBuilder typedSoFar = new StringBuilder();
    private int correctKeystrokes;
    private int incorrectKeystrokes;
    /** Set on first keystroke, used for WPM elapsed time. */
    private long startTimeMs;

    /** The full target word for this round. */
    public String getActiveWord() {
        return activeWord;
    }

    /** How much of that word is filled in so far. */
    public String getTypedSoFar() {
        return typedSoFar.toString();
    }

    /** Keys that advanced the word. */
    public int getCorrectKeystrokes() {
        return correctKeystrokes;
    }

    /** Keys that didn't match the next letter. */
    public int getIncorrectKeystrokes() {
        return incorrectKeystrokes;
    }

    /** When we started the clock, in milliseconds since the start of the program, zero means nobody's typed yet. */
    public long getStartTimeMs() {
        return startTimeMs;
    }

    /** Swap in a new target and wipe the progress and timer, called when the enemy word changes. */
    public void setActiveWord(String word) {
        Objects.requireNonNull(word, "word");
        String w = word.trim();
        if (w.isEmpty()) {
            throw new IllegalArgumentException("word must be non-blank");
        }
        this.activeWord = w;
        typedSoFar.setLength(0);
        startTimeMs = 0;
    }

    /**
     * Feed one character from the player. Right letter (case doesn't matter) appends and counts as good, wrong letter
     * only ticks the mistake counter. Returns whether that key was the one we wanted. First keypress starts the timer.
     */
    public boolean validateKeystroke(char key) {
        if (activeWord.isEmpty() || isWordComplete()) {
            return false;
        }
        if (startTimeMs == 0) {
            startTimeMs = System.currentTimeMillis();
        }
        int next = typedSoFar.length();
        char expected = activeWord.charAt(next);
        if (Character.toLowerCase(key) == Character.toLowerCase(expected)) {
            typedSoFar.append(expected);
            correctKeystrokes++;
            return true;
        }
        incorrectKeystrokes++;
        return false;
    }

    /** True once the buffer length matches the word, we only ever append on correct keys, so that's done. */
    public boolean isWordComplete() {
        return !activeWord.isEmpty() && typedSoFar.length() == activeWord.length();
    }

    /** Clear word, progress, both counters, and the clock, for a fresh run. */
    public void reset() {
        activeWord = "";
        typedSoFar.setLength(0);
        correctKeystrokes = 0;
        incorrectKeystrokes = 0;
        startTimeMs = 0;
    }

    /**
     * Calculate WPM by treating every five correct keystrokes as one word, divide by minutes since the first key.
     * Stays at zero until the timer has started.
     */
    public double calculateWPM() {
        if (startTimeMs == 0) {
            return 0.0;
        }
        double elapsedMin = (System.currentTimeMillis() - startTimeMs) / 60_000.0;
        if (elapsedMin <= 0) {
            return 0.0;
        }
        double words = correctKeystrokes / 5.0;
        return words / elapsedMin;
    }

    /** Percentage of keys that were hits vs misses. Before anything's been typed, we report 100 so you don't show 0/0. */
    public double calculateAccuracy() {
        int total = correctKeystrokes + incorrectKeystrokes;
        if (total == 0) {
            return 100.0;
        }
        return 100.0 * correctKeystrokes / total;
    }
}

package com.group25.talesofartemis;

import com.group25.talesofartemis.model.TypingManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TypingManagerTest {

    /**
     * Tests that a correct keystroke is accepted and appended to progress.
     */
    @Test
    public void testCorrectKeystroke() {
        TypingManager tm = new TypingManager();

        // Set active word
        tm.setActiveWord("cat");

        // Input correct character
        boolean result = tm.validateKeystroke('c');

        // Verify correct behavior
        assertTrue(result);
        assertEquals("c", tm.getTypedSoFar());
    }

    /**
     * Tests that an incorrect keystroke is rejected.
     */
    @Test
    public void testIncorrectKeystroke() {
        TypingManager tm = new TypingManager();

        tm.setActiveWord("cat");

        // Input incorrect character
        boolean result = tm.validateKeystroke('x');

        // Verify rejection
        assertFalse(result);
    }

    /**
     * Tests that a word is marked complete after all characters are typed.
     */
    @Test
    public void testWordComplete() {
        TypingManager tm = new TypingManager();

        tm.setActiveWord("cat");

        // Type full word
        tm.validateKeystroke('c');
        tm.validateKeystroke('a');
        tm.validateKeystroke('t');

        // Verify completion
        assertTrue(tm.isWordComplete());
    }

    /**
     * UT-03: Tests that setting an empty/blank word throws an appropriate exception.
     * This verifies the system handles invalid input gracefully (NFR-4).
     */
    @Test
    public void testEmptyWordRejectedSafely() {
        TypingManager tm = new TypingManager();

        // Setting an empty word should throw IllegalArgumentException, not silently corrupt state
        assertThrows(IllegalArgumentException.class, () -> tm.setActiveWord(""));
    }
}
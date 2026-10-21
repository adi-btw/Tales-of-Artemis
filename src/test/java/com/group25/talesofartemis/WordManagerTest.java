package com.group25.talesofartemis;

import com.group25.talesofartemis.model.WordManager;
import com.group25.talesofartemis.model.DifficultyLevel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WordManagerTest {

    /**
     * Tests that a random word is returned for a given difficulty level.
     */
    @Test
    public void testGetWord() throws Exception {
        WordManager wm = new WordManager();

        // Retrieve word for EASY difficulty
        String word = wm.getRandomWord(DifficultyLevel.EASY);

        // Verify word is not null
        assertNotNull(word);
    }
}
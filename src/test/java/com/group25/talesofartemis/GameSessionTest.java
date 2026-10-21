package com.group25.talesofartemis;

import com.group25.talesofartemis.game.GameSession;
import com.group25.talesofartemis.game.Level;
import com.group25.talesofartemis.model.DifficultyLevel;
import com.group25.talesofartemis.model.PlayerCharacter;
import com.group25.talesofartemis.model.WordManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GameSession (UT-04).
 * Verifies that game session state is managed correctly.
 */
public class GameSessionTest {

    /**
     * UT-04: Tests that a game session can be started and the current level is set.
     */
    @Test
    public void testGameSessionLevelSet() throws Exception {
        PlayerCharacter player = new PlayerCharacter(100, 300, 100);
        GameSession session = new GameSession(player, null);

        WordManager wm = new WordManager();
        Level level = new Level(DifficultyLevel.EASY, wm, 1200, 280);
        level.setChaseTarget(player);
        session.setCurrentLevel(level);

        // Verify that the level is correctly assigned to the session
        assertEquals(level, session.getCurrentLevel());
    }

    /**
     * Tests that word-to-action mapping returns a valid ActionType for a known keyword.
     */
    @Test
    public void testWordToActionMapping() throws Exception {
        PlayerCharacter player = new PlayerCharacter(100, 300, 100);
        GameSession session = new GameSession(player, null);

        WordManager wm = new WordManager();
        Level level = new Level(DifficultyLevel.EASY, wm, 1200, 280);
        level.setChaseTarget(player);
        session.setCurrentLevel(level);

        // "jump" is a known action keyword — should map to a non-null ActionType
        assertNotNull(session.mapWordToAction("jump"));
    }

    /**
     * Tests that a non-action word returns null from the word-to-action mapping.
     */
    @Test
    public void testNonActionWordReturnsNull() throws Exception {
        PlayerCharacter player = new PlayerCharacter(100, 300, 100);
        GameSession session = new GameSession(player, null);

        // A random non-action word should return null
        assertNull(session.mapWordToAction("elephant"));
    }
}

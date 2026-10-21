package com.group25.talesofartemis;

import com.group25.talesofartemis.game.Level;
import com.group25.talesofartemis.model.WordManager;
import com.group25.talesofartemis.model.DifficultyLevel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LevelTest {

    /**
     * Tests that a level is initialized with the correct difficulty.
     */
    @Test
    public void testLevelCreation() throws Exception {
        WordManager wm = new WordManager();

        // Create level
        Level level = new Level(DifficultyLevel.EASY, wm, 0, 0);

        // Verify difficulty is set correctly
        assertEquals(DifficultyLevel.EASY, level.getDifficulty());
    }

    /**
     * UT-06: Tests that a level reports cleared once all enemies are spawned and defeated.
     */
    @Test
    public void testLevelClearedAfterAllEnemiesDefeated() throws Exception {
        WordManager wm = new WordManager();
        Level level = new Level(DifficultyLevel.EASY, wm, 0, 0);

        com.group25.talesofartemis.model.PlayerCharacter player =
                new com.group25.talesofartemis.model.PlayerCharacter(0, 0, 100);
        level.setChaseTarget(player);

        // Spawn all enemies (EASY = 5 max spawns)
        while (level.getSpawnCount() < level.getMaxSpawns()) {
            level.spawnEnemy();
        }

        // Defeat all enemies by dealing massive damage
        for (com.group25.talesofartemis.model.Enemy e : level.getActiveEnemies()) {
            e.takeDamage(99999);
        }
        level.removeDefeatedEnemies();

        // Level should now be cleared
        assertTrue(level.isCleared());
    }
}
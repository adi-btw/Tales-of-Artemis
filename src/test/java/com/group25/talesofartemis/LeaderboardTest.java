package com.group25.talesofartemis;

import com.group25.talesofartemis.model.Leaderboard;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LeaderboardTest {

    /**
     * Tests that adding a score entry increases leaderboard size.
     */
    @Test
    public void testAddEntry() {
        Leaderboard lb = new Leaderboard();

        // Add entry
        lb.addEntry("user1", 100);

        // Verify entry added
        assertEquals(1, lb.getEntries().size());
    }
}
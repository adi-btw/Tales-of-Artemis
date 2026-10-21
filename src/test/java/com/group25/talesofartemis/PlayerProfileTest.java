package com.group25.talesofartemis;

import com.group25.talesofartemis.model.PlayerProfile;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerProfileTest {

    /**
     * Tests that password hashing and verification work correctly.
     */
    @Test
    public void testPasswordCheck() {
        // Generate hash for password
        String hash = PlayerProfile.hashPassword("password");

        // Create profile with hashed password
        PlayerProfile profile = new PlayerProfile(
                "user1", hash, false, 1, 0, 0, 0, 0, 0
        );

        // Verify correct password passes
        assertTrue(profile.checkPassword("password"));

        // Verify incorrect password fails
        assertFalse(profile.checkPassword("wrong"));
    }

    /**
     * Tests that player stats are updated correctly after a session.
     */
    @Test
    public void testUpdateStats() {
        String hash = PlayerProfile.hashPassword("password");

        PlayerProfile profile = new PlayerProfile(
                "user1", hash, false, 1, 0, 0, 0, 0, 0
        );

        // Update stats with session data
        profile.updateStats(100, 10, 50, 2, 2);

        // Verify values updated correctly
        assertEquals(100, profile.getTopScore());
        assertEquals(2, profile.getMaxLevelUnlocked());
    }
}
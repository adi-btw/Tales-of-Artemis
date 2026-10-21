package com.group25.talesofartemis;

import com.group25.talesofartemis.model.AccountManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AccountManagerTest {

    /** Generates a unique username per test run to avoid conflicts with existing saved data. */
    private String uniqueUser(String base) {
        return base + "_" + System.nanoTime();
    }

    /**
     * Tests that a new account can be created and successfully logged into.
     */
    @Test
    public void testCreateAccountAndLogin() throws Exception {
        AccountManager am = new AccountManager();
        String username = uniqueUser("acct_login");

        am.createAccount(username, "password");

        boolean result = am.login(username, "password");
        assertTrue(result);
    }

    /**
     * Tests that login fails when an incorrect password is provided.
     */
    @Test
    public void testLoginFailWrongPassword() throws Exception {
        AccountManager am = new AccountManager();
        String username = uniqueUser("acct_wrongpw");

        am.createAccount(username, "password");

        boolean result = am.login(username, "wrong");
        assertFalse(result);
    }

    /**
     * Tests that a user profile can be deleted successfully.
     */
    @Test
    public void testDeleteProfile() throws Exception {
        AccountManager am = new AccountManager();
        String username = uniqueUser("acct_delete");

        am.createAccount(username, "password");

        boolean deleted = am.deleteProfile(username);
        assertTrue(deleted);
    }
}

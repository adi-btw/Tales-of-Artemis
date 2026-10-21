package com.group25.talesofartemis;

import javax.swing.*;

import com.group25.talesofartemis.model.AccountManager;
import com.group25.talesofartemis.model.AudioManager;
import com.group25.talesofartemis.model.DifficultyLevel;
import com.group25.talesofartemis.model.PlayerProfile;

import java.awt.*;
import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * MainWindow is the primary JFrame for the Tales of Artemis game.
 *
 * <p>This class manages all UI screens using a CardLayout, including:
 * main menu, login/register, gameplay, tutorial, player hub,
 * parental controls, and high scores.</p>
 *
 * <p>It also handles:
 * screen navigation,
 * audio playback,
 * account management,
 * and integration with GameplayPanel.</p>
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */


public class MainWindow extends JFrame {

   private AccountManager accountManager;

    {
        try {
            accountManager = new AccountManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private CardLayout layout;
    private JPanel container;
    private GameplayPanel gameplay;
    private int tutorialPage = 0;
    private JLabel tutorialText;
    private JLabel pageIndicator;

    private Clip menuMusic;
    private final AudioManager audioManager = new AudioManager();
    
    /**
 * Plays looping background music for the main menu.
 *
 * <p>If the music clip has already been initialized, it resets
 * the playback position and resumes looping. Otherwise, it loads
 * the audio resource from "/menu.wav", initializes the clip,
 * and begins continuous playback.</p>
 *
 * <p>This method ensures only one menu music instance is active
 * at a time and prevents overlapping audio.</p>
 */

    private void playMenuMusic() {
        try {
            if (menuMusic != null) {
                menuMusic.stop();
                menuMusic.setFramePosition(0); 
                menuMusic.start();             
                menuMusic.loop(Clip.LOOP_CONTINUOUSLY);
                return;
            }

            AudioInputStream audioStream =
                AudioSystem.getAudioInputStream(getClass().getResource("/menu.wav"));

            menuMusic = AudioSystem.getClip();
            menuMusic.open(audioStream);
            menuMusic.start(); 
            menuMusic.loop(Clip.LOOP_CONTINUOUSLY);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
 * Constructs the main application window and initializes all UI screens,
 * layout management, event handlers, and background music.
 *
 * <p>This sets up the CardLayout container, links all panels together,
 * and configures gameplay callbacks such as game over and level completion.</p>
 */

    public MainWindow() {
        setTitle("Tales of Artemis");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        layout = new CardLayout();
        container = new JPanel(layout);

        // Add screens
        container.add(createMainMenu(), "MENU");
        container.add(createLoginScreen(), "LOGIN");
        container.add(createRegisterScreen(), "REGISTER");
        container.add(createTutorialScreen(), "TUTORIAL");
        container.add(createHighScoresScreen(), "SCORES");
        container.add(createParentalScreen(), "PARENTAL");
        gameplay = new GameplayPanel();
        gameplay.setAudioManager(audioManager);
        container.add(createPlayerHubScreen(), "PLAYER_HUB");
        container.add(gameplay, "GAME");
        GameOverPanel gameOverPanel = new GameOverPanel();
        container.add(gameOverPanel, "GAME_OVER");
        

        gameplay.setOnGameOver(() -> {
            gameplay.stopGame();
            audioManager.playGameOver();
            PlayerProfile profile = accountManager.getActiveProfile();
            if (profile != null) {
                // Game over = failed run: update top score but don't advance level progression
                profile.updateStats(gameplay.getScore(), gameplay.getPlayTimeMins(), (int) gameplay.getWpm(), 0, profile.getMaxLevelUnlocked());
                profile.setLastSessionAccuracy(gameplay.getAccuracy());
                try {
                    accountManager.saveProfilesToJSON();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                // Add to leaderboard
                accountManager.getLeaderboard().addEntry(
                    new com.group25.talesofartemis.model.ScoreEntry(
                        profile.getUsername(), gameplay.getScore(),
                        gameplay.getWpm(), profile.getMaxLevelUnlocked()));
                try {
                    accountManager.getLeaderboard().saveToJSON();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            gameOverPanel.setGameOver(gameplay.getScore());
            layout.show(container, "GAME_OVER");
        });

        gameplay.setOnLevelComplete(() -> {
            gameplay.stopGame();
            audioManager.playLevelComplete();
            PlayerProfile profile = accountManager.getActiveProfile();
            if (profile != null) {
                int levelNum;
                switch (gameplay.getDifficulty()) {
                    case MEDIUM: levelNum = 2; break;
                    case HARD:   levelNum = 3; break;
                    default:     levelNum = 1; break;
                }
                profile.updateStats(gameplay.getScore(), gameplay.getPlayTimeMins(), (int) gameplay.getWpm(), 0, levelNum);
                profile.setLastSessionAccuracy(gameplay.getAccuracy());
                try {
                    accountManager.saveProfilesToJSON();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                // Add to leaderboard
                accountManager.getLeaderboard().addEntry(
                    new com.group25.talesofartemis.model.ScoreEntry(
                        profile.getUsername(), gameplay.getScore(),
                        gameplay.getWpm(), levelNum));
                try {
                    accountManager.getLeaderboard().saveToJSON();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            gameOverPanel.setLevelComplete(gameplay.getScore());
            layout.show(container, "GAME_OVER");
        });

    gameOverPanel.setPlayerHubAction(e -> layout.show(container, "PLAYER_HUB"));
    gameOverPanel.setPlayAgainAction(e -> {
        gameplay.restartGame();
        layout.show(container, "GAME");
    });
        gameOverPanel.setHighScoresAction(e -> layout.show(container, "SCORES"));
        gameOverPanel.setMenuAction(e -> {
            playMenuMusic(); 
            layout.show(container, "MENU");
        });

        gameplay.setOnReturnToMenu(() -> {
             gameplay.stopGame(); 
            playMenuMusic(); 
            layout.show(container, "MENU");
        });

        add(createMuteBar(), BorderLayout.NORTH);
        add(container, BorderLayout.CENTER);
        setVisible(true);
        playMenuMusic();
    }

    /**
 * Creates the top mute control bar.
 * Allows toggling audio on/off and updates button text accordingly.
 *
 * @return the mute control panel
 */

    private JPanel createMuteBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        bar.setBackground(Color.BLACK);

        JButton muteBtn = new JButton("Mute");
        muteBtn.setForeground(Color.CYAN);
        muteBtn.setBackground(Color.BLACK);
        muteBtn.setFont(new Font("Serif", Font.BOLD, 13));
        muteBtn.setFocusable(false);

        muteBtn.addActionListener(e -> {
            boolean nowMuted = audioManager.toggleMute();
            muteBtn.setText(nowMuted ? "Unmute" : "Mute");
            if (menuMusic != null) {
                if (nowMuted) menuMusic.stop();
                else if (menuMusic.isOpen()) menuMusic.start();
            }
        });

        bar.add(muteBtn);
        return bar;
    }

     /**
     * Creates the main menu screen with navigation buttons.
     *
     * @return the main menu panel
     */

    private JPanel createMainMenu() {

    JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                ImageIcon bg = new ImageIcon(getClass().getResource("/space.jpg"));
                g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(20, 10, 20, 10);

        // ===== TITLE =====
        gbc.gridy = 0;

        JLabel title = new JLabel("Tales of Artemis", SwingConstants.CENTER);
        title.setFont(new Font("Monotype Corsiva", Font.BOLD, 64));
        title.setForeground(Color.CYAN);

        panel.add(title, gbc); //  YOU MISSED THIS

        // ===== BUTTON ROW =====
        gbc.gridy = 1;

        JPanel buttonRow = new JPanel();
        buttonRow.setOpaque(false);
        buttonRow.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));

        JButton startBtn = createStyledButton("Start Game");
        JButton loginBtn = createStyledButton("Register");
        JButton tutorialBtn = createStyledButton("Tutorial");
        JButton playerHubBtn = createStyledButton("Player Hub");
        JButton exitBtn = createStyledButton("Exit");
        JButton scoresBtn = createStyledButton("High Scores");

        JButton parentalBtn = createStyledButton("Parental Controls");
        parentalBtn.addActionListener(e -> layout.show(container, "PARENTAL"));

        // ACTIONS
        startBtn.addActionListener(e -> layout.show(container, "LOGIN"));
        loginBtn.addActionListener(e -> layout.show(container, "REGISTER"));
        tutorialBtn.addActionListener(e -> layout.show(container, "TUTORIAL"));
        playerHubBtn.addActionListener(e -> layout.show(container, "LOGIN"));
        exitBtn.addActionListener(e -> System.exit(0));
        scoresBtn.addActionListener(e -> layout.show(container, "SCORES"));
        

        // ADD BUTTONS
       JPanel buttonRow1 = new JPanel();
buttonRow1.setOpaque(false);
buttonRow1.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 10));

buttonRow1.add(startBtn);
buttonRow1.add(loginBtn);
buttonRow1.add(tutorialBtn);

JPanel buttonRow2 = new JPanel();
buttonRow2.setOpaque(false);
buttonRow2.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 10));

buttonRow2.add(playerHubBtn);
buttonRow2.add(scoresBtn);
buttonRow2.add(parentalBtn);
buttonRow2.add(exitBtn);

    // wrapper
    JPanel wrapper = new JPanel();
    wrapper.setOpaque(false);
    wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

    wrapper.add(buttonRow1);
    wrapper.add(buttonRow2);

    panel.add(wrapper, gbc);

        

        return panel;
    }

     /**
     * Creates a styled button with a custom appearance.
     *
     * @param text the text to display on the button
     * @return the styled button
     */

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);

        btn.setForeground(Color.CYAN);
        btn.setFont(new Font("Serif", Font.BOLD, 20));

       
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);

        return btn;
    }
    
    /**
 * Creates a placeholder game screen panel used for testing navigation.
 *
 * @return the game screen panel
 */
    
    private JPanel createGameScreen() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BorderLayout());
        

        JLabel label = new JLabel("Game Screen", SwingConstants.CENTER);
        label.setFont(new Font("Monotype Corsiva", Font.PLAIN, 42));
        label.setForeground(Color.CYAN);

        JButton backBtn = createStyledButton("Back to Menu");
        backBtn.addActionListener(e -> {
            layout.show(container, "MENU");
            playMenuMusic(); //  restart music
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.add(backBtn);

        panel.add(label, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

     /**
     * Creates the tutorial screen explaining game mechanics.
     *
     * @return the tutorial panel
     */
    
    private JPanel createTutorialScreen() {

        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bg = new ImageIcon(getClass().getResource("/space.jpg"));
                g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        panel.setLayout(new BorderLayout());

        // ===== TITLE =====
        JLabel title = new JLabel("Tutorial", SwingConstants.CENTER);
        title.setFont(new Font("Monotype Corsiva", Font.BOLD, 50));
        title.setForeground(Color.CYAN);

        panel.add(title, BorderLayout.NORTH);

        // ===== CENTER PANEL (TEXT + PAGE) =====
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        tutorialText = new JLabel("", SwingConstants.CENTER);
        tutorialText.setAlignmentX(Component.CENTER_ALIGNMENT);
        tutorialText.setFont(new Font("Serif", Font.BOLD, 20));
        tutorialText.setForeground(Color.CYAN);

        pageIndicator = new JLabel("", SwingConstants.CENTER);
        pageIndicator.setAlignmentX(Component.CENTER_ALIGNMENT);
        pageIndicator.setFont(new Font("Serif", Font.BOLD, 16));
        pageIndicator.setForeground(Color.CYAN);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(tutorialText);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(pageIndicator);
        centerPanel.add(Box.createVerticalGlue());

        panel.add(centerPanel, BorderLayout.CENTER);

        // ===== BUTTONS =====
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);

        JButton prevBtn = createStyledButton("Prev");
        JButton nextBtn = createStyledButton("Next");
        JButton backBtn = createStyledButton("Back");

        prevBtn.addActionListener(e -> {
            if (tutorialPage > 0) {
                tutorialPage--;
                updateTutorialText();
            }
        });

        nextBtn.addActionListener(e -> {
            if (tutorialPage < 3) {
                tutorialPage++;
                updateTutorialText();
            }
        });

        backBtn.addActionListener(e -> layout.show(container, "MENU"));

        buttonPanel.add(prevBtn);
        buttonPanel.add(nextBtn);
        buttonPanel.add(backBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        updateTutorialText(); //  VERY IMPORTANT

        return panel;
    }

    /**
 * Updates the tutorial text and page indicator
 * based on the current tutorial page.
 */

    private void updateTutorialText() {

        String text = "";

        switch (tutorialPage) {
            case 0:
                text = "<html><center><h2>Movement</h2>Use keyboard to move.<br>Type fast to react.</center></html>";
                break;
            case 1:
                text = "<html><center><h2>Attack</h2>Type words to attack enemies.<br>Speed = power.</center></html>";
                break;
            case 2:
                text = "<html><center><h2>Powerups</h2>Collect boosts for health and speed.</center></html>";
                break;
            case 3:
                text = "<html><center><h2>Objective</h2>Survive and reach high scores.</center></html>";
                break;
        }

        tutorialText.setText(text);

        //  PAGE INDICATOR
        pageIndicator.setText("Page " + (tutorialPage + 1) + " / 4");
    }
    
     /**
     * Creates the login screen where users can enter credentials.
     *
     * @return the login panel
     */
    
    private JPanel createLoginScreen() {

        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                ImageIcon bg = new ImageIcon(getClass().getResource("/space.jpg"));
                g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        // ===== TITLE =====
        gbc.gridy = 0;
        JLabel title = new JLabel("Login", SwingConstants.CENTER);
        title.setFont(new Font("Monotype Corsiva", Font.BOLD, 48));
        title.setForeground(Color.CYAN);
        panel.add(title, gbc);

        // ===== USERNAME =====
        gbc.gridy = 1;
        JTextField usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        // ===== PASSWORD =====
        gbc.gridy = 2;
        JPasswordField passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        // ===== BUTTONS =====
        gbc.gridy = 3;

        JPanel buttonRow = new JPanel();
        buttonRow.setOpaque(false);

        JButton loginBtn = createStyledButton("Login");
        JButton registerBtn = createStyledButton("Register"); //  added (matches docs)
        JButton backBtn = createStyledButton("Back");

        buttonRow.add(loginBtn);
        buttonRow.add(registerBtn);
        buttonRow.add(backBtn);

        // ===== LOGIN ACTION =====
        loginBtn.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());

            if (accountManager.login(user, pass)) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                layout.show(container, "PLAYER_HUB");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid login");
            }
        });

        // ===== REGISTER ACTION =====
       registerBtn.addActionListener(e -> layout.show(container, "REGISTER"));

        // ===== BACK =====
        backBtn.addActionListener(e -> layout.show(container, "MENU"));

        panel.add(buttonRow, gbc);

        // Clear fields every time this panel is shown
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                usernameField.setText("");
                passwordField.setText("");
            }
        });

        return panel;
    }
    
     /**
     * Creates the account registration screen.
     *
     * @return the register panel
     */
    
    private JPanel createRegisterScreen() {

    JPanel panel = new JPanel() {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            ImageIcon bg = new ImageIcon(getClass().getResource("/space.jpg"));
            g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
    };

    panel.setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.insets = new Insets(10, 10, 10, 10);

    // TITLE
    gbc.gridy = 0;
    JLabel title = new JLabel("Register", SwingConstants.CENTER);
    title.setFont(new Font("Monotype Corsiva", Font.BOLD, 48));
    title.setForeground(Color.CYAN);
    panel.add(title, gbc);

    // USERNAME
    gbc.gridy = 1;
    JTextField usernameField = new JTextField(15);
    panel.add(usernameField, gbc);

    // PASSWORD
    gbc.gridy = 2;
    JPasswordField passwordField = new JPasswordField(15);
    panel.add(passwordField, gbc);

    // BUTTONS
    gbc.gridy = 3;
    JPanel buttonRow = new JPanel();
    buttonRow.setOpaque(false);

    JButton registerBtn = createStyledButton("Register");
    JButton backBtn = createStyledButton("Back");

    buttonRow.add(registerBtn);
    buttonRow.add(backBtn);

        // REGISTER ACTION
        registerBtn.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());

            try {
                accountManager.createAccount(user, pass);
                accountManager.saveProfilesToJSON();

                JOptionPane.showMessageDialog(this, "Account created!");
                layout.show(container, "LOGIN");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        // BACK BUTTON
        backBtn.addActionListener(e -> layout.show(container, "LOGIN"));

        panel.add(buttonRow, gbc);

        // Clear fields every time this panel is shown
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                usernameField.setText("");
                passwordField.setText("");
            }
        });

        return panel;
    }

    /**
     * Creates the high scores leaderboard screen.
     *
     * @return the high scores panel
     */
    
    private JPanel createHighScoresScreen() {

        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bg = new ImageIcon(getClass().getResource("/space.jpg"));
                g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        panel.setLayout(new BorderLayout());

        // ===== TITLE =====
        JLabel title = new JLabel("High Scores", SwingConstants.CENTER);
        title.setFont(new Font("Monotype Corsiva", Font.BOLD, 50));
        title.setForeground(Color.CYAN);
        panel.add(title, BorderLayout.NORTH);

        // ===== TABLE =====
        String[] columns = {"Rank", "Player", "Score", "Level"};

        javax.swing.table.DefaultTableModel tableModel =
                new javax.swing.table.DefaultTableModel(getLeaderboardData(), columns) {
                    @Override public boolean isCellEditable(int r, int c) { return false; }
                };

        JTable table = new JTable(tableModel) {
            //  highlight top 3
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (row == 0) c.setForeground(Color.YELLOW);
                else if (row == 1) c.setForeground(Color.LIGHT_GRAY);
                else if (row == 2) c.setForeground(Color.ORANGE);
                else c.setForeground(Color.CYAN);
                c.setBackground(Color.BLACK);
                return c;
            }
        };

        table.setFont(new Font("Serif", Font.BOLD, 16));
        table.setRowHeight(30);
        table.setBackground(Color.BLACK);
        table.setGridColor(Color.CYAN);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        panel.add(scrollPane, BorderLayout.CENTER);

        // ===== BACK BUTTON =====
        JButton backBtn = createStyledButton("Back");
        backBtn.addActionListener(e -> layout.show(container, "MENU"));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(backBtn);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Refresh table data every time this screen is shown
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                tableModel.setRowCount(0);
                for (Object[] row : getLeaderboardData()) {
                    tableModel.addRow(row);
                }
            }
        });

        return panel;
    }


    /**
 * Retrieves and sorts leaderboard data from player profiles.
 *
 * @return a 2D array of leaderboard entries (rank, username, score, level)
 */

    private Object[][] getLeaderboardData() {
        List<com.group25.talesofartemis.model.ScoreEntry> entries =
                accountManager.getLeaderboard().getEntries();
        List<Object[]> rows = new ArrayList<>();
        int rank = 1;
        for (com.group25.talesofartemis.model.ScoreEntry e : entries) {
            rows.add(new Object[]{rank++, e.getUsername(), e.getScore(), e.getLevelReached()});
        }
        return rows.toArray(new Object[0][]);
    }
    
     /**
     * Creates the parental control panel for admin functionality.
     *
     * @return the parental control panel
     */
    
    private JPanel createParentalScreen() {

        JPanel panel = new JPanel(new CardLayout());
        CardLayout innerLayout = (CardLayout) panel.getLayout();

        // ===== LOGIN PANEL =====
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Parental Access");
        title.setForeground(Color.CYAN);
        title.setFont(new Font("Monotype Corsiva", Font.BOLD, 48));

        gbc.gridy = 0;
        loginPanel.add(title, gbc);

        gbc.gridy = 1;
        JPasswordField passwordField = new JPasswordField(15);
        loginPanel.add(passwordField, gbc);

        gbc.gridy = 2;
        JButton enterBtn = createStyledButton("Enter");
        JButton backBtn = createStyledButton("Back");

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.add(enterBtn);
        row.add(backBtn);

        loginPanel.add(row, gbc);

        // ===== ADMIN DASHBOARD =====
        JPanel adminPanel = new JPanel(new BorderLayout());
        adminPanel.setBackground(Color.BLACK);

        JLabel adminTitle = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        adminTitle.setForeground(Color.CYAN);
        adminTitle.setFont(new Font("Monotype Corsiva", Font.BOLD, 50));

        adminPanel.add(adminTitle, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(4, 1, 15, 15));
        actions.setOpaque(false);

        JButton viewUsersBtn = createStyledButton("View Users");
        JButton createUserBtn = createStyledButton("Create Account");
        JButton resetPassBtn = createStyledButton("Reset Password");

        JButton manageBtn = createStyledButton("Delete User"); // renamed cleaner
        JButton wipeStatsBtn = createStyledButton("Wipe Statistics");
        JButton resetLeaderboardBtn = createStyledButton("Reset Leaderboard");
        JButton saveBtn = createStyledButton("Save Changes");

        // ===== BUTTON LOGIC =====

        // VIEW USERS
        viewUsersBtn.addActionListener(e -> {
            java.util.List<PlayerProfile> profiles = accountManager.getAllProfiles();

            StringBuilder users = new StringBuilder();

            for (PlayerProfile p : profiles) {
                users.append(p.getUsername())
                    .append(" | Score: ").append(p.getTopScore())
                    .append(" | Level: ").append(p.getMaxLevelUnlocked())
                    .append("\n");
            }

            JOptionPane.showMessageDialog(this, users.toString(), "Users", JOptionPane.INFORMATION_MESSAGE);
        });

        // CREATE ACCOUNT
        createUserBtn.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(this, "Enter username:");
            String password = JOptionPane.showInputDialog(this, "Enter password:");

            if (username != null && password != null) {
                try {
                    accountManager.createAccount(username, password);
                    
                    PlayerProfile newUser = accountManager.getAllProfiles()
                        .get(accountManager.getAllProfiles().size() - 1);


                    newUser.setParent(true);

                    JOptionPane.showMessageDialog(this, "Admin account created!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }
        });

        // RESET PASSWORD
        resetPassBtn.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(this, "Enter username:");
            String newPassword = JOptionPane.showInputDialog(this, "Enter new password:");

            if (username != null && newPassword != null) {
                try {
                    accountManager.resetPassword(username, newPassword);
                    JOptionPane.showMessageDialog(this, "Password reset!");
                } catch (Exception ex) {
                 JOptionPane.showMessageDialog(this, ex.getMessage());
             }
            }
        });

        actions.add(viewUsersBtn);
        actions.add(createUserBtn);
        actions.add(resetPassBtn);
        actions.add(manageBtn);
        actions.add(wipeStatsBtn);
        actions.add(resetLeaderboardBtn);
        

        adminPanel.add(actions, BorderLayout.CENTER);

        JButton backAdminBtn = createStyledButton("Back");
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backAdminBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottom.add(saveBtn);
        bottom.add(Box.createRigidArea(new Dimension(0, 10)));
        bottom.add(backAdminBtn);

        adminPanel.add(bottom, BorderLayout.SOUTH);

        // ===== LOGIN LOGIC =====
        enterBtn.addActionListener(e -> {
        
        String input = new String(passwordField.getPassword());
        PlayerProfile active = accountManager.getActiveProfile();

        boolean isDefaultAdmin = input.equals("Parent123!");
        boolean isAdminUser = active != null && active.isParent() && active.checkPassword(input);

        if (isDefaultAdmin || isAdminUser) {
            JOptionPane.showMessageDialog(this, "Admin dashboard displayed.");
            innerLayout.show(panel, "ADMIN");
        } else {
            JOptionPane.showMessageDialog(this, "Access denied");
        }

           
        });

        backBtn.addActionListener(e -> layout.show(container, "MENU"));
        backAdminBtn.addActionListener(e -> innerLayout.show(panel, "LOGIN"));

        // ===== MANAGE USERS (DELETE FEATURE) =====
        manageBtn.addActionListener(e -> {

            StringBuilder users = new StringBuilder();
            for (PlayerProfile p : accountManager.getAllProfiles()) {
                users.append(p.getUsername()).append("\n");
            }

            String userToDelete = JOptionPane.showInputDialog(
                this,
                "Users:\n" + users + "\nEnter username to delete:"
            );

            if (userToDelete != null && !userToDelete.isEmpty()) {
                boolean removed = accountManager.deleteProfile(userToDelete);

                if (removed) {
                    // Also remove all leaderboard entries for the deleted user
                    accountManager.getLeaderboard().removeByUsername(userToDelete);
                    try {
                        accountManager.getLeaderboard().saveToJSON();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(this, "User removed.");
                } else {
                    JOptionPane.showMessageDialog(this, "User not found.");
                }
            }
        });

        // ===== WIPE STATS =====
        wipeStatsBtn.addActionListener(e -> {
            for (PlayerProfile p : accountManager.getAllProfiles()) {
                p.resetStats();
            }
            JOptionPane.showMessageDialog(this, "All player statistics wiped.");
        });

        // ===== RESET LEADERBOARD =====
        resetLeaderboardBtn.addActionListener(e -> {
            accountManager.getLeaderboard().clear();
            try {
                accountManager.getLeaderboard().saveToJSON();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Leaderboard reset.");
        });

        // ===== SAVE (WRITE TO FILE) =====
        saveBtn.addActionListener(e -> {
            try {
                accountManager.saveProfilesToJSON();
                JOptionPane.showMessageDialog(this, "System updated stored files.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ===== ADD PANELS =====
        panel.add(loginPanel, "LOGIN");
        panel.add(adminPanel, "ADMIN");

        // Clear the password field and return to the login sub-panel every time this screen is shown
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                passwordField.setText("");
                ((java.awt.CardLayout) panel.getLayout()).show(panel, "LOGIN");
            }
        });

        return panel;
    }
    
     /**
     * Creates the player hub displaying stats and level selection.
     *
     * @return the player hub panel
     */
    
    private JPanel createPlayerHubScreen() {

        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bg = new ImageIcon(getClass().getResource("/space.jpg"));
                g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        panel.setLayout(new BorderLayout());

        // ===== TITLE =====
        JLabel title = new JLabel("Player Hub", SwingConstants.CENTER);
        title.setFont(new Font("Monotype Corsiva", Font.BOLD, 50));
        title.setForeground(Color.CYAN);

        panel.add(title, BorderLayout.NORTH);

        // ===== CENTER (STATS) =====
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.setOpaque(false);

        statsPanel.add(Box.createVerticalGlue());
        

        JLabel usernameLabel  = new JLabel();
        JLabel wpmLatestLabel = new JLabel();
        JLabel wpmPeakLabel   = new JLabel();
        JLabel wpmAvgLabel    = new JLabel();
        JLabel accuracyLabel  = new JLabel();
        JLabel scoreLabel     = new JLabel();
        JLabel levelLabel     = new JLabel();

        Font font = new Font("Serif", Font.BOLD, 20);

        for (JLabel lbl : new JLabel[]{usernameLabel, wpmLatestLabel, wpmPeakLabel, wpmAvgLabel,
                                       accuracyLabel, scoreLabel, levelLabel}) {
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(font);
            lbl.setForeground(Color.CYAN);
            statsPanel.add(lbl);
        }

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        centerWrapper.add(statsPanel); // centers everything perfectly

        panel.add(centerWrapper, BorderLayout.CENTER);

        // ===== BUTTONS =====
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);

        JButton easyBtn   = createStyledButton("Easy");
        JButton mediumBtn = createStyledButton("Medium");
        JButton hardBtn   = createStyledButton("Hard");
        JButton logoutBtn = createStyledButton("Log Out");

        // Medium and Hard start disabled until the player's progression unlocks them
        mediumBtn.setEnabled(false);
        hardBtn.setEnabled(false);

        buttonPanel.add(easyBtn);
        buttonPanel.add(mediumBtn);
        buttonPanel.add(hardBtn);
        buttonPanel.add(logoutBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // ===== UPDATE STATS + BUTTON STATES WHEN OPENED =====
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {

                PlayerProfile p = accountManager.getActiveProfile();

                if (p != null) {
                    int unlocked = p.getMaxLevelUnlocked();
                    usernameLabel.setText("Player: " + p.getUsername());
                    wpmLatestLabel.setText("WPM (Latest): " + (int) p.getLastSessionWpm());
                    wpmPeakLabel.setText("WPM (Peak): " + (int) p.getPeakWPM());
                    wpmAvgLabel.setText(String.format("WPM (Avg): %.0f", p.getAverageWPM()));
                    accuracyLabel.setText(String.format("Accuracy: %.0f%%", p.getLastSessionAccuracy()));
                    scoreLabel.setText("High Score: " + p.getTopScore());
                    String worldsStr = unlocked >= 2 ? "Easy, Medium & Hard"
                                     : unlocked >= 1 ? "Easy & Medium"
                                                     : "Easy only";
                    levelLabel.setText("Worlds Unlocked: " + worldsStr);
                    easyBtn.setEnabled(true);
                    mediumBtn.setEnabled(unlocked >= 1);
                    hardBtn.setEnabled(unlocked >= 2);
                }
            }
        });

        // ===== BUTTON ACTIONS =====
        easyBtn.addActionListener(e -> {
            gameplay.setDifficulty(DifficultyLevel.EASY);

             if (menuMusic != null && menuMusic.isRunning()) {
                menuMusic.stop(); 
            }
            layout.show(container, "GAME");
        });

        mediumBtn.addActionListener(e -> {
            gameplay.setDifficulty(DifficultyLevel.MEDIUM);
             if (menuMusic != null && menuMusic.isRunning()) {
                menuMusic.stop(); 
            }

            layout.show(container, "GAME");
        });

        hardBtn.addActionListener(e -> {
            gameplay.setDifficulty(DifficultyLevel.HARD);

             if (menuMusic != null && menuMusic.isRunning()) {
                menuMusic.stop(); 
            }
            layout.show(container, "GAME");
        });

        logoutBtn.addActionListener(e -> {
            accountManager.logout();
            playMenuMusic();
            layout.show(container, "MENU");
        });

        return panel;
    }
    

}
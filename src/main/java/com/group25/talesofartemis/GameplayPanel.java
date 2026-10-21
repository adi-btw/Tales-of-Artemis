package com.group25.talesofartemis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import com.group25.talesofartemis.game.GameSession;
import com.group25.talesofartemis.game.Level;
import com.group25.talesofartemis.model.ActionType;
import com.group25.talesofartemis.model.AudioManager;
import com.group25.talesofartemis.model.DifficultyLevel;
import com.group25.talesofartemis.model.Enemy;
import com.group25.talesofartemis.model.HealthPowerup;
import com.group25.talesofartemis.model.PlayerCharacter;
import com.group25.talesofartemis.model.Powerup;
import com.group25.talesofartemis.model.SlowTimePowerup;
import com.group25.talesofartemis.model.TypingManager;
import com.group25.talesofartemis.model.WordManager;
import java.io.IOException;
import java.util.Random;

/**
 * The GameplayPanel class manages the main gameplay screen for Tales of Artemis.
 *
 * <p>This panel is responsible for rendering the player, enemies, power-ups,
 * HUD elements, typing interactions, action-word effects, pause functionality,
 * and game progression such as level completion or game over.</p>
 *
 * <p>It integrates with the game model layer including:
 * GameSession, Level, PlayerCharacter, Enemy, Powerup,
 * TypingManager, and AudioManager.</p>
 *
 * <p>The panel also handles:
 * keyboard input,
 * collision detection,
 * word-based attacks,
 * score tracking,
 * health updates,
 * WPM and accuracy calculation,
 * and special action-word visual feedback.</p>
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */

public class GameplayPanel extends JPanel {

    // GAME STATE
    private int playerX = 100;
    private int playerY = 300;

    private int health = 100;
    private int score = 0;
    private int level = 1;
    private boolean gameStarted = false;

    private String currentWord = "attack";
    private String typed = "";

    private java.util.List<Point> enemies = new ArrayList<>();
    private java.util.List<Point> powerups = new ArrayList<>();

    private Timer gameLoop;
    private Runnable onGameOver;
    private Runnable onReturnToMenu;
    private Runnable onLevelComplete;
    

    private DifficultyLevel selectedDifficulty = DifficultyLevel.EASY;

    // ISSUE 54 (flash)
    private boolean flashRed = false;
    private int flashTimer = 0;

    // ISSUE 55 (pause)
    private boolean isPaused = false;
    private JPanel pauseOverlay;

    // ISSUE 56 (KeyListener --> GameSession)
    private GameSession session;

    // Mute button
    private AudioManager audioManager = new AudioManager();

    // Model layer — Level, player, and enemy targeting (issues #57, #58)
    private Level gameLevel;
    private Enemy targetEnemy;
    private PlayerCharacter playerChar;

    // Typing stats (issue #69)
    private final TypingManager typingManager = new TypingManager();
    private double lastWpm = 0.0;
    private long gameStartMs = 0;

    // Powerup spawning (issue #68)
    private static final Random RNG = new Random();

    // Y coordinate the player rests on; PlayerCharacter physics are clamped here (issue #65)
    private static final int GROUND_Y = 350 - 70;

    // Action-word visual feedback: label shown above the player for ~40 frames (issue #65)
    private String activeActionLabel = null;
    private int actionLabelTimer = 0;
    private Color actionColor = null;
    private int actionFlashTimer = 0;

    // UI
    private JLabel scoreLabel;
    private JLabel levelLabel;
    private JLabel hpLabel;
    private JLabel wpmLabel;
    private JLabel accuracyLabel;
    private JProgressBar healthBar;

    private JPanel gameWorld;

    // IMAGES
    private Image playerImg;
    private Image enemyImg;
    private Image healthPowerImg;
    private Image slowtimePowerImg;

    public GameplayPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        playerImg = new ImageIcon(getClass().getResource("/player.png")).getImage();
        enemyImg = new ImageIcon(getClass().getResource("/enemy.png")).getImage();
        healthPowerImg = new ImageIcon(getClass().getResource("/healthpowerup.png")).getImage();
        slowtimePowerImg = new ImageIcon(getClass().getResource("/slowtimepowerup.png")).getImage();

        add(createHUD(), BorderLayout.NORTH);
        add(createGameWorld(), BorderLayout.CENTER);
        add(createControlsBar(), BorderLayout.SOUTH);

        initLevel(DifficultyLevel.EASY);
        if (gameLevel == null) spawnEntities(); // fallback if WordManager fails
        setupInput();
        

        setFocusable(true);
        gameStarted = false;
    }

    /**
 * Creates the heads-up display (HUD) shown at the top of the gameplay screen.
 *
 * <p>The HUD displays player health (with a progress bar),
 * score, level, words per minute (WPM), and typing accuracy.
 * It is divided into left, center, and right sections for clarity.</p>
 *
 * @return the HUD panel containing all player statistics
 */

    private JPanel createHUD() {
        JPanel hud = new JPanel(new BorderLayout());
        hud.setBackground(Color.BLACK);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        left.setOpaque(false);

        JLabel hpIcon = new JLabel("❤ HP");
        hpIcon.setForeground(new Color(220, 50, 50));
        hpIcon.setFont(new Font("Serif", Font.BOLD, 14));

        healthBar = new JProgressBar(0, 100) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // background
                g2.setColor(new Color(50, 50, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // fill
                int fillW = (int)(getWidth() * getValue() / (double) getMaximum());
                g2.setColor(getForeground());
                g2.fillRoundRect(0, 0, fillW, getHeight(), 10, 10);
                // border
                g2.setColor(new Color(180, 180, 180, 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        healthBar.setValue(health);
        healthBar.setPreferredSize(new Dimension(180, 18));
        healthBar.setForeground(new Color(50, 200, 80));
        healthBar.setBorderPainted(false);
        healthBar.setOpaque(false);

        hpLabel = new JLabel("100%");
        hpLabel.setForeground(Color.WHITE);
        hpLabel.setFont(new Font("Serif", Font.BOLD, 13));

        left.add(hpIcon);
        left.add(healthBar);
        left.add(hpLabel);

        JPanel center = new JPanel();
        center.setOpaque(false);
        wpmLabel = createLabel("WPM: 0");
        accuracyLabel = createLabel("Acc: 100%");
        center.add(wpmLabel);
        center.add(accuracyLabel);

        JPanel right = new JPanel(new GridLayout(2, 1));
        right.setOpaque(false);

        scoreLabel = createLabel("Score: 0");
        levelLabel = createLabel("Level: Easy");

        right.add(scoreLabel);
        right.add(levelLabel);

        hud.add(left, BorderLayout.WEST);
        hud.add(center, BorderLayout.CENTER);
        hud.add(right, BorderLayout.EAST);

        return hud;
    }

    /**
 * Creates the main game rendering panel where all gameplay visuals are drawn.
 *
 * <p>This panel handles rendering of the player, enemies, powerups,
 * ground platform, action-word feedback, and screen effects such as
 * flashes and highlights. It also overlays the pause menu when active.</p>
 *
 * @return a layered panel containing the game world and pause overlay
 */

    private JPanel createGameWorld() {

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new OverlayLayout(wrapper));

        gameWorld = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                g.setColor(Color.GRAY);
                g.fillRect(0, 350, getWidth(), 20);

                g.drawImage(playerImg, playerX, playerY, 70, 70, null);

                // Action-word label: colored pill + white text above the player
                if (activeActionLabel != null && actionLabelTimer > 0 && actionColor != null) {
                    g.setFont(new Font("Arial", Font.BOLD, 28));
                    FontMetrics afm = g.getFontMetrics();
                    int lw = afm.stringWidth(activeActionLabel);
                    int lx = playerX + 35 - lw / 2;
                    int ly = playerY - 20;
                    int pad = 8;
                    g.setColor(new Color(actionColor.getRed(), actionColor.getGreen(), actionColor.getBlue(), 210));
                    g.fillRoundRect(lx - pad, ly - afm.getAscent() - pad / 2, lw + pad * 2, afm.getAscent() + afm.getDescent() + pad, 14, 14);
                    g.setColor(Color.WHITE);
                    g.drawString(activeActionLabel, lx, ly);
                }

                g.setFont(new Font("Arial", Font.BOLD, 18));

                if (gameLevel != null) {
                    for (Enemy e : gameLevel.getActiveEnemies()) {
                        int ex = (int) e.getX();
                        int ey = (int) e.getY();
                        g.drawImage(enemyImg, ex, ey, 90, 70, null);

                        FontMetrics fm = g.getFontMetrics();
                        String word = e.getAssignedWord();
                        int textWidth = fm.stringWidth(word);
                        int wordX = ex + (70 - textWidth) / 2;
                        int wordY = ey - 15;

                        int shakeOffset = flashRed ? (int)(Math.random() * 6 - 3) : 0;
                        String typedProgress = (e == targetEnemy) ? typed : "";
                        drawHighlightedWord(g, word, typedProgress, wordX + shakeOffset, wordY);
                    }
                } else {
                    for (Point e : enemies) {
                        g.drawImage(enemyImg, e.x, e.y, 70, 70, null);

                        FontMetrics fm = g.getFontMetrics();
                        int textWidth = fm.stringWidth(currentWord);
                        int wordX = e.x + (70 - textWidth) / 2;
                        int wordY = e.y - 15;
                        int shakeOffset = flashRed ? (int)(Math.random() * 6 - 3) : 0;
                        drawHighlightedWord(g, currentWord, typed, wordX + shakeOffset, wordY);
                    }
                }

                if (gameLevel != null) {
                    for (Powerup p : gameLevel.getActivePowerups()) {
                        Image img = (p instanceof SlowTimePowerup) ? slowtimePowerImg : healthPowerImg;
                        g.drawImage(img, (int) p.getX(), (int) p.getY(), 40, 40, null);
                    }
                } else {
                    for (Point p : powerups) {
                        g.drawImage(healthPowerImg, p.x, p.y, 40, 40, null);
                    }
                }

                // Screen flash: brief colored tint that fades over ~15 frames when an action fires
                if (actionFlashTimer > 0 && actionColor != null) {
                    int alpha = (int)(80.0 * actionFlashTimer / 15);
                    g.setColor(new Color(actionColor.getRed(), actionColor.getGreen(), actionColor.getBlue(), alpha));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        gameWorld.setOpaque(false);

        createPauseOverlay();

        wrapper.add(pauseOverlay);
        wrapper.add(gameWorld);

        return wrapper;
    }

    // ISSUE 53 + 54
    private void drawHighlightedWord(Graphics g, String word, String typed, int x, int y) {

        FontMetrics fm = g.getFontMetrics();
        int startX = x;

        boolean isMistake = flashRed;

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);

            if (isMistake) g.setColor(Color.RED);
            else if (i < typed.length()) g.setColor(Color.GREEN);
            else g.setColor(Color.WHITE);

            g.drawString(String.valueOf(c), startX, y);
            startX += fm.stringWidth(String.valueOf(c));
        }
    }

    /**
 * Creates the pause overlay displayed when the game is paused.
 *
 * <p>The overlay darkens the screen and shows options to resume
 * the game or return to the main menu.</p>
 */

    private void createPauseOverlay() {
        pauseOverlay = new JPanel();
        pauseOverlay.setLayout(new BoxLayout(pauseOverlay, BoxLayout.Y_AXIS));
        pauseOverlay.setBackground(new Color(0, 0, 0, 200));
        pauseOverlay.setVisible(false);

        JLabel title = new JLabel("Paused");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.CYAN);
        title.setFont(new Font("Monotype Corsiva", Font.BOLD, 50));

        JButton resume = createPauseButton("Resume");
        JButton menu = createPauseButton("Main Menu");

        resume.addActionListener(e -> togglePause());

        menu.addActionListener(e -> {
            togglePause();
            if (onReturnToMenu != null) {
                onReturnToMenu.run();
            }
        });

        pauseOverlay.add(Box.createVerticalGlue());
        pauseOverlay.add(title);
        pauseOverlay.add(Box.createRigidArea(new Dimension(0, 20)));
        pauseOverlay.add(resume);
        pauseOverlay.add(menu);
        pauseOverlay.add(Box.createVerticalGlue());
    }

    /**
 * Creates a styled button used within the pause overlay.
 *
 * @param text the label displayed on the button
 * @return the styled pause menu button
 */

    private JButton createPauseButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.CYAN);
        btn.setBackground(Color.BLACK);
        btn.setFont(new Font("Serif", Font.BOLD, 18));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    /**
 * Creates the control bar displayed at the bottom of the screen.
 *
 * <p>This includes gameplay hints and a pause button that allows
 * the player to pause and resume the game.</p>
 *
 * @return the controls panel
 */

    private JPanel createControlsBar() {
        JPanel controls = new JPanel();
        controls.setBackground(Color.BLACK);

        JLabel hint = new JLabel("Type words to attack • ← → move");
        hint.setForeground(Color.CYAN);

        JButton pauseBtn = new JButton("Pause");
        pauseBtn.setForeground(Color.CYAN);
        pauseBtn.setBackground(Color.BLACK);

        pauseBtn.addActionListener(e -> togglePause());

        controls.add(hint);
        controls.add(pauseBtn);

        return controls;
    }

    /**
 * Toggles the pause state of the game.
 *
 * <p>When paused, the game loop is stopped and the pause overlay
 * is displayed. When resumed, the game loop continues execution.</p>
 */

    private void togglePause() {
        isPaused = !isPaused;
        pauseOverlay.setVisible(isPaused);

        if (isPaused) gameLoop.stop();
        else gameLoop.start();
    }

    /**
 * Creates a styled label used throughout the HUD.
 *
 * @param text the text to display
 * @return the styled JLabel
 */

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.CYAN);
        label.setFont(new Font("Serif", Font.BOLD, 16));
        return label;
    }

    /**
     * Connects the gameplay panel to a live game session so that keystrokes are forwarded to
     * GameSession.processKeystroke(char) in addition to visually as well.
     * @param session the active game session
     */
    public void setGameSession(GameSession session) {
        this.session = session;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    /**
     * Creates a fresh Level with a WordManager and spawns the first enemy. Falls back to null on I/O error.
     * @param difficulty the difficulty level to build the new Level with, which determines enemy spawn count, powerup strength, and word lengths
     */
    private void initLevel(DifficultyLevel difficulty) {
        try {
            gameStartMs = System.currentTimeMillis();
            WordManager wordManager = new WordManager();
            playerChar = new PlayerCharacter(playerX, playerY, 100);
            health = 100;
            int spawnX = Math.max(getWidth(), 1200) + 100;
            gameLevel = new Level(difficulty, wordManager, spawnX, 280);
            gameLevel.setChaseTarget(playerChar);
            gameLevel.spawnEnemy();
            typed = "";
            targetEnemy = null;
            // Build a local session so powerup effects (heal, slow) route through GameSession
            session = new GameSession(playerChar, null);
            session.setCurrentLevel(gameLevel);
        } catch (IOException e) {
            gameLevel = null;
            playerChar = null;
            e.printStackTrace();
        }
    }

    /**
     * Returns the first active enemy whose assignedWord starts with the given prefix, or null.
     * @param prefix the current typed string to match against enemy words
     * @return the first active enemy whose assignedWord starts with the given prefix, or null if no match or if gameLevel is not initialized
     */
    private Enemy findMatchingEnemy(String prefix) {
        if (gameLevel == null || prefix.isEmpty()) return null;
        for (Enemy e : gameLevel.getActiveEnemies()) {
            if (e.getAssignedWord().startsWith(prefix)) {
                return e;
            }
        }
        return null;
    }

    // INPUT — KeyEventDispatcher intercepts keys regardless of which component has focus
    private void setupInput() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(event -> {
            if (!isShowing()) return false;

            if (event.getID() == KeyEvent.KEY_PRESSED && event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                togglePause();
                return true;
            }

            if (isPaused) return false;

            if (event.getID() == KeyEvent.KEY_TYPED) {
                char key = event.getKeyChar();
                if (session != null) session.processKeystroke(key);

                if (gameLevel != null) {
                    typed += key;
                    Enemy matched = findMatchingEnemy(typed);
                    if (matched != null) {
                        if (targetEnemy != matched) {
                            targetEnemy = matched;
                            typingManager.setActiveWord(targetEnemy.getAssignedWord());
                        }
                        typingManager.validateKeystroke(key);
                        if (typed.equals(targetEnemy.getAssignedWord())) {
                            // Save the word before clearing so we can map it to an action below
                            String completedWord = typed;

                            long elapsedMs = System.currentTimeMillis() - typingManager.getStartTimeMs();
                            if (elapsedMs > 0) {
                                lastWpm = (targetEnemy.getAssignedWord().length() / 5.0) / (elapsedMs / 60000.0);
                            }
                            targetEnemy.takeDamage(10000);
                            gameLevel.removeDefeatedEnemies();
                            score += 10 * targetEnemy.getAssignedWord().length()
                                     + (int)(targetEnemy.getSpeed() / 10);
                            audioManager.playEnemyDefeated();
                            maybeSpawnPowerup();

                            // Word-to-action mapping (FR-9 / UC-10): check if the word is a
                            // special action keyword. GameSession handles the lookup and calls
                            // playerChar.triggerAction(ActionType) if it matches.
                            if (session != null) {
                                ActionType triggered = session.mapWordToAction(completedWord);
                                if (triggered != null) {
                                    activeActionLabel = actionLabelFor(triggered);
                                    actionColor = actionColorFor(triggered);
                                    actionLabelTimer = 40;  // ~1.2 s at 30 fps
                                    actionFlashTimer = 15;  // ~0.45 s screen flash
                                    spawnActionPowerup();   // guaranteed drop for action keywords
                                }
                            
                            }

                            typed = "";
                            targetEnemy = null;
                            if (gameLevel.getSpawnCount() < gameLevel.getMaxSpawns()) {
                                gameLevel.spawnEnemy();
                            }
                        }
                    } else {
                        if (targetEnemy != null) typingManager.validateKeystroke(key);
                        flashRed = true;
                        flashTimer = 10;
                        typed = "";
                        targetEnemy = null;
                    }
                } else {
                    typed += key;
                    if (currentWord.startsWith(typed)) {
                        if (typed.equals(currentWord)) {
                            score += 10;
                            enemies.clear();
                            spawnEntities();
                            typed = "";
                        }
                    } else {
                        flashRed = true;
                        flashTimer = 10;
                        typed = "";
                    }
                }
                return true;
            }

            if (event.getID() == KeyEvent.KEY_PRESSED) {
                if (event.getKeyCode() == KeyEvent.VK_LEFT)  playerX -= 10;
                if (event.getKeyCode() == KeyEvent.VK_RIGHT) playerX += 10;
            }

            return false;
        });
    }

    /**
     * Returns a short display string for an action that just fired (FR-9 / UC-10).
     * Shown above the player for ~40 frames to confirm the word-to-action mapping worked.
     */
    private String actionLabelFor(ActionType action) {
        return switch (action) {
            case JUMP    -> "JUMP!";
            case DUCK    -> "DUCK!";
            case ATTACK  -> "ATTACK!";
            case SWORD   -> "SWORD!";
            case SHIELD  -> "SHIELD!";
            case BLOCK   -> "BLOCK!";
            case SLASH   -> "SLASH!";
            case SPELL   -> "SPELL!";
            case ARROW   -> "ARROW!";
            case COUNTER -> "COUNTER!";
        };
    }

    /** Unique color for each action type, used for the pill label and screen flash. */
    private Color actionColorFor(ActionType action) {
        return switch (action) {
            case JUMP    -> new Color(0,   191, 255); // sky blue
            case DUCK    -> new Color(173, 255,  47); // yellow-green
            case ATTACK  -> new Color(255,  64,  64); // red
            case SWORD   -> new Color(135, 206, 235); // steel blue
            case SHIELD  -> new Color( 65, 105, 225); // royal blue
            case BLOCK   -> new Color(255, 140,   0); // orange
            case SLASH   -> new Color(220,  20,  60); // crimson
            case SPELL   -> new Color(148,   0, 211); // purple
            case ARROW   -> new Color( 50, 205,  50); // lime green
            case COUNTER -> new Color(255, 215,   0); // gold
        };
    }

    /**
 * Starts the main game loop timer.
 *
 * <p>The loop updates the game state at a fixed interval (~30 FPS)
 * and triggers repainting of the game world.</p>
 */

    private void startGameLoop() {
        gameLoop = new Timer(30, e -> {
            updateGame();
            gameWorld.repaint();
        });
        gameLoop.start();
    }

    /**
 * Updates the entire game state on each frame.
 *
 * <p>This includes player movement, enemy movement,
 * collision detection, powerup handling, scoring,
 * typing feedback, and win/loss condition checks.</p>
 */

    private void updateGame() {

        if (flashTimer > 0) flashTimer--;
        else flashRed = false;

        // Tick down the action label and screen flash (FR-9/UC-10 visual feedback)
        if (actionLabelTimer > 0) {
            actionLabelTimer--;
            if (actionLabelTimer == 0) activeActionLabel = null;
        }
        if (actionFlashTimer > 0) actionFlashTimer--;

        if (gameLevel != null && playerChar != null) {
            // Sync X from panel (arrow-key controlled); preserve Y so physics can run
            playerChar.setPosition(playerX, playerChar.getY());
            // Apply gravity and jump velocity each frame
            playerChar.move(0.030);
            // Land on the ground — zeroes vertical velocity and snaps position
            playerChar.clampToGround(GROUND_Y);
            // Read the physics Y back so the renderer and enemy-collision code use it
            playerY = (int) playerChar.getY();

            // Move all active enemies toward the player (issue #58)
            gameLevel.updateEnemies(0.030);

            // Collision check: enemies that reach the player deal damage then are removed
            ArrayList<Enemy> attackers = new ArrayList<>();
            for (Enemy e : gameLevel.getActiveEnemies()) {
                if (Math.abs(e.getX() - playerX) < 60 && Math.abs(e.getY() - playerY) < 60) {
                    attackers.add(e);
                }
            }
            if (!gameStarted) return;

            for (Enemy e : attackers) {
                playerChar.takeDamage(e.getAttackDamage());

                audioManager.playPlayerHit();

                gameLevel.removeEnemy(e);
                if (gameLevel.getSpawnCount() < gameLevel.getMaxSpawns()) {
                    gameLevel.spawnEnemy();
                }
            }

            // Tick session — handles slow-time countdown and restores enemy speeds when it expires (#67)
            if (session != null) session.updateGame(0.030);

            // Powerup collision: player (70×70) overlaps a powerup (40×40) — pick it up (#66, #67, #68)
            ArrayList<Powerup> pickedUp = new ArrayList<>();
            for (Powerup p : gameLevel.getActivePowerups()) {
                int ppx = (int) p.getX(), ppy = (int) p.getY();
                if (playerX < ppx + 40 && playerX + 70 > ppx
                        && playerY < ppy + 40 && playerY + 70 > ppy) {
                    pickedUp.add(p);
                }
            }
            for (Powerup p : pickedUp) {
                p.applyEffect(session);          // HealthPowerup heals playerChar; SlowTimePowerup slows enemies
                gameLevel.removePowerup(p);
                audioManager.playPowerupPickup();
            }

            health = playerChar.getHealth();     // syncs health bar after any heal
        } else {
            // Fallback Point-based movement
            for (Point e : enemies) {
                e.x -= 2;
                if (Math.abs(e.x - playerX) < 40) health--;
                if (e.x < -50) e.x = 800;
            }
        }

        healthBar.setValue(health);
        hpLabel.setText(health + "%");
        if (health > 60)       healthBar.setForeground(new Color(50, 200, 80));
        else if (health > 30)  healthBar.setForeground(new Color(230, 180, 0));
        else                   healthBar.setForeground(new Color(210, 40, 40));
        scoreLabel.setText("Score: " + score);
        String diffName = selectedDifficulty == DifficultyLevel.HARD ? "Hard"
                        : selectedDifficulty == DifficultyLevel.MEDIUM ? "Medium" : "Easy";
        levelLabel.setText("Level: " + diffName);
        wpmLabel.setText(String.format("WPM: %.0f", lastWpm));
        accuracyLabel.setText(String.format("Acc: %.0f%%", typingManager.calculateAccuracy()));

        
        if (gameStarted && health <= 0 && onGameOver != null) {
            gameLoop.stop();
            onGameOver.run();
            return;
}

        if (gameStarted && onLevelComplete != null && gameLevel != null
            && gameLevel.getSpawnCount() >= gameLevel.getMaxSpawns()
            && gameLevel.getActiveEnemies().isEmpty()
            && health > 0) {

            gameLoop.stop();
            onLevelComplete.run();
            return;
        }
    }

    /**
     * After killing an enemy, 40% chance to drop a powerup in the world (capped at 3 active at once).
     * Easy → HealthPowerup(20hp); Hard → SlowTimePowerup(0.5×, 5s); Medium → either randomly.
     * The concrete Powerup carries its own world position — no wrapper needed.
     */
    /**
     * After killing an enemy, 70% chance to drop a powerup near the player (capped at 3 active at once).
     * Easy → always HealthPowerup(20hp).
     * Medium → 50% HealthPowerup(15hp) / 50% SlowTimePowerup.
     * Hard → 30% HealthPowerup(10hp) / 70% SlowTimePowerup.
     */
    private void maybeSpawnPowerup() {
        if (gameLevel == null) return;
        if (gameLevel.getActivePowerups().size() >= 3) return;
        if (RNG.nextDouble() > 0.70) return;

        // Spawn within reach of the player (100 – 400 px from left edge)
        double px = Math.max(100, Math.min(400, playerX + 50 + RNG.nextInt(250)));
        double py = 290;

        DifficultyLevel diff = gameLevel.getDifficulty();
        Powerup powerup;
        double roll = RNG.nextDouble();
        if (diff == DifficultyLevel.EASY || roll < 0.50) {
            int healAmt = switch (diff) {
                case EASY   -> 20;
                case MEDIUM -> 15;
                case HARD   -> 10;
            };
            powerup = new HealthPowerup(px, py, healAmt);
        } else {
            powerup = new SlowTimePowerup(px, py, 0.5, 5000L);
        }
        gameLevel.addPowerup(powerup);
    }

    /**
     * Guaranteed powerup drop when the player types an action keyword.
     * Skips the probability roll but still respects the cap of 3 active powerups.
     */
    private void spawnActionPowerup() {
        if (gameLevel == null) return;
        if (gameLevel.getActivePowerups().size() >= 3) return;

        double px = Math.max(100, Math.min(400, playerX + 50 + RNG.nextInt(250)));
        double py = 290;

        DifficultyLevel diff = gameLevel.getDifficulty();
        Powerup powerup;
        double roll = RNG.nextDouble();
        if (diff == DifficultyLevel.EASY || roll < 0.50) {
            int healAmt = switch (diff) {
                case EASY   -> 20;
                case MEDIUM -> 15;
                case HARD   -> 10;
            };
            powerup = new HealthPowerup(px, py, healAmt);
        } else {
            powerup = new SlowTimePowerup(px, py, 0.5, 5000L);
        }
        gameLevel.addPowerup(powerup);
    }

    /**
 * Spawns default enemies and powerups for fallback gameplay.
 *
 * <p>This method is used when the Level system fails to initialize,
 * ensuring that basic gameplay can still proceed.</p>
 */

    private void spawnEntities() {
        enemies.clear();
        powerups.clear();

        enemies.add(new Point(500, 300));
        enemies.add(new Point(700, 300));
        powerups.add(new Point(650, 280));
    }

    /**
 * @return the player's current score
 */
    public int getScore() {
        return score;
    }

    /**
 * @return the player's current words per minute (WPM)
 */
    public double getWpm() {
        return lastWpm;
    }
  
    /**
 * @return the player's current typing accuracy
 */
    public double getAccuracy() {
        return typingManager.calculateAccuracy();
    }

    /**
 * @return the player's current play time in minutes
 */
    public int getPlayTimeMins() {
        if (gameStartMs == 0) return 1;
        return Math.max(1, (int)((System.currentTimeMillis() - gameStartMs) / 60000.0));
    }

    /**
 * Sets the action to execute when the game ends.
 *
 * @param action the game over callback
 */
    public void setOnGameOver(Runnable action) {
        this.onGameOver = action;
    }

    /**
 * Sets the action to execute when returning to the main menu.
 *
 * @param action the return-to-menu callback
 */

    public void setOnReturnToMenu(Runnable action) {
        this.onReturnToMenu = action;
    }

    /**
 * Sets the action to execute when a level is completed.
 *
 * @param action the level completion callback
 */
    public void setOnLevelComplete(Runnable action) {
        this.onLevelComplete = action;
    }

    /**
 * Sets the game difficulty and initializes a new session.
 *
 * @param d the selected difficulty level
 */

    public void setDifficulty(DifficultyLevel d) {
        gameStarted = true;
        this.selectedDifficulty = d;
        health = 100;
        score = 0;
        level = 1;
        typed = "";
        targetEnemy = null;
        typingManager.reset();
        lastWpm = 0.0;
        activeActionLabel = null;
        actionLabelTimer = 0;
        actionColor = null;
        actionFlashTimer = 0;
        initLevel(d);
        if (gameLevel == null) spawnEntities();
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop.start();
        }
        startGameLoop();
    }

    /**
 * @return the current difficulty level
 */
    public DifficultyLevel getDifficulty() {
        return selectedDifficulty;
    }

    /**
 * Restarts the game with the current difficulty and resets all stats.
 */
    public void restartGame() {
        gameStarted = true;
        health = 100;
        score = 0;
        level = 1;
        typingManager.reset();
        lastWpm = 0.0;
        activeActionLabel = null;
        actionLabelTimer = 0;
        actionColor = null;
        actionFlashTimer = 0;
        initLevel(selectedDifficulty);
        if (gameLevel == null) spawnEntities();
        gameLoop.start();
    }

    /**
 * Stops the game loop and ends the current session.
 */
    public void stopGame() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameStarted = false;
    }
}
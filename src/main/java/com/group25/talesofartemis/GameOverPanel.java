package com.group25.talesofartemis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * A JPanel used to display the game over or level complete screen.
 * 
 * This panel shows the final score and provides buttons for
 * replaying the game, viewing high scores, returning to the
 * main menu, or going to the player hub.
 *
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public class GameOverPanel extends JPanel {

    private JLabel titleLabel;
    private JLabel scoreLabel;

    private JButton playAgainBtn;
    private JButton highScoresBtn;
    private JButton menuBtn;
    private JButton playerHubBtn;

    /**
     * Constructs the GameOverPanel and initializes
     * the layout, center content, and buttons.
     */
    public GameOverPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        add(createCenter(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);
    }

    /**
     * Creates the center panel containing the title
     * and score labels.
     * 
     * @return a JPanel containing the main display content
     */
    private JPanel createCenter() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        titleLabel = new JLabel("Game Over");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.CYAN);
        titleLabel.setFont(new Font("Monotype Corsiva", Font.BOLD, 50));

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreLabel.setForeground(Color.CYAN);
        scoreLabel.setFont(new Font("Serif", Font.BOLD, 24));

        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(scoreLabel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Creates the bottom button panel containing
     * all navigation and action buttons.
     * 
     * @return a JPanel containing all buttons
     */
    private JPanel createButtons() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        playAgainBtn = createButton("Play Again");
        playerHubBtn = createButton("Player Hub");
        highScoresBtn = createButton("View High Scores");
        menuBtn = createButton("Main Menu");

        panel.add(playAgainBtn);
        panel.add(playerHubBtn);
        panel.add(highScoresBtn);
        panel.add(menuBtn);

        return panel;
    }

    /**
     * Creates a styled JButton with the given text.
     * 
     * @param text the text displayed on the button
     * @return the styled JButton
     */
    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(Color.CYAN);
        btn.setBackground(Color.BLACK);
        btn.setFont(new Font("Serif", Font.BOLD, 16));
        return btn;
    }

    /**
     * Sets the panel to display a game over message
     * with the final score.
     * 
     * @param score the player's final score
     */
    public void setGameOver(int score) {
        titleLabel.setText("Game Over");
        scoreLabel.setText("Final Score: " + score);
    }

    /**
     * Sets the panel to display a level complete message
     * with the current score.
     * 
     * @param score the player's score
     */
    public void setLevelComplete(int score) {
        titleLabel.setText("Level Complete");
        scoreLabel.setText("Score: " + score);
    }

    /**
     * Assigns an action listener to the Play Again button.
     * 
     * @param action the action to perform when clicked
     */
    public void setPlayAgainAction(ActionListener action) {
        playAgainBtn.addActionListener(action);
    }

    /**
     * Assigns an action listener to the High Scores button.
     * 
     * @param action the action to perform when clicked
     */
    public void setHighScoresAction(ActionListener action) {
        highScoresBtn.addActionListener(action);
    }

    /**
     * Assigns an action listener to the Main Menu button.
     * 
     * @param action the action to perform when clicked
     */
    public void setMenuAction(ActionListener action) {
        menuBtn.addActionListener(action);
    }

    /**
     * Assigns an action listener to the Player Hub button.
     * 
     * @param action the action to perform when clicked
     */
    public void setPlayerHubAction(ActionListener action) {
        playerHubBtn.addActionListener(action);
    }
}
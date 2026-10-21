package com.group25.talesofartemis.model;

import com.group25.talesofartemis.game.GameSession;

/**
 * A pickup effect you can plug in, like heal, slow enemies, whatever. Each class does its own thing in
 * applyEffect, and the game only talks to this interface. getDescription is the description for tutorials and UI.
 * Position (x, y) is the world-space location of this pickup sprite so the Level can store and render
 * concrete instances directly without a separate wrapper.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public interface Powerup {

    /** What to show the player (tooltip, help screen, etc.). */
    String getDescription();

    /** Run the effect, usually touching the session (player, level, etc.). */
    void applyEffect(GameSession session);

    /** World-space X of the pickup sprite. */
    double getX();

    /** World-space Y of the pickup sprite. */
    double getY();
}

package com.group25.talesofartemis.model;

/**
 * What happens when the player finishes typing a special word — jump, duck, swing a sword, etc.
 * (FR-9 / UC-10: words map to actions.) Easy/medium/hard worlds in our plan used shorter commands
 * vs longer ones; the game turns a typed string into one of these and calls triggerAction on the player.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public enum ActionType {

    JUMP,
    DUCK,

    /** Generic hit — use when you don't care which weapon label the UI shows. */
    ATTACK,

    SWORD,
    SHIELD,
    BLOCK,
    SLASH,

    SPELL,
    ARROW,
    COUNTER
}

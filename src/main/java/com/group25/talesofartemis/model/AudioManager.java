package com.group25.talesofartemis.model;

import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * All the sound effects. Everything gets pulled into memory when this object is built, after that playing
 * is just rewind and start on a Clip. Files sit in src/main/resources/audio/; if one isn't there or won't decode, we
 * skip it instead of throwing an exception.
 * @author Group 25 - Vinay, Krish, Daniel, Vishal, Adityan
 */
public class AudioManager {

    private static final String RES_ENEMY_DEFEATED = "audio/enemy_defeated.wav";
    private static final String RES_POWERUP_PICKUP = "audio/powerup_pickup.wav";
    private static final String RES_LEVEL_COMPLETE = "audio/level_complete.wav";
    private static final String RES_GAME_OVER = "audio/game_over.wav";
    private static final String RES_PLAYER_HIT = "audio/player_hit.wav";

    private final Clip enemyDefeatedClip;
    private final Clip powerupPickupClip;
    private final Clip levelCompleteClip;
    private final Clip gameOverClip;
    private final Clip playerHitClip;

    /** When true, the play methods no-op, useful for a settings toggle. */
    private volatile boolean muted;

    /** Grabs the four wavs off the classpath and opens them once. */
    public AudioManager() {
        this.enemyDefeatedClip = loadClip(RES_ENEMY_DEFEATED);
        this.powerupPickupClip = loadClip(RES_POWERUP_PICKUP);
        this.levelCompleteClip = loadClip(RES_LEVEL_COMPLETE);
        this.gameOverClip = loadClip(RES_GAME_OVER);
        this.playerHitClip = loadClip(RES_PLAYER_HIT);
    }
    public void playPlayerHit() {
        playClip(playerHitClip);
    }
    /** Whether we're silencing every cue. */
    public boolean isMuted() {
        return muted;
    }

    /** Force mute on or off from settings. */
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    /** Toggle mute and hand back the new value so your UI can react. */
    public boolean toggleMute() {
        muted = !muted;
        return muted;
    }

    /** Someone finished typing a word on an enemy. */
    public void playEnemyDefeated() {
        playClip(enemyDefeatedClip);
    }

    /** Picked up a buff. */
    public void playPowerupPickup() {
        playClip(powerupPickupClip);
    }

    /** Beat the stage. */
    public void playLevelComplete() {
        if (muted || levelCompleteClip == null) return;

        stopAllSounds(); 

        levelCompleteClip.setFramePosition(0);
        levelCompleteClip.start();
    }
    private void stopAllSounds() {
        if (enemyDefeatedClip != null) enemyDefeatedClip.stop();
        if (powerupPickupClip != null) powerupPickupClip.stop();
        if (levelCompleteClip != null) levelCompleteClip.stop();
        if (gameOverClip != null) gameOverClip.stop();
    }
    /** Player died or ran out of whatever ends the run. */
    public void playGameOver() {
        if (muted || gameOverClip == null) return;

        stopAllSounds();

        gameOverClip.setFramePosition(0);
        gameOverClip.start();
    }

    /**
     * Rewind the clip back to the beginning and hit play.
     */
    private void playClip(Clip clip) {
        if (muted || clip == null) {
            return;
        }
        synchronized (clip) {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    /** Read the wav from the classpath and open a line, return null if anything goes wrong. */
    private static Clip loadClip(String classpathResource) {
        ClassLoader cl = AudioManager.class.getClassLoader();
        if (cl == null) {
            return null;
        }
        try (InputStream in = cl.getResourceAsStream(classpathResource)) {
            if (in == null) {
                return null;
            }
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(in)) {
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                return clip;
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            return null;
        }
    }

    /** Call when shutting down to stop and close all clips so the mixer doesn't hold resources. */
    public void close() {
        closeClip(enemyDefeatedClip);
        closeClip(powerupPickupClip);
        closeClip(levelCompleteClip);
        closeClip(gameOverClip);
    }

    private static void closeClip(Clip clip) {
        if (clip == null) {
            return;
        }
        // Same lock as play to avoid fighting over the same line while shutting down.
        synchronized (clip) {
            clip.stop();
            clip.close();
        }
    }
}

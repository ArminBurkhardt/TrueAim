package org.trueaim.sound;

import org.joml.Vector3f;
import org.trueaim.Camera;
import org.lwjgl.openal.*;

/**
 * Zentrale Sound-Steuerung ohne Enums.
 * Bietet typsichere Methoden für alle Sound-Ereignisse.
 * Eigentlich nicht nötig, aber entlastet Game Engine enorm.
 */
public class SoundPlayer {
    private final SoundManager soundManager;

    // Sound-Konstanten für bessere Lesbarkeit
    public static final String SHOOT = "SHOOT";
    public static final String HIT = "HIT";
    public static final String HEADSHOT = "HEADSHOT";
    public static final String RELOAD = "RELOAD";
    public static final String UI_CLICK = "UI_CLICK";

    public SoundPlayer() {
        soundManager = new SoundManager();
        soundManager.setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
        initializeSounds();
    }

    /**
     * Initialisiert alle Sounds und ihre Quellen.
     */
    private void initializeSounds() {
        // Schusssound (relativer UI-Sound)
        //https://freesound.org/people/Cloud-10/sounds/632821/
        createSound(SHOOT, "shot.ogg", true, false);
//TODO
//        // Treffersounds (3D-Positioniert)
//        createSound(HIT, "hit.ogg", false, false);
//        createSound(HEADSHOT, "headshot.ogg", false, false);
//
        // Nachladesound (relativer UI-Sound
        // https://freesound.org/people/ser%C3%B8t%C5%8Dnin/sounds/674742/
        createSound(RELOAD, "reload.ogg", true, false);
//
//        // UI-Sound (relativer UI-Sound)
//        createSound(UI_CLICK, "ui_click.ogg", true, false);
    }

    /**
     * Erstellt einen vollständig konfigurierten Sound.
     * @param name Eindeutiger Sound-Name
     * @param file Dateiname im sounds/-Verzeichnis
     * @param isUI Ist es ein UI-Sound (relative Position)?
     * @param loop Soll der Sound in Schleife abgespielt werden?
     */
    private void createSound(String name, String file, boolean isUI, boolean loop) {
        soundManager.loadSound(name, "/sounds/" + file);
        soundManager.createSourcePool(name, 5, false, true); // Poolgröße 5
        soundManager.setPoolBuffer(name, name);
        soundManager.createSource(name + "_SOURCE", loop, isUI);
        soundManager.setSourceBuffer(name + "_SOURCE", name);
    }

    /**
     * Aktualisiert die Hörerposition.
     * @param camera Aktuelle Spielkamera
     */
    public void update(Camera camera) {
        soundManager.updateListenerPosition(camera);
    }

    /**
     * Spielt einen 3D-Sound an einer bestimmten Position ab.
     * @param soundType Sound-Konstante (z.B. SoundPlayer.HIT)
     * @param position Position im 3D-Raum
     */
    public void playAt(String soundType, Vector3f position) {
        soundManager.setSourcePosition(soundType + "_SOURCE", position);
        soundManager.playSound(soundType + "_SOURCE");
    }

    /**
     * Spielt einen UI-Sound ab (relative Position).
     * @param soundType Sound-Konstante (z.B. SoundPlayer.SHOOT)
     */
    public void play(String soundType) {
        if (SHOOT.equals(soundType)) {
            soundManager.playFromPool(SHOOT);
        } else {
            soundManager.playSound(soundType + "_SOURCE");
        }
    }
    /**
     * Gibt alle Sound-Ressourcen frei.
     */
    public void cleanup() {
        soundManager.cleanup();
    }
}
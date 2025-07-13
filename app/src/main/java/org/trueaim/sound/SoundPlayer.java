package org.trueaim.sound;

import org.joml.Vector3f;
import org.trueaim.Camera;
import org.lwjgl.openal.*;

// DISCLAIMER: Der Code hier ist tatsächlich nicht aus dem Tutorial übernommen xd

/**
 * Zentrale Sound-Steuerung ohne Enums.
 * Bietet typsichere Methoden für alle Sound-Ereignisse.
 * Eigentlich nicht nötig, aber entlastet Game Engine enorm.
 */
public class SoundPlayer {
    private final SoundManager soundManager;

    // Sound-Konstanten für bessere Lesbarkeit
    public static final String AK_SHOOT = "AK_SHOOT";
    public static final String AK_RELOAD = "AK_RELOAD";
    public static final String UI_CLICK = "UI_CLICK";
    public static final String GUN_EMPTY = "GUN_EMPTY";
    public static final String V9S_SHOOT = "V9S_SHOOT";
    public static final String V9S_RELOAD = "V9S_RELOAD";

    public SoundPlayer() {
        soundManager = new SoundManager();
        soundManager.setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
        initializeSounds();
    }

    /**
     * Initialisiert alle Sounds und ihre Quellen.
     */
    private void initializeSounds() {
        // Schusssound
        //https://freesound.org/people/Cloud-10/sounds/632821/
        createSound(AK_SHOOT, "shot.ogg", true, false);

        // Nachladesound
        // https://freesound.org/people/ser%C3%B8t%C5%8Dnin/sounds/674742/
        createSound(AK_RELOAD, "reload.ogg", true, false);


        // UI-Sound
        //https://freesound.org/people/el_boss/sounds/677860/
        createSound(UI_CLICK, "ui_click.ogg", true, false);

        // Gun Empty Sound
        //https://freesound.org/people/KlawyKogut/sounds/154934/
        createSound(GUN_EMPTY, "gun_empty.ogg",true, false);

        // V9S Schuss-Sound
        //https://freesound.org/people/LeMudCrab/sounds/163456/
        createSound(V9S_SHOOT, "v9s_shoot.ogg", true, false);

        // V9S Nachlade-Sound
        //https://freesound.org/people/Sophia_C/sounds/467182/
        createSound(V9S_RELOAD, "v9s_reload.ogg", true, false);
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
     * currently useless, da alle Sounds von User ausgehen
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
            soundManager.playFromPool(soundType);
    }
    /**
     * Gibt alle Sound-Ressourcen frei.
     */
    public void cleanup() {
        soundManager.cleanup();
    }
}
package org.trueaim.sound;


import org.joml.Vector3f;
import static org.lwjgl.openal.AL10.*;

/**
 * Repräsentiert den Hörer in der 3D-Audioumgebung.
 * Es gibt immer genau einen Hörer (den Spieler).
 */
public class SoundListener {
    /**
     * Konstruktor - Initialisiert den Hörer an einer Position.
     * @param position Startposition des Hörers
     */
    public SoundListener(Vector3f position) {
        // Position setzen
        setPosition(position);
        // Geschwindigkeit initialisieren (stationär)
        alListener3f(AL_VELOCITY, 0, 0, 0);
    }

    /**
     * Setzt die Position des Hörers.
     * @param position Neue Position
     */
    public void setPosition(Vector3f position) {
        alListener3f(AL_POSITION, position.x, position.y, position.z);
    }

    /**
     * Setzt die Ausrichtung des Hörers.
     * @param at Blickrichtung (wohin der Hörer schaut)
     * @param up Aufwärtsrichtung (was für den Hörer "oben" ist)
     */
    public void setOrientation(Vector3f at, Vector3f up) {
        // Daten als Float-Array: [atX, atY, atZ, upX, upY, upZ]
        float[] data = { at.x, at.y, at.z, up.x, up.y, up.z };
        alListenerfv(AL_ORIENTATION, data);
    }
}
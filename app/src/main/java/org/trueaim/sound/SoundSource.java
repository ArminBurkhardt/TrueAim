package org.trueaim.sound;

import org.joml.Vector3f;
import static org.lwjgl.openal.AL10.*;

/**
 * Repräsentiert eine OpenAL-Soundquelle.
 * Steuert Wiedergabe, Position und Eigenschaften eines Sounds.
 */
public class SoundSource {
    // OpenAL-Source-ID
    private final int sourceId;

    /**
     * Erstellt eine neue Soundquelle.
     * @param loop Soll der Sound in Schleife abgespielt werden?
     * @param relative Ist die Position relativ zum Hörer?
     */
    public SoundSource(boolean loop, boolean relative) {
        // OpenAL-Soundquelle generieren
        this.sourceId = alGenSources();

        // Looping-Eigenschaft setzen
        alSourcei(sourceId, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);

        // Relative Positionierung setzen
        alSourcei(sourceId, AL_SOURCE_RELATIVE, relative ? AL_TRUE : AL_FALSE);
    }

    /**
     * Weist der Quelle einen Soundbuffer zu.
     * @param bufferId OpenAL-Buffer-ID
     */
    public void setBuffer(int bufferId) {
        // Vorherige Wiedergabe stoppen
        stop();
        // Buffer mit Quelle verbinden
        alSourcei(sourceId, AL_BUFFER, bufferId);
    }

    /**
     * Setzt die 3D-Position der Quelle.
     * @param position Position im 3D-Raum
     */
    public void setPosition(Vector3f position) {
        alSource3f(sourceId, AL_POSITION, position.x, position.y, position.z);
    }

    /**
     * Setzt die Lautstärke der Quelle.
     * @param gain Lautstärke (0.0 = stumm, 1.0 = voll)
     */
    public void setGain(float gain) {
        alSourcef(sourceId, AL_GAIN, gain);
    }

    /**
     * Startet die Wiedergabe.
     */
    public void play() {
        alSourcePlay(sourceId);
    }

    /**
     * Stoppt die Wiedergabe.
     */
    public void stop() {
        alSourceStop(sourceId);
    }

    /**
     * Pausiert die Wiedergabe.
     */
    public void pause() {
        alSourcePause(sourceId);
    }

    /**
     * Überprüft, ob die Quelle abspielt.
     * @return true wenn abspielend, sonst false
     */
    public boolean isPlaying() {
        return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
    }

    /**
     * Gibt OpenAL-Ressourcen frei.
     */
    public void cleanup() {
        stop();
        alDeleteSources(sourceId);
    }
}
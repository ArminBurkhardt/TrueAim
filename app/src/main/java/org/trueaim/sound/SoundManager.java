package org.trueaim.sound;


import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.trueaim.Camera;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.openal.AL10.alDistanceModel;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Verwaltet das gesamte Audio-System.
 * Initialisiert OpenAL, verwaltet Soundpuffer und -quellen,
 * und aktualisiert den Hörer.
 */
public class SoundManager {
    // OpenAL-Gerät und Kontext
    private final long device;
    private final long context;

    // Verwaltung von Soundpuffern und -quellen
    private final List<SoundBuffer> soundBuffers = new ArrayList<>();
    private final Map<String, SoundSource> soundSources = new HashMap<>();
    private final Map<String, SoundBuffer> bufferMap = new HashMap<>();
    private final Map<String, List<SoundSource>> soundSourcePools = new HashMap<>();


    // Der Hörer (Spielerperspektive)
    private SoundListener listener;

    /**
     * Konstruktor - Initialisiert das OpenAL-System.
     */
    public SoundManager() {
        // Standard-Audiogerät öffnen
        device = alcOpenDevice((ByteBuffer) null);
        if (device == NULL) {
            throw new IllegalStateException("Fehler beim Öffnen des OpenAL-Geräts");
        }

        // Gerätefähigkeiten laden
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);

        // Audio-Kontext erstellen
        context = alcCreateContext(device, (IntBuffer) null);
        if (context == NULL) {
            throw new IllegalStateException("Fehler beim Erstellen des OpenAL-Kontexts");
        }

        // Kontext aktivieren
        alcMakeContextCurrent(context);

        // OpenAL-Fähigkeiten laden
        AL.createCapabilities(deviceCaps);

        // Standard-Hörer erstellen (Position 0,0,0)
        listener = new SoundListener(new Vector3f(0, 0, 0));
    }

    public void loadSound(String name, String filePath) {
        SoundBuffer buffer = new SoundBuffer(filePath);
        soundBuffers.add(buffer);
        bufferMap.put(name, buffer);
    }

    public void createSource(String name, boolean loop, boolean relative) {
        SoundSource source = new SoundSource(loop, relative);
        soundSources.put(name, source);
    }

    public void setSourceBuffer(String sourceName, String bufferName) {
        SoundSource source = soundSources.get(sourceName);
        SoundBuffer buffer = bufferMap.get(bufferName);
        if (source != null && buffer != null) {
            source.setBuffer(buffer.getBufferId());
        }
    }

    public void setSourcePosition(String sourceName, Vector3f position) {
        SoundSource source = soundSources.get(sourceName);
        if (source != null) {
            source.setPosition(position);
        }
    }

    public void playSound(String sourceName) {
        SoundSource source = soundSources.get(sourceName);
        if (source != null && !source.isPlaying()) {
            source.play();
        }
    }

    public void createSourcePool(String name, int poolSize, boolean loop, boolean relative) {
        List<SoundSource> pool = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            SoundSource source = new SoundSource(loop, relative);
            pool.add(source);
        }
        soundSourcePools.put(name, pool);
    }

    public void setPoolBuffer(String poolName, String bufferName) {
        List<SoundSource> pool = soundSourcePools.get(poolName);
        SoundBuffer buffer = bufferMap.get(bufferName);
        if (pool != null && buffer != null) {
            for (SoundSource source : pool) {
                source.setBuffer(buffer.getBufferId());
            }
        }
    }

    public void playFromPool(String poolName) {
        List<SoundSource> pool = soundSourcePools.get(poolName);
        if (pool != null) {
            // Finde die erste verfügbare Quelle
            for (SoundSource source : pool) {
                if (!source.isPlaying()) {
                    source.play();
                    return;
                }
            }
            // Falls alle belegt sind, überschreibe die älteste Quelle
            pool.get(0).play();
        }
    }


    /**
     * Fügt einen Soundpuffer zur Verwaltung hinzu.
     * @param soundBuffer Soundpuffer-Objekt
     */
    public void addSoundBuffer(SoundBuffer soundBuffer) {
        soundBuffers.add(soundBuffer);
    }

    /**
     * Fügt eine Soundquelle mit Namen hinzu.
     * @param name Eindeutiger Name der Quelle
     * @param soundSource Soundquelle-Objekt
     */
    public void addSoundSource(String name, SoundSource soundSource) {
        soundSources.put(name, soundSource);
    }

    /**
     * Holt eine Soundquelle anhand ihres Namens.
     * @param name Name der Quelle
     * @return Soundquelle oder null
     */
    public SoundSource getSoundSource(String name) {
        return soundSources.get(name);
    }

    /**
     * Spielt eine Soundquelle ab, falls nicht bereits spielend.
     * @param name Name der Quelle
     */
    public void playSoundSource(String name) {
        SoundSource source = soundSources.get(name);
        if (source != null && !source.isPlaying()) {
            source.play();
        }
    }

    /**
     * Aktualisiert Hörer-Position und Ausrichtung basierend auf der Kamera.
     * @param camera Spielkamera (enthält Position und Blickrichtung)
     */
    public void updateListenerPosition(Camera camera) {
        // Hörerposition = Kameraposition
        listener.setPosition(camera.getPosition());

        // Blickrichtung aus Kameramatrix berechnen
        Matrix4f viewMatrix = camera.getViewMatrix();
        Vector3f at = new Vector3f();
        viewMatrix.positiveZ(at).negate();  // Blickrichtung ist negative Z-Achse
        Vector3f up = new Vector3f();
        viewMatrix.positiveY(up);           // Aufwärtsrichtung ist Y-Achse

        // Ausrichtung setzen
        listener.setOrientation(at, up);
    }

    /**
     * Setzt das Dämpfungsmodell für Entfernungseffekte.
     * @param model OpenAL-Dämpfungsmodell (z.B. AL11.AL_EXPONENT_DISTANCE)
     */
    public void setAttenuationModel(int model) {
        alDistanceModel(model);
    }

    /**
     * Gibt alle Audio-Ressourcen frei.
     * Muss beim Beenden des Programms aufgerufen werden.
     */
    public void cleanup() {
        // Soundquellen aufräumen
        soundSources.values().forEach(SoundSource::cleanup);
        soundSources.clear();

        // Soundpools aufräumen
        soundSourcePools.values().forEach(pool -> pool.forEach(SoundSource::cleanup));
        soundSourcePools.clear();

        // Soundpuffer aufräumen
        soundBuffers.forEach(SoundBuffer::cleanup);
        soundBuffers.clear();
        bufferMap.clear();

        // OpenAL-Kontext und Gerät schließen
        alcDestroyContext(context);
        alcCloseDevice(device);
    }
}
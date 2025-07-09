package org.trueaim.sound;

import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.net.URL;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import java.io.File;

/**
 * Repräsentiert einen OpenAL-Soundpuffer.
 * Lädt Audiodateien (OGG Vorbis Format) und speichert sie in OpenAL-Buffern.
 */
public class SoundBuffer {

    // OpenAL-Buffer-ID
    private final int bufferId;


    /**
     * Konstruktor - Lädt eine Audiodatei und erstellt einen OpenAL-Buffer.
     * @param filePath Pfad zur OGG Vorbis Audiodatei
     */
    public SoundBuffer(String filePath) {
        // OpenAL-Buffer generieren
        this.bufferId = alGenBuffers();

        // STBVorbisInfo für Metadaten der Audiodatei
        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            // Daten in OpenAL-Buffer laden
            // Format: Mono (1 Kanal) oder Stereo (2 Kanäle)
            ShortBuffer pcm = readVorbis(filePath, info);
            int format = info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
            alBufferData(bufferId, format, pcm, info.sample_rate());
            MemoryUtil.memFree(pcm); // Direkt freigeben
        }
    }

    /**
     * Gibt OpenAL-Ressourcen frei.
     * Muss am Ende des Programms aufgerufen werden.
     */
    public void cleanup() {
        // OpenAL-Buffer löschen
        alDeleteBuffers(bufferId);
    }

    /**
     * @return OpenAL-Buffer-ID
     */
    public int getBufferId() {
        return bufferId;
    }

    /**
     * Lädt eine OGG Vorbis-Datei und dekodiert sie zu PCM-Daten.
     * @param filePath Pfad zur Audiodatei
     * @param info Container für Audio-Metadaten
     * @return PCM-Daten als ShortBuffer
     */
    private ShortBuffer readVorbis(String filePath, STBVorbisInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // 1. Pfadüberprüfung
            File file = new File("C:\\TrueAim\\app\\src\\main\\resources\\sounds\\shot.ogg");
            if (!file.exists()) {
                throw new RuntimeException("File not found: " + file.getAbsolutePath());
            }

            // 2. Als URL aus Resources laden falls nötig
            URL url = SoundBuffer.class.getResource(filePath.startsWith("/") ? filePath : "/" + filePath);
            if (url != null) {
                filePath = url.getPath();
            }

            // 3. Debug-Ausgabe
            System.out.println("Loading sound from: " + filePath);

            IntBuffer error = stack.mallocInt(1);
            long decoder = stb_vorbis_open_filename("C:\\TrueAim\\app\\src\\main\\resources\\sounds\\shot.ogg", error, null);

            if (decoder == MemoryUtil.NULL) {
                System.out.println("Failed to open OGG file: " + filePath);
            }

            stb_vorbis_get_info(decoder, info);

            int channels = info.channels();

            int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

            ShortBuffer result = MemoryUtil.memAllocShort(lengthSamples * channels);

            result.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, result) * channels);
            stb_vorbis_close(decoder);

            return result;
        }
    }
}
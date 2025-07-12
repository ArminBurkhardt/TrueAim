package org.trueaim.sound;

import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
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
            IntBuffer error = stack.mallocInt(1);
            long decoder = NULL;
            ByteBuffer vorbisBuffer = null;

            // 1. Versuch: Als Ressource aus dem Classpath laden
            InputStream in = SoundBuffer.class.getResourceAsStream(filePath);
            if (in != null) {
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytes;
                    while ((bytes = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytes);
                    }
                    byte[] data = out.toByteArray();
                    vorbisBuffer = MemoryUtil.memAlloc(data.length);
                    vorbisBuffer.put(data).flip();
                    decoder = stb_vorbis_open_memory(vorbisBuffer, error, null);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read resource: " + filePath, e);
                }
            }
            // 2. Versuch: Als Dateisystempfad laden
            else {
                decoder = stb_vorbis_open_filename(filePath, error, null);
            }

            if (decoder == NULL) {
                throw new RuntimeException("Failed to load sound: " + filePath + ". Error: " + error.get(0));
            }

            stb_vorbis_get_info(decoder, info);
            int channels = info.channels();
            int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

            ShortBuffer pcmBuffer = MemoryUtil.memAllocShort(lengthSamples * channels);
            pcmBuffer.limit(stb_vorbis_get_samples_short_interleaved(
                    decoder, channels, pcmBuffer) * channels
            );
            stb_vorbis_close(decoder);

            if (vorbisBuffer != null) {
                MemoryUtil.memFree(vorbisBuffer);
            }

            return pcmBuffer;
        }
    }
}
package org.trueaim;

// credit to https://github.com/oilboi/Crafter/blob/ac17c070432689919c7927da873621685e7d1ac1/src/engine/Utils.java

import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGColor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.lwjgl.BufferUtils.createByteBuffer;

public class Utils {

    public static String loadResource(String fileName) throws Exception{
        String result;
        try(InputStream in = Utils.class.getResourceAsStream(fileName);
            Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name())){
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }
    public static List<String> readAllLines(String fileName) throws Exception {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Class.forName(Utils.class.getName()).getResourceAsStream(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        }
        return list;
    }

    public static float[] listToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] floatArr = new float[size];
        for (int i = 0; i < size; i++) {
            floatArr[i] = list.get(i);
        }
        return floatArr;
    }



    /**
     * Lädt eine Datei als ByteBuffer. Wenn die Datei nicht lesbar ist, wird sie als Resource geladen.
     * @param resource Pfad zur Datei oder Resource
     * @param bufferSize Initiale Puffergröße
     * @return ByteBuffer mit dem Inhalt der Datei
     * @throws IOException wenn die Datei nicht gelesen werden kann
     */
    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException{
        ByteBuffer buffer;

        Path path = Paths.get(resource);

        if(Files.isReadable(path)){
            try (SeekableByteChannel fc = Files.newByteChannel(path)){
                buffer = createByteBuffer((int)fc.size()+1);

                while (fc.read(buffer) != -1);
            }
        } else{
            try(InputStream source = Utils.class.getResourceAsStream(resource); ReadableByteChannel rbc = Channels.newChannel(source)){
                buffer = createByteBuffer(bufferSize);

                while(true){
                    int bytes = rbc.read(buffer);
                    if(bytes == -1){
                        break;
                    }
                    if(buffer.remaining() == 0){
                        buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                    }
                }
            }
        }

        buffer.flip();
        return buffer;
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity){
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    // NVG Utils

    public static NVGColor rgba(int r, int g, int b, int a, NVGColor colour) {
        colour.r(r / 255.0f);
        colour.g(g / 255.0f);
        colour.b(b / 255.0f);
        colour.a(a / 255.0f);

        return colour;
    }

    public static NVGColor rgba(int[] rgba, NVGColor colour) {
        colour.r(rgba[0] / 255.0f);
        colour.g(rgba[1] / 255.0f);
        colour.b(rgba[2] / 255.0f);
        colour.a(rgba[3] / 255.0f);

        return colour;
    }


}

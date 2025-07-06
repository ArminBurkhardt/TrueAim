package org.trueaim.rendering.GUI;

// https://github.com/lwjglgamedev/lwjglbook-leg/blob/master/chapter24/src/main/java/org/lwjglb/game/Hud.java

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.system.MemoryUtil;
import org.trueaim.Window;
import org.trueaim.entities.weapons.GenericWeapon;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class IngameHUD {
    private long vg;
    private ByteBuffer fontBuffer;
    private NVGColor colour;
    private static final String FONT_NAME = "OpenSans-Bold"; // ByteBuffer FONT_NAME = BufferUtils.createByteBuffer(64).put("OpenSans-Bold".getBytes()).flip();
    private DoubleBuffer posx, posy; // Position für Textanzeige
    private GenericWeapon equippedWeapon = null; // Aktuell ausgerüstete Waffe

    public IngameHUD(Window window) {
        try {this.init(window);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void setEquippedWeapon(GenericWeapon weapon) {
        this.equippedWeapon = weapon;
    }
    public GenericWeapon getEquippedWeapon() {
        return equippedWeapon;
    }

    public void init(Window window) throws Exception {
        this.vg = window.antialiasing ? nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES) : nvgCreate(NVG_STENCIL_STROKES);
        if (this.vg == NULL) {
            throw new Exception("Could not init nanovg");
        }

        fontBuffer = ioResourceToByteBuffer("/fonts/OpenSans-Bold.ttf", 150 * 1024);
        int font = nvgCreateFontMem(vg, FONT_NAME, fontBuffer, false);
        if (font == -1) {
            throw new Exception("Could not add font");
        }
        colour = NVGColor.create();

        posx = MemoryUtil.memAllocDouble(1);
        posy = MemoryUtil.memAllocDouble(1);

    }

    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = Paths.get(resource);

        if(Files.isReadable(path)){
            try (SeekableByteChannel fc = Files.newByteChannel(path)){
                buffer = createByteBuffer((int)fc.size()+1);

                while (fc.read(buffer) != -1);
            }
        } else{
            try(InputStream source = IngameHUD.class.getResourceAsStream(resource); ReadableByteChannel rbc = Channels.newChannel(source)){
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


    public void render(Window window) {
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1.0f);

        // Upper ribbon
        nvgBeginPath(vg);
        nvgRect(vg, 0, window.getHeight() - 100, window.getWidth(), 50);
        nvgFillColor(vg, rgba(0x23, 0xa1, 0xf1, 200, colour));
        nvgFill(vg);

        // Lower ribbon
        nvgBeginPath(vg);
        nvgRect(vg, 0, window.getHeight() - 50, window.getWidth(), 10);
        nvgFillColor(vg, rgba(0xc1, 0xe3, 0xf9, 200, colour));
        nvgFill(vg);



        // Clicks Text
        nvgFontSize(vg, 25.0f);
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_TOP);
        nvgFillColor(vg, rgba(0x00, 0x00, 0x00, 255, colour));
        if (equippedWeapon != null) {
            nvgText(vg, window.getWidth() / 3, window.getHeight() - 87, String.format("%s %02d/%02d",
                    equippedWeapon.getClass().getName().split("\\.")[equippedWeapon.getClass().getName().split("\\.").length - 1],
                    equippedWeapon.getBulletCount(), equippedWeapon.getAmmo()));
        } else {
            nvgText(vg, window.getWidth() / 3, window.getHeight() - 87, "No Weapon");
        }


        nvgEndFrame(vg);

        window.restoreState();
    }

    private NVGColor rgba(int r, int g, int b, int a, NVGColor colour) {
        colour.r(r / 255.0f);
        colour.g(g / 255.0f);
        colour.b(b / 255.0f);
        colour.a(a / 255.0f);

        return colour;
    }

    public void cleanup() {
        nvgDelete(vg);
        if (posx != null) {
            MemoryUtil.memFree(posx);
        }
        if (posy != null) {
            MemoryUtil.memFree(posy);
        }
    }
}

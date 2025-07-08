package org.trueaim.rendering.GUI;

// https://github.com/lwjglgamedev/lwjglbook-leg/blob/master/chapter24/src/main/java/org/lwjglb/game/Hud.java

import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.system.MemoryUtil;
import org.trueaim.Utils;
import org.trueaim.Window;
import org.trueaim.entities.weapons.GenericWeapon;
import org.trueaim.rendering.OverlayRenderer;
import org.trueaim.stats.StatTracker;


import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;


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
    private StatTracker statTracker;
    private long timeSinceLastUpdate = System.nanoTime(); // Zeit seit der letzten Aktualisierung in Nanosekunden
    private int frameCount = 0; // Anzahl der Frames seit der letzten Aktualisierung
    private double fps = 0; // Berechnete FPS
    private Crosshairs crosshair;
    private CrosshairManager crosshairManager;

    public IngameHUD(Window window, StatTracker statTracker) {
        try {this.init(window);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        this.statTracker = statTracker;
        this.crosshair = Crosshairs.DEFAULT;
        this.crosshairManager = new CrosshairManager();
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

        fontBuffer = Utils.ioResourceToByteBuffer("/fonts/OpenSans-Bold.ttf", 150 * 1024);
        int font = nvgCreateFontMem(vg, FONT_NAME, fontBuffer, false);
        if (font == -1) {
            throw new Exception("Could not add font");
        }
        colour = NVGColor.create();

        posx = MemoryUtil.memAllocDouble(1);
        posy = MemoryUtil.memAllocDouble(1);

    }


    private void _drawDeprecated(Window window) {
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
    }

    private void drawStats(Window window, OverlaySetting orientation) {
        // Setzt die Position für die Statistiken
        int offset = window.getWidth() / 30;
        int x = 0;
        int y = 0;
        int w = window.getWidth() / 8;
        int h = window.getHeight() / 4;
        switch (orientation) {
            case TOP_LEFT -> {
                x = offset; // x-Position
                y = offset; // y-Position
            }
            case TOP_RIGHT -> {
                x = window.getWidth() - offset - w; // x-Position
                y = offset; // y-Position
            }
            case BOTTOM_LEFT -> {
                x = offset; // x-Position
                y = window.getHeight() - offset - h; // y-Position
            }
            case BOTTOM_RIGHT -> {
                x = window.getWidth() - offset - w; // x-Position
                y = window.getHeight() - offset - h; // y-Position
            }
        }

        // Hintergrund für die Statistiken
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, w, h, 10.0f);
        nvgFillColor(vg, rgba(0x1a, 0x3b, 0x69, 70, colour));
        nvgFill(vg);


        // FPS Text
        nvgFontSize(vg, 16.0f);
        nvgFillColor(vg, rgba(0x80, 0x80, 0x80, 140, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, window.getWidth() - offset - 3, 15,
                String.format("%.2f FPS", fps));



        int dist = 32; // Abstand zwischen den Textzeilen
        x += 4; // Padding für den Text
        y += 4; // Padding für den Text

        // WIP
        nvgFontSize(vg, 25.0f);
        nvgFillColor(vg, rgba(0xff, 0x30, 0x30, 200, colour));
        nvgText(vg, x, y, "Work in Progress");

        // Statistiken zeichnen
        int color = 0xff; // Textfarbe
        nvgFontSize(vg, 25.0f);
        nvgFillColor(vg, rgba(color, color, color, 200, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);

        nvgText(vg, x, y + dist,
                String.format("Accuracy: %.2f%%", statTracker.getAccuracy()));
        nvgText(vg, x, y + 2*dist,
                String.format("Headshot Rate: %.2f%%", statTracker.getHeadshotRate()));
        nvgText(vg, x, y + 3*dist,
                String.format("Shots per Min: %.2f", statTracker.getShotsPerMinute()));
        nvgText(vg, x, y + 4*dist,
                String.format("Hits: %s", statTracker.getHits()));



    }

    private void drawWeaponInfo(Window window) {
        // Weiß und unten rechts (meiner Meinung nach hübscher)
        int offset = window.getWidth() / 14; // Offset for the text
        int x = window.getWidth() - 2*offset;
        int y = window.getHeight() - offset;
        // white background
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x-20, y-15, offset*1.6f, 4, 3.0f);
        nvgFillColor(vg, rgba(0xff, 0xff, 0xff, 100, colour));
        nvgFill(vg);


        int color = 0xff; // Textfarbe
        if (equippedWeapon != null) {
            nvgFontSize(vg, 25.0f);
            nvgFontFace(vg, FONT_NAME);
            nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
            // Textfarbe abhängig von der Munition
            NVGColor bullet_text_color = ((((float) equippedWeapon.getBulletCount()) / equippedWeapon.getAmmo()) > (1/4f)) ? rgba(color, color, color, 200, colour) : rgba(0xff, 0x30, 0x30, 200, colour);
            nvgFillColor(vg, bullet_text_color);
            nvgText(vg, x, y-40,
                    String.format("%02d/%02d",
                            equippedWeapon.getBulletCount(), equippedWeapon.getAmmo()));
            // Waffennamen
            nvgFillColor(vg, rgba(color, color, color, 200, colour)); // für max opacity a = 255
            nvgText(vg, x+offset, y-40,
                    equippedWeapon.getClass().getSimpleName());
        }
        else {
            nvgFontSize(vg, 25.0f);
            nvgFontFace(vg, FONT_NAME);
            nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
            nvgFillColor(vg, rgba(color, color, color, 255, colour));
            nvgText(vg, x, y,
                    "No Weapon Equipped");
        }
    }

    private void drawWeaponInfo2(Window window) {
        // Schwarz und unten links
        int offset = 80; // Offset for the text
        int x = offset;
        int y = window.getHeight() - offset;
        // white background
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x-20, y-60, (float) (offset *1.5), y/14, 10.0f);
        nvgFillColor(vg, rgba(0xff, 0xff, 0xff, 10, colour));
        nvgFill(vg);

        if (equippedWeapon != null) {
            nvgFontSize(vg, 25.0f);
            nvgFontFace(vg, FONT_NAME);
            nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
            nvgFillColor(vg, rgba(0x10, 0x10, 0x10, 200, colour)); // 255
            nvgText(vg, x, y,
                    String.format("%02d/%02d",
                            equippedWeapon.getBulletCount(), equippedWeapon.getAmmo()));

            nvgText(vg, x, y-40,
                    equippedWeapon.getClass().getSimpleName());
        }
        else {
            nvgFontSize(vg, 25.0f);
            nvgFontFace(vg, FONT_NAME);
            nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
            nvgFillColor(vg, rgba(0x00, 0x00, 0x00, 255, colour));
            nvgText(vg, x, y,
                    "No Weapon Equipped");
        }
    }

    public void drawCrosshair(Window window) {
        switch (crosshair) {
            case PLUS -> crosshairManager.drawPreset1(vg, window.getWidth() / 2, window.getHeight() / 2, 20, 0xff);
            case DOT -> crosshairManager.drawPreset2(vg, window.getWidth() / 2, window.getHeight() / 2, 2, 0xff);
            case SMALL_PLUS -> crosshairManager.drawPreset3(vg, window.getWidth() / 2, window.getHeight() / 2, 15, 0xff);
        }
    }

    public void render(Window window) {
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1.0f);
        calculateFPS();

        // _drawDeprecated(window);

        drawStats(window, OverlaySetting.TOP_RIGHT);
        drawWeaponInfo(window);
        drawCrosshair(window);

        nvgEndFrame(vg);

        window.restoreState();
    }

    public void render(Window window, OverlaySetting orientation) {
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1.0f);
        calculateFPS();

        // _drawDeprecated(window);

        drawStats(window, orientation);
        drawWeaponInfo(window);
        drawCrosshair(window);

        nvgEndFrame(vg);

        window.restoreState();
    }

    public void setCrosshair(Crosshairs crosshair) {
        this.crosshair = crosshair;
    }
    public Crosshairs getCrosshair() {
        return crosshair;
    }

    private void calculateFPS() {
        long currentTime = System.nanoTime();
        frameCount++;
        if (currentTime - timeSinceLastUpdate >= 1_000_000_000) { // 1 Sekunde
            fps = frameCount / ((currentTime - timeSinceLastUpdate) / 1_000_000_000.0);
            timeSinceLastUpdate = currentTime;
            frameCount = 0;
        }
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

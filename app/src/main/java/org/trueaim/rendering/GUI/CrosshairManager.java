package org.trueaim.rendering.GUI;

import org.lwjgl.nanovg.NVGColor;

import static org.trueaim.Utils.*;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.system.MemoryUtil.memAllocInt;


/**
 * Verwalter für das Fadenkreuz im Spiel.
 * Bietet verschiedene vordefinierte Fadenkreuz-Designs und zeichnet sie auf dem Bildschirm.
 */

public class CrosshairManager {
    NVGColor col;
    public CrosshairManager() {
        col = NVGColor.create();
    }


    // PLUS
    public void drawPreset1(long vg, int x, int y, int size, int graytone) {
        // nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1.0f);

        nvgBeginPath(vg);
        nvgStrokeWidth(vg, 2.0f);
        nvgMoveTo(vg, x - size, y);
        nvgLineTo(vg, x + size, y);
        nvgMoveTo(vg, x, y - size);
        nvgLineTo(vg, x, y + size);
        nvgStrokeColor(vg, rgba(graytone, graytone, graytone, 220, col));
        nvgStroke(vg);
        nvgClosePath(vg);


        // nvgEndFrame(vg);

    }

    // DOT
    public void drawPreset2(long vg, int x, int y, int size, int graytone) {
        nvgBeginPath(vg);
        nvgStrokeWidth(vg, 4.0f);
        nvgCircle(vg, x, y, size);
        nvgStrokeColor(vg, rgba(graytone, graytone, graytone, 220, col));
        nvgStroke(vg);
        nvgClosePath(vg);
    }

    // SMALL PLUS
    public void drawPreset3(long vg, int x, int y, int size, int graytone) {
        nvgBeginPath(vg);
        nvgStrokeWidth(vg, 2.0f);
        nvgMoveTo(vg, x - size, y);
        nvgLineTo(vg, x- size / 4f, y);
        nvgMoveTo(vg, x + size, y);
        nvgLineTo(vg, x + size / 4f, y);
        nvgMoveTo(vg, x, y - size);
        nvgLineTo(vg, x, y - size / 4f);
        nvgMoveTo(vg, x, y + size);
        nvgLineTo(vg, x, y + size / 4f);
        nvgStrokeColor(vg, rgba(graytone, graytone, graytone, 220, col));
        nvgStroke(vg);
        nvgClosePath(vg);
    }
    public void drawPreset3(long vg, int x, int y, int size, NVGColor color) {
        nvgBeginPath(vg);
        nvgStrokeWidth(vg, 2.0f);
        nvgMoveTo(vg, x - size, y);
        nvgLineTo(vg, x- size / 4f, y);
        nvgMoveTo(vg, x + size, y);
        nvgLineTo(vg, x + size / 4f, y);
        nvgMoveTo(vg, x, y - size);
        nvgLineTo(vg, x, y - size / 4f);
        nvgMoveTo(vg, x, y + size);
        nvgLineTo(vg, x, y + size / 4f);
        nvgStrokeColor(vg, color);
        nvgStroke(vg);
        nvgClosePath(vg);
    }

    // Very Small Plus
    public void drawPreset4(long vg, int x, int y, int size, int graytone) {
        nvgBeginPath(vg);
        nvgStrokeWidth(vg, 2.0f);
        nvgMoveTo(vg, x - size/2f, y);
        nvgLineTo(vg, x + size/2f, y);
        nvgMoveTo(vg, x, y - size/2f);
        nvgLineTo(vg, x, y + size/2f);
        nvgStrokeColor(vg, rgba(graytone, graytone, graytone, 220, col));
        nvgStroke(vg);
        nvgClosePath(vg);
    }

}

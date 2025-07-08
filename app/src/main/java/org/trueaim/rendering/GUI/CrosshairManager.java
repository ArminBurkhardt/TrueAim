package org.trueaim.rendering.GUI;

import org.lwjgl.nanovg.NVGColor;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.system.MemoryUtil.memAllocInt;

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



    private NVGColor rgba(int r, int g, int b, int a, NVGColor colour) {
        colour.r(r / 255.0f);
        colour.g(g / 255.0f);
        colour.b(b / 255.0f);
        colour.a(a / 255.0f);

        return colour;
    }
}

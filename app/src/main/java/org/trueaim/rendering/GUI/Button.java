package org.trueaim.rendering.GUI;

import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.system.MemoryUtil;
import org.trueaim.Utils;
import org.trueaim.Window;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Button {
    private int x;
    private int y;
    private int w;
    private int h;
    private String label;
    private boolean isPressed; // Button ist gedrückt (für Toggle-Buttons)
    private boolean togglable = false; // Button kann umgeschaltet werden
    private boolean enabled;
    private boolean isHovered;
    private Runnable onClickAction;
    private boolean isVisible;      // Sichtbarkeit des Buttons, kann unsichtbar sein, aber immer noch aktiv
    private NVGColor colorA;
    private NVGColor colorB;
    private NVGColor colorC;
    private NVGPaint paint;
    private final String FONT_NAME;
    DoubleBuffer posx, posy; // Position für Mauszeiger
    private Runnable drawInButton = null; // Optionale Funktion für benutzerdefinierte Zeichnung im Button

    public Button(int x, int y, int width, int height, String label, Runnable onClickAction, String fontName) {
        this.x = x;
        this.y = y;
        this.w = width;
        this.h = height;
        this.label = label;
        this.onClickAction = onClickAction;
        this.isPressed = false;
        this.isHovered = false;
        this.enabled = true; // Button ist standardmäßig aktiviert
        this.isVisible = true; // Button ist standardmäßig sichtbar
        this.FONT_NAME = fontName;
        init();

    }
    public Button(int x, int y, int width, int height, String label, Runnable onClickAction, String fontName, Runnable drawInButton) {
        this.x = x;
        this.y = y;
        this.w = width;
        this.h = height;
        this.label = label;
        this.onClickAction = onClickAction;
        this.isPressed = false;
        this.isHovered = false;
        this.enabled = true; // Button ist standardmäßig aktiviert
        this.isVisible = true; // Button ist standardmäßig sichtbar
        this.FONT_NAME = fontName;
        this.drawInButton = drawInButton;
        init();

    }

    public Button(int x, int y, int width, int height, String label, Runnable onClickAction, String fontName, Runnable drawInButton, boolean togglable) {
        this(x, y, width, height, label, onClickAction, fontName, drawInButton);
        this.togglable = togglable; // Button kann umgeschaltet werden

    }

    public Button(int x, int y, int width, int height, String label, Runnable onClickAction, String fontName, boolean togglable) {
        this(x, y, width, height, label, onClickAction, fontName);
        this.togglable = togglable; // Button kann umgeschaltet werden

    }

    public void init() {
        colorA = NVGColor.create();
        colorB = NVGColor.create();
        colorC = NVGColor.create();
        paint = NVGPaint.create();

        posx = MemoryUtil.memAllocDouble(1);
        posy = MemoryUtil.memAllocDouble(1);

    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }
    public boolean isVisible() {
        return isVisible;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        this.enabled = true;
    }
    public void disable() {
        this.enabled = false;
    }

    public boolean isPressed() {
        return isPressed;
    }
    public void setPressed(boolean pressed) {
        this.isPressed = pressed;
    }

    public void setOnClickAction(Runnable onClickAction) {
        this.onClickAction = onClickAction;
    }

    public void onClick() {
        if (onClickAction != null && isHovered && enabled) {
            onClickAction.run();
        }
    }

    /**
     * Rendert den Button im angegebenen Fenster. Sollte nur innerhalb eines Frames aufgerufen werden.
     * @param window
     */
    public void render(long vg, Window window) {
        if (!isVisible) return; // Button nicht rendern, wenn er nicht sichtbar ist

        glfwGetCursorPos(window.getHandle(), posx, posy);
        double mouseX = posx.get(0);
        double mouseY = posy.get(0);
        isHovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        // glfwSetCursorPos(window.getHandle(), x + w / 2, y + h / 2); // Mauszeiger zentrieren

        // Button Hintergrund
        if (enabled && isPressed) {
            nvgBoxGradient(vg, x, y + 4, w, h, 4 * 2, 20, rgba(0x20, 0x20, 0x20, 255, colorA), rgba(0x30, 0x30, 0x30, 200, colorB), paint);
        } else if (isHovered && enabled) {
            nvgBoxGradient(vg, x, y + 4, w, h, 4 * 2, 20, rgba(0x1a, 0x3b, 0x69, 255, colorA), rgba(0x1a, 0x3b, 0x69, 180, colorB), paint);
        } else if (enabled) {
            nvgBoxGradient(vg, x, y + 4, w, h, 4 * 2, 20, rgba(0x10, 0x20, 0x30, 255, colorA), rgba(0, 0, 0, 200, colorB), paint);
        } else {
            nvgBoxGradient(vg, x, y + 4, w, h, 4 * 2, 20, rgba(0, 0, 0, 255, colorA), rgba(0, 0, 0, 200, colorB), paint);
        }
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, w, h, 10.0f);
        nvgFillPaint(vg, paint);
        nvgFill(vg);

        if (drawInButton != null) {
            drawInButton.run(); // Benutzerdefinierte Zeichnung im Button ausführen
        }

        // Button Text
        int color = (enabled) ? 0xff: 0x80; // Textfarbe
        nvgFontSize(vg, 25.0f);
        nvgFontFace(vg, FONT_NAME);
        nvgFillColor(vg, rgba(color, color, color, 200, colorC));
        nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_MIDDLE);
        nvgText(vg, x + w / 2f, y + h / 2f, label);

    }

    private NVGColor rgba(int r, int g, int b, int a, NVGColor colour) {
        colour.r(r / 255.0f);
        colour.g(g / 255.0f);
        colour.b(b / 255.0f);
        colour.a(a / 255.0f);

        return colour;
    }

}

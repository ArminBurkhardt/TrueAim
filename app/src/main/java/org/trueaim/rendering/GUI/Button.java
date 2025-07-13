package org.trueaim.rendering.GUI;

import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.system.MemoryUtil;
import org.trueaim.Window;
import org.trueaim.sound.SoundPlayer;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.trueaim.Utils.rgba;

/**
 * Repräsentiert einen interaktiven Button im GUI.
 * Der Button kann geklickt (mit Funktionbindings) und angepasst werden.
 * Er unterstützt benutzerdefinierte Zeichnungen und kann sichtbar oder unsichtbar sein.
 * Spzialfälle:
 *      - "TOGGLE" Button für toggleable = true, der zwischen "Enabled" und "Disabled" umschaltet.
 *      - "Quit" Button: ist rot.
 */
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

    /**
     * Konstruktor für einen Button mit benutzerdefinierter Zeichnung.
     * @param x X-Position des Buttons
     * @param y Y-Position des Buttons
     * @param width Breite des Buttons
     * @param height Höhe des Buttons
     * @param label Textlabel des Buttons
     * @param onClickAction Aktion, die beim Klicken auf den Button ausgeführt wird
     * @param fontName Name der Schriftart für den Button-Text
     */
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

    /**
     * Konstruktor für einen Button mit benutzerdefinierter Zeichnung.
     * @param x X-Position des Buttons
     * @param y Y-Position des Buttons
     * @param width Breite des Buttons
     * @param height Höhe des Buttons
     * @param label Textlabel des Buttons
     * @param onClickAction Aktion, die beim Klicken auf den Button ausgeführt wird
     * @param fontName Name der Schriftart für den Button-Text
     * @param drawInButton Optional: Funktion, die im Button gezeichnet wird (kann null sein)
     */
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

    /**
     * Konstruktor für einen Button, der umschaltbar ist (Togglable).
     * @param x X-Position des Buttons
     * @param y Y-Position des Buttons
     * @param width Breite des Buttons
     * @param height Höhe des Buttons
     * @param label Textlabel des Buttons
     * @param onClickAction Aktion, die beim Klicken auf den Button ausgeführt wird
     * @param fontName Name der Schriftart für den Button-Text
     * @param drawInButton Optional: Funktion, die im Button gezeichnet wird (kann null sein)
     * @param togglable Gibt an, ob der Button umschaltbar ist. Falls true, kann der Button zwischen gedrückt und nicht gedrückt wechseln. Mit label "TOGGLE" kann der Button selbst "Enabled" und "Disabled" setzten.
     */
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
            if (togglable) {
                isPressed = !isPressed; // Toggle-Zustand umschalten
            }
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

        int redoffset = 0; // Offset für den Button, falls benötigt

        // Spezieller Offset für bestimmte Buttons
        if (label.equals("Quit")) {
            redoffset = 0x70; // Offset für Quit-Button
        }

        // Font-Größe basierend auf Fenstergröße
        float fontSize = window.getWidth() / 92.14f;

        // Label setzen
        String localLabel = label; // Lokales Label für den Button

        // Button Hintergrund
        if (togglable && this.label.equals("TOGGLE")) {
            // Spzialfall für togglable Buttons
            localLabel = (isPressed) ? "Enabled" : "Disabled"; // Label anpassen basierend auf gedrückt oder nicht gedrückt, setzt lokales label
            int colorOffset = (isHovered) ? 0x40 : 0x00; // Offset für die Farbe basierend auf Hover-Zustand
            if (isPressed) {
                nvgBoxGradient(vg, x, y + 4, w, h, 4 * 2, 20, rgba(0x20, 0xA0, 0x20 + colorOffset, 255, colorA), rgba(0x30, 0xA8, 0x30 + colorOffset, 200, colorB), paint);
            } else {
                nvgBoxGradient(vg, x, y + 4, w, h, 4 * 2, 20, rgba(0x90, 0x20, 0x20 + colorOffset, 255, colorA), rgba(0x80, 0x10, 0x10 + colorOffset, 200, colorB), paint);
            }

        } else if (enabled && isPressed) {
            nvgBoxGradient(vg, x, y + 4, w, h, 4 * 2, 20, rgba(0x20 + redoffset, 0x20, 0x20, 255, colorA), rgba(0x30 + redoffset, 0x30, 0x30, 200, colorB), paint);
        } else if (isHovered && enabled) {
            nvgBoxGradient(vg, x, y + 4, w, h, 4 * 2, 20, rgba(0x1a, 0x3b, 0x69, 255, colorA), rgba(0x1a, 0x3b, 0x69, 180, colorB), paint);
        } else if (enabled) {
            nvgBoxGradient(vg, x, y + 4, w, h, 4 * 2, 20, rgba(0x10 + redoffset, 0x20, 0x30, 255, colorA), rgba(0 + redoffset, 0, 0, 200, colorB), paint);
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
        nvgFontSize(vg, fontSize);
        nvgFontFace(vg, FONT_NAME);
        nvgFillColor(vg, rgba(color, color, color, 200, colorC));
        nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_MIDDLE);
        nvgText(vg, x + w / 2f, y + h / 2f, localLabel);

    }


}

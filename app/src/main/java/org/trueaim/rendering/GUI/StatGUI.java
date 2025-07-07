package org.trueaim.rendering.GUI;

// Nützlich: https://github.com/SpinyOwl/SpinyGUI/blob/develop/demo.simple/src/main/java/com/spinyowl/spinygui/demo/simple/OtherMain.java


import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.system.MemoryUtil;
import org.trueaim.Utils;
import org.trueaim.Window;
import org.trueaim.stats.StatTracker;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_STENCIL_STROKES;
import static org.lwjgl.nanovg.NanoVGGL3.nvgCreate;
import static org.lwjgl.system.MemoryUtil.NULL;

public class StatGUI {
    private boolean visible = false; // Sichtbarkeit des HUDs
    private final StatTracker statTracker; // Statistiken-Tracker
    private long vg;
    private ByteBuffer fontBuffer;
    private NVGColor colour;
    private static final String FONT_NAME = "OpenSans-Bold";
    private DoubleBuffer posx, posy; // Position für Textanzeige
    private List<Button> buttons; // Liste von Buttons
    public StatGUI(Window window, StatTracker statTracker) {
        try {
            this.init(window);
            this.initElements(window);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        this.statTracker = statTracker;
    }

    // TODO: Vielleicht einen Setter für StatTracker hinzufügen (& in der GameEngine setzen), falls sich die Waffe ändert und das nicht übernommen wird

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

    private void initElements(Window window) {
        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();
        int offset1 = 100; // Abstand von oben
        int buttonWidth = 200; // Breite der Buttons
        int buttonHeight = 50; // Höhe der Buttons
        buttons = new ArrayList<Button>();

        // Erzeugt die Buttons und fügt sie der Liste hinzu
        Button b1 = new Button(windowWidth-offset1 - buttonWidth, offset1, buttonWidth, buttonHeight, "Quit", window::forceClose, FONT_NAME); // Quit-Button
        buttons.add(b1);


    }

    private void drawBackground(Window window) {
        nvgBeginPath(vg);
        nvgRect(vg, 0, 0, window.getWidth(), window.getHeight()); // Beispielgröße
        rgba(0, 0, 0, 200, colour); // Halbtransparenter schwarzer Hintergrund
        nvgFillColor(vg, colour);
        nvgFill(vg);
    }


    public void onClick() {
        // Überprüfen, ob ein Button geklickt wurde
        for (Button button : buttons) {
            button.onClick();
        }
    }



    /**
     * Rendert das HUD, wenn es sichtbar ist.
     * @param window Das Fenster, in dem das HUD gerendert wird.
     */
    public void render(Window window) {
        if (!visible) return; // Wenn HUD nicht sichtbar, nichts rendern

        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1.0f);

        drawBackground(window);

        // Alle Buttons rendern
        for (Button button : buttons) {
            button.render(vg, window);
        }

        nvgEndFrame(vg); // Frame beenden
    }


    public void enable() {
        visible = true; // HUD anzeigen
    }
    public void disable() {
        visible = false; // HUD verstecken
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

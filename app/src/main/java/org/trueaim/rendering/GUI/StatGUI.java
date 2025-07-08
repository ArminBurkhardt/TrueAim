package org.trueaim.rendering.GUI;

// Nützlich: https://github.com/SpinyOwl/SpinyGUI/blob/develop/demo.simple/src/main/java/com/spinyowl/spinygui/demo/simple/OtherMain.java


import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.system.MemoryUtil;
import org.trueaim.Utils;
import org.trueaim.Window;
import org.trueaim.entities.targets.TargetManager;
import org.trueaim.rendering.OverlayRenderer;
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
    private CrosshairManager crosshairManager;
    private OverlayRenderer overlayRenderer; // Renderer für Overlay-Elemente, wird verwendet, um das Fadenkreuz zu zeichnen
    private TargetManager targetManager; // Manager für Ziele, wird verwendet, um die Ziele zu ändern

    private int windowWidth; // Breite des Fensters
    private int windowHeight; // Höhe des Fensters
    private int offset1; // Abstand 1
    private int offset2; // Abstand 2
    private int offset3; // Abstand 3
    private int buttonWidth; // Breite der Buttons
    private int buttonHeight; // Höhe der Buttons
    private int buttonOffset; // Abstand zwischen den Buttons
    private int r = 6; // Radius für die Ecken der Buttons
    private int f = 30; // Abstand für die Box-Gradienten

    private NVGColor colorA;
    private NVGColor colorB;
    private NVGColor colorC;
    private NVGPaint paint;


    public StatGUI(Window window, StatTracker statTracker, OverlayRenderer overlayRenderer, TargetManager targetManager) {
        try {
            this.init(window);
            this.initElements(window);
            this.setValues(window);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        this.statTracker = statTracker;
        this.crosshairManager = new CrosshairManager();
        this.overlayRenderer = overlayRenderer;
        this.targetManager = targetManager;
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
        colorA = NVGColor.create();
        colorB = NVGColor.create();
        colorC = NVGColor.create();
        paint = NVGPaint.create();

        posx = MemoryUtil.memAllocDouble(1);
        posy = MemoryUtil.memAllocDouble(1);

    }

    private void setValues(Window window) {
        // Setzt die Werte für die Position und Größe der Buttons
        windowWidth = window.getWidth();
        windowHeight = window.getHeight();
        offset1 = 100; // Abstand von oben
        offset2 = 40; // Abstand von oben
        offset3 = 20;
        buttonWidth = 220; // Breite der Buttons
        buttonHeight = 50; // Höhe der Buttons

    }

    // Diese Methode ist ein Platzhalter
    protected void none() {}

    private void initElements(Window window) {
        setValues(window);
        buttons = new ArrayList<Button>();

        // Erzeugt die Buttons und fügt sie der Liste hinzu
        Button b1 = new Button(windowWidth-offset1 - buttonWidth, offset1, buttonWidth, buttonHeight, "Quit", window::forceClose, FONT_NAME); // Quit-Button
        buttons.add(b1);

        // Target Sektion
        Button b2 = new Button(offset1 + offset2, offset1 + offset2 + offset3, buttonWidth, buttonHeight, "Static Targets", this::_setStaticTargets, FONT_NAME);
        buttons.add(b2);
        Button b3 = new Button(offset1 + offset2*2 + buttonWidth, offset1 + offset2 + offset3, buttonWidth, buttonHeight, "Moving Targets", this::_setMovingTargets, FONT_NAME);
        buttons.add(b3);
        Button b4 = new Button(offset1 + offset2*3 + buttonWidth*2, offset1 + offset2 + offset3, buttonWidth, buttonHeight, "Random Targets", this::_setRandomTargets, FONT_NAME);
        buttons.add(b4);

        // Crosshair Sektion
        Button b5 = new Button(offset1 + buttonWidth*5 + offset2, offset1 + offset2 + offset3, buttonWidth, buttonHeight, "Default", this::_setDefaultCrosshair, FONT_NAME);
        buttons.add(b5);
        Button b6 = new Button(offset1 + buttonWidth*5 + offset2*2 + buttonWidth, offset1 + offset2 + offset3, buttonWidth, buttonHeight, "Add Custom", this::none, FONT_NAME);
        b6.disable();
        buttons.add(b6);
        Button b7 = new Button(offset1 + buttonWidth*5 + offset2, offset1 + offset2*2 + offset3 + buttonHeight, buttonWidth/2, buttonWidth/2, "", this::_setCrosshair1, FONT_NAME, this::_crosshair1);
        buttons.add(b7);
        Button b8 = new Button((int) (offset1 + buttonWidth*5.5f + offset2*2), offset1 + offset2*2 + offset3 + buttonHeight, buttonWidth/2, buttonWidth/2, "", this::_setCrosshair2, FONT_NAME, this::_crosshair2);
        buttons.add(b8);
        Button b9 = new Button(offset1 + buttonWidth*5 + offset2, offset1 + offset2*3 + offset3 + buttonHeight + buttonWidth/2, buttonWidth/2, buttonWidth/2, "", this::_setCrosshair3, FONT_NAME, this::_crosshair3);
        buttons.add(b9);




    }

    // Methoden zum Setzen der Ziele

    private void _setStaticTargets() {
        targetManager.setTargets(0, 5); // Setzt 4 statische Ziele
    }
    private void _setMovingTargets() {
        targetManager.setTargets(5, 0);
    }
    private void _setRandomTargets() {
        targetManager.setTargetsRandom(5);
    }


    // Methoden zum Zeichnen und Setzen der Fadenkreuze

    private void _crosshair1() {
        crosshairManager.drawPreset1(vg, (int) (offset1 + buttonWidth*5 + offset2 + buttonWidth / 4), (int) (offset1 + offset2*2 + offset3 + buttonHeight + buttonWidth / 4), 20, 0xff);
    }
    private void _crosshair2() {
        crosshairManager.drawPreset2(vg, (int) (offset1 + buttonWidth*5.5f + offset2*2 + buttonWidth / 4), (int) (offset1 + offset2*2 + offset3 + buttonHeight + buttonWidth / 4), 2, 0xff);
    }
    private void _crosshair3() {
        crosshairManager.drawPreset3(vg, (int) (offset1 + buttonWidth*5 + offset2 + buttonWidth / 4), (int) (offset1 + offset2*3 + offset3 + buttonHeight + buttonWidth / 4 + buttonWidth/2), 15, 0xff);
    }

    private void _setDefaultCrosshair() {
        overlayRenderer.setCrosshair(Crosshairs.DEFAULT);
    }

    private void _setCrosshair1() {
        overlayRenderer.setCrosshair(Crosshairs.PLUS);
    }
    private void _setCrosshair2() {
        overlayRenderer.setCrosshair(Crosshairs.DOT);
    }
    private void _setCrosshair3() {
        overlayRenderer.setCrosshair(Crosshairs.SMALL_PLUS);
    }


    // Zeichnet den Hintergrund des HUDs (grautransparent)
    private void drawBackground(Window window) {
        nvgBeginPath(vg);
        nvgRect(vg, 0, 0, window.getWidth(), window.getHeight()); // Beispielgröße
        rgba(0, 0, 0, 200, colour); // Halbtransparenter schwarzer Hintergrund
        nvgFillColor(vg, colour);
        nvgFill(vg);
    }


    private void drawTargetSection(Window window) {
        // Hintergrund
        nvgBoxGradient(vg, offset1, offset1, buttonWidth * 4.5f, buttonHeight * 4.5f, r * 2, f, rgba(0x2a, 0x4b, 0x59, 255, colorA), rgba(0x1a, 0x3b, 0x69, 180, colorB), paint);
        nvgBeginPath(vg);
        nvgRoundedRect(vg, offset1, offset1, buttonWidth * 4.5f, buttonHeight * 4.5f, 10.0f);
        nvgFillPaint(vg, paint);
        nvgFill(vg);

        // Text
        int color = 0xff; // Textfarbe
        nvgFontSize(vg, 28.0f);
        nvgFillColor(vg, rgba(color, color, color, 140, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, offset1 + 20, offset1 + 20, "Target Settings"); // Titel des Abschnitts
    }


    private void drawCrosshairSection(Window window) {
        // Hintergrund
        nvgBoxGradient(vg, offset1 + buttonWidth*5f, offset1, buttonWidth * 3.5f, buttonHeight * 8.5f, r * 2, f, rgba(0x2a, 0x4b, 0x59, 255, colorA), rgba(0x1a, 0x3b, 0x69, 180, colorB), paint);
        nvgBeginPath(vg);
        nvgRoundedRect(vg, offset1 + buttonWidth*5f, offset1, buttonWidth * 3.5f, buttonHeight * 8.5f, 10.0f);
        nvgFillPaint(vg, paint);
        nvgFill(vg);

        // Text
        int color = 0xff; // Textfarbe
        nvgFontSize(vg, 28.0f);
        nvgFillColor(vg, rgba(color, color, color, 140, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, offset1 + buttonWidth*5f + 20, offset1 + 20, "Crosshair Options"); // Titel des Abschnitts
    }

    private void drawStatSection(Window window) {
        // Hintergrund

        nvgBoxGradient(vg, offset1, windowHeight / 2f, windowWidth - offset1*2, windowHeight / 2f - offset1*2, r * 2, f, rgba(0x1c, 0x7e, 0x80, 255, colorA), rgba(0x0a, 0x5b, 0x69, 240, colorB), paint);
        nvgBeginPath(vg);
        nvgRoundedRect(vg, offset1, windowHeight / 2f, windowWidth - offset1*2, windowHeight / 2f - offset1*2, 10.0f);
        nvgFillPaint(vg, paint);
        nvgFill(vg);

        // Text
        int color = 0xff; // Textfarbe
        nvgFontSize(vg, 28.0f);
        nvgFillColor(vg, rgba(color, color, color, 140, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, offset1 + 20, windowHeight / 2f  + 20, "Statistics"); // Titel des Abschnitts


        // Placeholder Text
        nvgFontSize(vg, 108.0f);
        nvgFillColor(vg, rgba(0xff, 0x0, 0x0, 200, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, offset1*6 + 20, windowHeight / 1.6f  + 20, "COMING SOON (nya :3)"); // Titel des Abschnitts
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

        setValues(window);

        drawBackground(window);

        drawTargetSection(window);
        drawCrosshairSection(window);
        drawStatSection(window);

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
    public boolean isVisible() {
        return visible; // Sichtbarkeit des HUDs zurückgeben
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

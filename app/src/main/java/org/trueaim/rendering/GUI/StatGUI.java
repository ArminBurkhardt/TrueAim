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

/**
 * StatGUI ist eine Klasse, die ein GUI für Statistiken und Einstellungen im Spiel bereitstellt.
 * Hauptsächlich Buttons zum Einstellen von Crosshair/Targets/Waffe/...
 * Die Klasse verwaltet auch die Sichtbarkeit des GUIs und zeichnet es auf dem Bildschirm.
 */

public class StatGUI {
    private boolean visible = false; // Sichtbarkeit des HUDs
    private final StatTracker statTracker; // Statistiken-Tracker
    private long vg;
    private ByteBuffer fontBuffer;
    private NVGColor colour;
    private static final String FONT_NAME = "OpenSans-Bold";
    private DoubleBuffer posx, posy; // Position für Textanzeige
    private List<Button> buttons; // Liste von Buttons
    private List<Plot> plots; // Liste von Plots
    private CrosshairManager crosshairManager;
    private OverlayRenderer overlayRenderer; // Renderer für Overlay-Elemente, wird verwendet, um das Fadenkreuz zu zeichnen
    private TargetManager targetManager; // Manager für Ziele, wird verwendet, um die Ziele zu ändern
    private boolean gunHasRecoil = false; // Flag, ob Recoil aktiviert ist (für die Gun-Sektion)
    private Button recoilButton; // Button für Recoil, der toggled werden kann

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
    private float fontSize = 28f; // Schriftgröße für den Text

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
        // Setzt die Werte für die Position und Größe der Buttons (abgeleitet von der Fenstergröße)
        windowWidth = window.getWidth();
        windowHeight = window.getHeight();
        offset1 = windowWidth / 24; // 100; // Abstand von oben
        offset2 = offset1 / 2; // 40; // Abstand von oben
        offset3 = offset2 / 2; // 20;
        buttonWidth = (int) (offset1 * 2.2f); // Breite der Buttons
        buttonHeight = (int) (offset3 * 2.25f); // Höhe der Buttons
        fontSize = windowWidth / 102f; // Schriftgröße für den Text

    }

    // Diese Methode ist ein Platzhalter
    protected void none() {}

    /**
     * Initialisiert die GUI-Elemente (Buttons, Plots, etc.) und fügt sie der Liste hinzu.
     * @param window Das Fenster, in dem die GUI gerendert wird.
     */
    private void initElements(Window window) {
        setValues(window);
        buttons = new ArrayList<Button>();
        plots = new ArrayList<Plot>();

        // Erzeugt die Buttons und fügt sie der Liste hinzu
        Button b1 = new Button(windowWidth-offset1 - buttonWidth, offset1*2, buttonWidth, buttonHeight, "Quit", window::forceClose, FONT_NAME); // Quit-Button
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
        Button b9 = new Button(offset1 + buttonWidth*5 + offset2, offset1 + offset2*3 + buttonHeight + buttonWidth/2, buttonWidth/2, buttonWidth/2, "", this::_setCrosshair3, FONT_NAME, this::_crosshair3);
        buttons.add(b9);


        // Statistiken Sektion

        Button b10 = new Button(offset1*2, windowHeight / 2 + offset1, buttonWidth, buttonHeight, "Reset Stats", this::_resetStats, FONT_NAME);
        buttons.add(b10);


        // Button zum Schlie0en des GUIs
        Button b11 = new Button(windowWidth-offset1 - buttonWidth/4, offset1, buttonWidth/4, buttonWidth/4, "X", this::disable, FONT_NAME);
        buttons.add(b11);


        // Gun Sektion
        Button b12 = new Button(offset1 + offset3*2, (int) (offset1 + offset2 + buttonHeight * 4.5f + offset2 + offset3*2), buttonWidth, buttonHeight, "TOGGLE", this::_recoilToggle, FONT_NAME, true);
        // Standardmäßig ist Recoil aktiviert
        b12.setPressed(true);
        this.gunHasRecoil = true;
        buttons.add(b12);
        this.recoilButton = b12; // Speichert den Recoil-Button für späteren Zugriff

        // User Sektion
        Button b13 = new Button((int) (buttonWidth * 4.5f + offset1 - buttonWidth * 1f - offset1*2 + offset3), (int) (offset1 + offset2 + buttonHeight * 4.5f + offset2 + offset3), buttonHeight, buttonHeight, "-", this::_decreaseSensitivity, FONT_NAME);
        buttons.add(b13);
        Button b14 = new Button((int) (buttonWidth * 4.5f + offset1 - buttonWidth * 1f - offset1*2 + buttonHeight + offset3*2 + offset2), (int) (offset1 + offset2 + buttonHeight * 4.5f + offset2 + offset3), buttonHeight, buttonHeight, "+", this::_increaseSensitivity, FONT_NAME);
        buttons.add(b14);

        Button b15 = new Button((int) (buttonWidth * 4.5f + offset1 - buttonWidth * 1f - offset1*2 + offset3), (int) (offset1 + offset2 + buttonHeight * 4.5f + offset2 + offset3+2 + buttonHeight), buttonHeight, buttonHeight, "-", this::_decreaseFOV, FONT_NAME);
        buttons.add(b15);
        Button b16 = new Button((int) (buttonWidth * 4.5f + offset1 - buttonWidth * 1f - offset1*2 + buttonHeight + offset3*2 + offset2), (int) (offset1 + offset2 + buttonHeight * 4.5f + offset2 + offset3+2 + buttonHeight), buttonHeight, buttonHeight, "+", this::_increaseFOV, FONT_NAME);
        buttons.add(b16);


        // Plots Sektion
        float[][] placeholderData = new float[][] {{1, 1}, {-1, -1}};
        int[][] placeholderColors = new int[][] {{0x0, 0x0, 0xff, 0xff}, {0x0, 0xf0, 0x00, 0xff}}; // Beispiel-Daten für die Plots
        Plot plot1 = new Plot(offset1*2 + buttonWidth + offset2 + offset2, windowHeight / 2 + offset1, buttonWidth * 2, buttonWidth * 2, "Heatmap (am besten ArrayList<float> mit [x error, y error, hit?] pro Element)", placeholderData, "scatter");
        plot1.setDataSingleRGBA(placeholderColors);
        plot1.setFontSize(windowWidth/122f);
        plots.add(plot1);







    }

    // ///////////////////////////
    // Methoden für die Buttons //
    // ///////////////////////////

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
        crosshairManager.drawPreset1(vg, (int) (offset1 + buttonWidth*5 + offset2 + buttonWidth / 4f), (int) (offset1 + offset2*2 + offset3 + buttonHeight + buttonWidth / 4), offset3, 0xff);
    }
    private void _crosshair2() {
        crosshairManager.drawPreset2(vg, (int) (offset1 + buttonWidth*5.5f + offset2*2 + buttonWidth / 4f), (int) (offset1 + offset2*2 + offset3 + buttonHeight + buttonWidth / 4), 2, 0xff);
    }
    private void _crosshair3() {
        crosshairManager.drawPreset3(vg, (int) (offset1 + buttonWidth*5 + offset2 + buttonWidth / 4f), (int) (offset1 + offset2*3 + buttonHeight + buttonWidth / 4 + buttonWidth/2), windowWidth / 128, 0xff);
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

    // Methoden der Statistiken
    private void _resetStats() {
        statTracker.resetStats(); // Setzt die Statistiken zurück
    }

    // Methoden für die Gun-Sektion
    private void _recoilToggle() {
        this.gunHasRecoil = !this.gunHasRecoil; // Toggle für Recoil
    }

    // Methoden für die User-Sektion
    private void _decreaseSensitivity() {

    }
    private void _increaseSensitivity() {

    }
    private void _increaseFOV() {

    }
    private void _decreaseFOV() {

    }
    // TODO: fill methods
    private int _getFOV() {
        return 0;
    }
    private int _getSensitivity() {
        return 0;
    }







    // Zeichnet den Hintergrund des GUIs (grautransparent)
    private void drawBackground(Window window) {
        nvgBeginPath(vg);
        nvgRect(vg, 0, 0, window.getWidth(), window.getHeight()); // Beispielgröße
        rgba(0, 0, 0, 200, colour); // Halbtransparenter schwarzer Hintergrund
        nvgFillColor(vg, colour);
        nvgFill(vg);
    }

    // Zeichnet die Target-Sektion des GUIs
    private void drawTargetSection() {
        // Hintergrund
        nvgBoxGradient(vg, offset1, offset1, buttonWidth * 4.5f, buttonHeight * 4.5f, r * 2, f, rgba(0x2a, 0x4b, 0x59, 255, colorA), rgba(0x1a, 0x3b, 0x69, 180, colorB), paint);
        nvgBeginPath(vg);
        nvgRoundedRect(vg, offset1, offset1, buttonWidth * 4.5f, buttonHeight * 4.5f, 10.0f);
        nvgFillPaint(vg, paint);
        nvgFill(vg);

        // Text
        int color = 0xff; // Textfarbe
        nvgFontSize(vg, fontSize);
        nvgFillColor(vg, rgba(color, color, color, 140, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, offset1 + offset3, offset1 + offset3, "Target Settings"); // Titel des Abschnitts
    }

    // Zeichnet die Crosshair-Sektion des GUIs
    private void drawCrosshairSection() {
        // Hintergrund
        nvgBoxGradient(vg, offset1 + buttonWidth*5f, offset1, buttonWidth * 3.5f, buttonHeight * 8.5f, r * 2, f, rgba(0x2a, 0x4b, 0x59, 255, colorA), rgba(0x1a, 0x3b, 0x69, 180, colorB), paint);
        nvgBeginPath(vg);
        nvgRoundedRect(vg, offset1 + buttonWidth*5f, offset1, buttonWidth * 3.5f, buttonHeight * 8.5f, 10.0f);
        nvgFillPaint(vg, paint);
        nvgFill(vg);

        // Text
        int color = 0xff; // Textfarbe
        nvgFontSize(vg, fontSize);
        nvgFillColor(vg, rgba(color, color, color, 140, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, offset1 + buttonWidth*5f + offset3, offset1 + offset3, "Crosshair Options"); // Titel des Abschnitts
    }


    // Zeichnet die Gun-Sektion des GUIs (Recoil, etc.)
    private void drawGunSection() {
        // Hintergrund
        nvgBoxGradient(vg, offset1, offset1 + offset2 + buttonHeight * 4.5f , buttonWidth * 1f + offset1*2, buttonHeight * 2f + offset1, r * 2, f, rgba(0x2a, 0x4b, 0x59, 255, colorA), rgba(0x1a, 0x3b, 0x69, 180, colorB), paint);
        nvgBeginPath(vg);
        nvgRoundedRect(vg, offset1, offset1 + offset2 + buttonHeight * 4.5f , buttonWidth * 1f + offset1*2, buttonHeight * 2f + offset1, 10.0f);
        nvgFillPaint(vg, paint);
        nvgFill(vg);

        // Text
        int color = 0xff; // Textfarbe
        nvgFontSize(vg, fontSize);
        nvgFillColor(vg, rgba(color, color, color, 140, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, offset1+ offset3, offset1 + offset2 + buttonHeight * 4.5f + offset3, "Gun"); // Titel des Abschnitts


        nvgText(vg, offset1+ offset3*2, offset1 + offset2 + buttonHeight * 4.5f + offset2 + offset3, "Recoil"); // Titel des Abschnitts


    }

    private void drawUserSection() {
        // Hintergrund
        float width = buttonWidth * 1f + offset1*2f;
        nvgBoxGradient(vg, buttonWidth * 4.5f + offset1 - width, offset1 + offset2 + buttonHeight * 4.5f , width, buttonHeight * 2f + offset1, r * 2, f, rgba(0x2a, 0x4b, 0x59, 255, colorA), rgba(0x1a, 0x3b, 0x69, 180, colorB), paint);
        nvgBeginPath(vg);
        nvgRoundedRect(vg, buttonWidth * 4.5f + offset1 - width, offset1 + offset2 + buttonHeight * 4.5f , width, buttonHeight * 2f + offset1, 10.0f);
        nvgFillPaint(vg, paint);
        nvgFill(vg);

        // Text
        int color = 0xff; // Textfarbe
        nvgFontSize(vg, fontSize);
        nvgFillColor(vg, rgba(color, color, color, 140, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, buttonWidth * 4.5f + offset1 - width + offset3, offset1 + offset2 + buttonHeight * 4.5f + offset3, "User"); // Titel des Abschnitts


        // Sensitivität Buttons
        nvgFillColor(vg, rgba(color, color, color, 200, colour));
        nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_MIDDLE);
        nvgText(vg, buttonWidth * 4.5f + offset1 - buttonWidth * 1f - offset1*2 + buttonHeight*4 + offset3*2 + offset2, (int) (offset1 + offset2 + buttonHeight * 4.5f + offset2 + offset3*2), "Sensitivity"); // Titel des Abschnitts
        // aktuelle Sensitivität
        nvgFontSize(vg, fontSize * 0.9f);
        nvgFontFace(vg, FONT_NAME);
        nvgText(vg, buttonWidth * 4.5f + offset1 - width + offset3*2.5f + buttonHeight, (int) (offset1 + offset2 + buttonHeight * 4.5f + offset2 + offset3*2), String.format("%d", _getSensitivity())); // Aktuelle Sensitivität anzeigen

        // FOV Buttons
        nvgFontSize(vg, fontSize);
        nvgText(vg, buttonWidth * 4.5f + offset1 - buttonWidth * 1f - offset1*2 + buttonHeight*4 + offset3*2 + offset2, (int) (offset1 + offset2 + buttonHeight * 5f + offset2+2 + offset3 + buttonHeight), "FOV"); // Titel des Abschnitts
        // aktuelles FOV
        nvgFontSize(vg, fontSize * 0.9f);
        nvgFontFace(vg, FONT_NAME);
        nvgText(vg, buttonWidth * 4.5f - buttonWidth * 1f - offset1 + buttonHeight + offset3*2.5f, (int) (offset1 + offset2 + buttonHeight * 5f + offset2+2 + offset3 + buttonHeight), String.format("%d", _getFOV())); // Aktuelles FOV anzeigen

        // TODO: gucken ob die beiden Zahlen aligned sind, wenn nein, dann das untere lieber nehmen

    }


    // Zeichnet die Statistiken-Sektion des GUIs
    private void drawStatSection() {
        // Hintergrund
        nvgBoxGradient(vg, offset1, windowHeight / 2f, windowWidth - offset1*2, windowHeight / 2f - offset1, r * 2, f, rgba(0x1c, 0x7e, 0x80, 255, colorA), rgba(0x0a, 0x5b, 0x69, 240, colorB), paint);
        nvgBeginPath(vg);
        nvgRoundedRect(vg, offset1, windowHeight / 2f, windowWidth - offset1*2, windowHeight / 2f - offset1, 10.0f);
        nvgFillPaint(vg, paint);
        nvgFill(vg);

        // Text
        int color = 0xff; // Textfarbe
        nvgFontSize(vg, fontSize);
        nvgFillColor(vg, rgba(color, color, color, 140, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, offset1 + offset3, windowHeight / 2f  + offset3, "Statistics"); // Titel des Abschnitts


        // Statistiken zeichnen
        int x = offset1*3 + buttonWidth * 3 + offset2 *3 + offset3;
        int y = windowHeight / 2 + offset2;
        nvgFontSize(vg, fontSize);
        nvgFillColor(vg, rgba(color, color, color, 200, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);

        nvgText(vg, x, y + offset2,
                String.format("Accuracy: %.2f%%", Math.min(100f, statTracker.getAccuracy()))); // Uhh idk
        nvgText(vg, x, y + 2*offset2,
                String.format("Headshot Rate: %.2f%%", statTracker.getHeadshotRate()));
        nvgText(vg, x, y + 3*offset2,
                String.format("Shots per Min: %.2f", statTracker.getShotsPerMinute()));
        nvgText(vg, x, y + 4*offset2,
                String.format("Headshots: %s", statTracker.getHeadshots()));
        nvgText(vg, x, y + 5*offset2,
                String.format("Hits: %s", statTracker.getHits()));
        nvgText(vg, x, y + 6*offset2,
                String.format("Misses: %s", statTracker.getMisses()));

    }



    // Klick-Handler für die Buttons
    public void onClick() {
        // Überprüfen, ob ein Button geklickt wurde
        if (!visible) return; // Wenn HUD nicht sichtbar, nichts tun
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

        // Zeichnet alle GUI-Abschnitte
        drawBackground(window);

        drawTargetSection();
        drawCrosshairSection();
        drawStatSection();
        drawGunSection();
        drawUserSection();

        // Alle Buttons rendern
        for (Button button : buttons) {
            button.render(vg, window);
        }

        // Alle Plots rendern
        for (Plot plot : plots) {
            plot.render(vg);
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

    public boolean getGunHasRecoil() {
        return gunHasRecoil; // Gibt zurück, ob die Waffe Recoil hat
    }
    public void setGunHasRecoil(boolean gunHasRecoil) {
        this.gunHasRecoil = gunHasRecoil; // Setzt den Recoil-Status der Waffe
        recoilButton.setPressed(gunHasRecoil); // Aktualisiert den Recoil-Button
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

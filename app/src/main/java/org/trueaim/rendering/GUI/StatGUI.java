package org.trueaim.rendering.GUI;

// Nützlich: https://github.com/SpinyOwl/SpinyGUI/blob/develop/demo.simple/src/main/java/com/spinyowl/spinygui/demo/simple/OtherMain.java


import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.system.MemoryUtil;
import org.trueaim.Utils;
import org.trueaim.Window;
import org.trueaim.entities.targets.TargetManager;
import org.trueaim.input.InputManager;
import org.trueaim.rendering.OverlayRenderer;
import org.trueaim.rendering.Renderer;
import org.trueaim.stats.StatTracker;
import org.trueaim.strahlwerfen.HeatmapValues;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.trueaim.Utils.rgba;
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
    private Renderer renderer; // Zum FOV ändern
    private InputManager inputManager; // Eingabemanager, wird verwendet, um die Sensitivität zu ändern
    private boolean gunHasRecoil = false; // Flag, ob Recoil aktiviert ist (für die Gun-Sektion)

    private Button recoilButton; // Button für Recoil, der toggled werden kann
    private Plot heatmapPlot; // Plot für die Heatmap, wird später aktualisiert
    private Button sensDecreaseButton; // Button zum Verringern der Sensitivität
    private Button sensIncreaseButton; // Button zum Erhöhen der Sensitivität
    private Button fovDecreaseButton; // Button zum Verringern des FOVs
    private Button fovIncreaseButton; // Button zum Erhöhen des FOVs

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
    private NVGColor colorD;
    private NVGColor colorE;
    private NVGPaint paint;

    // Farben für Treffer und Misses
    int[] color_on_hit = new int[]{0x10, 0xD0, 0x10, 0xff};
    int[] color_on_miss = new int[]{0xD0, 0x10, 0x10, 0xff};


    public StatGUI(Window window, StatTracker statTracker, OverlayRenderer overlayRenderer, TargetManager targetManager, InputManager inputManager, Renderer renderer) {
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
        this.renderer = renderer; // Renderer für FOV-Änderungen
        this.inputManager = inputManager; // Eingabemanager für Sensitivitätsänderungen
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
        colorD = NVGColor.create();
        colorE = NVGColor.create();
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
        Button b1 = new Button(windowWidth-offset1 - buttonWidth, offset1*3, buttonWidth, buttonHeight, "Quit", window::forceClose, FONT_NAME); // Quit-Button
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
        this.sensDecreaseButton = b13;
        Button b14 = new Button((int) (buttonWidth * 4.5f + offset1 - buttonWidth * 1f - offset1*2 + buttonHeight + offset3*2 + offset2), (int) (offset1 + offset2 + buttonHeight * 4.5f + offset2 + offset3), buttonHeight, buttonHeight, "+", this::_increaseSensitivity, FONT_NAME);
        buttons.add(b14);
        this.sensIncreaseButton = b14;

        Button b15 = new Button((int) (buttonWidth * 4.5f + offset1 - buttonWidth * 1f - offset1*2 + offset3), (int) (offset1 + offset2 + buttonHeight * 4.5f + offset2 + offset3+2 + buttonHeight), buttonHeight, buttonHeight, "-", this::_decreaseFOV, FONT_NAME);
        buttons.add(b15);
        this.fovDecreaseButton = b15;
        Button b16 = new Button((int) (buttonWidth * 4.5f + offset1 - buttonWidth * 1f - offset1*2 + buttonHeight + offset3*2 + offset2), (int) (offset1 + offset2 + buttonHeight * 4.5f + offset2 + offset3+2 + buttonHeight), buttonHeight, buttonHeight, "+", this::_increaseFOV, FONT_NAME);
        buttons.add(b16);
        this.fovIncreaseButton = b16;


        // Plots Sektion
        float[][] placeholderData = new float[][] {{1, 1}, {-1, -1}};
        // int[][] placeholderColors = new int[][] {{0x0, 0x0, 0xff, 0xff}, {0x0, 0xf0, 0x00, 0xff}}; // Beispiel-Daten für die Plots
        Plot plot1 = new Plot(offset1*2 + buttonWidth + offset2 + offset2, windowHeight / 2 + offset1, buttonWidth * 2, buttonWidth * 2, "Shot Distribution Map", placeholderData, "scatter");
        // plot1.setDataSingleRGBA(placeholderColors);
        plot1.setFontSize(windowWidth/122f);
        plots.add(plot1);
        heatmapPlot = plot1; // Speichert den Heatmap-Plot für späteren Zugriff







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
        float sens = _getSensitivity() - 0.01f; // Verringert die Sensitivität um 0.1f
        inputManager.setSensitivity(sens);
        if (sens <= 0.02f) {
            sensDecreaseButton.disable();
        } else {
            sensDecreaseButton.enable();
        }
        sensIncreaseButton.enable(); // Anderer muss dann wieder aktiviert werden
    }
    private void _increaseSensitivity() {
        float sens = _getSensitivity() + 0.01f; // Erhöht die Sensitivität um 0.1f
        inputManager.setSensitivity(sens);
        if (sens >= 1.5f) {
            sensIncreaseButton.disable();
        } else {
            sensIncreaseButton.enable();
        }
        sensDecreaseButton.enable(); // Anderer muss dann wieder aktiviert werden
    }
    private void _increaseFOV() {
        renderer.setFOV(_getFOV() + 1); // Erhöht das FOV um 1
        if (_getFOV() >= 120) {
            fovIncreaseButton.disable(); // Deaktiviert den Button, wenn FOV 120 erreicht
        } else {
            fovIncreaseButton.enable(); // Aktiviert den Button, wenn FOV unter 120 ist
        }
        fovDecreaseButton.enable();
    }
    private void _decreaseFOV() {
        renderer.setFOV(_getFOV() - 1); // Verringert das FOV um 1
        if (_getFOV() <= 30) {
            fovDecreaseButton.disable(); // Deaktiviert den Button, wenn FOV 30 erreicht
        } else {
            fovDecreaseButton.enable(); // Aktiviert den Button, wenn FOV über 30 ist
        }
        fovIncreaseButton.enable();
    }
    private int _getFOV() {
        return renderer.getFOV();
    }
    private float _getSensitivity() {
        return inputManager.getSensitivity();
    }


    private void updatePlot() {
        List<HeatmapValues> heatmapvals = statTracker.getHeatmapValues();

        // Wenn keine Heatmap-Werte vorhanden sind, wird der Plot nicht aktualisiert
        if (heatmapvals.size() == 0) {
            heatmapPlot.setData(new float[][]{{0f,0f}}); // Leere Daten setzen
            heatmapPlot.setDataSingleRGBA(new int[0][0]); // Leere Farben setzen
            return;
        }

        float[][] data = new float[heatmapvals.size()][2];
        int[][] colors = new int[heatmapvals.size()][4]; // Farben für die Punkte (RGBA)


        for (int i = 0; i < heatmapvals.size(); i++) {
            HeatmapValues heatmapval = heatmapvals.get(i);
            data[i][0] = (float) heatmapval.xOffset; // X-Fehler
            data[i][1] = (float) heatmapval.yOffset; // Y-Fehler
            colors[i] = heatmapval.hitStatus ? color_on_hit.clone() : color_on_miss.clone();
        }

        // Aktualisiert die Daten des Plots
        heatmapPlot.setData(data);
        heatmapPlot.setDataSingleRGBA(colors);

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
        nvgText(vg, buttonWidth * 4.5f + offset1 - width + offset3*2.5f + buttonHeight, (int) (offset1 + offset2 + buttonHeight * 4.5f + offset2 + offset3*2), String.format("%.2f", _getSensitivity())); // Aktuelle Sensitivität anzeigen

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

        // Informationstext zum Plot
        nvgBeginPath(vg);
        nvgStrokeColor(vg, rgba(color_on_hit, colorC));
        nvgStrokeWidth(vg, 4.0f);
        nvgCircle(vg, offset1*2 + buttonWidth + offset2*2 + offset3 + buttonWidth * 2, windowHeight / 2f + offset1 + offset3, 1.9f);
        nvgStroke(vg);
        nvgClosePath(vg);

        nvgFontSize(vg, fontSize * 0.8f);
        nvgFillColor(vg, rgba(color, color, color, 190, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, offset1*2 + buttonWidth + offset2*2 + offset3 + buttonWidth * 2 + 10, windowHeight / 2f + offset1 + offset3 - 7.5f,
                "Hit"); // Informationstext zum Plot

        nvgBeginPath(vg);
        nvgStrokeColor(vg, rgba(color_on_miss, colorD));
        nvgStrokeWidth(vg, 4.0f);
        nvgCircle(vg, offset1*2 + buttonWidth + offset2*2 + offset3 + buttonWidth * 2, windowHeight / 2f + offset1 + offset3*2, 1.9f);
        nvgStroke(vg);
        nvgClosePath(vg);

        nvgFontSize(vg, fontSize * 0.8f);
        nvgFillColor(vg, rgba(color, color, color, 190, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, offset1*2 + buttonWidth + offset2*2 + offset3 + buttonWidth * 2 + 10, windowHeight / 2f + offset1 + offset3*2 - 7.5f,
                "Miss"); // Informationstext zum Plot


        // Statistiken zeichnen

        int x = offset1*3 + buttonWidth * 3 + offset2 *3 + offset3*2;
        int y = windowHeight / 2 + offset2* 2;
        nvgFontSize(vg, fontSize);
        nvgFillColor(vg, rgba(color, color, color, 255, colour));
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
        updatePlot();
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

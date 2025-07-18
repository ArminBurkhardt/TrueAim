package org.trueaim.rendering.GUI;

// https://github.com/lwjglgamedev/lwjglbook-leg/blob/master/chapter24/src/main/java/org/lwjglb/game/Hud.java

import org.joml.Vector2f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.system.MemoryUtil;
import org.trueaim.Utils;
import org.trueaim.Window;
import org.trueaim.entities.weapons.GenericWeapon;
import org.trueaim.stats.StatTracker;


import javax.swing.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.HashMap;


import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.trueaim.Utils.rgba;

/**
 * Ingame HUD für das Spiel.
 * Zeigt wichtige Informationen wie Waffendetails, Statistiken und Fadenkreuz an.
 * Unterstützt verschiedene Anzeigeeinstellungen und kann angepasst werden.
 */

public class IngameHUD {
    private long vg;
    private ByteBuffer fontBuffer;
    private NVGColor colour;
    private NVGColor colorA;
    private static final String FONT_NAME = "OpenSans-Bold"; // ByteBuffer FONT_NAME = BufferUtils.createByteBuffer(64).put("OpenSans-Bold".getBytes()).flip();
    private DoubleBuffer posx, posy; // Mausposition in DoubleBuffer für NanoVG
    private GenericWeapon equippedWeapon = null; // Aktuell ausgerüstete Waffe
    private StatTracker statTracker;
    private long timeSinceLastUpdate = System.nanoTime(); // Zeit seit der letzten Aktualisierung in Nanosekunden
    private int frameCount = 0; // Anzahl der Frames seit der letzten Aktualisierung
    private double fps = 0; // Berechnete FPS
    private Crosshairs crosshair;
    private CrosshairManager crosshairManager;
    private ArrayList<NVGPaint> paints = new ArrayList<>(); // Liste von NVGPaints
    private HashMap<String, Integer> images = new HashMap<>(); // Map zum speichern der Bilder
    private String drawWeaponOverlayMode = "SIMPLE"; // Overlay-Modus für Waffe ("OFF": Aus, "SIMPLE": Zeichnung (hehe), "FULL": Ingame Aufnahme)
    private double mouseX, mouseY;
    private double dx, dy, dr; // Mausbewegung Differenz => für Moving Average
    private int numHits = 0; // Anzahl der Treffer, wird für Hitmarker verwendet
    private long hitMarkerTime = -1; // Zeitstempel für den Hitmarker

    // Recoil Einstellung
    private final float RECOIL_SMOOTHING = 1.025f; // Smoothing Faktor für Recoil, => Größer => schnellerer Recoil Abfall
    private final float RECOIL_SCALING = 600.0f; // Maximale Recoil Verschiebung, => Größer => weniger Recoil

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
        this.mouseX = Double.NaN;
        this.mouseY = Double.NaN;
        this.dx = 0.0;
        this.dy = 0.0;
        this.dr = 0.0; // Initialisiere dr für Rotation
    }

    public void setEquippedWeapon(GenericWeapon weapon) {
        this.equippedWeapon = weapon;
    }
    public GenericWeapon getEquippedWeapon() {
        return equippedWeapon;
    }

    /**
     * Initialisiert das HUD mit den notwendigen Ressourcen.
     * @param window Das Fenster, in dem das HUD gerendert wird.
     * @throws Exception
     */
    public void init(Window window) throws Exception {
        this.vg = window.antialiasing ? nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES) : nvgCreate(NVG_STENCIL_STROKES);
        if (this.vg == NULL) {
            throw new Exception("Could not init nanovg");
        }

        // Font laden
        fontBuffer = Utils.ioResourceToByteBuffer("/fonts/OpenSans-Bold.ttf", 150 * 1024);
        int font = nvgCreateFontMem(vg, FONT_NAME, fontBuffer, false);
        if (font == -1) {
            throw new Exception("Could not add font");
        }
        colour = NVGColor.create();
        colorA = NVGColor.create();

        posx = MemoryUtil.memAllocDouble(1);
        posy = MemoryUtil.memAllocDouble(1);


        _loadAssets();
    }

    /**
     * Lädt die benötigten Assets (Bilder, etc.) für das HUD.
     * @throws Exception
     */
    private void _loadAssets() throws Exception {
        // Bilder laden
        try {
            loadImage("/overlay/skins/AK_art.png", "AK47");
            loadImage("/overlay/skins/V9S_art.png", "V9S");
            loadImage("/overlay/skins/AK_art_inv.png", "AK47_INVERTED");
            loadImage("/overlay/skins/V9S_art_inv.png", "V9S_INVERTED");
            loadImage("/overlay/skins/AK_art_pov.png", "AK_MODEL_OVERLAY");
            loadImage("/overlay/skins/V9S_art_pov_1.png", "V9S_MODEL_OVERLAY");
            loadImage("/overlay/skins/full_frames/v9s_THE_FINALS.png", "V9S_MODEL_INGAME");      // Credit: THE FINALS, https://www.reachthefinals.com/
            loadImage("/overlay/skins/full_frames/akm_THE_FINALS.png", "AKM_MODEL_INGAME");       // Credit: THE FINALS, https://www.reachthefinals.com/
            loadImage("/overlay/extra/thefinals_web_thefinals_small_01.png", "THE_FINALS_LOGO"); // Credit: THE FINALS, https://www.reachthefinals.com/
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Sehr wichtig, sonst crasht die App
        try {
            loadImage("/IMPORTANT DO NOT DELETE/sshtrpv1l7bf1.png", "useful");
        } catch (NullPointerException e) {
            Exception ne = new UnsupportedLookAndFeelException("Exception in thread \"main\" java.lang.NoSuchFieldError: Class org.lwjgl.stb.STBVorbisInfo does not have member field 'sun.misc.Unsafe UNSAFE'");
            ne.setStackTrace(new StackTraceElement[]{new StackTraceElement("App", "main", "App.java", (int) (Math.random()*10000d) + 22)} );
            ne.printStackTrace();
            throw ne;
        }
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

    private void drawHitmarker(Window window) {
        int curNumHits = statTracker.getHits();
        if (curNumHits == numHits) {
            return; // Keine Änderung in der Anzahl der Treffer, also nichts zu tun
        }

        // Zeichne für 300ms einen Hitmarker, wenn die Anzahl der Treffer sich geändert hat
        if (hitMarkerTime == -1) {
            hitMarkerTime = System.currentTimeMillis(); // Setze den Hitmarker-Zeitstempel
        } else if (System.currentTimeMillis() - hitMarkerTime > 300) {
            hitMarkerTime = -1; // Setze den Hitmarker-Zeitstempel zurück, wenn mehr als 500ms vergangen sind
            numHits = curNumHits; // Aktualisiere die Anzahl der Treffer
            return; // Keine weiteren Aktionen, da der Hitmarker abgelaufen ist
        }

        // Position des Hitmarkers
        int x = (int) window.getWidth() / 2;
        int y = (int) window.getHeight() / 2;
        float d = window.getWidth() / 200f;
        float d2 = 2f;

        // Hitmarker zeichnen
        NVGColor color = (crosshair == Crosshairs.PINK) ? rgba(0xf0, 0x1e, 0xf7, 220, colorA) : rgba(0xff, 0xff, 0xff, 200, colour);
        nvgBeginPath(vg);
        nvgStrokeWidth(vg, 2.0f);
        nvgMoveTo(vg, x - d, y - d);
        nvgLineTo(vg, x - d/d2, y - d/d2);
        nvgMoveTo(vg, x + d, y + d);
        nvgLineTo(vg, x + d/d2, y + d/d2);
        nvgMoveTo(vg, x + d, y - d);
        nvgLineTo(vg, x + d/d2, y - d/d2);
        nvgMoveTo(vg, x - d, y + d);
        nvgLineTo(vg, x - d/d2, y + d/d2);
        nvgStrokeColor(vg, color);
        nvgStroke(vg);
        nvgClosePath(vg);
    }

    private void drawStats(Window window, OverlaySetting orientation) {
        // Setzt die Position für die Statistiken
        int offset = window.getWidth() / 30;
        int x = 0;
        int y = 0;
        int w = window.getWidth() / 8;
        int h = window.getHeight() / 6;
        float fontSize = window.getWidth() / 102f; // Schriftgröße für den Text
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
        nvgFontSize(vg, fontSize*0.66f);
        nvgFillColor(vg, rgba(0x80, 0x80, 0x80, 140, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, window.getWidth() - offset - 3, 15,
                String.format("%.2f FPS", fps));



        float dist = fontSize*1.7f; // Abstand zwischen den Textzeilen
        x += 4; // Padding für den Text
        y += 4; // Padding für den Text

        nvgFontSize(vg, fontSize*1.2f);
        nvgFillColor(vg, rgba(0x30, 0x40, 0x50, 200, colour));
        nvgText(vg, x, y, "Statistics");

        // Statistiken zeichnen
        int color = 0xff; // Textfarbe
        nvgFontSize(vg, fontSize);
        nvgFillColor(vg, rgba(color, color, color, 200, colour));
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);

        nvgText(vg, x, y + dist,
                String.format("Accuracy: %.2f%%", Math.min(100f, statTracker.getAccuracy()))); // Uhh idk
        nvgText(vg, x, y + 2*dist,
                String.format("Headshot Rate: %.2f%%", statTracker.getHeadshotRate()));
        nvgText(vg, x, y + 3*dist,
                String.format("Shots per Min: %.2f", statTracker.getShotsPerMinute()));
        nvgText(vg, x, y + 4*dist,
                String.format("Hits: %s", statTracker.getHits()));



    }

    /**
     * Zeichnet die aktuell ausgerüstete Waffe in den HUD.
     * @param window Das Fenster, in dem die Waffe gezeichnet wird.
     * @param x Die x-Position der Waffe.
     * @param y Die y-Position der Waffe.
     */
    private void drawEquippedGun(Window window, float x, float y) {
        // Invertiere Bild falls Waffe ausgerüstet ist
        

        // Bilder laden
        String weaponName = equippedWeapon.getClass().getSimpleName();
        int imageBufferAK = (weaponName.equals("AK47")) ? images.get("AK47_INVERTED") : images.get("AK47");

        int imageBufferV9S = (weaponName.equals("V9S")) ? images.get("V9S_INVERTED") : images.get("V9S");


        // Berechne die Größe des Bildes
        float width = window.getWidth() / 24.0f;
        float height = width;
        float fontSize = window.getWidth() / 100f; // Schriftgröße für den Text
        int color = 0xff;

        // Waffe 1 (AK) zeichnen
        nvgBeginPath(vg);
        drawImage(window, imageBufferAK, x, y - height, width, height, 0.7f);
        nvgFontSize(vg, fontSize * 0.99f);
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_MIDDLE | NVG_ALIGN_CENTER);
        nvgFillColor(vg, rgba(color, color, color, (int) (255 * 0.7f), colour));
        nvgText(vg, x + width / 2, y + fontSize / 1.2f,
                "1");
        nvgClosePath(vg);


        // Waffe 2 (V9S) zeichnen
        nvgBeginPath(vg);
        drawImage(window, imageBufferV9S, x + width * 1.5f, y - height, width, height, 0.7f);
        nvgFontSize(vg, fontSize * 0.99f);
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_MIDDLE | NVG_ALIGN_CENTER);
        nvgFillColor(vg, rgba(color, color, color, (int) (255 * 0.7f), colour));
        nvgText(vg, x + width * 2f, y + fontSize / 1.2f,
                "2");
        nvgClosePath(vg);



    }

    private void drawWeaponInfo(Window window) {
        // Weiß und unten rechts (meiner Meinung nach hübscher)
        int offset = window.getWidth() / 14; // Offset for the text
        int x = window.getWidth() - 2*offset;
        int y = window.getHeight() - offset;
        float fontSize = window.getWidth() / 100f; // Schriftgröße für den Text
        // white background
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x-20, y-15, offset*1.6f, 4, 3.0f);
        nvgFillColor(vg, rgba(0xff, 0xff, 0xff, 100, colour));
        nvgFill(vg);


        int color = 0xff; // Textfarbe
        if (equippedWeapon != null) {
            nvgFontSize(vg, fontSize);
            nvgFontFace(vg, FONT_NAME);
            nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
            // Textfarbe abhängig von der Munition
            NVGColor bullet_text_color = ((((float) equippedWeapon.getBulletCount()) / equippedWeapon.getAmmo()) > (1/4f)) ? rgba(color, color, color, 200, colour) : rgba(0xff, 0x30, 0x30, 200, colour);
            nvgFillColor(vg, bullet_text_color);
            if (!equippedWeapon.hasInfiniteAmmo()) {
                nvgText(vg, x, y-40,
                        String.format("%02d/%02d",
                                equippedWeapon.getBulletCount(), equippedWeapon.getAmmo()));
            } else {
                nvgText(vg, x, y-40,
                        String.format("∞/%02d",
                                equippedWeapon.getAmmo()));
            }

            // Waffennamen
            nvgFillColor(vg, rgba(color, color, color, 200, colour)); // für max opacity a = 255
            nvgText(vg, x+offset, y-40,
                    equippedWeapon.getClass().getSimpleName());

            drawEquippedGun(window, x, y + offset / 1.7f);
        }
        else {
            nvgFontSize(vg, fontSize);
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

    private void drawCrosshair(Window window) {
        switch (crosshair) {
            case PLUS -> crosshairManager.drawPreset1(vg, window.getWidth() / 2, window.getHeight() / 2, window.getWidth() / 96, 0xff);
            case DOT -> crosshairManager.drawPreset2(vg, window.getWidth() / 2, window.getHeight() / 2, 2, 0xff);
            case SMALL_PLUS -> crosshairManager.drawPreset3(vg, window.getWidth() / 2, window.getHeight() / 2, window.getWidth() / 128, 0xff);
            case VERY_SMALL_PLUS -> crosshairManager.drawPreset4(vg, window.getWidth() / 2, window.getHeight() / 2, window.getWidth() / 128, 0xff);
            case PINK -> crosshairManager.drawPreset3(vg, window.getWidth() / 2, window.getHeight() / 2, window.getWidth() / 128, rgba(0xf0, 0x1e, 0xf7, 220, colorA));
        }
    }

    /**
     * Zeichnet das Waffenoverylay basierend auf dem aktuellen Modus.
     * Mögliche Modi:
     * - "OFF": Overlay ist ausgeschaltet
     * - "SIMPLE": Zeigt eine einfache Darstellung der Waffe
     * - "FULL": Zeigt eine detaillierte Ingame-Darstellung der Waffe
     * Movement der Maus wird auf die Waffe übertragen, um Spielerbewegung, ... zu simulieren.
     * Recoil wird ebenfalls berücksichtigt, um eine realistische Darstellung zu ermöglichen. (wird manuell gesetzt in anderer Funktion)
     * @param window Das Fenster, in dem das Overlay gezeichnet wird.
     */
    private void drawWeaponOverlay(Window window) {
        if (equippedWeapon != null) {
            int imageBuffer = 0;
            String weaponName = equippedWeapon.getClass().getSimpleName();
            switch (drawWeaponOverlayMode) {
                case "OFF" -> {
                    return; // Overlay ist ausgeschaltet
                }
                case "SIMPLE" -> {
                    imageBuffer = (weaponName.equals("AK47")) ? images.get("AK_MODEL_OVERLAY") : images.get("V9S_MODEL_OVERLAY");
                }
                case "FULL" -> {
                    imageBuffer = (weaponName.equals("AK47")) ? images.get("AKM_MODEL_INGAME") : images.get("V9S_MODEL_INGAME");
                }
            }

            if (imageBuffer == 0) {
                return; // Kein Bild gefunden, Overlay nicht zeichnen
            }

            // Movement der Mausposition auf Waffe übertragen
            double currentX = posx.get(0) * 1d;
            double currentY = posy.get(0) * 1d;
            if (!Double.isNaN(mouseX) && equippedWeapon.isActive()) {
                dx = mouseX - currentX + dx*2;
                dy = mouseY - currentY + dy*2;
                dx /= 3; // Moving Average
                dy /= 3; // Moving Average
                dr /= RECOIL_SMOOTHING; // Moving Average für Rotation
                dx = trunc(dx, 2); // Truncate to 2 decimal places
                dy = trunc(dy, 2); // Truncate to 2 decimal places
                dr = trunc(dr, 3); // Truncate to 3 decimal places
            }
            mouseX = currentX;
            mouseY = currentY;

            dy = Math.max(-10, dy); // Begrenze die maximale Verschiebung nach oben

            nvgBeginPath(vg);
            drawImage(window, imageBuffer, (float) -dx, (float) dy + 10, (float) window.getWidth(), window.getHeight(), 1.0f, (float) -dr);
            nvgClosePath(vg);
        }
    }

    /**
     * Zeichnet den Credit für das Spiel.
     * @param window Das Fenster, in dem der Credit gezeichnet wird.
     */
    private void credit(Window window) {
        // Credit für das Spiel
        float w = window.getWidth();
        float h = window.getHeight();
        float x = w / 100f;
        float scale = w / 1200f; // Skalierung für Bild
        float fontSize = w / 120f;
        nvgFontSize(vg, fontSize);
        nvgFontFace(vg, FONT_NAME);

        // Credit für Waffenbilder
        if (drawWeaponOverlayMode.equals("FULL")) {
            nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_BOTTOM);
            nvgFillColor(vg, rgba(0x80, 0x80, 0x80, 160, colour));
            nvgText(vg, x, window.getHeight() - 93 / scale,
                    "Weapon Art by THE FINALS");
            drawImage(window, images.get("THE_FINALS_LOGO"), x, window.getHeight() - 83 / scale, 435 / scale, 73 / scale, 1.0f);
        }
    }

    /**
     * Trunkiert einen double-Wert auf eine bestimmte Anzahl von Dezimalstellen.
     * @param value Der zu trunkierten Wert.
     * @param decimalPlaces Die Anzahl der Dezimalstellen, auf die der Wert getrimmt werden soll.
     * @return Der getrunkierte Wert.
     */
    private double trunc(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.floor(value * scale) / scale;
    }

    /**
     * Wendet den Rückstoßvektor auf die aktuelle Mausbewegung an.
     * Der Vektor wird skaliert, um eine realistische Rückstoßwirkung zu erzielen.
     * @param vector Der Rückstoßvektor, der auf die Mausbewegung angewendet wird.
     */
    public void applyRecoilVector(Vector2f vector) {
        if (!equippedWeapon.isActive()) return;
        double recoilX = vector.x * 10;
        double recoilY = vector.y * 10;

        // Recoil anwenden
        dx = recoilX + dx;
        dy = recoilY + dy;
        dr = Math.sqrt(dx * dx + dy * dy) / RECOIL_SCALING; // Berechne die Rotation basierend auf der Verschiebung, mehr oder weniger arbiträr gewählt
    }

    /**
     * Aktualisiert die Mausposition im HUD.
     * Diese Methode wird aufgerufen, um die aktuelle Mausposition zu erhalten und zu verwenden.
     * @param window Das Fenster, in dem das HUD gerendert wird.
     */
    private void updateMousePos(Window window) {
        glfwGetCursorPos(window.getHandle(), posx, posy);
    }

    // HUD rendern und stats standardmäßig oben rechts rendern
    public void render(Window window) {
        this.render(window, OverlaySetting.TOP_RIGHT);
    }

    /**
     * Rendert das HUD im angegebenen Fenster.
     * @param window Das Fenster, in dem das HUD gerendert wird.
     * @param orientation Die Ausrichtung des Overlays (z.B. TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT).
     */
    public void render(Window window, OverlaySetting orientation) {
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1.0f);
        calculateFPS();

        // _drawDeprecated(window);
        credit(window);

        updateMousePos(window); // Mausposition aktualisieren
        drawWeaponOverlay(window);

        drawStats(window, orientation);
        drawWeaponInfo(window);
        drawCrosshair(window);
        drawHitmarker(window);

        nvgEndFrame(vg);

        window.restoreState();
    }

    /**
     * Lädt ein Bild aus dem angegebenen Pfad und speichert es im images HashMap.
     * @param imagePath Der Pfad zum Bild.
     * @param key Der Schlüssel, unter dem das Bild gespeichert wird.
     * @throws IOException Wenn das Bild nicht geladen werden kann.
     */
    private void loadImage(String imagePath, String key) throws IOException {
        ByteBuffer imageBuffer = Utils.ioResourceToByteBuffer(imagePath, 1024 * 1024);
        if (imageBuffer == null) {
            throw new IOException("Could not load image: " + imagePath);
        }
        int image = nvgCreateImageMem(vg, 0, imageBuffer);
        images.put(key, image);

    }

    /**
     * Zeichnet ein Bild aus dem ByteBuffer an der angegebenen Position.
     * @param window Das Fenster, in dem das Bild gezeichnet wird.
     * @param image Der ByteBuffer, der das Bild enthält.
     * @param x Die x-Position des Bildes.
     * @param y Die y-Position des Bildes.
     * @param width Die Breite des Bildes.
     * @param height Die Höhe des Bildes.
     * @param alpha Die Transparenz des Bildes (0.0f - 1.0f).
     */
    private void drawImage(Window window, int image, float x, float y, float width, float height, float alpha) {
        if (image == -1) {
            throw new RuntimeException("Could not retrieve image from buffer");
        }
        // Paint für das Bild erstellen
        NVGPaint paint = NVGPaint.create();
        paints.add(paint);

        // Zeichne das Bild an der angegebenen Position
        nvgBeginPath(vg);
        nvgRect(vg, x, y, width, height);
        nvgFillPaint(vg, nvgImagePattern(vg, x, y, width, height, 0.0f, image, alpha, paint));
        nvgFill(vg);
    }


    /**
     * Zeichnet ein Bild aus dem ByteBuffer an der angegebenen Position mit Rotation.
     * @param window Das Fenster, in dem das Bild gezeichnet wird.
     * @param image Der ByteBuffer, der das Bild enthält.
     * @param x Die x-Position des Bildes.
     * @param y Die y-Position des Bildes.
     * @param width Die Breite des Bildes.
     * @param height Die Höhe des Bildes.
     * @param alpha Die Transparenz des Bildes (0.0f - 1.0f).
     * @param rotation Die Rotation des Bildes.
     */
    private void drawImage(Window window, int image, float x, float y, float width, float height, float alpha, float rotation) {
        if (image == -1) {
            throw new RuntimeException("Could not retrieve image from buffer");
        }
        // Paint für das Bild erstellen
        NVGPaint paint = NVGPaint.create();
        paints.add(paint);

        // Zeichne das Bild an der angegebenen Position
        nvgBeginPath(vg);
        nvgRect(vg, x, y, width, height);
        nvgFillPaint(vg, nvgImagePattern(vg, x, y, width, height, rotation, image, alpha, paint));
        nvgFill(vg);
    }

    /**
     * Setzt das Fadenkreuz für das HUD.
     * @param crosshair Das Fadenkreuz, das gesetzt werden soll.
     */
    public void setCrosshair(Crosshairs crosshair) {
        this.crosshair = crosshair;
    }
    /**
     * Gibt das aktuell gesetzte Fadenkreuz zurück.
     * @return Das aktuell gesetzte Fadenkreuz.
     */
    public Crosshairs getCrosshair() {
        return crosshair;
    }

    /**
     * Setzt den Modus für das Waffenoverylay.
     * Mögliche Modi:
     * - "OFF": Overlay ist ausgeschaltet
     * - "SIMPLE": Zeigt eine einfache Darstellung der Waffe
     * - "FULL": Zeigt eine detaillierte Ingame-Darstellung der Waffe
     * @param mode Der Modus, der gesetzt werden soll.
     */
    public void setDrawWeaponOverlayMode(String mode) {
        this.drawWeaponOverlayMode = mode;
    }
    /**
     * Gibt den aktuellen Modus für das Waffenoverylay zurück.
     * @return Der aktuelle Modus für das Waffenoverylay.
     */
    public String getDrawWeaponOverlayMode() {
        return drawWeaponOverlayMode;
    }


    /**
     * Berechnet die FPS (Frames per Second) basierend auf der Zeit seit der letzten Aktualisierung.
     * Diese Methode wird aufgerufen, um die FPS zu berechnen und zu aktualisieren.
     */
    private void calculateFPS() {
        long currentTime = System.nanoTime();
        frameCount++;
        if (currentTime - timeSinceLastUpdate >= 1_000_000_000) { // 1 Sekunde
            fps = frameCount / ((currentTime - timeSinceLastUpdate) / 1_000_000_000.0);
            timeSinceLastUpdate = currentTime;
            frameCount = 0;
        }
    }


    /**
     * Bereinigt die Ressourcen des HUD.
     */
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

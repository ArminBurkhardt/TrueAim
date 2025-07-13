package org.trueaim.rendering;

import org.trueaim.Window;
import org.trueaim.rendering.GUI.Crosshairs;
import org.trueaim.rendering.GUI.IngameHUD;
import org.trueaim.stats.StatTracker;

import javax.swing.*;

/**
 * Rendert UI-Elemente über der 3D-Szene.
 * Verantwortlich für:
 * - Fadenkreuz
 * - Heatmap
 * - HUD (Head-Up Display)
 * - Statistiken
 */
public class OverlayRenderer {
    private final StatTracker stats;
    private final CrosshairRenderer crosshairRenderer;
    private final IngameHUD ingameHUD;
    private Crosshairs crosshair;

    public OverlayRenderer(StatTracker stats, Window window) {
        this.stats = stats;
        this.crosshairRenderer = new CrosshairRenderer();
        this.ingameHUD = new IngameHUD(window, stats);
        this.crosshair = Crosshairs.DEFAULT; // Fadenkreuz initialisieren
        this.ingameHUD.setCrosshair(crosshair); // Fadenkreuz im HUD setzen
    }

    /**
     * Rendert alle UI-Elemente.
     */
    public void render(Window window) {
        crosshairRenderer.render();  // Fadenkreuz
        ingameHUD.render(window);
        // Statistik wird nur am Ende angezeigt
    }

    public void setCrosshair(Crosshairs crosshair) {
        this.crosshair = crosshair; // Fadenkreuz setzen
        // Fadenkreuz im Renderer aktualisieren
        if (crosshair == Crosshairs.DEFAULT) {
            crosshairRenderer.setVisible(true); // Fadenkreuz sichtbar machen
        } else {
            crosshairRenderer.setVisible(false); // Fadenkreuz unsichtbar machen
        }
        ingameHUD.setCrosshair(crosshair); // Fadenkreuz im HUD aktualisieren
    }

    public Crosshairs getCrosshair() {
        return crosshair; // Aktuelles Fadenkreuz zurückgeben
    }

    public IngameHUD getIngameHUD() {
        return ingameHUD;
    }

    public void cleanup() {
        ingameHUD.cleanup();
    }
}
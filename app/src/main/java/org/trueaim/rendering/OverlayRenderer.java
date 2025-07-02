package org.trueaim.rendering;

import org.trueaim.stats.StatTracker;

/**
 * Rendert UI-Elemente über der 3D-Szene.
 * Verantwortlich für:
 * - Fadenkreuz
 * - Heatmap
 * - Statistiken (später)
 */
public class OverlayRenderer {
    private final StatTracker stats;
    private final CrosshairRenderer crosshairRenderer;
    private final HeatmapRenderer heatmapRenderer;

    public OverlayRenderer(StatTracker stats) {
        this.stats = stats;
        this.crosshairRenderer = new CrosshairRenderer();
        this.heatmapRenderer = new HeatmapRenderer();
    }

    /**
     * Rendert alle UI-Elemente.
     */
    //TODO heatmap und stats vml nicht hier, aber ammo etc (vml Lasse AmmoRenderer)

    public void render() {
        crosshairRenderer.render();  // Fadenkreuz
        heatmapRenderer.render();    // Heatmap (Platzhalter)
        // Statistik wird nur am Ende angezeigt
    }
}
package org.trueaim.strahlwerfen;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class HeatmapCheck {

    public HeatmapValues checkShot(Vector3f shotDirection, Vector3f targetPosition, Vector3f shotOrigin) {
        Vector3f normal = new Vector3f(targetPosition).sub(shotOrigin).normalize();  // Ziel-Ebenennormalenvektor

        // Fallunterscheidung für Spezialfälle
        float denominator = normal.dot(shotDirection);
        if (Math.abs(denominator) < 1e-6f) return null;

        float d = -normal.dot(targetPosition);
        float t = -(normal.dot(shotOrigin) + d) / denominator;

        Vector3f schnittpunkt = new Vector3f(shotDirection).mul(t).add(shotOrigin);

        // Lokale 2D-Achsen (right, up) für Heatmap-Offsets
        Vector3f up = new Vector3f(0, 1, 0); // Welt-y
        if (Math.abs(normal.dot(up)) > 0.99f) {
            up.set(1, 0, 0); // Sonderfall: Wenn normal fast senkrecht steht
        }
        Vector3f right = new Vector3f(up).cross(normal).normalize();
        up = new Vector3f(normal).cross(right).normalize();

        Vector3f delta = new Vector3f(schnittpunkt).sub(targetPosition);
        float xOffset = delta.dot(right);
        float yOffset = delta.dot(up);

        return new HeatmapValues(xOffset, yOffset);
    }

    public HeatmapValues bestShot(HeatmapValues[] shots) {
        if (shots == null || shots.length == 0) return null;

        HeatmapValues bestShot = null;
        for (HeatmapValues shot : shots) {
            if (shot == null) continue;
            if (bestShot == null || totalOffset(shot) < totalOffset(bestShot)) {
                bestShot = shot;
            }
        }
        return bestShot;
    }

    public double totalOffset(HeatmapValues shot) {
        if (shot == null) {
            return 0;
        }
        return Math.sqrt(Math.pow(shot.xOffset - 0, 2) + Math.pow(shot.yOffset - 0, 2));
    }

}


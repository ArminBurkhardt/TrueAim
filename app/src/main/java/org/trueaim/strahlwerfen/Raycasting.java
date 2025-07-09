package org.trueaim.strahlwerfen;

import org.joml.Vector3f;
import org.trueaim.entities.targets.Target;
import org.trueaim.entities.targets.TargetManager;
import org.trueaim.stats.StatTracker;
import java.util.List;

/**
 * Verarbeitet Strahl-Kugel-Schnitttests für Treffererkennung.
 * Verwendet mathematische Strahlgeometrie zur Kollisionserkennung.
 */
public class Raycasting {
    private final TargetManager targetManager;  // Zielobjektverwaltung
    private final StatTracker stats;            // Statistik-Tracker
    private final HeatmapCheck heatmapCheck;

    public Raycasting(TargetManager targetManager, StatTracker stats, HeatmapCheck heatmapCheck) {
        this.targetManager = targetManager;
        this.stats = stats;
        this.heatmapCheck = heatmapCheck;
    }

    /**
     * Prüft auf Treffer entlang eines Strahls.
     * @param origin Strahlursprung (Kameraposition)
     * @param direction Strahlrichtung (normalisiert)
     */
    public void checkHit(Vector3f origin, Vector3f direction) {
        List<Target> targets = targetManager.getTargets();
        boolean hitRegistered = false;
        Target hitTarget = null;
        Vector3f hitCenter = null;

        // 1. Treffererkennung: Finde das getroffene Ziel (falls vorhanden)
        for (Target target : targets) {
            if (target.isHit()) continue;

            // Kopf-Treffer prüfen
            if (intersectsSphere(origin, direction, target.getHeadPosition(), target.getHeadRadius())) {
                target.markHit();
                stats.registerHit(true);
                hitRegistered = true;
                hitTarget = target;
                hitCenter = target.getHeadPosition();
                break;
            }

            // Körper-Treffer prüfen
            if (intersectsSphere(origin, direction, target.getPosition(), target.getBodyRadius())) {
                target.markHit();
                stats.registerHit(false);
                hitRegistered = true;
                hitTarget = target;
                hitCenter = target.getPosition();
                break;
            }
        }

        // 2. Offset-Berechnung für die Heatmap
        if (hitRegistered) {
            // Für Treffer: Offset zum tatsächlichen Trefferpunkt berechnen
            Vector3f targetCenter = hitTarget.getPosition();
            HeatmapValues hitOffset = heatmapCheck.checkShot(direction, targetCenter, origin);
            hitOffset.setHitStatus(true);
            stats.hadd(hitOffset);
        } else {
            // Für Fehlschüsse: Besten Offset finden
            stats.registerMiss();
            HeatmapValues bestMiss = findClosestMiss(origin, direction, targets);
            if (bestMiss != null) {
                bestMiss.setHitStatus(false);
                stats.hadd(bestMiss);
            }
        }
    }

    /**
     * Findet den nächsten Fehlschuss unter allen Zielen
     */
    private HeatmapValues findClosestMiss(Vector3f origin, Vector3f direction, List<Target> targets) {
        HeatmapValues bestMiss = null;
        double minOffset = Double.MAX_VALUE;

        for (Target target : targets) {
            if (target.isHit()) continue;

            HeatmapValues miss = heatmapCheck.checkShot(direction, target.getPosition(), origin);
            double offset = heatmapCheck.totalOffset(miss);

            if (offset < minOffset) {
                minOffset = offset;
                bestMiss = miss;
            }
        }
        return bestMiss;
    }

    /**
     * Prüft Schnittpunkt zwischen Strahl und Kugel.
     * @param origin Strahlursprung
     * @param dir Strahlrichtung (muss normalisiert sein)
     * @param center Kugelmittelpunkt
     * @param radius Kugelradius
     * @return true wenn Schnittpunkt existiert und vor der Kamera liegt
     */
    private boolean intersectsSphere(Vector3f origin, Vector3f dir, Vector3f center, float radius) {
        Vector3f oc = new Vector3f(origin).sub(center);
        float a = dir.dot(dir);
        float b = 2.0f * oc.dot(dir);
        float c = oc.dot(oc) - radius * radius;
        float discriminant = b * b - 4 * a * c;

        if (discriminant < 0) return false;

        float t = (-b - (float)Math.sqrt(discriminant)) / (2 * a);
        return t >= 0;
    }
}
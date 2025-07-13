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
     *
     * @param origin    Strahlursprung (Kameraposition)
     * @param direction Strahlrichtung (normalisiert)
     */
    public void checkHit(Vector3f origin, Vector3f direction) {
        Target closestTarget = null;
        Vector3f closestHitCenter = null;
        Vector3f actualHitPoint = null;
        boolean isHeadHit = false;
        float minT = Float.MAX_VALUE;

        // 1. Finde nächstes gültiges Ziel
        for (Target target : targetManager.getTargets()) {
            if (target.isHit()) continue;

            // Kopfprüfung (immer über Boden)
            Float tHead = intersectSphereT(origin, direction,
                    target.getHeadPosition(), target.getHeadRadius());
            if (tHead != null && tHead < minT) {
                minT = tHead;
                closestTarget = target;
                closestHitCenter = target.getHeadPosition();
                actualHitPoint = calculateHitPoint(origin, direction, tHead);
                isHeadHit = true;
            }

            // Körperprüfung mit Bodenbedingung
            Float tBody = intersectSphereT(origin, direction,
                    target.getPosition(), target.getBodyRadius());
            if (tBody != null && tBody < minT) {
                Vector3f bodyHitPoint = calculateHitPoint(origin, direction, tBody);

                // WICHTIG: Nur Treffer oberhalb des Bodens zählen (y >= 0)
                if (bodyHitPoint.y >= 0) {
                    minT = tBody;
                    closestTarget = target;
                    closestHitCenter = target.getPosition();
                    actualHitPoint = bodyHitPoint;
                    isHeadHit = false;
                }
            }
        }

        // 2. Verarbeite Treffer/Fehlschuss (Rest bleibt ähnlich)
        if (closestTarget != null) {
            closestTarget.markHit();
            stats.registerHit(isHeadHit);

            // Für Heatmap: Zentrum des getroffenen Bereichs (Kopf/Körper)
            HeatmapValues hitOffset = heatmapCheck.checkShot(
                    direction,
                    closestHitCenter,
                    origin
            );
            hitOffset.setHitStatus(true);
            stats.hadd(hitOffset);

        } else {
            // Fehlschussbehandlung unverändert
            stats.registerMiss();
            HeatmapValues bestMiss = findClosestMiss(origin, direction, targetManager.getTargets());
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
     * Berechnet den Schnittpunkt zwischen Strahl und Kugel
     *
     * @return t-Wert (Entfernung entlang des Strahls) oder null wenn kein Treffer
     */
    private Float intersectSphereT(Vector3f origin, Vector3f dir, Vector3f center, float radius) {
        Vector3f oc = new Vector3f(origin).sub(center);
        float a = dir.dot(dir);
        float b = 2.0f * oc.dot(dir);
        float c = oc.dot(oc) - radius * radius;
        float discriminant = b * b - 4 * a * c;

        if (discriminant < 0) return null;

        float sqrtDisc = (float) Math.sqrt(discriminant);
        float t1 = (-b - sqrtDisc) / (2 * a);
        float t2 = (-b + sqrtDisc) / (2 * a);

        // Wähle kleinste positive Lösung
        float t = Float.MAX_VALUE;
        if (t1 >= 0) t = Math.min(t, t1);
        if (t2 >= 0) t = Math.min(t, t2);

        return (t < Float.MAX_VALUE) ? t : null;
    }

    /**
     * Berechnet den tatsächlichen Trefferpunkt
     */
    private Vector3f calculateHitPoint(Vector3f origin, Vector3f dir, float t) {
        return new Vector3f(dir).mul(t).add(origin);
    }
}
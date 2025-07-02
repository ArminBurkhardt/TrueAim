package org.trueaim;

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

    public Raycasting(TargetManager targetManager, StatTracker stats) {
        this.targetManager = targetManager;
        this.stats = stats;
    }

    /**
     * Prüft auf Treffer entlang eines Strahls.
     * @param origin Strahlursprung (Kameraposition)
     * @param direction Strahlrichtung (normalisiert)
     */
    public void checkHit(Vector3f origin, Vector3f direction) {
        List<Target> targets = targetManager.getTargets();
        boolean hitRegistered = false;

        // Alle aktiven Ziele prüfen
        for (Target target : targets) {
            if (target.isHit()) continue;  // Überspringe bereits getroffene Ziele

            // Kopf-Treffer prüfen (höhere Priorität)
            if (intersectsSphere(origin, direction, target.getHeadPosition(), target.getHeadRadius())) {
                target.markHit();
                stats.registerHit(true);  // Kopftreffer registrieren
                hitRegistered = true;
                break;  // Nur ein Treffer pro Schuss
            }

            // Körper-Treffer prüfen
            if (intersectsSphere(origin, direction, target.getPosition(), target.getBodyRadius())) {
                target.markHit();
                stats.registerHit(false);  // Körpertreffer registrieren
                hitRegistered = true;
                break;  // Nur ein Treffer pro Schuss
            }
        }

        // Fehlschuss registrieren, wenn kein Ziel getroffen
        if (!hitRegistered) stats.registerMiss();
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
        Vector3f oc = new Vector3f(origin).sub(center);  // Vektor: Kugelmittelpunkt -> Strahlursprung
        float a = dir.dot(dir);           // Koeffizient a (immer 1 bei normalisiertem Vektor)
        float b = 2.0f * oc.dot(dir);     // Koeffizient b
        float c = oc.dot(oc) - radius * radius;  // Koeffizient c

        // Diskriminante der quadratischen Gleichung
        float discriminant = b * b - 4 * a * c;

        // Kein Schnittpunkt wenn Diskriminante negativ
        if (discriminant < 0) return false;

        // Kleinere Lösung berechnen (erster Schnittpunkt)
        float t = (-b - (float)Math.sqrt(discriminant)) / (2 * a);

        // Nur Schnittpunkte vor der Kamera berücksichtigen (t >= 0)
        return t >= 0;
    }
}
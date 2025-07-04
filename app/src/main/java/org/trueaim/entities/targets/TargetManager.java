package org.trueaim.entities.targets;

import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Verwaltet alle Zielobjekte im Spiel.
 * Handhabt:
 * - Erstellung und Platzierung
 * - Bewegungslogik
 * - Zurücksetzen
 */
public class TargetManager {
    private final List<Target> targets = new ArrayList<>(); // Zielobjekte
    private final Random random = new Random();             // Zufallsgenerator

    public TargetManager() {
        spawnInitialTargets();  // Initialziele erstellen
    }

    /**
     * Erstellt die Startkonfiguration der Ziele.
     */
    private void spawnInitialTargets() {
        // Statische Ziele
        targets.add(new Target(new Vector3f(0, 0, -5)));
        targets.add(new Target(new Vector3f(2.5f, 0, -6)));
        targets.add(new Target(new Vector3f(-2, 0, -7)));
        targets.add(new Target(new Vector3f(-1.5f, 0, -6)));

        // Bewegliche Ziele
        targets.add(new MovingTarget(
                new Vector3f(1.5f, 0, -8),
                new Vector3f(0.5f, 0, 0)  // Bewegungsrichtung
        ));

    }

    // Zugriffsmethoden
    public List<Target> getTargets() { return targets; }

    /**
     * Aktualisiert alle Ziele.
     * @param dt Zeit seit letztem Frame
     */
    public void update(float dt) {
        for (Target target : targets) target.update(dt);
    }

    /**
     * Setzt alle Ziele zurück und platziert sie neu.
     */
    public void resetAll() {
        for (Target target : targets) target.reset(); // Trefferstatus zurücksetzen


        // Neue zufällige Positionen
        for (Target target : targets) {
            float x = random.nextFloat() * 8 - 4; // X: -4 bis +4
            float z = -5 - random.nextFloat() * 5; // Z: -5 bis -10
            target.setPosition(new Vector3f(x, 0, z));
        }
    }

    // Innere Klassen für spezielle Zieltypen
    private static class MovingTarget extends Target {
        public MovingTarget(Vector3f position, Vector3f velocity) {
            super(position, velocity);
        }

        @Override
        public void update(float deltaTime) {
            super.update(deltaTime);
            // Richtungswechsel bei Grenzerreichen
            //TODO auch abfrage für andere Richtungen falls man Movement anpassen will
            if (getPosition().x > 3 || getPosition().x < -3) {
                setVelocity(getVelocity().mul(-1, new Vector3f()));
            }
        }
    }
}
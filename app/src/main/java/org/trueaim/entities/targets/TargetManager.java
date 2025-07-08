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

    // Neue Bereichsgrenzen für die Zielpositionierung
    private final float MIN_X = 5f;      // Start der Ziele (nicht zu nah an Kamera)
    private final float MAX_X = 25f;     // Ende vor der Wand (x=30)
    private final float MIN_Z = -10f;    // Linke Grenze
    private final float MAX_Z = 10f;     // Rechte Grenze

    public TargetManager() {
        spawnInitialTargets();  // Initialziele erstellen
    }

    /**
     * Erstellt die Startkonfiguration der Ziele.
     */
    private void spawnInitialTargets() {
        // Statische Ziele
        targets.add(new Target(new Vector3f(10, 0, -5)));
        targets.add(new Target(new Vector3f(15, 0, 3)));
        targets.add(new Target(new Vector3f(20, 0, -2)));
        targets.add(new Target(new Vector3f(12, 0, 4)));

        // Bewegliches Ziel - Bewegung in Z-Richtung
        targets.add(new MovingTarget(
                new Vector3f(18, 0, 0),      // Startposition
                new Vector3f(0, 0, 2.0f)     // Erhöhte Geschwindigkeit
        ));
    }

    // Zugriffsmethoden
    public List<Target> getTargets() { return targets; }


    // TODO: vllt könntest du nochmal drüberschauen, ob das so passt

    public void setTargets(int numMoving, int numStatic) {
        targets.clear(); // Alte Ziele entfernen

        // Statische Ziele
        for (int i = 0; i < numStatic; i++) {
            float x = MIN_X + random.nextFloat() * (MAX_X - MIN_X);
            float z = MIN_Z + random.nextFloat() * (MAX_Z - MIN_Z);
            targets.add(new Target(new Vector3f(x, 0, z)));
        }

        // Bewegliche Ziele
        for (int i = 0; i < numMoving; i++) {
            float x = MIN_X + random.nextFloat() * (MAX_X - MIN_X);
            float z = MIN_Z + random.nextFloat() * (MAX_Z - MIN_Z);
            targets.add(new MovingTarget(new Vector3f(x, 0, z), new Vector3f(0, 0, 2.0f)));
        }
    }

    public void setTargetsRandom(int numTargets) {
        targets.clear(); // Alte Ziele entfernen

        for (int i = 0; i < numTargets; i++) {
            float x = MIN_X + random.nextFloat() * (MAX_X - MIN_X);
            float z = MIN_Z + random.nextFloat() * (MAX_Z - MIN_Z);
            // 50% Chance für bewegliches Ziel
            if (random.nextBoolean()) {
                targets.add(new MovingTarget(new Vector3f(x, 0, z), new Vector3f(0, 0, 2.0f)));
            } else {
                targets.add(new Target(new Vector3f(x, 0, z)));
            }
        }
    }


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
            // Neue zufällige Position im definierten Bereich
            float x = MIN_X + random.nextFloat() * (MAX_X - MIN_X);
            float z = MIN_Z + random.nextFloat() * (MAX_Z - MIN_Z);
            target.setPosition(new Vector3f(x, 0, z));
        }
    }

    // Innere Klassen für spezielle Zieltypen
    // TODO eigene Klasse wenn man Targets erweitern will
    private static class MovingTarget extends Target {
        public MovingTarget(Vector3f position, Vector3f velocity) {
            super(position, velocity);
        }

        @Override
        public void update(float deltaTime) {
            super.update(deltaTime);
            // Richtungswechsel bei Grenzerreichen
            //TODO auch abfrage für andere Richtungen falls man Movement anpassen will
            if (getPosition().z > 7 || getPosition().z < -7) {
                setVelocity(getVelocity().mul(-1, new Vector3f()));
            }
        }
    }
}
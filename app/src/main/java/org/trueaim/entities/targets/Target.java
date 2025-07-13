package org.trueaim.entities.targets;
import org.joml.Vector3f;

/**
 * Basisklasse für alle Zielobjekte.
 * Repräsentiert ein treffbares Objekt mit Position und Größe.
 */
public class Target {
    private Vector3f position;          // Körperposition
    private final Vector3f headOffset = new Vector3f(0, 0.70f, 0); // Kopfposition relativ zum Körper
    private final float bodyRadius = 0.5f;    // Körperradius
    private final float headRadius = 0.25f;   // Kopfradius
    private boolean hit = false;        // Trefferstatus
    private Vector3f velocity;          // Bewegungsvektor

    // Konstruktoren
    public Target(Vector3f position) {
        this(position, new Vector3f(0, 0, 0)); // Stationäres Ziel
    }
    public Target(Vector3f position, Vector3f velocity) {
        this.position = position;
        this.velocity = velocity;
    }

    /**
     * Aktualisiert die Zielposition basierend auf Geschwindigkeit.
     * @param deltaTime Zeit seit letztem Frame
     */
    public void update(float deltaTime) {
        // Bewegung: position += velocity * deltaTime
        position.add(velocity.mul(deltaTime, new Vector3f()));
    }

    // Zugriffs- und Settermethoden
    public void setPosition(Vector3f position) { this.position = position; }
    public Vector3f getPosition() { return new Vector3f(position); }
    public Vector3f getHeadPosition() { return new Vector3f(position).add(headOffset); }
    public float getBodyRadius() { return bodyRadius; }
    public float getHeadRadius() { return headRadius; }
    public boolean isHit() { return hit; }
    public Vector3f getVelocity() { return new Vector3f(velocity); }
    public void setVelocity(Vector3f velocity) { this.velocity = velocity; }
    public void markHit() { hit = true; }       // Ziel als getroffen markieren
    public void reset() { hit = false; }        // Trefferstatus zurücksetzen
}
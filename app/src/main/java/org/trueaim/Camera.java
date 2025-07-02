package org.trueaim;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Repräsentiert die Spielkamera mit Position und Orientierung.
 * Verwaltet:
 * - Kameraposition und Blickrichtung
 * - Rotationsberechnungen
 * - View-Matrix-Generierung
 */
public class Camera {
    // Kameraposition im 3D-Raum
    private final Vector3f position = new Vector3f(0, 1, 3);
    // Euler-Rotation (x: Pitch, y: Yaw, z: Roll)
    private final Vector3f rotation = new Vector3f();
    // Richtungsvektoren
    private final Vector3f front = new Vector3f(0, 0, -1);  // Blickrichtung
    private final Vector3f up = new Vector3f(0, 1, 0);      // Hochvektor
    private final Vector3f right = new Vector3f(1, 0, 0);   // Rechtsvektor
    // View-Matrix (berechnet bei Bedarf)
    private final Matrix4f viewMatrix = new Matrix4f();

    // Bewegungsmethoden (relativ zur aktuellen Ausrichtung)
    public void moveForward(float dist) { position.fma(dist, front); }
    public void moveBackward(float dist) { position.fma(-dist, front); }
    public void moveLeft(float dist) { position.fma(-dist, right); }
    public void moveRight(float dist) { position.fma(dist, right); }

    /**
     * Rotiert die Kamera um die angegebenen Winkel.
     * @param yaw Horizontalrotation (Grad)
     * @param pitch Vertikalrotation (Grad)
     */
    public void rotate(float yaw, float pitch) {
        // Rotation anwenden
        rotation.y += yaw;    // Yaw (horizontale Drehung)
        rotation.x += pitch;   // Pitch (vertikale Drehung)

        // Pitch auf -89° bis +89° begrenzen (verhindert Gimbal Lock)
        rotation.x = Math.max(-89, Math.min(89, rotation.x));

        // Richtungsvektoren neu berechnen
        updateVectors();
    }

    /**
     * Aktualisiert die Richtungsvektoren basierend auf der aktuellen Rotation.
     * Verwendet sphärische Koordinaten für Blickrichtung.
     */
    private void updateVectors() {
        // Umrechnung in Radians
        float yawRad = (float) Math.toRadians(rotation.y);
        float pitchRad = (float) Math.toRadians(rotation.x);

        // Blickrichtung berechnen (sphärische Koordinaten)
        front.x = (float) (Math.cos(yawRad) * Math.cos(pitchRad));
        front.y = (float) Math.sin(pitchRad);
        front.z = (float) (Math.sin(yawRad) * Math.cos(pitchRad));
        front.normalize();  // Normalisierung für konstante Geschwindigkeit

        // Rechts- und Hochvektor neu berechnen
        right.set(front).cross(up).normalize();
    }

    /**
     * @return Aktuelle View-Matrix (Kameraorientierung)
     */
    public Matrix4f getViewMatrix() {
        return viewMatrix.identity().lookAt(
                position,                  // Kameraposition
                position.add(front, new Vector3f()),  // Blickpunkt (Position + Front)
                up                        // Welt-Up-Vektor
        );
    }

    // Zugriffsmethoden
    public Vector3f getPosition() { return new Vector3f(position); }
    public Vector3f getFront() { return new Vector3f(front); }
}
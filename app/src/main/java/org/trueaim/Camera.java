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
    private Vector3f position = new Vector3f(0,1, 0);
    // Euler-Rotation (x: Pitch, y: Yaw, z: Roll)
    private final Vector3f rotation = new Vector3f();
    // Richtungsvektoren
    private Vector3f front = new Vector3f(1, 0, 0);  // Blickrichtung
    private final Vector3f up = new Vector3f(0, 1, 0);      // Hochvektor
    private final Vector3f right = new Vector3f(1, 0, 0);   // Rechtsvektor
    // View-Matrix
    private final Matrix4f viewMatrix = new Matrix4f();

    // Bewegungsmethoden (relativ zur aktuellen Ausrichtung) (beschränkter Bewegungsraum)
    public void moveForward(float dist) {
        Vector3f theoreticalPosition = position.fma(dist, front);
        position =  vectorSqueeze(theoreticalPosition, -3, 3);
    }
    public void moveBackward(float dist) {
        Vector3f theoreticalPosition = position.fma(-dist, front);
        position = vectorSqueeze(theoreticalPosition, -3, 3);
    }
    public void moveLeft(float dist) {
        Vector3f theoreticalPosition = position.fma(-dist, right);
        position = vectorSqueeze(theoreticalPosition, -3, 3);
    }
    public void moveRight(float dist) {
        Vector3f theoreticalPosition = position.fma(dist, right);
        position = vectorSqueeze(theoreticalPosition, -3, 3);
    }

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
    // Squeeze-Funktion, um Werte zwischen min und max zu begrenzen
    public float squeeze(float value, float min, float max) {
        // Squeezed value between min and max
        return Math.max(min, Math.min(max, value));
    }

    public Vector3f vectorSqueeze(Vector3f vector, float min, float max) {
        // Squeeze each component of the vector
        return new Vector3f(
                squeeze(vector.x, min, max),
                1.0f, // Y-Position auf 1 setzen (Bodenhöhe)
                squeeze(vector.z, min, max)
        );
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
    public void setFront(float x, float y, float z) {
        this.front = new Vector3f(x, y, z);
    }
}
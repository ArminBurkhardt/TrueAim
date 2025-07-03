package org.trueaim.rendering;

import org.trueaim.Camera;
import org.trueaim.entities.targets.Target;
import org.trueaim.entities.targets.TargetManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;

/**
 * Verantwortlich für das Rendern der 3D-Spielwelt.
 * Verwendet Fixed-Function Pipeline
 */
public class Renderer {
    private final Matrix4f proj = new Matrix4f();  // Projektionsmatrix
    private final SphereRenderer bodyRenderer = new SphereRenderer(0.5f);  // Körperrenderer
    private final SphereRenderer headRenderer = new SphereRenderer(0.25f); // Kopfrenderer
    private int FOV = 70;      //FOV Wert (Standard ist 70)

    public Renderer(int width, int height) {
        // Projektionsmatrix einstellen (Perspektive)
        proj.identity().perspective(
                (float)Math.toRadians(FOV),  // FOV
                (float)width/height,       // Seitenverhältnis
                0.1f,                       // Nahclipping
                100f                        // Fernclipping
        );
        glEnable(GL_DEPTH_TEST);  // Tiefentest aktivieren
    }

    /**
     * Ändert die FOV
     * @param fov neuer FOV Wert
     */
    public void setFOV(int fov){
        this.FOV = fov;
    }


    /**
     * Rendert die gesamte Szene.
     * @param cam Aktive Kamera
     * @param tm Zielmanager mit darzustellenden Objekten
     */
    public void render(Camera cam, TargetManager tm) {
        // Hintergrundfarbe (Himmel)
        glClearColor(0.5f, 0.7f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Projektionsmatrix laden
        glMatrixMode(GL_PROJECTION);
        glLoadMatrixf(proj.get(new float[16]));

        // View-Matrix laden
        glMatrixMode(GL_MODELVIEW);
        glLoadMatrixf(cam.getViewMatrix().get(new float[16]));

        // Boden rendern
        renderFloor();

        // Aktive Ziele rendern
        for (Target t : tm.getTargets()) {
            if (t.isHit()) continue;  // Überspringe getroffene Ziele

            // Körper rendern
            glPushMatrix();
            glTranslatef(t.getPosition().x, t.getPosition().y, t.getPosition().z);
            glColor3f(1f, 0f, 0f);  // Rot
            bodyRenderer.render();
            glPopMatrix();

            // Kopf rendern
            glPushMatrix();
            Vector3f headPos = t.getHeadPosition();
            glTranslatef(headPos.x, headPos.y, headPos.z);
            glColor3f(1f, 1f, 0f);  // Gelb
            headRenderer.render();
            glPopMatrix();
        }
    }

    /**
     * Rendert den Boden und Wände.
     */
    private void renderFloor() {
        // Boden (grün)
        glColor3f(0.2f, 0.6f, 0.3f);
        glBegin(GL_QUADS);
        glVertex3f(-10, 0, -10);
        glVertex3f(10, 0, -10);
        glVertex3f(10, 0, 10);
        glVertex3f(-10, 0, 10);
        glEnd();

        // Rückwand (grau)
        glColor3f(0.7f, 0.7f, 0.7f);
        glBegin(GL_QUADS);
        glVertex3f(-10, 0, -10);
        glVertex3f(10, 0, -10);
        glVertex3f(10, 5, -10);
        glVertex3f(-10, 5, -10);
        glEnd();
    }
}
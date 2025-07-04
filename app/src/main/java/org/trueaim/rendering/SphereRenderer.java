package org.trueaim.rendering;
import static org.lwjgl.opengl.GL11.*;

/**
 * Rendert einfache Kugeln
 * vielleicht Löschen wenn richtige Targets implementiert werden
 */

//TODO maybe löschen wenn  Targets verbessert werden, muss aber nicht sein
public class SphereRenderer {
    private int displayListId;  // OpenGL-Display-List-ID

    public SphereRenderer(float radius) {
        // Display-List für Kugel erstellen
        displayListId = glGenLists(1);
        glNewList(displayListId, GL_COMPILE);
        drawImmediateSphere(radius, 16, 16);  // Kugel zeichnen
        glEndList();
    }

    /**
     * Zeichnet Kugel mit Immediate Mode.
     * @param r Radius
     * @param stacks Vertikale Segmente
     * @param slices Horizontale Segmente
     */
    private void drawImmediateSphere(float r, int stacks, int slices) {
        // Kugel in vertikalen Stacks und horizontalen Slices aufteilen
        for (int i = 0; i < stacks; i++) {
            // Aktuelle Stack-Berechnung
            float lat0 = (float) Math.PI * (-0.5f + (float)i / stacks);
            float z0 = (float) Math.sin(lat0);
            float zr0 = (float) Math.cos(lat0);

            // Nächster Stack
            float lat1 = (float) Math.PI * (-0.5f + (float)(i + 1) / stacks);
            float z1 = (float) Math.sin(lat1);
            float zr1 = (float) Math.cos(lat1);

            // Quad-Strip für aktuellen Stack
            glBegin(GL_QUAD_STRIP);
            for (int j = 0; j <= slices; j++) {
                float lng = 2 * (float) Math.PI * j / slices;
                float x = (float) Math.cos(lng);
                float y = (float) Math.sin(lng);

                // Vertices für aktuellen und nächsten Stack
                glVertex3f(x * zr0 * r, y * zr0 * r, z0 * r);
                glVertex3f(x * zr1 * r, y * zr1 * r, z1 * r);
            }
            glEnd();
        }
    }

    /**
     * Rendert die Kugel mit Display-List.
     */
    public void render() {
        glCallList(displayListId);
    }
}
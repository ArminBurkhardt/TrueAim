package org.trueaim.rendering;
import static org.lwjgl.opengl.GL11.*;

/**
 * Rendert ein einfaches Fadenkreuz in der Bildschirmmitte.
 */
public class CrosshairRenderer {
    /**
     * Zeichnet das Fadenkreuz.
     * Verwendet orthogonale Projektung für 2D-Rendering.
     */
    private boolean isVisible = true; // Sichtbarkeit des Fadenkreuzes

    public void render() {
        if (!isVisible) {
            return; // Fadenkreuz nicht zeichnen, wenn es nicht sichtbar ist
        }
        glDisable(GL_DEPTH_TEST);  // Tiefentest deaktivieren

        // Projektionsmatrix für 2D setzen
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, 1, 1, 0, -1, 1);  // Normierte Koordinaten [0,1]

        // ModelView-Matrix zurücksetzen
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        // Linien zeichnen
        glLineWidth(2);  // Linienstärke
        glColor3f(1.0f, 1.0f, 1.0f);  // Weiß
        glBegin(GL_LINES);
        // Horizontale Linie
        glVertex2f(0.49f, 0.5f);
        glVertex2f(0.51f, 0.5f);
        // Vertikale Linie
        glVertex2f(0.5f, 0.49f);
        glVertex2f(0.5f, 0.51f);
        glEnd();

        // Matrizen restaurieren
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);

        glEnable(GL_DEPTH_TEST);  // Tiefentest wieder aktivieren
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible; // Sichtbarkeit des Fadenkreuzes setzen
    }
}
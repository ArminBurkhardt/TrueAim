package org.trueaim.entities.weapons;
import org.joml.Vector2f;

/**
 * Definiert Rückstoßmuster für Waffen.
 * Speichert vordefinierte Muster für realistischen Waffenrückstoß.
 */

/**TODO Rückstoß erhöhen, (vielleicht erst navchdem auto fire implementiert + getestet)
 * TODO wenn unterschiedliche Waffen, vielleicht dies Klasse je in WAffenklasse einbinden TBD
 */
public class RecoilPattern {
    // AK-47 Rückstoßmuster (x: horizontal, y: vertikal)
    private static final Vector2f[] AK47_PATTERN = {
            new Vector2f(0.0f, 1.6f),  // Schuss 1: Hauptsächlich vertikal
            new Vector2f(0.6f, 1.4f),  // Schuss 2: Rechts + oben
            new Vector2f(1.0f, 1.2f),  // Schuss 3: Stärker rechts
            new Vector2f(0.8f, 1.0f),  // Schuss 4: Weniger vertikal
            new Vector2f(0.4f, 0.8f),  // Schuss 5: Zentriert sich
            new Vector2f(-0.2f, 0.6f), // Schuss 6: Leicht links
            new Vector2f(-0.8f, 0.4f)  // Schuss 7: Stärker links
    };

    /**
     * Gibt Rückstoß für bestimmten Schuss zurück.
     * @param shotCount Fortlaufende Schussnummer (1-basiert)
     * @return Rückstoßvektor (x: horizontal, y: vertikal)
     */
    public static Vector2f getRecoil(int shotCount) {
        // Nur definierte Schüsse zurückgeben
        if (shotCount < 1 ) {
            return new Vector2f(0, 0);
        }
        return AK47_PATTERN[(shotCount - 1) % AK47_PATTERN.length];
    }
}
package org.trueaim.stats;
import java.util.ArrayList;
import java.util.List;

/**
 * Sammelt und berechnet Spielstatistiken.
 * Verfolgt:
 * - Schüsse, Treffer, Fehlschüsse
 * - Kopftrefferquote
 * - Schussfrequenz
 */
//TODO testen (nach ändern von Waffen) (v.a. rpm)

public class StatTracker {
    private int shotsFired = 0;      // Gesamtschüsse
    private int hits = 0;            // Erfolgreiche Treffer
    private int misses = 0;          // Fehlschüsse
    private int headshots = 0;       // Kopftreffer
    private int reloads = 0;         // Reloads, ka ob interessant vielleicht entfernen//TODO
    private final List<Long> shotTimes = new ArrayList<>(); // Zeitpunkte der Schüsse
    private long sessionStartTime = System.currentTimeMillis(); // Spielstartzeit

    // Ereignisregistrierung
    public void incrementShotsFired() {
        shotsFired++;
        shotTimes.add(System.currentTimeMillis());
    }
    public void registerHit(boolean isHeadshot) {
        hits++;
        if(isHeadshot) headshots++;
    }
    public void registerMiss() {
        misses++;
    }
    public void registerReload(){reloads++;}

    // Statistikkalkulation
    public float getAccuracy() {
        return shotsFired > 0 ? (float) hits / shotsFired * 100 : 0;
    }
    public float getHeadshotRate() {
        return hits > 0 ? (float) headshots / hits * 100 : 0;
    }
    public float getShotsPerMinute() {
        long sessionDuration = (System.currentTimeMillis() - sessionStartTime) / 60000;
        return sessionDuration > 0 ? shotsFired / (float)sessionDuration : 0;
    }

    // Zugriffsmethoden
    public int getShotsFired() { return shotsFired; }
    public int getHits() { return hits; }
    public int getMisses() { return misses; }
    public int getHeadshots() { return headshots; }
    public int getReloads() { return reloads;}
}
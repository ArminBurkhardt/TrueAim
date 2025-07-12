package org.trueaim.stats;
import org.trueaim.strahlwerfen.HeatmapValues;

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
    private boolean enabled = true;
    private List<HeatmapValues> heatmapValues = new ArrayList<>(); // Liste mit Heatmap Werten

    // Ereignisregistrierung
    public void incrementShotsFired() {
        if (!enabled) return; // Wenn deaktiviert, nichts tun
        shotsFired++;
        shotTimes.add(System.currentTimeMillis());
    }
    public void registerHit(boolean isHeadshot) {
        if (!enabled) return; // Wenn deaktiviert, nichts tun
        hits++;
        if(isHeadshot) headshots++;
    }
    public void registerMiss() {
        if (!enabled) return; // Wenn deaktiviert, nichts tun
        misses++;
    }
    public void registerReload(){
        if (!enabled) return; // Wenn deaktiviert, nichts tun
        reloads++;
    }

    // Statistikkalkulation
    public float getAccuracy() {
        return shotsFired > 0 ? (float) hits / shotsFired * 100 : 0;
    }
    public float getHeadshotRate() {
        return hits > 0 ? (float) headshots / hits * 100 : 0;
    }
    public float getShotsPerMinute() {
        float sessionDuration = (System.currentTimeMillis() - sessionStartTime) / 60000f; // Dauer in Minuten
        if (sessionDuration <= 0) {
            return 0; // Verhindert Division durch Null
        } else if (sessionDuration <= 1) {
            return shotsFired; // Wenn weniger als 1 Minute, einfach Schüsse zurückgeben
        } else {
            return shotsFired / sessionDuration; // Schüsse pro Minute
        }
        // return sessionDuration > 0 ? shotsFired / (float)sessionDuration : 0;
    }

    // Zugriffsmethoden
    public int getShotsFired() { return shotsFired; }
    public int getHits() { return hits; }
    public int getMisses() { return misses; }
    public int getHeadshots() { return headshots; }
    public int getReloads() { return reloads;}
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public List<HeatmapValues> getHeatmapValues() {
        return heatmapValues;
    }

    public void resetStats() {
        shotsFired = 0;
        hits = 0;
        misses = 0;
        headshots = 0;
        reloads = 0;
        shotTimes.clear();
        sessionStartTime = System.currentTimeMillis(); // Reset Startzeit
        heatmapValues.clear(); // Heatmap-Werte zurücksetzen
    }

    // Heatmap-Werte hinzufügen
    public void hadd(HeatmapValues value){
        if (!enabled) return; // Wenn deaktiviert, nichts tun
        if (value != null) {
            heatmapValues.add(value);
        }
    }


    //Debgug TODO delete
    public void fprint(){
        for (HeatmapValues shot : getHeatmapValues()) {
            shot.print();
        }
    }
}
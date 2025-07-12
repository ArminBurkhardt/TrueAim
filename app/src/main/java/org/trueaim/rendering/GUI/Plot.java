package org.trueaim.rendering.GUI;

//TODO x Werte sind im Plot gespiegelt. KA warum, man könnte sie aber einfach hier mit * -1 noichmal spiegeln

import org.lwjgl.nanovg.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import static org.trueaim.Utils.rgba;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVG.nvgClosePath;
import static org.lwjgl.nanovg.NanoVG.nvgStroke;
import static org.lwjgl.nanovg.NanoVG.nvgStrokeColor;

/**
 * Repräsentiert ein Plot-Objekt, das Daten in einem Diagramm darstellt.
 * Unterstützt verschiedene Typen von Plots (z.B. Linien- oder Streudiagramme).
 * Ermöglicht das Hinzufügen von Datenpunkten, das Anpassen von Farben und das Zeichnen des Plots.
 */
public class Plot {

    private int x;
    private int y;
    private int width;
    private int height;
    private String title;
    private float[][] data; // Daten für das Plot
    private int centerX;
    private int centerY;
    private boolean isVisible;
    private String type; // Typ des Plots, z.B. "line", "scatter", etc.
    private ArrayList<Runnable> extraPoints;
    private NVGColor[] extraColors; // Farben für DataSingleRGBA
    DoubleBuffer posx, posy; // Position für Mauszeiger (falls benötigt)
    private boolean drawLabels = true; // Ob Achsenbeschriftungen gezeichnet werden sollen
    private float fontSize = 20.0f; // Schriftgröße für Achsenbeschriftungen

    private float forcedMaxX = Float.MIN_VALUE; // Optionaler Maximalwert für X-Achse
    private float forcedMaxY = Float.MIN_VALUE; // Optionaler Maximalwert für Y-Achse
    private boolean useTrueCenter = false; // Ob das Zentrum der Achsen auf den Nullpunkt gesetzt werden soll

    private NVGColor colorA;
    private NVGColor colorB;
    private NVGColor colorC;
    private NVGColor colorD;
    private NVGColor colorE;
    private NVGPaint paint;


    private int[] BackgroundRGBA = new int[4]; // Hintergrundfarbe des Plots, z.B. {255, 255, 255, 255} für Weiß
    private int[] DataRGBA = new int[4];       // Farbe für die Datenlinien oder -punkte
    private int[] AxisRGBA = new int[4];       // Farbe für die Achsen
    private int[][] DataSingleRGBA = new int[0][4]; // Farbe für einzelne Datenpunkte

    /**
     * Konstruktor für den Plot.
     *
     * @param x      X-Position des Plots
     * @param y      Y-Position des Plots
     * @param width  Breite des Plots
     * @param height Höhe des Plots
     * @param title  Titel des Plots
     * @param data   Daten für das Plot, als 2D-Array (z.B. { {x1, y1}, {x2, y2}, ... })
     * @param type   Typ des Plots (z.B. "line", "scatter")
     */

    public Plot(int x, int y, int width, int height, String title, float[][] data, String type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
        this.data = data;
        this.isVisible = true; // Standardmäßig sichtbar
        this.type = type;
        setCenter();
        extraPoints = new ArrayList<>();
        init();
        initColors();
    }

    public Plot( int x, int y, int width, int height, String title, float[][] data) {
        this(x, y, width, height, title, data, "line"); // Standardtyp ist "line"
    }
    public Plot(int x, int y, int width, int height, String title) {
        this(x, y, width, height, title, new float[0][0]); // Leeres Datenarray
    }

    private void init() {
        colorA = NVGColor.create();
        colorB = NVGColor.create();
        colorC = NVGColor.create();
        colorD = NVGColor.create();
        colorE = NVGColor.create();
        paint = NVGPaint.create();



        posx = MemoryUtil.memAllocDouble(1);
        posy = MemoryUtil.memAllocDouble(1);

    }

    private void initColors() {
        // Initialisiert die Farben für den Plot
        DataRGBA = new int[]{255, 255, 255, 255}; // Weiß
        BackgroundRGBA = new int[]{0, 0, 0, 255}; // Schwarz
        AxisRGBA = new int[]{200, 200, 200, 255}; // Hellgrau
        DataSingleRGBA = null; // Keine einzelnen Datenfarben gesetzt, muss manuell gesetzt werden
    }



    // Fügt einen Punkt zum Plot hinzu, der später gezeichnet wird.
    // Nicht wirklich ne Funktion, die automatisch aufgerufen wird, sondern eine, die manuell aufgerufen wird, um Punkte hinzuzufügen.
    public void _addPoint(long vg, float X, float Y, int[] rgba, int radius) {
        extraPoints.add(() -> {
            nvgBeginPath(vg);
            nvgStrokeWidth(vg, radius+.1f);
            nvgCircle(vg, X, Y, radius);
            nvgStrokeColor(vg, rgba(rgba, colorA));
            nvgStroke(vg);
            nvgClosePath(vg);
        });
    }

    public void render(long vg) {
        if (!isVisible || data == null || data.length == 0) return;
        createColorsForSinglePoints();

        // Zeichnet den Hintergrund des Plots
        nvgBeginPath(vg);
        nvgRect(vg, x-10, y-10, width+20, height+20);
        nvgFillColor(vg, rgba(BackgroundRGBA, colorB));
        nvgFill(vg);
        nvgClosePath(vg);

        // Zeichnet den Titel des Plots
        nvgBeginPath(vg);
        nvgFontSize(vg, fontSize);
        nvgFontFace(vg, "OpenSans-Bold"); // Sollte inzwischen registriert sein
        nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_BOTTOM);
        nvgFillColor(vg, rgba(AxisRGBA, colorC));
        nvgText(vg, x + width / 2f, y - 10, title);
        nvgClosePath(vg);

        // Zeichnet die Achsen
        nvgBeginPath(vg);
        nvgStrokeWidth(vg, 4.0f);
        nvgMoveTo(vg, x, (y + height) - centerY); // X-Achse
        nvgLineTo(vg, x + width, (y + height) - centerY);
        nvgMoveTo(vg, x + centerX, y); // Y-Achse
        nvgLineTo(vg, x + centerX, y + height);
        nvgStrokeColor(vg, rgba(AxisRGBA, colorC));
        nvgStroke(vg);
        nvgClosePath(vg);

        // Offset für den Mittelpunkt
        int x = this.x + centerX;
        int y = this.y + height - centerY;

        // Daten skalieren
        float[][] scaledData = scalePointsForPlotting((float) (width - centerX) / maxX(), (float) (height - centerY) / maxY());
        if (scaledData == null) {
            scaledData = new float[0][0]; // Falls keine Daten vorhanden sind, leeres Array
        }

        // Zeichnet die Datenpunkte oder Linien
        if (hasData()) {
            if (type.equals("line")) {
                nvgBeginPath(vg);
                nvgStrokeWidth(vg, 4.0f);
                nvgFillColor(vg, rgba(DataRGBA, colorD));
                nvgStrokeColor(vg, rgba(DataRGBA, colorD));
                // Zeichnet eine Linie zwischen den Punkten
                for (int i = 0; i < scaledData.length - 1; i++) {
                    nvgMoveTo(vg, x + scaledData[i][0], y - scaledData[i][1]);                  // y-Achse geht nach unten, x-Achse nach rechts (=> invertiertes Y)
                    nvgLineTo(vg, x + scaledData[i + 1][0], y - scaledData[i + 1][1]);
                }
                nvgStroke(vg);
                nvgClosePath(vg);

            } else if (type.equals("scatter")) {
                // Zeichnet einzelne Punkte
                for (int i = 0; i < scaledData.length; i++) {
                    float[] point = scaledData[i];
                    nvgBeginPath(vg);
                    nvgStrokeWidth(vg, 4.0f);
                    if (extraColors != null && extraColors.length > i) {
                        nvgStrokeColor(vg, extraColors[i]);
                        nvgFillColor(vg, extraColors[i]);
                    } else {
                        nvgStrokeColor(vg, rgba(DataRGBA, colorD));
                        nvgFillColor(vg, rgba(DataRGBA, colorD));
                    }
                    nvgCircle(vg, x + point[0], y - point[1], 1.9f);
                    nvgStroke(vg);
                    nvgClosePath(vg);
                }
            }

        }

        x = this.x;
        y = this.y;

        // Zeichnet Achsenbeschriftungen
        if (drawLabels) {
            // Zentrum der Achsen / Nullpunkt
            nvgBeginPath(vg);
            nvgFontSize(vg, fontSize * 0.75f);
            nvgFontFace(vg, "OpenSans-Bold");
            nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_MIDDLE);
            nvgFillColor(vg, rgba(AxisRGBA, colorC));
            nvgText(vg, x + centerX - 10, y + height - centerY + 10, "0");
            nvgClosePath(vg);

            // X-Achsen-Beschriftung
            nvgBeginPath(vg);
            nvgFontSize(vg, fontSize * 0.75f);
            nvgFontFace(vg, "OpenSans-Bold");
            nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_MIDDLE);
            nvgFillColor(vg, rgba(AxisRGBA, colorC));
            nvgText(vg, x + width - 10, (y + height) - centerY + 10, String.format("%.02f", maxX())); // X-Achsen-Beschriftung
            if (hasNegativeDataX()) {
                nvgText(vg, x + 10, (y + height) - centerY + 10, String.format("%.02f", -maxX())); // X-Achsen-Beschriftung für negative Werte
            }
            nvgClosePath(vg);

            // Y-Achsen-Beschriftung
            nvgBeginPath(vg);
            nvgFontSize(vg, fontSize * 0.75f);
            nvgFontFace(vg, "OpenSans-Bold");
            nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_MIDDLE);
            nvgFillColor(vg, rgba(AxisRGBA, colorC));
            nvgText(vg, x + centerX - 20, y + 10, String.format("%.02f", maxY())); // Y-Achsen-Beschriftung
            if (hasNegativeDataY()) {
                nvgText(vg, x + centerX - 10, y + height - 10, String.format("%.02f", -maxY())); // Y-Achsen-Beschriftung für negative Werte
            }
            nvgClosePath(vg);

        }

        // Zeichnet zusätzliche Punkte, die manuell hinzugefügt wurden
        for (Runnable point : extraPoints) {
            point.run(); // Führt die benutzerdefinierte Zeichnung aus
        }


    }

    private void createColorsForSinglePoints() {
        if (DataSingleRGBA == null || DataSingleRGBA.length == 0) {
            extraColors = new NVGColor[0]; // Leere Liste, wenn keine Farben definiert sind
            return;
        }
        if (extraColors == null || extraColors.length != DataSingleRGBA.length) {
            // Wenn das Array nicht existiert oder die Länge nicht übereinstimmt, wird es neu erstellt
            extraColors = new NVGColor[DataSingleRGBA.length]; // Initialisiert das Array für die Farben
            for (int i = 0; i < DataSingleRGBA.length; i++) {
                extraColors[i] = rgba(DataSingleRGBA[i], NVGColor.create()); // Erstellt eine NVGColor für jeden Datenpunkt
            }
        }
    }

    // Getter und Setter für die Plot-Eigenschaften

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setData(float[][] data) {
        this.data = data;
        setCenter(); // Aktualisiert das Zentrum, wenn die Daten geändert werden
    }
    public float[][] getData() {
        return data;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
    public void setBackgroundRGBA(int[] rgba) {
        this.BackgroundRGBA = rgba;
    }
    public int[] getBackgroundRGBA() {
        return BackgroundRGBA;
    }
    public void setDataRGBA(int[] rgba) {
        this.DataRGBA = rgba;
    }
    public int[] getDataRGBA() {
        return DataRGBA;
    }
    public void setAxisRGBA(int[] rgba) {
        this.AxisRGBA = rgba;
    }
    public int[] getAxisRGBA() {
        return AxisRGBA;
    }
    public void setDataSingleRGBA(int[][] rgba) {
        this.DataSingleRGBA = rgba;
    }
    public int[][] getDataSingleRGBA() {
        return DataSingleRGBA;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize; // Setzt die Schriftgröße für Achsenbeschriftungen
    }
    public float getFontSize() {
        return fontSize; // Gibt die aktuelle Schriftgröße zurück
    }

    public void showAxisLabels() {
        this.drawLabels = true; // Ob Achsenbeschriftungen gezeichnet werden sollen
    }
    public void hideAxisLabels() {
        this.drawLabels = false; // Ob Achsenbeschriftungen gezeichnet werden sollen
    }

    private boolean hasData() {
        return data != null && data.length > 0;
    }

    private boolean hasNegativeDataX() {
        if (data == null || data[0].length == 0) return false;
        if (useTrueCenter) {
            return true;
        }
        for (float[] value : data) {
            if (value[0] < 0) return true; // Überprüfen, ob ein Wert negativ ist
        }
        return false;
    }

    private boolean hasNegativeDataY() {
        if (data == null || data[0].length == 0) return false;
        if (useTrueCenter) {
            return true;
        }
        for (float[] value : data) {
            if (value[1] < 0) return true; // Überprüfen, ob ein Wert negativ ist
        }
        return false;
    }

    private void setCenter() {
        this.centerX = hasNegativeDataX() ? (width / 2) : 0; // Berechnung des Zentrums
        this.centerY = hasNegativeDataY() ? (height / 2) : 0; // Berechnung des Zentrums
    }

    private float maxX() {
        if (forcedMaxX != Float.MIN_VALUE) {
            return forcedMaxX; // Gibt den festgelegten Maximalwert zurück, wenn vorhanden
        }
        float max = Float.MIN_VALUE;
        for (float[] point : data) {
            if (Math.abs(point[0]) > max) {
                max = Math.abs(point[0]);
            }
        }
        return max;
    }

    private float maxY() {
        if (forcedMaxY != Float.MIN_VALUE) {
            return forcedMaxY; // Gibt den festgelegten Maximalwert zurück, wenn vorhanden
        }
        float max = Float.MIN_VALUE;
        for (float[] point : data) {
            if (Math.abs(point[1]) > max) {
                max = Math.abs(point[1]);
            }
        }
        return max;
    }

    public void scalePoints(float scaleX, float scaleY) {
        if (data == null || data.length == 0) return; // Keine Daten vorhanden

        for (float[] point : data) {
            point[0] *= scaleX; // Skaliert den X-Wert
            point[1] *= scaleY; // Skaliert den Y-Wert
        }
        setCenter(); // Aktualisiert das Zentrum nach der Skalierung
    }

    public void setForcedMaxX(float maxX) {
        this.forcedMaxX = maxX; // Setzt den festgelegten Maximalwert für X
        setCenter(); // Aktualisiert das Zentrum nach dem Setzen des Maximalwerts
    }

    public void setForcedMaxY(float maxY) {
        this.forcedMaxY = maxY; // Setzt den festgelegten Maximalwert für Y
        setCenter(); // Aktualisiert das Zentrum nach dem Setzen des Maximalwerts
    }

    public void setUseTrueCenter(boolean useTrueCenter) {
        this.useTrueCenter = useTrueCenter; // Setzt, ob das Zentrum der Achsen auf den Nullpunkt gesetzt werden soll
        setCenter(); // Aktualisiert das Zentrum nach dem Setzen
    }

    private float[][] scalePointsForPlotting(float scaleX, float scaleY) {
        if (data == null || data.length == 0) return null; // Keine Daten vorhanden

        // BRUHHHHHHHH WASSSSSSS ISTT DAS DENN
        // WIE KANN ES SEIN DASS DAS NICHT FÜR EIN 2D ARRAY FUNKTIONIERT
        // WER HAT SICH DAS AUSGEDACHT
        // WER?????????
        // WHAT
        // THE
        // ....
        // ICH MUSS EINFACH NE EXTRA METHODE MACHEN UM DAS ZU KOPIEREN
        // WEIL EINFACH DIE ARRAYS IM ARRAY NICHT KOPIERT WERDEN
        // WARUM
        // DAS RATIONALE IST ES SOWIESO ALLE ELEMENTE IM ARRAY ZU KOPIEREN UM DEN GANZEN ARRAY ZU KOPIEREN
        // WAS IST DAS
        // WER
        // WARUM
        // WIE
        // ICH VERSTEHE ES NICHT
        // ABSOLUTER JAVA MOMENT
        // float[][] result = Arrays.copyOf(data, data.length); // data.clone();

        // Kopiert die Daten in ein neues Array
        float[][] result = copy2dArray(data);

        for (float[] point : result) {
            point[0] *= scaleX; // Skaliert den X-Wert
            point[1] *= scaleY; // Skaliert den Y-Wert
        }
        return result;
    }

    public float[][] copy2dArray(float[][] original) {
        if (original == null) return null; // Keine Daten vorhanden
        float[][] copy = new float[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length); // Kopiert jedes innere Array
        }
        return copy;
    }

    public void cleanup() {
        MemoryUtil.memFree(posx);
        MemoryUtil.memFree(posy);
        colorA.free();
        colorB.free();
        colorC.free();
        colorD.free();
        colorE.free();
        paint.free();
    }


}

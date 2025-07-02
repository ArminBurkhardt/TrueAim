package org.trueaim.shootable;

/**
 * Interface für schießbare Waffen.
 * Definiert Grundfunktionen jeder Waffe.
 * kinda useless, vielleicht entfernen
 */

public interface Shootable {
    /**
     * Wird bei Linksklick (Primärfeuer) ausgelöst.
     */
    void onLeftPress();

    /**
     * Wird bei Rechtsklick (Zielfernrohr) ausgelöst.
     */
    void onRightPress();
}
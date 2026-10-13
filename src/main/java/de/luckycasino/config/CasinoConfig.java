package de.luckycasino.config;

import de.luckycasino.core.LuckMode;

/**
 * Einstellungen der Mod. Werden zusammen mit dem Spielstand in
 * {@code config/luckycasino.json} gespeichert (Abschnitt "settings").
 */
public class CasinoConfig {
    /** Glücksfaktor-Modus (Schwierigkeitsgrad). */
    public LuckMode luckMode = LuckMode.HOUSE;

    /** Chip-Anzeige im Spiel einblenden. */
    public boolean hudEnabled = true;
    /** HUD-Position als Anteil der freien Bildschirmbreite/-höhe (0 = links/oben, 1 = rechts/unten). */
    public float hudX = 0.0f;
    public float hudY = 0.0f;
    /** Ausgerüsteten Titel im HUD anzeigen. */
    public boolean hudShowTitle = true;

    public boolean soundsEnabled = true;
    /** Lautstärke der Casino-Sounds (0–1). */
    public float soundVolume = 0.8f;

    /** Animationen doppelt so schnell abspielen. */
    public boolean fastAnimations = false;

    /** Pausen-Erinnerung nach so vielen Minuten Casino-Spielzeit (0 = aus). */
    public int reminderMinutes = 60;

    public void sanitize() {
        if (luckMode == null) {
            luckMode = LuckMode.HOUSE;
        }
        hudX = clamp01(hudX);
        hudY = clamp01(hudY);
        soundVolume = clamp01(soundVolume);
        reminderMinutes = Math.max(0, Math.min(reminderMinutes, 600));
    }

    private static float clamp01(float v) {
        if (Float.isNaN(v)) {
            return 0;
        }
        return Math.max(0f, Math.min(1f, v));
    }
}

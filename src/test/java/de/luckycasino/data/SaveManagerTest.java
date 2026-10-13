package de.luckycasino.data;

import de.luckycasino.core.LuckMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveManagerTest {

    @TempDir
    Path dir;

    @Test
    void missingFileCreatesFreshSave() {
        SaveManager m = new SaveManager(dir.resolve("luckycasino.json"));
        SaveManager.LoadResult r = m.load();
        assertEquals(SaveManager.LoadStatus.CREATED, r.status());
        assertEquals(1_000, r.data().profile.balance);
        assertTrue(Files.exists(dir.resolve("luckycasino.json")));
    }

    @Test
    void roundTrip() {
        SaveManager m = new SaveManager(dir.resolve("luckycasino.json"));
        SaveData d = m.load().data();
        d.profile.balance = 4_321;
        d.settings.luckMode = LuckMode.HARDCORE;
        d.settings.hudX = 0.5f;
        assertTrue(m.save(d));
        SaveManager.LoadResult r = new SaveManager(dir.resolve("luckycasino.json")).load();
        assertEquals(SaveManager.LoadStatus.LOADED, r.status());
        assertEquals(4_321, r.data().profile.balance);
        assertEquals(LuckMode.HARDCORE, r.data().settings.luckMode);
        assertEquals(0.5f, r.data().settings.hudX);
    }

    @Test
    void corruptFileIsBackedUpAndRestoredFromBak() throws IOException {
        Path file = dir.resolve("luckycasino.json");
        SaveManager m = new SaveManager(file);
        SaveData d = m.load().data();
        d.profile.balance = 777;
        m.save(d);          // erzeugt .bak der vorherigen Version
        d.profile.balance = 888;
        m.save(d);          // .bak = 777, Haupt = 888
        Files.writeString(file, "{ \"profile\": { \"balance\": ", StandardCharsets.UTF_8); // abgeschnitten

        SaveManager.LoadResult r = new SaveManager(file).load();
        assertEquals(SaveManager.LoadStatus.RESTORED_FROM_BACKUP, r.status());
        assertEquals(777, r.data().profile.balance, "Backup enthält die Version vor dem letzten Speichern");
        assertNotNull(r.corruptCopy());
        assertTrue(Files.exists(r.corruptCopy()));
    }

    @Test
    void garbageWithoutBackupStartsFresh() throws IOException {
        Path file = dir.resolve("luckycasino.json");
        Files.writeString(file, "das ist kein JSON \u0000\u0001", StandardCharsets.UTF_8);
        SaveManager.LoadResult r = new SaveManager(file).load();
        assertEquals(SaveManager.LoadStatus.RESET_AFTER_CORRUPTION, r.status());
        assertEquals(1_000, r.data().profile.balance);
        try (Stream<Path> files = Files.list(dir)) {
            assertTrue(files.anyMatch(p -> p.getFileName().toString().startsWith("luckycasino.json.corrupt-")));
        }
        // Danach ist die Hauptdatei wieder gültig
        assertEquals(SaveManager.LoadStatus.LOADED, new SaveManager(file).load().status());
    }

    @Test
    void wrongTypesAndNullsAreRepaired() throws IOException {
        Path file = dir.resolve("luckycasino.json");
        Files.writeString(file, """
                { "settings": { "luckMode": "GIBTS_NICHT", "hudX": 7.5 },
                  "profile": { "balance": -50, "bet": 0, "stats": null, "achievements": { "unbekannt": 1 },
                               "cosmetics": ["frame.gold", "quatsch"], "equippedFrame": "frame.gold",
                               "balanceHistory": null } }
                """, StandardCharsets.UTF_8);
        SaveManager.LoadResult r = new SaveManager(file).load();
        assertEquals(SaveManager.LoadStatus.LOADED, r.status());
        SaveData d = r.data();
        assertEquals(LuckMode.HOUSE, d.settings.luckMode);
        assertEquals(1f, d.settings.hudX);
        assertEquals(0, d.profile.balance);
        assertEquals(1, d.profile.bet);
        assertNotNull(d.profile.stats);
        assertTrue(d.profile.achievements.isEmpty());
        assertTrue(d.profile.cosmetics.contains("frame.gold"));
        assertTrue(d.profile.cosmetics.contains("frame.classic"));
        assertEquals(2, d.profile.cosmetics.size());
        assertEquals("frame.gold", d.profile.equippedFrame);
        assertEquals(1, d.profile.balanceHistory.size());
    }

    @Test
    void typeMismatchCountsAsCorrupt() throws IOException {
        Path file = dir.resolve("luckycasino.json");
        Files.writeString(file, "{ \"profile\": { \"balance\": \"viel\" } }", StandardCharsets.UTF_8);
        SaveManager.LoadResult r = new SaveManager(file).load();
        assertEquals(SaveManager.LoadStatus.RESET_AFTER_CORRUPTION, r.status());
    }
}

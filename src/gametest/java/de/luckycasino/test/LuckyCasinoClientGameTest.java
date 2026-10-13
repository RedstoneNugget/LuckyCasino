package de.luckycasino.test;

import de.luckycasino.client.ClientSession;
import de.luckycasino.client.LuckyCasinoClient;
import de.luckycasino.client.gui.AchievementsScreen;
import de.luckycasino.client.gui.BlackjackScreen;
import de.luckycasino.client.gui.CasinoMenuScreen;
import de.luckycasino.client.gui.CoinFlipScreen;
import de.luckycasino.client.gui.CollectionScreen;
import de.luckycasino.client.gui.CrashScreen;
import de.luckycasino.client.gui.HudPositionScreen;
import de.luckycasino.client.gui.LootBoxScreen;
import de.luckycasino.client.gui.RecordsScreen;
import de.luckycasino.client.gui.RouletteScreen;
import de.luckycasino.client.gui.ScratchScreen;
import de.luckycasino.client.gui.SettingsScreen;
import de.luckycasino.client.gui.SlotsScreen;
import de.luckycasino.client.gui.StatsScreen;
import de.luckycasino.client.gui.WheelScreen;
import de.luckycasino.core.Casino;
import de.luckycasino.core.GameType;
import de.luckycasino.data.SaveManager;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.MouseInput;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * Automatischer In-Game-Test: startet den echten Client, spielt jedes Spiel einmal
 * durch, prüft HUD, Befehle, Speichern und die Verbindung zu einem Server ohne Mod.
 * Aufruf: gradlew runClientGameTest – Screenshots unter build/run/clientGameTest/screenshots.
 */
public class LuckyCasinoClientGameTest implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        context.getInput().resizeWindow(1920, 1080);
        context.waitTicks(5);

        // Titelbildschirm: Casino ohne Welt öffnen (z. B. über Mod Menu)
        context.setScreen(() -> new CasinoMenuScreen(null));
        context.waitTicks(5);
        context.takeScreenshot("00_menu_titlescreen");
        context.setScreen(() -> null);

        try (TestSingleplayerContext sp = context.worldBuilder().setUseConsistentSettings(true).create()) {
            sp.getClientWorld().waitForChunksRender();
            sp.getServer().runCommand("time set noon");
            context.waitTicks(10);
            context.takeScreenshot("01_hud_ingame");

            // Taste K öffnet das Menü
            context.getInput().pressKey(LuckyCasinoClient.openKey);
            context.waitForScreen(CasinoMenuScreen.class);
            context.waitTicks(3);
            context.takeScreenshot("02_menu");

            slots(context);
            roulette(context);
            coinFlip(context);
            blackjack(context);
            crash(context);
            scratch(context);
            wheel(context);
            plinko(context);
            mines(context);
            missions(context);
            lootBoxes(context);
            infoScreens(context);
            commands(context, sp);
            smallWindow(context);
        }

        dedicatedServer(context);
        saveFile(context);
        log("ALLE TESTS BESTANDEN");
    }

    // ------------------------------------------------------------------

    private void slots(ClientGameTestContext context) {
        open(context, () -> new SlotsScreen(null), SlotsScreen.class);
        long before = casino(context).balance();
        context.clickScreenButton("luckycasino.slots.spin");
        context.waitTicks(12);
        context.takeScreenshot("10_slots_spinning");
        context.waitTicks(50);
        context.takeScreenshot("11_slots_result");
        check(stats(context, GameType.SLOTS) == 1, "Slots: eine Runde gezählt");
        check(casino(context).balance() != before || casino(context).lastRound().payout() == casino(context).lastRound().stake(),
                "Slots: Guthaben verändert");
        // Freispiel aus dem Glücksrad simulieren
        context.runOnClient(c -> casino(c).profile().freeSpins = 2);
        context.waitTicks(2);
        pressButtonStartingWith(context, "Free spin");
        context.waitTicks(60);
        check(casino(context).profile().freeSpins == 1, "Slots: Freispiel verbraucht");
    }

    private void roulette(ClientGameTestContext context) {
        open(context, () -> new RouletteScreen(null), RouletteScreen.class);
        int[] p = panel(context);
        int x0 = p[0] + 168;
        int y0 = p[1] + 30;
        click(context, x0 + 108, y0 + 66 + 7, 0);             // Rot
        click(context, x0 + 18 + 5 * 18 + 9, y0 + 16 + 8, 0); // Einzelzahl 17
        click(context, x0 + 18 + 5 * 18 + 9, y0 + 16 + 8, 0); // nochmal 17
        click(context, x0 + 18 + 4 * 36 + 18, y0 + 66 + 7, 0); // Ungerade
        click(context, x0 + 18 + 4 * 36 + 18, y0 + 66 + 7, 1); // wieder entfernen
        context.getInput().setCursorPos((x0 + 117) * scale(context), (y0 + 24) * scale(context));
        context.waitTicks(3);
        context.takeScreenshot("20_roulette_bets");
        context.clickScreenButton("luckycasino.roulette.spin");
        context.waitTicks(30);
        context.takeScreenshot("21_roulette_spinning");
        context.waitTicks(100);
        context.takeScreenshot("22_roulette_result");
        check(stats(context, GameType.ROULETTE) == 1, "Roulette: eine Runde gezählt");
        check(casino(context).lastRound().stake() == 3 * casino(context).bet(), "Roulette: drei Chips gesetzt");
    }

    private void coinFlip(ClientGameTestContext context) {
        open(context, () -> new CoinFlipScreen(null), CoinFlipScreen.class);
        context.clickScreenButton("luckycasino.coin.tails");
        context.clickScreenButton("luckycasino.coin.flip");
        context.waitTicks(10);
        context.takeScreenshot("30_coin_flying");
        context.waitTicks(30);
        context.takeScreenshot("31_coin_result");
        // Bei Treffer: Doppelt oder nichts, danach auszahlen
        if (ClientSession.coinSeries != null && ClientSession.coinSeries.canCashOut()) {
            context.clickScreenButton("luckycasino.coin.double");
            context.waitTicks(40);
            if (ClientSession.coinSeries.canCashOut()) {
                pressButtonStartingWith(context, "Cash out");
                context.waitTicks(5);
            }
            context.takeScreenshot("32_coin_series");
        }
        check(stats(context, GameType.COINFLIP) == 1, "Münzwurf: eine Serie abgerechnet");
    }

    private void blackjack(ClientGameTestContext context) {
        open(context, () -> new BlackjackScreen(null), BlackjackScreen.class);
        context.clickScreenButton("luckycasino.blackjack.deal");
        context.waitTicks(40);
        context.takeScreenshot("40_blackjack_dealt");
        if (ClientSession.blackjack != null && !ClientSession.blackjack.isFinished()) {
            context.clickScreenButton("luckycasino.blackjack.stand");
        }
        context.waitTicks(80);
        context.takeScreenshot("41_blackjack_result");
        check(ClientSession.blackjack.isFinished(), "Blackjack: Hand beendet");
        check(stats(context, GameType.BLACKJACK) == 1, "Blackjack: eine Runde gezählt");
    }

    private void crash(ClientGameTestContext context) {
        open(context, () -> new CrashScreen(null), CrashScreen.class);
        int tries = 0;
        boolean cashed = false;
        while (!cashed && tries++ < 6) {
            context.clickScreenButton("luckycasino.crash.start");
            context.waitTicks(30);
            if (ClientSession.crash.isRunning()) {
                context.takeScreenshot("50_crash_running");
                pressButtonStartingWith(context, "Cash out");
                cashed = true;
            } else {
                context.waitTicks(10);
            }
        }
        context.waitTicks(40);
        context.takeScreenshot("51_crash_result");
        check(stats(context, GameType.CRASH) >= 1, "Crash: Runde abgerechnet");
        check(!ClientSession.crash.isRunning(), "Crash: Runde beendet");
    }

    private void scratch(ClientGameTestContext context) {
        open(context, () -> new ScratchScreen(null), ScratchScreen.class);
        pressButtonStartingWith(context, "Silver");
        context.waitTicks(3);
        int[] p = panel(context);
        double s = scale(context);
        // Erste Reihe freirubbeln
        int y = p[1] + 64 + 20;
        context.getInput().setCursorPos((p[0] + 138) * s, y * s);
        context.getInput().holdMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        for (int pass = 0; pass < 3; pass++) {
            for (int i = 0; i < 30; i++) {
                context.getInput().moveCursor((pass % 2 == 0 ? 4 : -4) * s, 0);
            }
            context.getInput().moveCursor(0, 6 * s);
            context.waitTick();
        }
        context.getInput().releaseMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTicks(3);
        context.takeScreenshot("60_scratch_partial");
        context.clickScreenButton("luckycasino.scratch.reveal_all");
        context.waitTicks(10);
        context.takeScreenshot("61_scratch_revealed");
        check(stats(context, GameType.SCRATCH) == 1, "Rubbellos: Runde abgerechnet");
        check(ClientSession.scratchRevealed[0], "Rubbellos: Felder aufgedeckt");
    }

    private void wheel(ClientGameTestContext context) {
        context.runOnClient(c -> casino(c).profile().lastWheelDay = "");
        open(context, () -> new WheelScreen(null), WheelScreen.class);
        context.takeScreenshot("70_wheel_ready");
        context.clickScreenButton("luckycasino.wheel.spin");
        context.waitTicks(25);
        context.takeScreenshot("71_wheel_spinning");
        context.waitTicks(100);
        context.takeScreenshot("72_wheel_result");
        check(LocalDate.now().toString().equals(casino(context).profile().lastWheelDay), "Glücksrad: heute gedreht");
        check(!casino(context).canSpinWheel(LocalDate.now()), "Glücksrad: nur einmal pro Tag");
    }

    private void plinko(ClientGameTestContext context) {
        open(context, () -> new de.luckycasino.client.gui.PlinkoScreen(null), de.luckycasino.client.gui.PlinkoScreen.class);
        for (int i = 0; i < 4; i++) {
            context.getInput().pressKey(GLFW.GLFW_KEY_SPACE);
            context.waitTicks(3);
        }
        context.waitTicks(10);
        context.takeScreenshot("73_plinko_falling");
        context.waitTicks(50);
        context.takeScreenshot("74_plinko_result");
        check(stats(context, GameType.PLINKO) == 4, "Plinko: vier Kugeln per Leertaste");
    }

    private void mines(ClientGameTestContext context) {
        open(context, () -> new de.luckycasino.client.gui.MinesScreen(null), de.luckycasino.client.gui.MinesScreen.class);
        context.clickScreenButton("luckycasino.mines.start");
        context.waitTicks(2);
        int[] p = panel(context);
        int clicked = 0;
        for (int i = 0; i < 25 && clicked < 3; i++) {
            int tile = i;
            boolean mine = context.computeOnClient(c -> ClientSession.mines.isMine(tile));
            if (!mine) {
                click(context, p[0] + 22 + (tile % 5) * 34 + 15, p[1] + 32 + (tile / 5) * 34 + 15, 0);
                clicked++;
            }
        }
        context.waitTicks(5);
        context.takeScreenshot("75_mines_running");
        check(context.computeOnClient(c -> ClientSession.mines.safeRevealed()) == 3, "Minenfeld: drei sichere Felder per Klick");
        pressButtonStartingWith(context, "Cash out");
        context.waitTicks(5);
        context.takeScreenshot("76_mines_cashed");
        check(stats(context, GameType.MINES) == 1, "Minenfeld: Runde ausgezahlt");
        // Zweite Runde: absichtlich auf eine Mine
        context.clickScreenButton("luckycasino.mines.start");
        context.waitTicks(2);
        for (int i = 0; i < 25; i++) {
            int tile = i;
            if (context.computeOnClient(c -> ClientSession.mines.isMine(tile))) {
                click(context, p[0] + 22 + (tile % 5) * 34 + 15, p[1] + 32 + (tile / 5) * 34 + 15, 0);
                break;
            }
        }
        context.waitTicks(5);
        context.takeScreenshot("77_mines_boom");
        check(stats(context, GameType.MINES) == 2, "Minenfeld: Mine beendet die Runde");
    }

    private void missions(ClientGameTestContext context) {
        // Slots-Auto-Spin
        open(context, () -> new SlotsScreen(null), SlotsScreen.class);
        long before = stats(context, GameType.SLOTS);
        pressButtonStartingWith(context, "Auto");
        context.waitTicks(160);
        check(stats(context, GameType.SLOTS) >= before + 3, "Slots: Auto-Spin dreht mehrfach");
        pressButtonStartingWith(context, "Stop");
        context.waitTicks(60);
        open(context, () -> new de.luckycasino.client.gui.MissionsScreen(null), de.luckycasino.client.gui.MissionsScreen.class);
        context.takeScreenshot("78_missions");
        check(context.computeOnClient(c -> casino(c).missions().size()) == 3, "Missionen: drei Tagesmissionen");
        check(context.computeOnClient(c -> casino(c).profile().level) >= 1, "Level vorhanden");
    }

    private void lootBoxes(ClientGameTestContext context) {
        context.runOnClient(c -> casino(c).profile().balance += 20_000);
        open(context, () -> new LootBoxScreen(null), LootBoxScreen.class);
        pressButtonStartingWith(context, "Open (1,000)");
        context.waitTicks(30);
        context.takeScreenshot("80_loot_rolling");
        context.waitTicks(90);
        context.takeScreenshot("81_loot_reveal");
        check(casino(context).profile().stats.lootBoxesOpened == 1, "Lootbox: eine Kiste geöffnet");
        // Weitere Kisten über die Logik, damit die Sammlung etwas zeigt
        context.runOnClient(c -> {
            for (int i = 0; i < 12; i++) {
                casino(c).openLootBox(de.luckycasino.core.loot.LootBoxType.DIAMOND);
            }
        });
        open(context, () -> new CollectionScreen(null), CollectionScreen.class);
        context.takeScreenshot("82_collection");
    }

    private void infoScreens(ClientGameTestContext context) {
        // Ein paar Runden mehr für Statistik und Diagramm
        context.runOnClient(c -> {
            for (int i = 0; i < 60; i++) {
                casino(c).playSlots(10);
            }
        });
        open(context, () -> new AchievementsScreen(null), AchievementsScreen.class);
        context.takeScreenshot("90_achievements");
        int[] ap = panel(context);
        context.getInput().setCursorPos((ap[0] + 210) * scale(context), (ap[1] + 120) * scale(context));
        context.getInput().scroll(-10);
        context.waitTicks(2);
        context.takeScreenshot("90b_achievements_scrolled");
        open(context, () -> new StatsScreen(null), StatsScreen.class);
        context.clickScreenButton("luckycasino.stats.tab.overview");
        context.waitTicks(2);
        context.takeScreenshot("91_stats_overview");
        context.clickScreenButton("luckycasino.stats.tab.games");
        context.waitTicks(2);
        context.takeScreenshot("92_stats_games");
        context.clickScreenButton("luckycasino.stats.tab.chart");
        context.waitTicks(2);
        context.takeScreenshot("93_stats_chart");
        open(context, () -> new RecordsScreen(null), RecordsScreen.class);
        context.takeScreenshot("94_records");
        open(context, () -> new SettingsScreen(null), SettingsScreen.class);
        context.takeScreenshot("95_settings");
        pressButtonStartingWith(context, "Luck mode");
        context.waitTicks(2);
        context.takeScreenshot("96_settings_mode");
        check(casino(context).mode() == de.luckycasino.core.LuckMode.HARDCORE, "Einstellungen: Modus gewechselt");
        pressButtonStartingWith(context, "Luck mode");
        pressButtonStartingWith(context, "Luck mode");
        context.setScreen(() -> new HudPositionScreen(null));
        context.waitTicks(3);
        context.takeScreenshot("97_hud_position");
        context.setScreen(() -> new CasinoMenuScreen(null));
        context.waitTicks(3);
        context.takeScreenshot("98_menu_after_play");
        context.setScreen(() -> null);
        context.waitTicks(20);
        context.takeScreenshot("99_ingame_hud_toasts");
    }

    private void commands(ClientGameTestContext context, TestSingleplayerContext sp) {
        context.runOnClient(c -> c.player.networkHandler.sendChatCommand("casino balance"));
        context.runOnClient(c -> c.player.networkHandler.sendChatCommand("casino reset"));
        context.waitTicks(5);
        context.takeScreenshot("A0_commands_chat");
        context.runOnClient(c -> c.player.networkHandler.sendChatCommand("casino open"));
        context.waitForScreen(CasinoMenuScreen.class);
        log("ok: /casino open öffnet das Menü");
        context.setScreen(() -> null);
        long before = context.computeOnClient(c -> casino(c).balance());
        check(before != 1_000 || casino(context).profile().stats.totalRounds() > 0, "Befehle: Spielstand vor Reset vorhanden");
        context.runOnClient(c -> c.player.networkHandler.sendChatCommand("casino reset confirm"));
        context.waitTicks(2);
        check(casino(context).balance() == 1_000, "Befehle: /casino reset confirm setzt auf 1000 zurück");
        check(casino(context).profile().stats.totalRounds() == 0, "Befehle: Statistik zurückgesetzt");
    }

    private void smallWindow(ClientGameTestContext context) {
        // Kleinstes übliches Fenster: 854x480 → GUI 427x240
        context.getInput().resizeWindow(854, 480);
        context.waitTicks(5);
        open(context, () -> new CasinoMenuScreen(null), CasinoMenuScreen.class);
        context.takeScreenshot("B0_small_menu");
        open(context, () -> new RouletteScreen(null), RouletteScreen.class);
        context.takeScreenshot("B1_small_roulette");
        context.setScreen(() -> null);
        context.getInput().resizeWindow(1920, 1080);
        context.waitTicks(5);
    }

    private void dedicatedServer(ClientGameTestContext context) {
        try (TestDedicatedServerContext server = context.worldBuilder().setUseConsistentSettings(true).createServer()) {
            try (TestServerConnection connection = server.connect()) {
                connection.getClientWorld().waitForChunksRender();
                context.waitTicks(10);
                context.getInput().pressKey(LuckyCasinoClient.openKey);
                context.waitForScreen(CasinoMenuScreen.class);
                context.waitTicks(3);
                context.takeScreenshot("C0_server_menu");
                open(context, () -> new SlotsScreen(null), SlotsScreen.class);
                context.clickScreenButton("luckycasino.slots.spin");
                context.waitTicks(60);
                check(stats(context, GameType.SLOTS) == 1, "Server: Slots funktionieren ohne Server-Mod");
                context.runOnClient(c -> c.player.networkHandler.sendChatCommand("casino balance"));
                context.setScreen(() -> null);
                context.waitTicks(5);
                context.takeScreenshot("C1_server_hud_chat");
            }
        }
    }

    private void saveFile(ClientGameTestContext context) {
        context.runOnClient(c -> LuckyCasinoClient.saveNow());
        Path file = FabricLoader.getInstance().getConfigDir().resolve("luckycasino.json");
        check(Files.exists(file), "Speichern: config/luckycasino.json existiert");
        SaveManager.LoadResult r = new SaveManager(file).load();
        check(r.status() == SaveManager.LoadStatus.LOADED, "Speichern: Datei ist gültiges JSON");
        long balance = casino(context).balance();
        check(r.data().profile.balance == balance, "Speichern: Guthaben stimmt (" + balance + ")");
    }

    // ------------------------------------------------------------------
    // Hilfen
    // ------------------------------------------------------------------

    private static Casino casino(MinecraftClient c) {
        return LuckyCasinoClient.casino();
    }

    private static Casino casino(ClientGameTestContext context) {
        return context.computeOnClient(LuckyCasinoClientGameTest::casino);
    }

    private static long stats(ClientGameTestContext context, GameType type) {
        return context.computeOnClient(c -> casino(c).profile().stats.get(type).rounds);
    }

    private static <T extends Screen> void open(ClientGameTestContext context, Supplier<Screen> factory, Class<T> type) {
        context.setScreen(factory);
        context.waitForScreen(type);
        context.waitTicks(3);
    }

    /** Linke obere Ecke des Casino-Panels (wie in CasinoScreen berechnet). */
    private static int[] panel(ClientGameTestContext context) {
        return context.computeOnClient(c -> {
            int w = c.getWindow().getScaledWidth();
            int h = c.getWindow().getScaledHeight();
            int pw = Math.min(420, w - 4);
            int ph = Math.min(240, h - 4);
            return new int[]{(w - pw) / 2, (h - ph) / 2};
        });
    }

    private static double scale(ClientGameTestContext context) {
        return context.computeOnClient(c -> c.getWindow().getScaleFactor());
    }

    private static void click(ClientGameTestContext context, int x, int y, int button) {
        double s = scale(context);
        context.getInput().setCursorPos(x * s + s / 2, y * s + s / 2);
        context.getInput().pressMouse(button);
        context.waitTick();
    }

    /** Drückt einen Button, dessen Text mit {@code prefix} beginnt (für dynamische Beschriftungen). */
    private static void pressButtonStartingWith(ClientGameTestContext context, String prefix) {
        boolean found = context.computeOnClient(c -> {
            if (c.currentScreen == null) {
                return false;
            }
            for (Element e : c.currentScreen.children()) {
                if (e instanceof ButtonWidget b && b.visible && b.getMessage().getString().startsWith(prefix)) {
                    b.onPress(new MouseInput(GLFW.GLFW_MOUSE_BUTTON_LEFT, 0));
                    return true;
                }
            }
            return false;
        });
        check(found, "Button '" + prefix + "…' gefunden");
        context.waitTick();
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            log("FEHLGESCHLAGEN: " + message);
            throw new AssertionError(message);
        }
        log("ok: " + message);
    }

    private static void log(String message) {
        System.out.println("[LuckyCasinoTest] " + message);
    }
}

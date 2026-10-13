package de.luckycasino.core;

import de.luckycasino.core.mines.MinesGame;
import de.luckycasino.core.plinko.Plinko;
import de.luckycasino.core.progress.Achievement;
import de.luckycasino.core.progress.Missions;
import de.luckycasino.core.progress.PlayerProfile;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests für Plinko, Minenfeld, Level und Tagesmissionen (Version 1.1). */
class NewFeaturesTest {

    @Test
    void plinkoTablesAreFairInFairMode() {
        assertEquals(1.0003, Plinko.returnToPlayer(Plinko.Risk.LOW, 0), 0.0002);
        assertEquals(0.9994, Plinko.returnToPlayer(Plinko.Risk.MEDIUM, 0), 0.0002);
        assertEquals(1.0002, Plinko.returnToPlayer(Plinko.Risk.HIGH, 0), 0.0002);
        double sum = 0;
        for (int k = 0; k <= Plinko.ROWS; k++) {
            sum += Plinko.bucketChance(k);
        }
        assertEquals(1.0, sum, 1e-12);
        for (Plinko.Risk r : Plinko.Risk.values()) {
            assertEquals(0.97, Plinko.returnToPlayer(r, 0.03), 0.005, r.name());
        }
    }

    @Test
    void plinkoSimulationMatches() {
        CasinoRng rng = new CasinoRng(11, LuckMode.FAIR);
        long paid = 0;
        int n = 1_000_000;
        int[] buckets = new int[Plinko.ROWS + 1];
        for (int i = 0; i < n; i++) {
            Plinko.Drop d = Plinko.drop(rng, Plinko.Risk.LOW, 100);
            paid += d.payout();
            buckets[d.bucket()]++;
            int rights = 0;
            for (boolean b : d.path()) {
                rights += b ? 1 : 0;
            }
            assertEquals(rights, d.bucket());
        }
        assertEquals(1.0, paid / (100.0 * n), 0.01);
        assertEquals(Plinko.bucketChance(6), buckets[6] / (double) n, 0.003);
    }

    @Test
    void minesMultipliersAreFair() {
        // 1 Mine, 1 Feld: 25/24
        assertEquals(1.04, MinesGame.multiplierFor(1, 1, 0), 1e-9);
        // 3 Minen, 5 Felder: Π (25-i)/(22-i)
        double exact = 25.0 / 22 * 24 / 21 * 23 / 20 * 22 / 19 * 21 / 18;
        assertEquals(Math.floor(exact * 100) / 100, MinesGame.multiplierFor(3, 5, 0), 1e-9);
        // Simulation: Strategie "3 Felder aufdecken, dann auszahlen" → RTP ≈ 1 − Hausvorteil
        for (LuckMode mode : LuckMode.values()) {
            CasinoRng rng = new CasinoRng(5 + mode.ordinal(), mode);
            long paid = 0;
            int n = 300_000;
            for (int i = 0; i < n; i++) {
                MinesGame g = new MinesGame(rng, 100, 5);
                boolean alive = true;
                for (int t = 0; t < 3 && alive; t++) {
                    alive = g.reveal(t);
                }
                if (alive) {
                    paid += g.cashOut();
                }
            }
            double edge = Odds.of(mode).minesHouseEdge();
            assertEquals(1 - edge, paid / (100.0 * n), 0.015, mode.name());
        }
    }

    @Test
    void minesGameFlow() {
        Casino casino = new Casino(new PlayerProfile(), new CasinoRng(3, LuckMode.HOUSE), () -> 1_700_000_000_000L);
        MinesGame g = casino.startMines(100, 3);
        assertNotNull(g);
        assertEquals(900, casino.balance());
        int tile = 0;
        while (tile < MinesGame.TILES && g.isMine(tile)) {
            tile++;
        }
        assertNull(casino.revealMine(g, tile), "sicheres Feld beendet die Runde nicht");
        assertTrue(g.canCashOut());
        RoundResult r = casino.cashOutMines(g);
        assertEquals((long) Math.floor(100 * g.currentMultiplier()), r.payout());
        assertNull(casino.startMines(5_000, 3));
    }

    @Test
    void missionsAreDeterministicPerDay() {
        LocalDate day = LocalDate.of(2026, 10, 11);
        List<Missions.Def> a = Missions.forDay(day);
        assertEquals(a, Missions.forDay(day));
        assertEquals(Missions.PER_DAY, new HashSet<>(a).size());
        // über viele Tage verschiedene Kombinationen
        HashSet<List<String>> combos = new HashSet<>();
        for (int i = 0; i < 30; i++) {
            List<String> ids = new ArrayList<>();
            Missions.forDay(day.plusDays(i)).forEach(d -> ids.add(d.id()));
            combos.add(ids);
        }
        assertTrue(combos.size() > 20);
    }

    @Test
    void missionProgressAndClaim() {
        AtomicLong clock = new AtomicLong(1_760_000_000_000L);
        List<Missions.Def> completed = new ArrayList<>();
        Casino casino = new Casino(new PlayerProfile(), new CasinoRng(8, LuckMode.FAIR), clock::get);
        casino.addListener(new Casino.Listener() {
            @Override
            public void onMissionComplete(Missions.Def mission) {
                completed.add(mission);
            }
        });
        casino.profile().balance = 1_000_000;
        // Viele Runden in allen Spielen erfüllen jede Mission irgendwann
        for (int i = 0; i < 400; i++) {
            casino.playSlots(50);
            casino.playPlinko(50, Plinko.Risk.HIGH);
            casino.buyScratchTicket(de.luckycasino.core.scratch.ScratchTier.BRONZE);
            var bj = casino.startBlackjack(50);
            if (!bj.isFinished()) {
                casino.blackjackStand(bj);
            }
            var coin = casino.startCoinFlip(50);
            if (casino.flipCoin(coin, de.luckycasino.core.coinflip.CoinSide.HEADS) == null) {
                casino.cashOutCoin(coin);
            }
            casino.playRoulette(java.util.Map.of(de.luckycasino.core.roulette.RouletteBet.of(
                    de.luckycasino.core.roulette.RouletteBet.Kind.RED), 50L));
            var crash = casino.startCrash(50, 1.5, 0);
            casino.updateCrash(crash, 100_000);
            var mines = casino.startMines(50, 1);
            int t = 0;
            while (mines.isMine(t)) {
                t++;
            }
            if (casino.revealMine(mines, t) == null) {
                casino.cashOutMines(mines);
            }
        }
        assertEquals(Missions.PER_DAY, completed.size(), "alle drei Missionen erfüllt");
        long before = casino.balance();
        String id = casino.missions().getFirst().id;
        assertTrue(casino.claimMission(id));
        assertFalse(casino.claimMission(id), "nur einmal abholbar");
        assertEquals(before + Missions.byId(id).reward(), casino.balance());
        assertEquals(1, casino.profile().missionsCompleted);

        // Neuer Tag → neue Missionen ohne Fortschritt
        clock.addAndGet(24L * 3600 * 1000);
        assertTrue(casino.missions().stream().allMatch(m -> m.progress == 0 && !m.claimed));
    }

    @Test
    void levelUpGivesReward() {
        List<Integer> levels = new ArrayList<>();
        Casino casino = new Casino(new PlayerProfile(), new CasinoRng(1, LuckMode.FAIR), () -> 1_700_000_000_000L);
        casino.addListener(new Casino.Listener() {
            @Override
            public void onLevelUp(int level, long reward) {
                levels.add(level);
                assertEquals(Casino.levelReward(level), reward);
            }
        });
        casino.profile().balance = 10_000_000;
        for (int i = 0; i < 2_000; i++) {
            casino.playSlots(100);
        }
        assertTrue(casino.profile().level >= 10, "Level " + casino.profile().level);
        assertEquals(casino.profile().level - 1, levels.size());
        assertTrue(casino.isUnlocked(Achievement.LEVEL_10));
        assertTrue(casino.profile().xp < Casino.xpForNextLevel(casino.profile().level));
    }
}

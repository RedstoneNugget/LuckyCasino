package de.luckycasino.core;

import de.luckycasino.core.blackjack.BlackjackGame;
import de.luckycasino.core.coinflip.CoinFlipSeries;
import de.luckycasino.core.coinflip.CoinSide;
import de.luckycasino.core.crash.CrashRound;
import de.luckycasino.core.loot.Cosmetic;
import de.luckycasino.core.loot.LootBoxType;
import de.luckycasino.core.loot.LootReward;
import de.luckycasino.core.progress.Achievement;
import de.luckycasino.core.progress.PlayerProfile;
import de.luckycasino.core.roulette.Roulette;
import de.luckycasino.core.roulette.RouletteBet;
import de.luckycasino.core.scratch.ScratchTicket;
import de.luckycasino.core.scratch.ScratchTier;
import de.luckycasino.core.wheel.LuckyWheel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CasinoTest {

    private final AtomicLong clock = new AtomicLong(1_700_000_000_000L);
    private final List<Achievement> unlocked = new ArrayList<>();
    private Casino casino;

    @BeforeEach
    void setUp() {
        casino = new Casino(new PlayerProfile(), new CasinoRng(99, LuckMode.HOUSE), clock::get);
        casino.addListener(new Casino.Listener() {
            @Override
            public void onAchievement(Achievement achievement) {
                unlocked.add(achievement);
            }
        });
    }

    @Test
    void startsWithThousandChips() {
        assertEquals(1_000, casino.balance());
    }

    @Test
    void slotsDeductAndPayConsistently() {
        long before = casino.balance();
        Casino.SlotSpin spin = casino.playSlots(10);
        assertNotNull(spin);
        assertEquals(before - 10 + spin.round().payout(), casino.balance());
        assertEquals(1, casino.profile().stats.get(GameType.SLOTS).rounds);
        assertNull(casino.playSlots(5_000), "zu hoher Einsatz darf nicht gehen");
        assertNull(casino.playSlots(0));
    }

    @Test
    void rouletteSettlesAllBets() {
        Map<RouletteBet, Long> bets = new LinkedHashMap<>();
        bets.put(RouletteBet.of(RouletteBet.Kind.RED), 50L);
        bets.put(RouletteBet.straight(17), 10L);
        long before = casino.balance();
        Casino.RouletteSpin spin = casino.playRoulette(bets);
        assertNotNull(spin);
        long expected = Roulette.payout(bets, spin.pocket());
        assertEquals(before - 60 + expected, casino.balance());
        assertNull(casino.playRoulette(Map.of()), "leerer Tisch");
    }

    @Test
    void coinFlipSeriesCashOut() {
        CoinFlipSeries s = null;
        // Bis zu einem Treffer weiterprobieren (deterministischer Seed)
        for (int i = 0; i < 50; i++) {
            s = casino.startCoinFlip(10);
            RoundResult r = casino.flipCoin(s, CoinSide.HEADS);
            if (r == null) {
                break;
            }
            assertTrue(r.isLoss());
        }
        assertNotNull(s);
        assertTrue(s.canCashOut());
        long before = casino.balance();
        RoundResult r = casino.cashOutCoin(s);
        assertEquals(20, r.payout());
        assertEquals(before + 20, casino.balance());
    }

    @Test
    void blackjackRoundIsSettledExactlyOnce() {
        long before = casino.balance();
        BlackjackGame game = casino.startBlackjack(100);
        assertNotNull(game);
        while (!game.isFinished()) {
            casino.blackjackStand(game);
        }
        assertEquals(before - game.stake() + game.payout(), casino.balance());
        assertEquals(1, casino.profile().stats.get(GameType.BLACKJACK).rounds);
    }

    @Test
    void crashCashOutBeforeCrash() {
        long t0 = 10_000;
        CrashRound round = null;
        for (int i = 0; i < 100; i++) {
            round = casino.startCrash(10, 0, t0);
            if (round.isRunning() && round.crashPoint() > 1.5) {
                break;
            }
            while (round.isRunning()) {
                casino.updateCrash(round, t0 += 1_000);
            }
        }
        assertTrue(round.isRunning());
        long when = t0 + CrashRound.timeToReach(1.4);
        RoundResult r = casino.cashOutCrash(round, when);
        assertNotNull(r);
        assertEquals(CrashRound.State.CASHED_OUT, round.state());
        assertEquals(14, r.payout(), 1);
    }

    @Test
    void crashAutoCashOut() {
        CrashRound round;
        long t = 0;
        do {
            round = casino.startCrash(100, 2.0, t);
            while (round.isRunning()) {
                t += 50;
                casino.updateCrash(round, t);
            }
        } while (round.crashPoint() <= 2.0);
        assertEquals(CrashRound.State.CASHED_OUT, round.state());
        assertEquals(200, round.payout());
    }

    @Test
    void scratchTicketPrizeIsCredited() {
        long before = casino.balance();
        ScratchTicket t = casino.buyScratchTicket(ScratchTier.BRONZE);
        assertNotNull(t);
        assertEquals(before - 50 + t.prize(), casino.balance());
    }

    @Test
    void emergencyLoanOnlyWhenBrokeAndEveryTenMinutes() {
        assertFalse(casino.canTakeLoan());
        casino.profile().balance = 0;
        assertTrue(casino.takeLoan());
        assertEquals(100, casino.balance());
        assertTrue(unlocked.contains(Achievement.EMERGENCY_LOAN));
        casino.profile().balance = 0;
        assertFalse(casino.takeLoan(), "Wartezeit");
        clock.addAndGet(Odds.LOAN_COOLDOWN_MS - 1);
        assertFalse(casino.canTakeLoan());
        clock.addAndGet(1);
        assertTrue(casino.takeLoan());
    }

    @Test
    void wheelOncePerDay() {
        LocalDate day = LocalDate.of(2026, 10, 11);
        assertTrue(casino.canSpinWheel(day));
        Casino.WheelSpin spin = casino.spinWheel(day);
        assertNotNull(spin);
        assertEquals(LuckyWheel.SEGMENTS.get(spin.index()), spin.segment());
        assertNull(casino.spinWheel(day));
        assertTrue(casino.canSpinWheel(day.plusDays(1)));
        assertTrue(unlocked.contains(Achievement.WHEEL_SPIN));
    }

    @Test
    void bonusMultiplierDoublesNextProfit() {
        casino.profile().bonusMultiplier = 2;
        RoundResult r = null;
        for (int i = 0; i < 200 && (r == null || !r.isWin()); i++) {
            CoinFlipSeries s = casino.startCoinFlip(10);
            if (casino.flipCoin(s, CoinSide.TAILS) == null) {
                r = casino.cashOutCoin(s);
            }
        }
        assertNotNull(r);
        assertEquals(2, r.bonusMultiplier());
        assertEquals(30, r.payout(), "Einsatz 10 + Reingewinn 10 × 2");
        assertEquals(1, casino.profile().bonusMultiplier, "Bonus ist verbraucht");
    }

    @Test
    void lootBoxGivesRewardAndCosmeticsCanBeEquipped() {
        casino.profile().balance = 100_000;
        int cosmetics = 0;
        for (int i = 0; i < 60; i++) {
            LootReward r = casino.openLootBox(LootBoxType.DIAMOND);
            assertNotNull(r);
            if (r.isNewCosmetic()) {
                cosmetics++;
                assertTrue(casino.equip(r.cosmetic()));
            }
        }
        assertTrue(cosmetics > 0);
        assertEquals(60, casino.profile().stats.lootBoxesOpened);
        for (Cosmetic c : Cosmetic.values()) {
            if (!casino.owns(c)) {
                assertFalse(casino.equip(c), "nicht besessene Kosmetik darf nicht anlegbar sein");
            }
        }
    }

    @Test
    void brokeAndFirstWinAchievements() {
        casino.profile().balance = 10;
        for (int i = 0; i < 100 && casino.balance() > 0; i++) {
            casino.playSlots(casino.balance());
        }
        if (casino.balance() == 0) {
            assertTrue(unlocked.contains(Achievement.BROKE));
        }
        assertEquals(casino.profile().balanceHistory.getLast(), casino.balance());
    }

    @Test
    void resetRestoresDefaults() {
        casino.playSlots(10);
        casino.reset();
        assertEquals(1_000, casino.balance());
        assertEquals(0, casino.profile().stats.totalRounds());
        assertEquals(0, casino.unlockedCount());
    }

    @Test
    void betMath() {
        assertEquals(20, BetMath.doubled(10, 1_000));
        assertEquals(1_000, BetMath.doubled(800, 1_000));
        assertEquals(5, BetMath.halved(10, 1_000));
        assertEquals(1, BetMath.halved(1, 1_000));
        assertEquals(110, BetMath.add(10, 100, 1_000));
        assertEquals(1, BetMath.clamp(50, 0));
        assertEquals(1_500, BetMath.parse("1.500"));
        assertEquals(2_000, BetMath.parse("2k"));
        assertEquals(-1, BetMath.parse("abc"));
        assertEquals(-1, BetMath.parse(""));
    }
}

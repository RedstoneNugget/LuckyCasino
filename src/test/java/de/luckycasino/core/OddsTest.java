package de.luckycasino.core;

import de.luckycasino.core.crash.CrashRound;
import de.luckycasino.core.roulette.Roulette;
import de.luckycasino.core.roulette.RouletteBet;
import de.luckycasino.core.roulette.RouletteVariant;
import de.luckycasino.core.scratch.ScratchTicket;
import de.luckycasino.core.slots.SlotMachine;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Rechnet die in {@link Odds} dokumentierten Werte nach. */
class OddsTest {

    @Test
    void slotReturnToPlayerMatchesDocumentation() {
        assertEquals(1.0001, SlotMachine.returnToPlayer(Odds.FAIR.slotWeights()), 0.0005);
        assertEquals(0.9500, SlotMachine.returnToPlayer(Odds.HOUSE.slotWeights()), 0.0005);
        assertEquals(0.8800, SlotMachine.returnToPlayer(Odds.HARDCORE.slotWeights()), 0.0005);
        assertEquals(7_762, 1 / SlotMachine.jackpotChance(Odds.FAIR.slotWeights()), 5);
        assertEquals(11_775, 1 / SlotMachine.jackpotChance(Odds.HOUSE.slotWeights()), 5);
        assertEquals(18_610, 1 / SlotMachine.jackpotChance(Odds.HARDCORE.slotWeights()), 5);
    }

    @Test
    void slotSimulationAgreesWithExactValue() {
        CasinoRng rng = new CasinoRng(42, LuckMode.HOUSE);
        long bet = 100;
        long wagered = 0;
        long paid = 0;
        for (int i = 0; i < 2_000_000; i++) {
            wagered += bet;
            paid += SlotMachine.spin(rng).payout(bet);
        }
        assertEquals(0.95, (double) paid / wagered, 0.015);
    }

    @Test
    void rouletteReturnToPlayerIsExact() {
        assertEquals(1.0, RouletteVariant.NO_ZERO.returnToPlayer(), 1e-12);
        assertEquals(36.0 / 37.0, RouletteVariant.SINGLE_ZERO.returnToPlayer(), 1e-12);
        assertEquals(36.0 / 38.0, RouletteVariant.DOUBLE_ZERO.returnToPlayer(), 1e-12);
        assertEquals(36, RouletteVariant.NO_ZERO.pocketCount());
        assertEquals(37, RouletteVariant.SINGLE_ZERO.pocketCount());
        assertEquals(38, RouletteVariant.DOUBLE_ZERO.pocketCount());
    }

    @Test
    void everyRouletteBetHasSameReturnOnEachWheel() {
        for (RouletteVariant v : RouletteVariant.values()) {
            for (RouletteBet bet : allBets(v)) {
                double expected = 0;
                for (int i = 0; i < v.pocketCount(); i++) {
                    expected += Roulette.payout(Map.of(bet, 1L), v.pocketAt(i));
                }
                assertEquals(v.returnToPlayer(), expected / v.pocketCount(), 1e-9, v + " " + bet);
            }
        }
    }

    private static java.util.List<RouletteBet> allBets(RouletteVariant v) {
        java.util.List<RouletteBet> bets = new java.util.ArrayList<>();
        for (RouletteBet.Kind k : RouletteBet.Kind.values()) {
            if (k != RouletteBet.Kind.STRAIGHT) {
                bets.add(RouletteBet.of(k));
            }
        }
        for (int i = 0; i < v.pocketCount(); i++) {
            bets.add(RouletteBet.straight(v.pocketAt(i)));
        }
        return bets;
    }

    @Test
    void coinFlipHouseEdge() {
        assertEquals(0.00, Odds.houseEdge(GameType.COINFLIP, LuckMode.FAIR), 1e-9);
        assertEquals(0.04, Odds.houseEdge(GameType.COINFLIP, LuckMode.HOUSE), 1e-9);
        assertEquals(0.10, Odds.houseEdge(GameType.COINFLIP, LuckMode.HARDCORE), 1e-9);
    }

    @Test
    void scratchReturnToPlayer() {
        assertEquals(1.000, ScratchTicket.returnToPlayer(Odds.FAIR.scratchChances()), 1e-9);
        assertEquals(0.905, ScratchTicket.returnToPlayer(Odds.HOUSE.scratchChances()), 1e-9);
        assertEquals(0.800, ScratchTicket.returnToPlayer(Odds.HARDCORE.scratchChances()), 1e-9);
    }

    @Test
    void crashPointDistributionGivesConstantReturn() {
        for (LuckMode mode : LuckMode.values()) {
            CasinoRng rng = new CasinoRng(7, mode);
            double edge = Odds.of(mode).crashHouseEdge();
            int n = 1_000_000;
            double[] targets = {1.5, 2.0, 5.0};
            int[] reached = new int[targets.length];
            for (int i = 0; i < n; i++) {
                double p = CrashRound.drawCrashPoint(rng);
                for (int t = 0; t < targets.length; t++) {
                    if (p > targets[t]) {
                        reached[t]++;
                    }
                }
            }
            for (int t = 0; t < targets.length; t++) {
                double rtp = targets[t] * reached[t] / n;
                assertEquals(1.0 - edge, rtp, 0.01, mode + " Cash-out bei " + targets[t]);
            }
        }
    }

    @Test
    void harderModesAlwaysHaveHigherHouseEdge() {
        for (GameType g : GameType.values()) {
            double fair = Odds.houseEdge(g, LuckMode.FAIR);
            double house = Odds.houseEdge(g, LuckMode.HOUSE);
            double hard = Odds.houseEdge(g, LuckMode.HARDCORE);
            assertTrue(fair < house && house < hard, g + ": " + fair + " / " + house + " / " + hard);
            assertTrue(fair >= -0.001 && fair < 0.01, g + " ist im Fair-Modus nicht fair: " + fair);
        }
    }

    @Test
    void printHouseEdgeTable() {
        System.out.println("Hausvorteil   FAIR    HOUSE   HARDCORE");
        for (GameType g : GameType.values()) {
            System.out.printf("%-12s %6.2f%% %6.2f%% %6.2f%%%n", g,
                    100 * Odds.houseEdge(g, LuckMode.FAIR),
                    100 * Odds.houseEdge(g, LuckMode.HOUSE),
                    100 * Odds.houseEdge(g, LuckMode.HARDCORE));
        }
    }
}

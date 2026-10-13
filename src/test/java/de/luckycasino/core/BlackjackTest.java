package de.luckycasino.core;

import de.luckycasino.core.blackjack.BlackjackGame;
import de.luckycasino.core.blackjack.BlackjackHand;
import de.luckycasino.core.blackjack.BlackjackRules;
import de.luckycasino.core.blackjack.Card;
import de.luckycasino.core.blackjack.Shoe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlackjackTest {

    private static Card c(int rank) {
        return new Card(rank, Card.Suit.SPADES);
    }

    private static BlackjackHand hand(int... ranks) {
        BlackjackHand h = new BlackjackHand();
        for (int r : ranks) {
            h.add(c(r));
        }
        return h;
    }

    @Test
    void handTotals() {
        assertEquals(21, hand(1, 13).total());
        assertTrue(hand(1, 13).isBlackjack());
        assertEquals(12, hand(1, 1).total());
        assertTrue(hand(1, 6).isSoft());
        assertEquals(17, hand(1, 6).total());
        assertEquals(17, hand(1, 6, 10).total());
        assertFalse(hand(1, 6, 10).isSoft());
        assertTrue(hand(10, 12, 2).isBust());
        assertEquals(21, hand(1, 1, 9).total());
        assertFalse(hand(1, 1, 9).isBlackjack());
    }

    @Test
    void payoutsFollowRules() {
        CasinoRng rng = new CasinoRng(1, LuckMode.FAIR);
        // Viele Runden spielen und prüfen, dass Auszahlung zum Ergebnis passt
        Shoe shoe = new Shoe(1, rng);
        BlackjackRules rules = new BlackjackRules(1, 1.5, false);
        for (int i = 0; i < 20_000; i++) {
            if (shoe.needsShuffle()) {
                shoe.shuffle();
            }
            BlackjackGame g = new BlackjackGame(shoe, rules, 100);
            while (!g.isFinished()) {
                if (g.player().total() < 17) {
                    g.hit();
                } else {
                    g.stand();
                }
            }
            long expected = switch (g.outcome()) {
                case BLACKJACK -> 250;
                case WIN, DEALER_BUST -> 200;
                case PUSH -> 100;
                default -> 0;
            };
            assertEquals(expected, g.payout(), g.outcome().name());
            if (g.outcome() == BlackjackGame.Outcome.DEALER_BUST) {
                assertTrue(g.dealer().isBust());
            }
            if (g.outcome() != BlackjackGame.Outcome.BUST && g.outcome() != BlackjackGame.Outcome.BLACKJACK
                    && g.outcome() != BlackjackGame.Outcome.DEALER_BLACKJACK && g.outcome() != BlackjackGame.Outcome.PUSH) {
                assertTrue(g.dealer().total() >= 17, "Dealer muss bis 17 ziehen");
            }
        }
    }

    /**
     * Basisstrategie ohne Split (Ass-Paar = Soft 12) – nur für die Schätzung des Hausvorteils.
     * Rückgabe: 'H' ziehen, 'S' stehen, 'D' verdoppeln (sonst ziehen), 'd' verdoppeln (sonst stehen).
     */
    private static char basicStrategy(BlackjackHand player, Card dealerUp, boolean h17) {
        int up = dealerUp.isAce() ? 11 : dealerUp.hardValue();
        int t = player.total();
        if (player.isSoft()) {
            if (t >= 19) {
                return 'S';
            }
            if (t == 18) {
                if (up >= 3 && up <= 6 || (h17 && up == 2)) {
                    return 'd';
                }
                return up <= 8 ? 'S' : 'H';
            }
            if (t == 17) {
                return up >= 3 && up <= 6 ? 'D' : 'H';
            }
            if (t >= 15) {
                return up >= 4 && up <= 6 ? 'D' : 'H';
            }
            if (t >= 13) {
                return up >= 5 && up <= 6 ? 'D' : 'H';
            }
            return 'H';
        }
        if (t >= 17) {
            return 'S';
        }
        if (t >= 13) {
            return up <= 6 ? 'S' : 'H';
        }
        if (t == 12) {
            return up >= 4 && up <= 6 ? 'S' : 'H';
        }
        if (t == 11) {
            return up <= 10 || h17 ? 'D' : 'H';
        }
        if (t == 10) {
            return up <= 9 ? 'D' : 'H';
        }
        if (t == 9) {
            return up >= 3 && up <= 6 ? 'D' : 'H';
        }
        return 'H';
    }

    private static double simulateEdge(LuckMode mode, int hands) {
        CasinoRng rng = new CasinoRng(1234 + mode.ordinal(), mode);
        BlackjackRules rules = BlackjackRules.of(Odds.of(mode));
        Shoe shoe = new Shoe(rules.decks(), rng);
        long bet = 100;
        long net = 0;
        for (int i = 0; i < hands; i++) {
            if (shoe.needsShuffle()) {
                shoe.shuffle();
            }
            BlackjackGame g = new BlackjackGame(shoe, rules, bet);
            Card up = g.dealer().cards().getFirst();
            while (!g.isFinished()) {
                char a = basicStrategy(g.player(), up, rules.dealerHitsSoft17());
                if ((a == 'D' || a == 'd') && g.canDouble()) {
                    g.doubleDown();
                } else if (a == 'H' || a == 'D') {
                    g.hit();
                } else {
                    g.stand();
                }
            }
            net += g.payout() - g.stake();
        }
        return -(double) net / ((double) bet * hands);
    }

    @Test
    void houseEdgeEstimatesAreRealistic() {
        int hands = 1_500_000;
        double fair = simulateEdge(LuckMode.FAIR, hands);
        double house = simulateEdge(LuckMode.HOUSE, hands);
        double hard = simulateEdge(LuckMode.HARDCORE, hands);
        System.out.printf("Blackjack-Hausvorteil (Basisstrategie, ohne Split): FAIR %.2f%%, HOUSE %.2f%%, HARDCORE %.2f%%%n",
                fair * 100, house * 100, hard * 100);
        // Standardfehler bei 1,5 Mio. Händen ≈ 0,1 %
        assertEquals(Odds.blackjackHouseEdgeEstimate(LuckMode.FAIR), fair, 0.004);
        assertEquals(Odds.blackjackHouseEdgeEstimate(LuckMode.HOUSE), house, 0.004);
        assertEquals(Odds.blackjackHouseEdgeEstimate(LuckMode.HARDCORE), hard, 0.004);
        assertTrue(fair < hard);
    }
}

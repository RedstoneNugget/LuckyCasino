package de.luckycasino.core;

import de.luckycasino.core.loot.LootBox;
import de.luckycasino.core.loot.LootBoxType;
import de.luckycasino.core.loot.Rarity;
import de.luckycasino.core.scratch.ScratchSymbol;
import de.luckycasino.core.scratch.ScratchTicket;
import de.luckycasino.core.scratch.ScratchTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScratchAndLootTest {

    @Test
    void ticketsHaveAtMostOneTriple() {
        CasinoRng rng = new CasinoRng(5, LuckMode.FAIR);
        long paid = 0;
        int n = 400_000;
        for (int i = 0; i < n; i++) {
            ScratchTicket t = ScratchTicket.generate(rng, ScratchTier.BRONZE);
            int[] counts = new int[ScratchSymbol.values().length];
            for (int f = 0; f < ScratchTicket.FIELDS; f++) {
                counts[t.symbol(f).ordinal()]++;
            }
            int triples = 0;
            for (int s = 0; s < counts.length; s++) {
                assertTrue(counts[s] <= 3);
                if (counts[s] == 3) {
                    triples++;
                    assertEquals(ScratchSymbol.values()[s], t.winSymbol());
                }
            }
            assertTrue(triples <= 1);
            assertEquals(triples == 1, t.winSymbol() != null);
            paid += t.prize();
        }
        double rtp = (double) paid / (n * ScratchTier.BRONZE.price());
        assertEquals(1.0, rtp, 0.05);
    }

    @Test
    void lootRarityDistribution() {
        CasinoRng rng = new CasinoRng(3, LuckMode.HOUSE);
        int n = 200_000;
        int[] counts = new int[Rarity.values().length];
        for (int i = 0; i < n; i++) {
            counts[LootBox.rollRarity(rng, LootBoxType.WOODEN).ordinal()]++;
        }
        int[] w = LootBoxType.WOODEN.rarityWeights();
        for (int i = 0; i < w.length; i++) {
            assertEquals(w[i] / 100.0, counts[i] / (double) n, 0.01, Rarity.values()[i].name());
        }
    }
}

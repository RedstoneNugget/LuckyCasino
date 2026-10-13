import json, re, sys, os

ROOT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
LANG_DIR = os.path.join(ROOT, "src/main/resources/assets/luckycasino/lang")

# (Schlüssel, Deutsch, Englisch)
T = []
def t(k, de, en):
    T.append((k, de, en))

t("key.luckycasino.open", "Casino öffnen", "Open casino")
t("key.category.luckycasino.main", "LuckyCasino", "LuckyCasino")
t("luckycasino.title", "LuckyCasino", "LuckyCasino")

# Allgemein
t("luckycasino.gui.back", "Zurück", "Back")
t("luckycasino.gui.close", "Schließen", "Close")
t("luckycasino.gui.history", "Verlauf:", "History:")
t("luckycasino.gui.not_enough", "Nicht genug Chips!", "Not enough chips!")
t("luckycasino.bet", "Einsatz", "Bet")
t("luckycasino.bet.label", "Einsatz:", "Bet:")
t("luckycasino.bet.half", "Einsatz halbieren", "Halve the bet")
t("luckycasino.bet.double", "Einsatz verdoppeln", "Double the bet")
t("luckycasino.bet.plus10", "+10 Chips", "+10 chips")
t("luckycasino.bet.plus100", "+100 Chips", "+100 chips")
t("luckycasino.bet.max", "Maximaler Einsatz (gesamtes Guthaben)", "Maximum bet (entire balance)")
t("luckycasino.result.win", "Gewonnen: %s Chips", "Won: %s chips")
t("luckycasino.result.lose", "Verloren: -%s Chips", "Lost: -%s chips")
t("luckycasino.result.push", "Unentschieden – Einsatz zurück", "Push – bet returned")
t("luckycasino.result.push_refund", "Einsatz zurück", "Bet returned")
t("luckycasino.result.chips", "%s Chips", "%s chips")
t("luckycasino.result.chips_bonus", "%s Chips (Bonus x%s)", "%s chips (bonus x%s)")
t("luckycasino.result.bonus", "Bonus x%s angewendet!", "Bonus x%s applied!")
t("luckycasino.toast.achievement", "Erfolg freigeschaltet!", "Achievement unlocked!")

# Glücksfaktor
t("luckycasino.luck.fair", "Fair", "Fair")
t("luckycasino.luck.fair.desc", "Faire Chancen: erwartete Auszahlung 100 %%. Roulette ohne Null, Münzwurf 50 %%, Crash ohne Hausvorteil.",
  "Fair odds: expected return 100%%. Roulette without zero, coin flip 50%%, crash without house edge.")
t("luckycasino.luck.house", "Casino-Hausvorteil", "Casino house edge")
t("luckycasino.luck.house.desc", "Wie im echten Casino: wenige Prozent Hausvorteil. Europäisches Roulette, Münzwurf 48 %%, 6 Decks beim Blackjack.",
  "Like a real casino: a few percent house edge. European roulette, coin flip 48%%, 6 decks at blackjack.")
t("luckycasino.luck.hardcore", "Hardcore", "Hardcore")
t("luckycasino.luck.hardcore.desc", "Das Haus gewinnt fast immer: amerikanisches Roulette (0 und 00), Blackjack zahlt 6:5, Münzwurf 45 %%.",
  "The house almost always wins: American roulette (0 and 00), blackjack pays 6:5, coin flip 45%%.")

# Spiele
t("luckycasino.game.slots", "Slot-Maschine", "Slot Machine")
t("luckycasino.game.roulette", "Roulette", "Roulette")
t("luckycasino.game.coinflip", "Münzwurf", "Coin Flip")
t("luckycasino.game.blackjack", "Blackjack", "Blackjack")
t("luckycasino.game.crash", "Crash", "Crash")
t("luckycasino.game.scratch", "Rubbellose", "Scratch Cards")

# Hauptmenü
t("luckycasino.menu.wheel", "Glücksrad", "Wheel of Fortune")
t("luckycasino.menu.loot", "Lootboxen", "Loot Boxes")
t("luckycasino.menu.achievements", "Erfolge", "Achievements")
t("luckycasino.menu.stats", "Statistik", "Statistics")
t("luckycasino.menu.records", "Bestenliste", "Leaderboard")
t("luckycasino.menu.settings", "Einstellungen", "Settings")
t("luckycasino.menu.greeting", "Willkommen im LuckyCasino!", "Welcome to LuckyCasino!")
t("luckycasino.menu.greeting_title", "Willkommen, %s!", "Welcome, %s!")
t("luckycasino.menu.tip", "Taste %s öffnet das Casino · /casino für Befehle", "Key %s opens the casino · /casino for commands")
t("luckycasino.menu.status_none", "Viel Glück an den Tischen!", "Good luck at the tables!")
t("luckycasino.menu.status_bonus", "Bonus aktiv: nächster Gewinn x%s", "Bonus active: next win x%s")
t("luckycasino.menu.status_spins", "%s Slot-Freispiele verfügbar", "%s free slot spins available")
t("luckycasino.menu.status_both", "Bonus x%s aktiv · %s Slot-Freispiele", "Bonus x%s active · %s free slot spins")
t("luckycasino.menu.sub.slots", "Jackpot: 1.000x", "Jackpot: 1,000x")
t("luckycasino.menu.sub.roulette", "Bis zu 36x", "Up to 36x")
t("luckycasino.menu.sub.coinflip", "Kopf oder Zahl?", "Heads or tails?")
t("luckycasino.menu.sub.blackjack", "Schlag den Dealer", "Beat the dealer")
t("luckycasino.menu.sub.crash", "Rechtzeitig raus!", "Cash out in time!")
t("luckycasino.menu.sub.scratch", "Ab 50 Chips", "From 50 chips")
t("luckycasino.menu.sub.wheel_ready", "Gratis bereit!", "Free spin ready!")
t("luckycasino.menu.sub.wheel_wait", "Wieder in %s", "Again in %s")
t("luckycasino.menu.sub.loot", "Sammlung %s/%s", "Collection %s/%s")
t("luckycasino.menu.sub.achievements", "%s / %s", "%s / %s")
t("luckycasino.menu.sub.stats", "Win-Rate %s", "Win rate %s")
t("luckycasino.menu.sub.records", "Rekord: %s", "Best: %s")

# Notkredit
t("luckycasino.loan.broke", "Pleite! Das Casino gewährt dir einen Notkredit:", "Broke! The casino grants you an emergency loan:")
t("luckycasino.loan.button", "Notkredit: +%s Chips", "Emergency loan: +%s chips")
t("luckycasino.loan.wait", "Notkredit in %s", "Loan available in %s")

# Slots
t("luckycasino.slots.spin", "Drehen!", "Spin!")
t("luckycasino.slots.free", "Freispiel (%s)", "Free spin (%s)")
t("luckycasino.slots.free_running", "Freispiel mit %s Chips Einsatz ...", "Free spin with a %s chip bet ...")
t("luckycasino.slots.running", "Die Walzen drehen sich ...", "The reels are spinning ...")
t("luckycasino.slots.hint", "Einsatz wählen und drehen!", "Choose your bet and spin!")
t("luckycasino.slots.win", "%s Chips (%sx %s)", "%s chips (%sx %s)")
t("luckycasino.slots.lose", "Kein Gewinn – nochmal?", "No win – again?")
t("luckycasino.slots.jackpot", "JACKPOT!", "JACKPOT!")
t("luckycasino.slots.paytable", "Gewinntabelle", "Paytable")
t("luckycasino.slots.jackpot_odds", "Jackpot-Chance 1 : %s", "Jackpot odds 1 : %s")
for k, de, en in [("coal", "Kohle", "Coal"), ("redstone", "Redstone", "Redstone"), ("lapis", "Lapislazuli", "Lapis Lazuli"),
                  ("iron", "Eisen", "Iron"), ("gold", "Gold", "Gold"), ("emerald", "Smaragd", "Emerald"),
                  ("creeper", "Creeper-Kopf", "Creeper Head"), ("diamond", "Diamant", "Diamond"), ("star", "Netherstern", "Nether Star")]:
    t("luckycasino.slots.symbol." + k, de, en)

# Roulette
t("luckycasino.roulette.spin", "Drehen", "Spin")
t("luckycasino.roulette.clear", "Leeren", "Clear")
t("luckycasino.roulette.rebet", "Nochmal", "Rebet")
t("luckycasino.roulette.placed", "Gesetzt: %s Chips", "Placed: %s chips")
t("luckycasino.roulette.help", "Links: Chip setzen · Rechts: entfernen", "Left: place chip · Right: remove")
t("luckycasino.roulette.rolling", "Die Kugel rollt ...", "The ball is rolling ...")
t("luckycasino.roulette.tooltip.payout", "Auszahlung: %sx", "Payout: %sx")
t("luckycasino.roulette.tooltip.placed", "Gesetzt: %s", "Placed: %s")
t("luckycasino.roulette.tooltip.help", "Links: +Einsatz · Rechts: entfernen", "Left: +bet · Right: remove")
t("luckycasino.roulette.bet.straight_n", "Einzelzahl %s", "Straight %s")
for k, de, en in [("red", "Rot", "Red"), ("black", "Schwarz", "Black"), ("even", "Gerade", "Even"), ("odd", "Ungerade", "Odd"),
                  ("low", "1-18", "1-18"), ("high", "19-36", "19-36"), ("dozen_1", "1-12", "1-12"), ("dozen_2", "13-24", "13-24"),
                  ("dozen_3", "25-36", "25-36"), ("straight", "Einzelzahl", "Straight")]:
    t("luckycasino.roulette.bet." + k, de, en)
for k, de, en in [("red", "Rot", "Red"), ("black", "Schwarz", "Black"), ("green", "Grün", "Green")]:
    t("luckycasino.roulette.color." + k, de, en)
t("luckycasino.roulette.variant.no_zero", "Fairer Kessel (ohne Null)", "Fair wheel (no zero)")
t("luckycasino.roulette.variant.single_zero", "Europäisch (0)", "European (0)")
t("luckycasino.roulette.variant.double_zero", "Amerikanisch (0, 00)", "American (0, 00)")

# Münzwurf
t("luckycasino.coin.heads", "Kopf", "Heads")
t("luckycasino.coin.tails", "Zahl", "Tails")
t("luckycasino.coin.heads.short", "K", "H")
t("luckycasino.coin.tails.short", "Z", "T")
t("luckycasino.coin.flip", "Münze werfen", "Flip the coin")
t("luckycasino.coin.double", "Doppelt oder nichts!", "Double or nothing!")
t("luckycasino.coin.cashout", "Auszahlen (%s)", "Cash out (%s)")
t("luckycasino.coin.flipping", "Die Münze fliegt ...", "The coin is in the air ...")
t("luckycasino.coin.hint", "Wähle Kopf oder Zahl und wirf die Münze!", "Pick heads or tails and flip the coin!")
t("luckycasino.coin.hit", "Treffer: %s! Im Topf: %s", "Hit: %s! In the pot: %s")
t("luckycasino.coin.lost", "%s – daneben! -%s Chips", "%s – missed! -%s chips")
t("luckycasino.coin.cashed", "Ausgezahlt: %s Chips", "Cashed out: %s chips")
t("luckycasino.coin.series", "Doppelt oder nichts", "Double or nothing")
t("luckycasino.coin.streak", "Serie", "Streak")
t("luckycasino.coin.pot", "Topf", "Pot")
t("luckycasino.coin.next", "Nächster Topf", "Next pot")
t("luckycasino.coin.chance", "Trefferchance", "Win chance")
t("luckycasino.coin.best", "Beste Serie", "Best streak")
t("luckycasino.coin.legend", "Kopf = Krone · Zahl = 1", "Heads = crown · Tails = 1")
t("luckycasino.coin.decide", "Auszahlen – oder alles auf \"Doppelt oder nichts\" setzen?",
  "Cash out – or risk it all on \"double or nothing\"?")

# Blackjack
t("luckycasino.blackjack.deal", "Austeilen", "Deal")
t("luckycasino.blackjack.hit", "Ziehen", "Hit")
t("luckycasino.blackjack.stand", "Halten", "Stand")
t("luckycasino.blackjack.double", "Verdoppeln", "Double down")
t("luckycasino.blackjack.hint", "Einsatz wählen und austeilen!", "Choose your bet and deal!")
t("luckycasino.blackjack.dealer", "Dealer: %s", "Dealer: %s")
t("luckycasino.blackjack.dealer_shows", "Dealer: %s + ?", "Dealer: %s + ?")
t("luckycasino.blackjack.player", "Du: %s", "You: %s")
t("luckycasino.blackjack.stake", "Einsatz: %s", "Bet: %s")
t("luckycasino.blackjack.shoe", "Schlitten: %s Karten", "Shoe: %s cards")
t("luckycasino.blackjack.rules", "Tischregeln", "Table rules")
t("luckycasino.blackjack.rule.decks", "%s Deck(s)", "%s deck(s)")
t("luckycasino.blackjack.rule.pays32", "Blackjack zahlt 3:2", "Blackjack pays 3:2")
t("luckycasino.blackjack.rule.pays65", "Blackjack zahlt nur 6:5", "Blackjack pays only 6:5")
t("luckycasino.blackjack.rule.h17", "Dealer zieht auf Soft 17", "Dealer hits soft 17")
t("luckycasino.blackjack.rule.s17", "Dealer steht auf Soft 17", "Dealer stands on soft 17")
t("luckycasino.blackjack.rule.double", "Verdoppeln auf 2 Karten", "Double on any 2 cards")
t("luckycasino.blackjack.rule.nosplit", "Kein Split/Versicherung", "No split/insurance")
for k, de, en in [("blackjack", "BLACKJACK!", "BLACKJACK!"), ("win", "Gewonnen!", "You win!"),
                  ("dealer_bust", "Dealer überkauft!", "Dealer busts!"), ("push", "Unentschieden", "Push"),
                  ("lose", "Verloren", "You lose"), ("bust", "Überkauft!", "Bust!"),
                  ("dealer_blackjack", "Dealer-Blackjack", "Dealer blackjack")]:
    t("luckycasino.blackjack.outcome." + k, de, en)

# Crash
t("luckycasino.crash.start", "Start", "Launch")
t("luckycasino.crash.cashout", "Cash Out (%s)", "Cash out (%s)")
t("luckycasino.crash.auto", "Auto-Stopp:", "Auto stop:")
t("luckycasino.crash.auto_off", "aus", "off")
t("luckycasino.crash.auto_active", "Auto-Stopp bei %s", "Auto stop at %s")
t("luckycasino.crash.history", "Letzte Crashes", "Recent crashes")
t("luckycasino.crash.best", "Bester: %s", "Best: %s")
t("luckycasino.crash.hint", "Starte die Rakete und steig rechtzeitig aus!", "Launch the rocket and cash out in time!")
t("luckycasino.crash.running", "Einsatz %s – jetzt %s Chips", "Bet %s – now %s chips")
t("luckycasino.crash.cashed_at", "Ausgestiegen bei %s", "Cashed out at %s")
t("luckycasino.crash.crashed_at", "CRASH bei %s!", "CRASHED at %s!")
t("luckycasino.crash.would_crash", "Gecrasht wäre es bei %s", "It would have crashed at %s")

# Rubbellose
t("luckycasino.scratch.choose", "Los kaufen:", "Buy a ticket:")
t("luckycasino.scratch.buy", "%s (%s)", "%s (%s)")
t("luckycasino.scratch.reveal_all", "Alles aufdecken", "Reveal all")
t("luckycasino.scratch.help", "Halte die linke Maustaste gedrückt und rubble die Felder frei.",
  "Hold the left mouse button and scratch the fields.")
t("luckycasino.scratch.none", "Kaufe links ein Rubbellos!", "Buy a scratch card on the left!")
t("luckycasino.scratch.ticket", "RUBBELLOS %s", "SCRATCH CARD %s")
t("luckycasino.scratch.rule", "3 gleiche gewinnen!", "Match 3 to win!")
t("luckycasino.scratch.prizes", "Gewinne", "Prizes")
t("luckycasino.scratch.price_note", "Lospreis: %s", "Ticket price: %s")
t("luckycasino.scratch.won", "Gewinn: %s Chips!", "Prize: %s chips!")
t("luckycasino.scratch.nothing", "Leider kein Gewinn.", "No win this time.")
for k, de, en in [("bronze", "Bronze", "Bronze"), ("silver", "Silber", "Silver"), ("gold", "Gold", "Gold")]:
    t("luckycasino.scratch.tier." + k, de, en)

# Glücksrad
t("luckycasino.wheel.spin", "Glücksrad drehen!", "Spin the wheel!")
t("luckycasino.wheel.spinning", "Das Rad dreht sich ...", "The wheel is spinning ...")
t("luckycasino.wheel.ready", "Gratis-Drehung verfügbar!", "Free spin available!")
t("luckycasino.wheel.used", "Heute schon gedreht.", "Already spun today.")
t("luckycasino.wheel.next", "Nächste Drehung in %s", "Next spin in %s")
t("luckycasino.wheel.info", "Einmal pro Tag gratis (echte Zeit). Alle 16 Felder sind gleich wahrscheinlich.",
  "Free once per day (real time). All 16 fields are equally likely.")
t("luckycasino.wheel.legend", "Preise:", "Prizes:")
t("luckycasino.wheel.legend.chips", "Chips", "Chips")
t("luckycasino.wheel.legend.bonus", "Bonus auf nächsten Gewinn", "Bonus on your next win")
t("luckycasino.wheel.legend.spins", "Slot-Freispiele", "Free slot spins")
t("luckycasino.wheel.legend.jackpot", "Jackpot: 2.500 Chips", "Jackpot: 2,500 chips")
t("luckycasino.wheel.seg.bonus", "Bonus x%s", "Bonus x%s")
t("luckycasino.wheel.seg.spins", "%s Freisp.", "%s spins")
t("luckycasino.wheel.won", "Gewonnen!", "You won!")
t("luckycasino.wheel.jackpot", "JACKPOT!", "JACKPOT!")
t("luckycasino.wheel.prize.bonus", "Bonus x%s auf deinen nächsten Gewinn!", "Bonus x%s on your next win!")
t("luckycasino.wheel.prize.spins", "%s Slot-Freispiel(e)!", "%s free slot spin(s)!")
t("luckycasino.wheel.status_bonus", "Bonus: %s", "Bonus: %s")
t("luckycasino.wheel.status_spins", "Freispiele: %s (à %s Chips)", "Free spins: %s (%s chips each)")

# Lootboxen
t("luckycasino.loot.box.wooden", "Holzkiste", "Wooden Chest")
t("luckycasino.loot.box.diamond", "Diamantkiste", "Diamond Chest")
t("luckycasino.loot.open", "Öffnen (%s)", "Open (%s)")
t("luckycasino.loot.price", "Preis: %s Chips", "Price: %s chips")
t("luckycasino.loot.odds", "Chancen in %%: %s", "Odds in %%: %s")
t("luckycasino.loot.hint", "Wähle eine Kiste und öffne sie!", "Pick a chest and open it!")
t("luckycasino.loot.chips", "%s Chips", "%s chips")
t("luckycasino.loot.info", "%s · %s", "%s · %s")
t("luckycasino.loot.info_chips", "%s · Chip-Bonus", "%s · chip bonus")
t("luckycasino.loot.new", "Neu in deiner Sammlung!", "New in your collection!")
t("luckycasino.loot.duplicate", "Duplikat – umgewandelt in %s Chips", "Duplicate – converted into %s chips")
t("luckycasino.loot.equip", "Anlegen", "Equip")
t("luckycasino.loot.equipped", "Angelegt", "Equipped")
t("luckycasino.loot.collection", "Sammlung", "Collection")
t("luckycasino.loot.opened", "Geöffnet: %s · Sammlung: %s/%s", "Opened: %s · Collection: %s/%s")
for k, de, en in [("common", "Gewöhnlich", "Common"), ("uncommon", "Ungewöhnlich", "Uncommon"), ("rare", "Selten", "Rare"),
                  ("epic", "Episch", "Epic"), ("legendary", "Legendär", "Legendary")]:
    t("luckycasino.rarity." + k, de, en)

# Kosmetik
t("luckycasino.cosmetic.type.title", "Titel", "Title")
t("luckycasino.cosmetic.type.titles", "Titel", "Titles")
t("luckycasino.cosmetic.type.frame", "Rahmenfarbe", "Frame color")
t("luckycasino.cosmetic.type.frames", "Rahmenfarben", "Frame colors")
for k, de, en in [("title.rookie", "Frischling", "Rookie"), ("title.gambler", "Zocker", "Gambler"),
                  ("title.lucky", "Glückspilz", "Lucky Duck"), ("title.card_shark", "Kartenhai", "Card Shark"),
                  ("title.high_roller", "High Roller", "High Roller"), ("title.daredevil", "Draufgänger", "Daredevil"),
                  ("title.diamond_hands", "Diamanthände", "Diamond Hands"), ("title.casino_king", "Casino-König", "Casino King"),
                  ("frame.classic", "Klassisch Gold", "Classic Gold"), ("frame.oak", "Eichenholz", "Oak"),
                  ("frame.stone", "Stein", "Stone"), ("frame.copper", "Kupfer", "Copper"), ("frame.emerald", "Smaragd", "Emerald"),
                  ("frame.lapis", "Lapislazuli", "Lapis"), ("frame.amethyst", "Amethyst", "Amethyst"),
                  ("frame.gold", "Pures Gold", "Pure Gold"), ("frame.rainbow", "Regenbogen", "Rainbow")]:
    t("luckycasino.cosmetic." + k, de, en)
t("luckycasino.collection.title", "Sammlung", "Collection")
t("luckycasino.collection.help", "Klick: anlegen / ablegen", "Click: equip / unequip")
t("luckycasino.collection.default", "Standard", "Default")
t("luckycasino.collection.equipped", "Angelegt", "Equipped")
t("luckycasino.collection.click", "Klicken zum Anlegen", "Click to equip")
t("luckycasino.collection.locked", "Aus Lootboxen erhältlich", "Obtainable from loot boxes")

# Erfolge
t("luckycasino.achievements.progress", "%s / %s freigeschaltet", "%s / %s unlocked")
t("luckycasino.achievements.unlocked_at", "Freigeschaltet: %s", "Unlocked: %s")
t("luckycasino.achievements.locked", "Noch nicht freigeschaltet", "Not unlocked yet")
for k, dn, dd, en_n, en_d in [
    ("first_win", "Anfängerglück", "Gewinne deine erste Runde.", "Beginner's Luck", "Win your first round."),
    ("first_jackpot", "Erster Jackpot", "Drei Nethersterne an der Slot-Maschine.", "First Jackpot", "Three nether stars on the slot machine."),
    ("win_streak_10", "Unaufhaltsam", "Gewinne 10 Runden in Folge.", "Unstoppable", "Win 10 rounds in a row."),
    ("broke", "Pleite gegangen", "Verliere dein gesamtes Guthaben.", "Gone Broke", "Lose your entire balance."),
    ("emergency_loan", "Notgroschen", "Nimm einen Notkredit auf.", "Rainy Day", "Take an emergency loan."),
    ("high_roller", "High Roller", "Setze mindestens 5.000 Chips in einer Runde.", "High Roller", "Bet at least 5,000 chips in one round."),
    ("rich", "Gut betucht", "Erreiche ein Guthaben von 10.000 Chips.", "Well Off", "Reach a balance of 10,000 chips."),
    ("tycoon", "Chip-Magnat", "Erreiche ein Guthaben von 100.000 Chips.", "Chip Tycoon", "Reach a balance of 100,000 chips."),
    ("natural", "Black Jack!", "Erhalte einen Blackjack mit den ersten zwei Karten.", "Natural!", "Get a blackjack with your first two cards."),
    ("bullseye", "Volltreffer", "Triff eine Einzelzahl beim Roulette.", "Bullseye", "Hit a straight number at roulette."),
    ("to_the_moon", "Ab zum Mond", "Steig beim Crash bei 10x oder mehr aus.", "To the Moon", "Cash out at 10x or more in crash."),
    ("double_trouble", "Doppelt hält besser", "Gewinne 5 Münzwürfe in einer Serie.", "Double Trouble", "Win 5 coin flips in one series."),
    ("scratch_luck", "Rubbelglück", "Gewinne mindestens das 10-Fache beim Rubbellos.", "Scratch Luck", "Win at least 10x on a scratch card."),
    ("wheel_spin", "Dreh am Rad", "Drehe das tägliche Glücksrad.", "Spin to Win", "Spin the daily wheel of fortune."),
    ("legendary_loot", "Legendär!", "Ziehe etwas Legendäres aus einer Lootbox.", "Legendary!", "Pull something legendary from a loot box."),
    ("all_rounder", "Allrounder", "Spiele jedes Casino-Spiel mindestens einmal.", "All-Rounder", "Play every casino game at least once."),
    ("regular", "Stammgast", "Spiele 100 Runden.", "Regular", "Play 100 rounds."),
    ("collector", "Sammler", "Besitze 8 kosmetische Gegenstände.", "Collector", "Own 8 cosmetic items."),
]:
    t("luckycasino.achievement." + k, dn, en_n)
    t("luckycasino.achievement." + k + ".desc", dd, en_d)

# Statistik
t("luckycasino.stats.tab.overview", "Übersicht", "Overview")
t("luckycasino.stats.tab.games", "Spiele", "Games")
t("luckycasino.stats.tab.chart", "Verlauf", "History")
t("luckycasino.stats.balance", "Guthaben", "Balance")
t("luckycasino.stats.rounds", "Runden gespielt", "Rounds played")
t("luckycasino.stats.wins_losses", "Siege / Niederlagen", "Wins / losses")
t("luckycasino.stats.winrate", "Win-Rate", "Win rate")
t("luckycasino.stats.wagered", "Gesamteinsatz", "Total wagered")
t("luckycasino.stats.paid", "Auszahlungen", "Total paid out")
t("luckycasino.stats.net", "Gewinn/Verlust", "Profit/loss")
t("luckycasino.stats.playtime", "Spielzeit", "Play time")
t("luckycasino.stats.biggest", "Größter Gewinn", "Biggest win")
t("luckycasino.stats.bonus_income", "Bonus-Einnahmen", "Bonus income")
t("luckycasino.stats.loans", "Notkredite", "Emergency loans")
t("luckycasino.stats.wheel", "Glücksrad-Drehungen", "Wheel spins")
t("luckycasino.stats.lootboxes", "Lootboxen geöffnet", "Loot boxes opened")
t("luckycasino.stats.shop", "Lootbox-Ausgaben", "Spent on loot boxes")
t("luckycasino.stats.achievements", "Erfolge", "Achievements")
t("luckycasino.stats.note", "Bonus-Einnahmen = Notkredite, Glücksrad und Chips aus Lootboxen. Spielzeit = Zeit mit geöffnetem Casino.",
  "Bonus income = emergency loans, wheel of fortune and chips from loot boxes. Play time = time with the casino open.")
t("luckycasino.stats.col.game", "Spiel", "Game")
t("luckycasino.stats.col.rounds", "Runden", "Rounds")
t("luckycasino.stats.col.winrate", "Win-Rate", "Win rate")
t("luckycasino.stats.col.wagered", "Einsatz", "Wagered")
t("luckycasino.stats.col.net", "Netto", "Net")
t("luckycasino.stats.col.best", "Bester", "Best")
t("luckycasino.stats.games_note", "Win-Rate = gewonnene / gespielte Runden. Netto = Auszahlungen - Einsätze.",
  "Win rate = rounds won / rounds played. Net = paid out - wagered.")
t("luckycasino.stats.chart_title", "Guthaben-Verlauf (letzte %s Einträge)", "Balance history (last %s entries)")
t("luckycasino.stats.chart_empty", "Noch zu wenig Daten – spiel ein paar Runden!", "Not enough data yet – play a few rounds!")
t("luckycasino.stats.chart_point", "Eintrag #%s", "Entry #%s")

# Bestenliste
t("luckycasino.records.personal", "Persönliche Rekorde", "Personal records")
t("luckycasino.records.highest", "Höchstes Guthaben", "Highest balance")
t("luckycasino.records.streak", "Längste Gewinnserie", "Longest win streak")
t("luckycasino.records.current_streak", "Aktuelle Gewinnserie", "Current win streak")
t("luckycasino.records.biggest", "Größter Einzelgewinn", "Biggest single win")
t("luckycasino.records.crash", "Höchster Crash-Ausstieg", "Best crash cash-out")
t("luckycasino.records.coin", "Längste Münzwurf-Serie", "Longest coin streak")
t("luckycasino.records.top", "Top 10 Einzelgewinne", "Top 10 single wins")
t("luckycasino.records.empty", "Noch keine Gewinne – ab an die Tische!", "No wins yet – off to the tables!")

# Einstellungen
t("luckycasino.settings.mode", "Glücksfaktor: %s", "Luck mode: %s")
t("luckycasino.settings.mode.tooltip", "Bestimmt Gewinnchancen und Hausvorteil aller Spiele.", "Controls the odds and house edge of all games.")
t("luckycasino.settings.hud", "Chip-Anzeige (HUD): %s", "Chip display (HUD): %s")
t("luckycasino.settings.hud_move", "HUD verschieben ...", "Move HUD ...")
t("luckycasino.settings.hud_title", "Titel im HUD: %s", "Title in HUD: %s")
t("luckycasino.settings.sounds", "Sounds: %s", "Sounds: %s")
t("luckycasino.settings.volume", "Lautstärke: %s %%", "Volume: %s%%")
t("luckycasino.settings.animations", "Animationen: %s", "Animations: %s")
t("luckycasino.settings.animations.normal", "Normal", "Normal")
t("luckycasino.settings.animations.fast", "Schnell", "Fast")
t("luckycasino.settings.edge_title", "Hausvorteil je Spiel:", "House edge per game:")
t("luckycasino.settings.config_file", "Datei: config/luckycasino.json", "File: config/luckycasino.json")
t("luckycasino.settings.reset", "Spielstand zurücksetzen ...", "Reset progress ...")
t("luckycasino.settings.reset.title", "Spielstand wirklich zurücksetzen?", "Really reset your progress?")
t("luckycasino.settings.reset.text", "Guthaben, Statistiken, Rekorde, Erfolge und Sammlung werden gelöscht. Einstellungen bleiben erhalten.",
  "Balance, statistics, records, achievements and collection will be deleted. Settings are kept.")

# HUD-Position
t("luckycasino.hudpos.title", "HUD-Position", "HUD position")
t("luckycasino.hudpos.help", "Ziehe die Chip-Anzeige mit der Maus an die gewünschte Stelle.", "Drag the chip display to where you want it.")
t("luckycasino.hudpos.reset", "Zurücksetzen", "Reset")

# Befehle
t("luckycasino.command.help", "Befehle: /casino open | balance | reset", "Commands: /casino open | balance | reset")
t("luckycasino.command.balance", "Guthaben: %s Chips", "Balance: %s chips")
t("luckycasino.command.loan_ready", "Du bist pleite – im Casino-Menü wartet ein Notkredit.", "You're broke – an emergency loan is waiting in the casino menu.")
t("luckycasino.command.loan_wait", "Nächster Notkredit in %s.", "Next emergency loan in %s.")
t("luckycasino.command.reset.warn", "Das löscht Guthaben, Statistiken, Erfolge und Sammlung!", "This deletes balance, statistics, achievements and collection!")
t("luckycasino.command.reset.click", "[Zum Bestätigen klicken]", "[Click to confirm]")
t("luckycasino.command.reset.done", "Spielstand zurückgesetzt. Startguthaben: 1.000 Chips.", "Progress reset. Starting balance: 1,000 chips.")

# Speicherdatei
t("luckycasino.save.restored.title", "LuckyCasino: Sicherung geladen", "LuckyCasino: backup loaded")
t("luckycasino.save.restored", "Die Speicherdatei war beschädigt – die letzte Sicherung wurde geladen.", "The save file was damaged – the last backup was loaded.")
t("luckycasino.save.reset.title", "LuckyCasino: Spielstand beschädigt", "LuckyCasino: save file damaged")
t("luckycasino.save.reset", "Eine Kopie der defekten Datei wurde gesichert. Neuer Spielstand begonnen.", "A copy of the damaged file was kept. Started a new save.")


# ---------------- Version 2.0: neue Spiele & Funktionen
t("luckycasino.game.plinko", "Plinko", "Plinko")
t("luckycasino.game.mines", "Minenfeld", "Mines")
t("luckycasino.menu.missions", "Missionen", "Missions")
t("luckycasino.menu.sub.plinko", "Bis zu 170x", "Up to 170x")
t("luckycasino.menu.sub.mines", "Meide die Minen", "Avoid the mines")
t("luckycasino.menu.sub.loot_buy", "Titel & Rahmen", "Titles & frames")
t("luckycasino.menu.sub.missions", "%s / %s erledigt", "%s / %s done")
t("luckycasino.menu.sub.missions_ready", "Belohnung bereit!", "Reward ready!")
t("luckycasino.level", "Level %s", "Level %s")
t("luckycasino.level.xp", "%s / %s XP · nächstes Level: +%s Chips", "%s / %s XP · next level: +%s chips")
t("luckycasino.toast.level", "Level %s erreicht!", "Reached level %s!")
t("luckycasino.toast.level_reward", "Belohnung: %s Chips", "Reward: %s chips")
t("luckycasino.toast.mission", "Mission erfüllt!", "Mission complete!")
t("luckycasino.toast.mission_claim", "Belohnung unter Missionen abholen", "Claim your reward in Missions")
t("luckycasino.reminder.title", "Zeit für eine Pause?", "Time for a break?")
t("luckycasino.reminder.text", "Du spielst seit %s Minuten im Casino.", "You have been at the casino for %s minutes.")
t("luckycasino.settings.reminder", "Pausen-Erinnerung: %s", "Break reminder: %s")
t("luckycasino.settings.reminder.minutes", "alle %s Min.", "every %s min")
t("luckycasino.settings.reminder.tooltip", "Erinnert dich nach dieser Casino-Spielzeit an eine Pause.", "Reminds you to take a break after this much casino time.")
t("luckycasino.slots.auto", "Auto x%s", "Auto x%s")
t("luckycasino.slots.auto_stop", "Stopp (%s)", "Stop (%s)")
t("luckycasino.stats.level", "Casino-Level", "Casino level")
t("luckycasino.stats.missions", "Missionen erfüllt", "Missions completed")

# Plinko
t("luckycasino.plinko.drop", "Kugel werfen", "Drop ball")
t("luckycasino.plinko.risk", "Risiko: %s", "Risk: %s")
t("luckycasino.plinko.risk.low", "Niedrig", "Low")
t("luckycasino.plinko.risk.medium", "Mittel", "Medium")
t("luckycasino.plinko.risk.high", "Hoch", "High")
t("luckycasino.plinko.chance", "Chance: %s", "Chance: %s")
t("luckycasino.plinko.max", "Höchstes Fach: %s", "Top bucket: %s")
t("luckycasino.plinko.balls", "Kugeln: %s / %s", "Balls: %s / %s")
t("luckycasino.plinko.help", "Leertaste wirft eine Kugel. Mehrere Kugeln gleichzeitig sind erlaubt.",
  "Space drops a ball. Several balls at once are allowed.")

# Minenfeld
t("luckycasino.mines.start", "Start", "Start")
t("luckycasino.mines.pick", "Feld anklicken!", "Click a tile!")
t("luckycasino.mines.cashout", "Auszahlen (%s)", "Cash out (%s)")
t("luckycasino.mines.count", "Anzahl Minen:", "Number of mines:")
t("luckycasino.mines.current", "Aktueller Multiplikator", "Current multiplier")
t("luckycasino.mines.next", "Nächstes Feld", "Next tile")
t("luckycasino.mines.chance", "Chance auf sicher", "Chance of safe")
t("luckycasino.mines.found", "Gefunden", "Found")
t("luckycasino.mines.first", "Nach 1 Feld", "After 1 tile")
t("luckycasino.mines.five", "Nach 5 Feldern", "After 5 tiles")
t("luckycasino.mines.help", "Decke Smaragde auf – jeder erhöht den Gewinn. Triffst du TNT, ist der Einsatz weg. Auszahlen jederzeit möglich.",
  "Reveal emeralds – each one raises your win. Hit TNT and the bet is gone. You can cash out any time.")
t("luckycasino.mines.boom", "BOOM! -%s Chips", "BOOM! -%s chips")

# Missionen
t("luckycasino.missions.today", "Heutige Missionen", "Today's missions")
t("luckycasino.missions.new_in", "Neue in %s", "New in %s")
t("luckycasino.missions.claim", "Abholen", "Claim")
t("luckycasino.missions.claimed", "Abgeholt", "Claimed")
t("luckycasino.missions.reward", "Belohnung: %s", "Reward: %s")
t("luckycasino.missions.total", "Insgesamt erfüllt: %s", "Completed in total: %s")
t("luckycasino.mission.play", "Spiele %s Runden %s", "Play %s rounds of %s")
t("luckycasino.mission.play_any", "Spiele %s Runden (beliebiges Spiel)", "Play %s rounds (any game)")
t("luckycasino.mission.win", "Gewinne %s Runden %s", "Win %s rounds of %s")
t("luckycasino.mission.win_any", "Gewinne %s Runden (beliebiges Spiel)", "Win %s rounds (any game)")
t("luckycasino.mission.wager", "Setze insgesamt %s Chips ein", "Wager %s chips in total")
t("luckycasino.mission.profit", "Erziele %s Chips Reingewinn", "Make %s chips of profit")

for k, dn, dd, en_n, en_d in [
    ("plinko_pro", "Plinko-Profi", "Triff bei Plinko ein Fach mit mindestens 10x.", "Plinko Pro", "Hit a Plinko bucket worth 10x or more."),
    ("minesweeper", "Minenräumer", "Zahle beim Minenfeld mit mindestens 5x aus.", "Minesweeper", "Cash out at 5x or more in Mines."),
    ("level_10", "Stammspieler", "Erreiche Casino-Level 10.", "Seasoned Player", "Reach casino level 10."),
    ("diligent", "Fleißig", "Erfülle 10 Tagesmissionen.", "Diligent", "Complete 10 daily missions."),
]:
    t("luckycasino.achievement." + k, dn, en_n)
    t("luckycasino.achievement." + k + ".desc", dd, en_d)

# ---------------------------------------------------------------- Prüfungen
keys = [k for k, _, _ in T]
dups = {k for k in keys if keys.count(k) > 1}
if dups:
    sys.exit("Doppelte Schlüssel: %s" % dups)

def placeholders(s):
    return len(re.findall(r"%(?:\d+\$)?s", s))

errors = []
for k, de, en in T:
    if placeholders(de) != placeholders(en):
        errors.append("Platzhalter ungleich: " + k)
    for s in (de, en):
        if "%" in re.sub(r"%%|%(?:\d+\$)?s", "", s):
            errors.append("Ungültiges %% in " + k + ": " + s)

static = set()
for dirpath, _, files in os.walk(os.path.join(ROOT, "src/main/java")):
    for fn in files:
        if fn.endswith(".java"):
            code = open(os.path.join(dirpath, fn), encoding="utf-8").read()
            static.update(re.findall(r'"((?:luckycasino|key\.luckycasino|key\.category\.luckycasino)\.[a-z0-9_.]+)"', code))
keyset = set(keys)
for k in static:
    if k.endswith("."):
        if not any(x.startswith(k) for x in keyset):
            errors.append("Kein Schlüssel mit Präfix " + k)
    elif k not in keyset:
        errors.append("Fehlt: " + k)
if errors:
    sys.exit("\n".join(errors))

os.makedirs(LANG_DIR, exist_ok=True)
for idx, name in ((1, "de_de.json"), (2, "en_us.json")):
    data = {row[0]: row[idx] for row in T}
    with open(os.path.join(LANG_DIR, name), "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")
print("OK: %d Schlüssel je Sprache geschrieben" % len(T))

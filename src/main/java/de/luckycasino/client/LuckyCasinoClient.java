package de.luckycasino.client;

import de.luckycasino.client.gui.CasinoMenuScreen;
import de.luckycasino.client.gui.CasinoScreen;
import de.luckycasino.client.gui.Fmt;
import de.luckycasino.client.gui.Icons;
import de.luckycasino.core.progress.Missions;
import de.luckycasino.config.CasinoConfig;
import de.luckycasino.core.Casino;
import de.luckycasino.core.CasinoRng;
import de.luckycasino.core.progress.Achievement;
import de.luckycasino.data.SaveData;
import de.luckycasino.data.SaveManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

/**
 * Einstiegspunkt der rein clientseitigen Mod. Verwaltet Spielstand, Einstellungen,
 * Tastenbelegung, HUD, Chat-Befehle und das automatische Speichern.
 */
public final class LuckyCasinoClient implements ClientModInitializer {

    public static final String MOD_ID = "luckycasino";
    public static final Logger LOGGER = LoggerFactory.getLogger("LuckyCasino");

    /** Mindestabstand zwischen zwei automatischen Speichervorgängen. */
    private static final long AUTOSAVE_INTERVAL_MS = 2_000;

    public static KeyBinding openKey;

    private static SaveManager saveManager;
    private static SaveData data;
    private static Casino casino;
    private static boolean dirty;
    private static long lastSave;
    private static long lastTick;
    private static boolean openRequested;
    private static final Deque<Supplier<Toast>> pendingToasts = new ArrayDeque<>();
    /** Zeit mit geöffnetem Casino in dieser Spielsitzung (für die Pausen-Erinnerung). */
    private static long sessionCasinoMs;
    private static long nextReminderAt;
    private static SaveManager.LoadResult loadResult;
    private static boolean loadNoticeShown;

    @Override
    public void onInitializeClient() {
        saveManager = new SaveManager(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json"));
        loadResult = saveManager.load();
        data = loadResult.data();
        casino = new Casino(data.profile, new CasinoRng(data.settings.luckMode), System::currentTimeMillis);
        casino.addListener(new Casino.Listener() {
            @Override
            public void onAchievement(Achievement achievement) {
                pendingToasts.add(() -> new CasinoToast(achievement));
            }

            @Override
            public void onLevelUp(int level, long reward) {
                pendingToasts.add(() -> new CasinoToast(Icons.of("minecraft:experience_bottle"),
                        Text.translatable("luckycasino.toast.level", level),
                        Text.translatable("luckycasino.toast.level_reward", Fmt.chips(reward)), 0xFF7CFC6A));
            }

            @Override
            public void onMissionComplete(Missions.Def mission) {
                pendingToasts.add(() -> new CasinoToast(Icons.of("minecraft:writable_book"),
                        Text.translatable("luckycasino.toast.mission"),
                        Text.translatable("luckycasino.toast.mission_claim"), 0xFF6FC3FF));
            }

            @Override
            public void onChanged() {
                dirty = true;
            }
        });
        LOGGER.info("[LuckyCasino] Spielstand {} ({}), Guthaben {} Chips",
                loadResult.status(), saveManager.file(), casino.balance());

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.luckycasino.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, category));

        ClientTickEvents.END_CLIENT_TICK.register(LuckyCasinoClient::tick);
        ClientCommandRegistrationCallback.EVENT.register(CasinoCommands::register);
        HudElementRegistry.addLast(Identifier.of(MOD_ID, "balance"), CasinoHud::render);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (client.currentScreen instanceof CasinoScreen screen) {
                screen.finishPendingRounds();
            }
            ClientSession.finishAll(casino);
            saveNow();
        });
    }

    private static void tick(MinecraftClient client) {
        long now = Util.getMeasuringTimeMs();
        if (client.currentScreen instanceof CasinoScreen && lastTick > 0) {
            long delta = Math.min(now - lastTick, 1_000);
            casino.addPlayTime(delta);
            sessionCasinoMs += delta;
            checkReminder();
        }
        lastTick = now;

        while (openKey.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new CasinoMenuScreen(null));
            }
        }
        // Befehl "/casino open": erst im nächsten Tick öffnen, weil der Chat sich sonst gleich wieder schließt
        if (openRequested) {
            openRequested = false;
            client.setScreen(new CasinoMenuScreen(null));
        }

        boolean animating = client.currentScreen instanceof CasinoScreen screen && screen.isAnimating();
        if (!animating && client.getOverlay() == null) {
            while (!pendingToasts.isEmpty()) {
                client.getToastManager().add(pendingToasts.poll().get());
            }
            showLoadNotice(client);
        }

        if (dirty && now - lastSave >= AUTOSAVE_INTERVAL_MS) {
            saveNow();
        }
    }

    /** Pausen-Erinnerung nach der eingestellten Casino-Spielzeit. */
    private static void checkReminder() {
        int minutes = data.settings.reminderMinutes;
        if (minutes <= 0) {
            nextReminderAt = 0;
            return;
        }
        long interval = minutes * 60_000L;
        if (nextReminderAt <= 0) {
            nextReminderAt = (sessionCasinoMs / interval + 1) * interval;
        }
        if (sessionCasinoMs >= nextReminderAt) {
            nextReminderAt += interval;
            long played = sessionCasinoMs / 60_000L;
            pendingToasts.add(() -> new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION,
                    Text.translatable("luckycasino.reminder.title"), Text.translatable("luckycasino.reminder.text", played)));
        }
    }

    /** Hinweis, falls der Spielstand beschädigt war und wiederhergestellt wurde. */
    private static void showLoadNotice(MinecraftClient client) {
        if (loadNoticeShown || loadResult == null) {
            return;
        }
        loadNoticeShown = true;
        switch (loadResult.status()) {
            case RESTORED_FROM_BACKUP -> SystemToast.add(client.getToastManager(), SystemToast.Type.PERIODIC_NOTIFICATION,
                    Text.translatable("luckycasino.save.restored.title"), Text.translatable("luckycasino.save.restored"));
            case RESET_AFTER_CORRUPTION -> SystemToast.add(client.getToastManager(), SystemToast.Type.PERIODIC_NOTIFICATION,
                    Text.translatable("luckycasino.save.reset.title"), Text.translatable("luckycasino.save.reset"));
            default -> {
            }
        }
    }

    public static Casino casino() {
        return casino;
    }

    public static CasinoConfig config() {
        return data.settings;
    }

    /** Einstellungen wurden geändert. */
    public static void markDirty() {
        dirty = true;
    }

    public static void requestOpen() {
        openRequested = true;
    }

    public static void saveNow() {
        if (saveManager == null) {
            return;
        }
        data.profile = casino.profile();
        data.settings.luckMode = casino.mode();
        saveManager.save(data);
        dirty = false;
        lastSave = Util.getMeasuringTimeMs();
    }
}

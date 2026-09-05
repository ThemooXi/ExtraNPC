package me.themoo.extranpc.integration;

import me.themoo.extranpc.ExtraNPCPlugin;

import java.lang.reflect.Constructor;
import java.util.logging.Level;

/**
 * Loads Mannequin player NPC backend reflectively on supported Paper versions.
 * Player NPCs always use the real player model (Mannequin) — never ArmorStand.
 */
public final class PlayerNpcProviderFactory {

    private static final String MANNEQUIN_PROVIDER =
            "me.themoo.extranpc.integration.mannequin.MannequinPlayerNpcProvider";

    private PlayerNpcProviderFactory() {
    }

    public static PlayerNpcProvider create(ExtraNPCPlugin plugin) {
        if (hasMannequin()) {
            try {
                Class<?> providerClass = Class.forName(MANNEQUIN_PROVIDER);
                Constructor<?> constructor = providerClass.getConstructor(ExtraNPCPlugin.class);
                PlayerNpcProvider provider = (PlayerNpcProvider) constructor.newInstance(plugin);
                if (provider.isAvailable()) {
                    plugin.getLogger().info("Player NPC engine: Mannequin (real player model).");
                    return provider;
                }
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.SEVERE,
                        "Mannequin provider failed to load.", ex);
            }
        } else {
            plugin.getLogger().warning(
                    "Mannequin not found — player NPCs require Paper 1.21.9+ or 26.1+. Mob NPCs still work.");
        }
        return new UnavailablePlayerNpcProvider(plugin);
    }

    private static boolean hasMannequin() {
        try {
            Class.forName("org.bukkit.entity.Mannequin");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}

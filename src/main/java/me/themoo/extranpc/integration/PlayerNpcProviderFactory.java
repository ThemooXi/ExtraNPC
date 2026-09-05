package me.themoo.extranpc.integration;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.integration.fallback.ArmorStandPlayerNpcProvider;

import java.lang.reflect.Constructor;
import java.util.logging.Level;

/**
 * Selects the best player NPC backend for the running server version.
 * Mannequin (Paper 1.21.9+ / 26.1+) is preferred; ArmorStand is used as fallback.
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
                plugin.getLogger().log(Level.WARNING,
                        "Mannequin provider unavailable, falling back to ArmorStand.", ex);
            }
        } else {
            plugin.getLogger().info("Mannequin not found on this server — using ArmorStand fallback for player NPCs.");
        }
        return new ArmorStandPlayerNpcProvider(plugin);
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

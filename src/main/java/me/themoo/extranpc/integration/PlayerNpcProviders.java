package me.themoo.extranpc.integration;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.compat.ServerCompat;

/**
 * Picks the best player-NPC backend for the running Paper build.
 * Mannequin is used on 1.21.9+ / 26.1 / 26.2; ArmorStand is the fallback.
 */
public final class PlayerNpcProviders {

    private PlayerNpcProviders() {
    }

    public static PlayerNpcProvider create(ExtraNPCPlugin plugin) {
        if (ServerCompat.hasMannequin()) {
            try {
                PlayerNpcProvider nativeProvider = new NativePlayerNpcProvider(plugin);
                if (nativeProvider.isAvailable()) {
                    return nativeProvider;
                }
            } catch (Throwable ex) {
                plugin.getLogger().warning("Mannequin backend unavailable (" + ex.getClass().getSimpleName()
                        + ": " + ex.getMessage() + ") — using ArmorStand fallback.");
            }
        } else {
            plugin.getLogger().info("Mannequin entity not present on this server — using ArmorStand player NPCs.");
        }
        return new ArmorStandPlayerNpcProvider(plugin);
    }
}

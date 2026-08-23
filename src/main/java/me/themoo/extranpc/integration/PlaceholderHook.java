package me.themoo.extranpc.integration;

import me.clip.placeholderapi.PlaceholderAPI;
import me.themoo.extranpc.ExtraNPCPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlaceholderHook {

    private final ExtraNPCPlugin plugin;
    private boolean enabled;

    public PlaceholderHook(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        enabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (enabled) {
            plugin.getLogger().info("Hooked into PlaceholderAPI.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String apply(Player player, String input) {
        if (input == null) {
            return "";
        }
        String value = input
                .replace("{player}", player.getName())
                .replace("{uuid}", player.getUniqueId().toString())
                .replace("%player%", player.getName())
                .replace("%player_name%", player.getName());
        if (enabled) {
            value = PlaceholderAPI.setPlaceholders(player, value);
        }
        return value;
    }
}

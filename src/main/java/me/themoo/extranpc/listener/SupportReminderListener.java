package me.themoo.extranpc.listener;

import me.themoo.extranpc.ExtraNPCPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Sends a one-time (or periodic) Discord welcome reminder to server operators.
 */
public final class SupportReminderListener implements Listener {

    private final ExtraNPCPlugin plugin;
    private final File dataFile;
    private FileConfiguration data;

    public SupportReminderListener(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "support-reminder.yml");
        reload();
    }

    public void reload() {
        if (!dataFile.exists()) {
            plugin.saveResource("support-reminder.yml", false);
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("settings.support-reminder.enabled", true)) {
            return;
        }
        if (!plugin.getConfig().getBoolean("settings.support-reminder.welcome-op-on-join", true)) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.isOp()) {
            return;
        }

        long now = System.currentTimeMillis();
        String path = "players." + player.getUniqueId();
        long last = data.getLong(path, 0L);
        int repeatDays = plugin.getConfig().getInt("settings.support-reminder.repeat-days", 7);

        if (last > 0L && repeatDays <= 0) {
            return;
        }
        if (last > 0L && repeatDays > 0) {
            long elapsed = now - last;
            if (elapsed < TimeUnit.DAYS.toMillis(repeatDays)) {
                return;
            }
        }

        data.set(path, now);
        saveQuietly();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            plugin.getMessages().sendWelcome(player);
        }, 40L);
    }

    private void saveQuietly() {
        try {
            data.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save support-reminder.yml: " + exception.getMessage());
        }
    }
}

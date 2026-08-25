package me.themoo.extranpc.util;

import me.themoo.extranpc.ExtraNPCPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SimpleUpdateChecker implements Listener {

    private static final Pattern VERSION_NAME =
            Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");

    private final ExtraNPCPlugin plugin;
    private final Logger logger;
    private final String currentVersion;
    private String latestVersion;
    private boolean updateAvailable;

    public SimpleUpdateChecker(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.currentVersion = plugin.getDescription().getVersion();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void checkForUpdates() {
        if (!plugin.getConfig().getBoolean("update-checker.enabled", true)) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                boolean success = fetchLatestVersion();
                if (!plugin.getConfig().getBoolean("update-checker.notify-console", true)) {
                    return;
                }
                if (updateAvailable) {
                    logUpdateAvailable();
                } else if (success) {
                    logLatestVersion();
                }
            } catch (Exception ignored) {
            }
        });
    }

    public void manualCheck(CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = fetchLatestVersion();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (updateAvailable) {
                    plugin.getMessages().sendUpdateAvailable(sender, currentVersion, latestVersion);
                } else if (success) {
                    plugin.getMessages().sendUpdateLatest(sender, currentVersion);
                } else {
                    plugin.getMessages().send(sender, "update-check-failed");
                }
            });
        });
    }

    private boolean fetchLatestVersion() {
        try {
            String api = "https://api.spiget.org/v2/resources/"
                    + SupportLinks.SPIGOT_RESOURCE_ID + "/versions?size=1&sort=-releaseDate";
            URLConnection connection = URI.create(api).toURL().openConnection();
            connection.setRequestProperty("User-Agent", "ExtraNPC-UpdateChecker");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            Matcher matcher = VERSION_NAME.matcher(response);
            if (!matcher.find()) {
                return false;
            }

            String cleaned = extractVersionNumber(matcher.group(1));
            if (cleaned.isEmpty()) {
                return false;
            }

            latestVersion = cleaned;
            updateAvailable = isNewerVersion(latestVersion, currentVersion);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private String extractVersionNumber(String versionName) {
        if (versionName == null) {
            return "";
        }
        String cleaned = versionName.replaceAll("[^0-9.]", "").trim();
        cleaned = cleaned.replaceAll("^\\.+|\\.+$", "");
        return cleaned.matches(".*\\d+.*") ? cleaned : "";
    }

    private boolean isNewerVersion(String newVersion, String current) {
        try {
            String[] newParts = newVersion.split("\\.");
            String[] currentParts = current.split("\\.");
            for (int index = 0; index < Math.max(newParts.length, currentParts.length); index++) {
                int newPart = index < newParts.length && !newParts[index].isEmpty()
                        ? Integer.parseInt(newParts[index]) : 0;
                int currentPart = index < currentParts.length && !currentParts[index].isEmpty()
                        ? Integer.parseInt(currentParts[index]) : 0;
                if (newPart > currentPart) {
                    return true;
                }
                if (newPart < currentPart) {
                    return false;
                }
            }
            return false;
        } catch (Exception exception) {
            return false;
        }
    }

    private void logUpdateAvailable() {
        logger.info("========================================");
        logger.info("[ExtraNPC] A new update is available!");
        logger.info("[ExtraNPC] Current: " + currentVersion + "  Latest: " + latestVersion);
        logger.info("[ExtraNPC] Download: " + SupportLinks.SPIGOT_URL);
        logger.info("[ExtraNPC] Support & updates: " + SupportLinks.DISCORD_INVITE);
        logger.info("========================================");
    }

    private void logLatestVersion() {
        logger.info("========================================");
        logger.info("[ExtraNPC] Running latest version: " + currentVersion);
        logger.info("[ExtraNPC] Join Discord for support & previews: " + SupportLinks.DISCORD_INVITE);
        logger.info("========================================");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!updateAvailable) {
            return;
        }
        if (!plugin.getConfig().getBoolean("update-checker.notify-staff", true)) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("extranpc.admin")) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin,
                () -> plugin.getMessages().sendUpdateAvailable(player, currentVersion, latestVersion), 60L);
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }
}

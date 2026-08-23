package me.themoo.extranpc.storage;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MessageService {

    private static final Set<String> NO_PREFIX_KEYS = Set.of("help", "about");

    private final ExtraNPCPlugin plugin;
    private FileConfiguration messages;

    public MessageService(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);

        // Upgrade older installs to the new professional message pack
        if (!messages.contains("about") || !messages.contains("npc-engine-error") || !messages.isList("help")) {
            plugin.saveResource("messages.yml", true);
            messages = YamlConfiguration.loadConfiguration(file);
        }
    }

    public String raw(String key) {
        return messages.getString(key, key);
    }

    public Component component(String key, Map<String, String> placeholders) {
        String value = applyPlaceholders(raw(key), placeholders);
        if (!NO_PREFIX_KEYS.contains(key)) {
            String prefix = plugin.getConfig().getString("settings.prefix", "");
            value = prefix + value;
        }
        return TextUtil.parse(value);
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Collections.emptyMap());
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(component(key, placeholders));
    }

    public void sendHelp(CommandSender sender) {
        sendList(sender, "help", Collections.emptyMap());
    }

    public void sendAbout(CommandSender sender) {
        sendList(sender, "about", Collections.emptyMap());
    }

    public void sendList(CommandSender sender, String key, Map<String, String> placeholders) {
        List<String> lines = messages.getStringList(key);
        if (lines.isEmpty()) {
            sender.sendMessage(component(key, placeholders));
            return;
        }
        for (String line : lines) {
            sender.sendMessage(TextUtil.parse(applyPlaceholders(line, placeholders)));
        }
    }

    private String applyPlaceholders(String value, Map<String, String> placeholders) {
        if (value == null) {
            return "";
        }
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                value = value.replace("{" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
            }
        }
        return value;
    }

    public static Map<String, String> map(String... keyValues) {
        Map<String, String> map = new HashMap<>();
        if (keyValues == null) {
            return map;
        }
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}

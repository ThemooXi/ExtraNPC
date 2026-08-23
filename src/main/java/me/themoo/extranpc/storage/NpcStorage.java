package me.themoo.extranpc.storage;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.NpcData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class NpcStorage {

    private final ExtraNPCPlugin plugin;
    private File file;
    private FileConfiguration config;

    public NpcStorage(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
        reloadFile();
    }

    public void reloadFile() {
        String name = plugin.getConfig().getString("storage.file", "npcs.yml");
        file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create npcs.yml", e);
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public List<NpcData> loadAll() {
        reloadFile();
        List<NpcData> list = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("npcs");
        if (section == null) {
            return list;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection npcSection = section.getConfigurationSection(id);
            if (npcSection != null) {
                list.add(NpcData.load(id, npcSection));
            }
        }
        return list;
    }

    public void saveAll(Iterable<NpcData> npcs) {
        config.set("npcs", null);
        for (NpcData npc : npcs) {
            npc.save(config);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save npcs.yml", e);
        }
    }
}

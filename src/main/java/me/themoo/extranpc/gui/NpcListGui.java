package me.themoo.extranpc.gui;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.util.ItemBuilder;
import me.themoo.extranpc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class NpcListGui {

    private final ExtraNPCPlugin plugin;
    private final Player player;

    public NpcListGui(ExtraNPCPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        GuiHolder holder = new GuiHolder(GuiHolder.GuiType.LIST, null);
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.parse("<aqua><bold>NPC List</bold></aqua>"));
        holder.setInventory(inv);

        List<NpcData> list = new ArrayList<>(plugin.getNpcManager().getNpcs());
        list.sort(Comparator.comparing(NpcData::getId));

        int slot = 0;
        for (NpcData data : list) {
            if (slot >= 45) {
                break;
            }
            ItemStack item = new ItemBuilder(data.getType().isPlayerLike() ? Material.PLAYER_HEAD : Material.NAME_TAG)
                    .name("<yellow>" + data.getId() + "</yellow>")
                    .loreMini(
                            "<gray>Name: </gray>" + data.getDisplayName(),
                            "<gray>Type: <white>" + data.getType().name() + "</white></gray>",
                            "<gray>World: <white>" + data.getWorldName() + "</white></gray>",
                            "",
                            "<green>Left-click: edit</green>",
                            "<gold>Right-click: select</gold>",
                            "<red>Shift-click: teleport</red>"
                    ).build();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "list-npc"), PersistentDataType.STRING, data.getId());
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        inv.setItem(49, new ItemBuilder(Material.ARROW).name("<gray>Back</gray>").build());
        player.openInventory(inv);
    }
}

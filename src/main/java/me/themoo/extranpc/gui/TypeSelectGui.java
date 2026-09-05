package me.themoo.extranpc.gui;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.model.NpcType;
import me.themoo.extranpc.util.ItemBuilder;
import me.themoo.extranpc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class TypeSelectGui {

    private final ExtraNPCPlugin plugin;
    private final Player player;
    private final NpcData data;

    public TypeSelectGui(ExtraNPCPlugin plugin, Player player, NpcData data) {
        this.plugin = plugin;
        this.player = player;
        this.data = data;
    }

    public void open() {
        GuiHolder holder = new GuiHolder(GuiHolder.GuiType.TYPE_SELECT, data.getId());
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.parse("<aqua>Select Type</aqua>"));
        holder.setInventory(inv);

        int slot = 0;
        for (NpcType type : NpcType.values()) {
            if (slot >= 45) {
                break;
            }
            if (!type.isAvailable()) {
                continue;
            }
            Material icon = type.icon();
            ItemStack item = new ItemBuilder(icon)
                    .name((data.getType() == type ? "<green>" : "<white>") + type.name() + "</white>")
                    .loreMini(data.getType() == type ? "<gray>Selected</gray>" : "<yellow>Click to select</yellow>")
                    .build();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "npc-type"), PersistentDataType.STRING, type.name());
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }
        inv.setItem(49, new ItemBuilder(Material.ARROW).name("<gray>Back</gray>").build());
        player.openInventory(inv);
    }
}

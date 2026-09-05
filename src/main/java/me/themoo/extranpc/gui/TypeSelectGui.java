package me.themoo.extranpc.gui;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.model.NpcType;
import me.themoo.extranpc.util.ItemBuilder;
import me.themoo.extranpc.util.ServerCompatibility;
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
            if (!type.isSupported()) {
                continue;
            }
            if (slot >= 45) {
                break;
            }
            Material icon = switch (type) {
                case PLAYER -> Material.PLAYER_HEAD;
                case VILLAGER -> Material.EMERALD;
                case COW -> Material.BEEF;
                case PIG -> Material.PORKCHOP;
                case SHEEP -> Material.WHITE_WOOL;
                case CHICKEN -> Material.EGG;
                case WOLF -> Material.BONE;
                case CAT -> Material.COD;
                case HORSE -> Material.SADDLE;
                case FOX -> Material.SWEET_BERRIES;
                case RABBIT -> Material.RABBIT_FOOT;
                case BEE -> Material.HONEYCOMB;
                case PANDA -> Material.BAMBOO;
                case IRON_GOLEM -> Material.IRON_BLOCK;
                case SNOW_GOLEM -> Material.SNOWBALL;
                case ZOMBIE -> Material.ROTTEN_FLESH;
                case SKELETON -> Material.BONE;
                case CREEPER -> Material.GUNPOWDER;
                case ENDERMAN -> Material.ENDER_PEARL;
                case BLAZE -> Material.BLAZE_ROD;
                case WITCH -> Material.GLASS_BOTTLE;
                case PILLAGER -> Material.CROSSBOW;
                case ALLAY -> Material.AMETHYST_SHARD;
                case CAMEL -> Material.CACTUS;
                case SNIFFER -> ServerCompatibility.material("TORCHFLOWER_SEEDS", Material.WHEAT_SEEDS);
                case ARMADILLO -> ServerCompatibility.material("ARMADILLO_SCUTE", Material.SCUTE);
            };
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

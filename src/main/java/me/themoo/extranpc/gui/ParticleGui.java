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

import java.util.List;

public final class ParticleGui {

    private static final List<String> PARTICLES = List.of(
            "NONE", "HEART", "FLAME", "HAPPY_VILLAGER", "NOTE", "CLOUD",
            "PORTAL", "ENCHANT", "CRIT", "SMOKE", "END_ROD", "SOUL_FIRE_FLAME"
    );

    private final ExtraNPCPlugin plugin;
    private final Player player;
    private final NpcData data;

    public ParticleGui(ExtraNPCPlugin plugin, Player player, NpcData data) {
        this.plugin = plugin;
        this.player = player;
        this.data = data;
    }

    public void open() {
        GuiHolder holder = new GuiHolder(GuiHolder.GuiType.PARTICLES, data.getId());
        Inventory inv = Bukkit.createInventory(holder, 27, TextUtil.parse("<gold>Particles</gold>"));
        holder.setInventory(inv);

        int slot = 0;
        for (String particle : PARTICLES) {
            boolean selected = data.getParticle().equalsIgnoreCase(particle);
            ItemStack item = new ItemBuilder(selected ? Material.LIME_DYE : Material.GRAY_DYE)
                    .name((selected ? "<green>" : "<white>") + particle + "</white>")
                    .build();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "particle"), PersistentDataType.STRING, particle);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }
        inv.setItem(22, new ItemBuilder(Material.ARROW).name("<gray>Back</gray>").build());
        player.openInventory(inv);
    }
}

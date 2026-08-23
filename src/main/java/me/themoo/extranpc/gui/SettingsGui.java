package me.themoo.extranpc.gui;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.util.ItemBuilder;
import me.themoo.extranpc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class SettingsGui {

    private final ExtraNPCPlugin plugin;
    private final Player player;
    private final NpcData data;

    public SettingsGui(ExtraNPCPlugin plugin, Player player, NpcData data) {
        this.plugin = plugin;
        this.player = player;
        this.data = data;
    }

    public void open() {
        GuiHolder holder = new GuiHolder(GuiHolder.GuiType.SETTINGS, data.getId());
        Inventory inv = Bukkit.createInventory(holder, 45, TextUtil.parse("<red>NPC Settings</red>"));
        holder.setInventory(inv);

        inv.setItem(10, toggle(Material.ENDER_EYE, "Look at Players", data.isLookAtPlayers()));
        inv.setItem(11, toggle(Material.SHIELD, "Invulnerable", data.isInvulnerable()));
        inv.setItem(12, toggle(Material.FEATHER, "Gravity", data.isGravity()));
        inv.setItem(13, toggle(Material.NOTE_BLOCK, "Silent", data.isSilent()));
        inv.setItem(14, toggle(Material.GLOWSTONE_DUST, "Glowing", data.isGlowing()));
        inv.setItem(15, toggle(Material.SLIME_BALL, "Collidable", data.isCollidable()));
        inv.setItem(16, toggle(Material.EGG, "Baby", data.isBaby()));

        inv.setItem(19, new ItemBuilder(Material.CLOCK)
                .name("<yellow>Cooldown: <white>" + data.getCooldownSeconds() + "s</white></yellow>")
                .loreMini("<green>Left: +1</green>", "<red>Right: -1</red>", "<gold>Shift: +/-5</gold>")
                .build());

        inv.setItem(21, new ItemBuilder(Material.SPYGLASS)
                .name("<aqua>Look Range: <white>" + data.getLookRange() + "</white></aqua>")
                .loreMini("<green>Left: +1</green>", "<red>Right: -1</red>")
                .build());

        inv.setItem(40, new ItemBuilder(Material.ARROW).name("<gray>Back</gray>").build());
        player.openInventory(inv);
    }

    private org.bukkit.inventory.ItemStack toggle(Material material, String name, boolean enabled) {
        return new ItemBuilder(material)
                .name("<white>" + name + ": " + (enabled ? "<green>ON" : "<red>OFF") + "</white>")
                .loreMini("<yellow>Click to toggle</yellow>")
                .build();
    }

    public static void handle(ExtraNPCPlugin plugin, Player player, NpcData data, int slot, boolean left, boolean shift) {
        boolean changed = true;
        switch (slot) {
            case 10 -> data.setLookAtPlayers(!data.isLookAtPlayers());
            case 11 -> data.setInvulnerable(!data.isInvulnerable());
            case 12 -> data.setGravity(!data.isGravity());
            case 13 -> data.setSilent(!data.isSilent());
            case 14 -> data.setGlowing(!data.isGlowing());
            case 15 -> data.setCollidable(!data.isCollidable());
            case 16 -> data.setBaby(!data.isBaby());
            case 19 -> {
                int delta = shift ? 5 : 1;
                if (!left) {
                    delta = -delta;
                }
                data.setCooldownSeconds(data.getCooldownSeconds() + delta);
            }
            case 21 -> data.setLookRange(data.getLookRange() + (left ? 1.0 : -1.0));
            case 40 -> {
                new NpcEditGui(plugin, player, data).open();
                return;
            }
            default -> changed = false;
        }
        if (changed) {
            plugin.getNpcManager().respawn(data);
            new SettingsGui(plugin, player, data).open();
        }
    }
}

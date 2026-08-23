package me.themoo.extranpc.gui;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.listener.ChatInputListener;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.model.SkinData;
import me.themoo.extranpc.util.ItemBuilder;
import me.themoo.extranpc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class SkinGui {

    private final ExtraNPCPlugin plugin;
    private final Player player;
    private final NpcData data;

    public SkinGui(ExtraNPCPlugin plugin, Player player, NpcData data) {
        this.plugin = plugin;
        this.player = player;
        this.data = data;
    }

    public void open() {
        GuiHolder holder = new GuiHolder(GuiHolder.GuiType.SKIN, data.getId());
        Inventory inv = Bukkit.createInventory(holder, 27, TextUtil.parse("<light_purple>Skin Settings</light_purple>"));
        holder.setInventory(inv);

        inv.setItem(11, new ItemBuilder(Material.PLAYER_HEAD)
                .name("<green>From Player Name</green>")
                .loreMini("<gray>Fetch skin from Mojang by username</gray>")
                .build());

        inv.setItem(13, new ItemBuilder(Material.PAINTING)
                .name("<aqua>From Texture URL</aqua>")
                .loreMini("<gray>Use a direct .png skin URL</gray>", "<dark_gray>textures.minecraft.net recommended</dark_gray>")
                .build());

        inv.setItem(15, new ItemBuilder(Material.BARRIER)
                .name("<red>Clear Skin</red>")
                .loreMini("<gray>Remove custom skin data</gray>")
                .build());

        inv.setItem(22, new ItemBuilder(Material.ARROW).name("<gray>Back</gray>").build());
        player.openInventory(inv);
    }

    public static void handle(ExtraNPCPlugin plugin, Player player, NpcData data, int slot) {
        switch (slot) {
            case 11 -> {
                player.closeInventory();
                plugin.getChatInputListener().request(player, ChatInputListener.InputType.SKIN_NAME, data.getId(), null);
            }
            case 13 -> {
                player.closeInventory();
                plugin.getChatInputListener().request(player, ChatInputListener.InputType.SKIN_URL, data.getId(), null);
            }
            case 15 -> {
                data.setSkin(new SkinData());
                plugin.getNpcManager().respawn(data);
                plugin.getMessages().send(player, "saved");
                new NpcEditGui(plugin, player, data).open();
            }
            case 22 -> new NpcEditGui(plugin, player, data).open();
            default -> {
            }
        }
    }
}

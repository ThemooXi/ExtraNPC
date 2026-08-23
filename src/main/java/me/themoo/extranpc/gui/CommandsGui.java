package me.themoo.extranpc.gui;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.listener.ChatInputListener;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.util.ItemBuilder;
import me.themoo.extranpc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class CommandsGui {

    private final ExtraNPCPlugin plugin;
    private final Player player;
    private final NpcData data;

    public CommandsGui(ExtraNPCPlugin plugin, Player player, NpcData data) {
        this.plugin = plugin;
        this.player = player;
        this.data = data;
    }

    public void open() {
        GuiHolder holder = new GuiHolder(GuiHolder.GuiType.COMMANDS, data.getId());
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.parse("<yellow>NPC Commands</yellow>"));
        holder.setInventory(inv);

        inv.setItem(11, new ItemBuilder(Material.WOODEN_SWORD)
                .name("<red>Add Left-Click Command</red>")
                .loreMini(
                        "<gray>Prefixes:</gray>",
                        "<white>player:</white> <gray>run as player</gray>",
                        "<white>console:</white> <gray>run as console</gray>",
                        "<white>message:</white> <gray>send message</gray>",
                        "<gray>Supports PlaceholderAPI & {player}</gray>"
                ).build());

        inv.setItem(15, new ItemBuilder(Material.BLAZE_ROD)
                .name("<green>Add Right-Click Command</green>")
                .loreMini("<gray>Same prefixes as left-click</gray>")
                .build());

        int slot = 27;
        for (String cmd : data.getLeftCommands()) {
            if (slot >= 36) break;
            inv.setItem(slot++, new ItemBuilder(Material.PAPER)
                    .name("<red>L: </red><white>" + cmd + "</white>")
                    .loreMini("<red>Click to remove</red>")
                    .build());
        }
        slot = 36;
        for (String cmd : data.getRightCommands()) {
            if (slot >= 45) break;
            inv.setItem(slot++, new ItemBuilder(Material.PAPER)
                    .name("<green>R: </green><white>" + cmd + "</white>")
                    .loreMini("<red>Click to remove</red>")
                    .build());
        }

        inv.setItem(48, new ItemBuilder(Material.TNT)
                .name("<red>Clear Left Commands</red>")
                .build());
        inv.setItem(50, new ItemBuilder(Material.TNT)
                .name("<red>Clear Right Commands</red>")
                .build());
        inv.setItem(49, new ItemBuilder(Material.ARROW).name("<gray>Back</gray>").build());
        player.openInventory(inv);
    }

    public static void handle(ExtraNPCPlugin plugin, Player player, NpcData data, int slot) {
        if (slot == 11) {
            player.closeInventory();
            plugin.getChatInputListener().request(player, ChatInputListener.InputType.ADD_LEFT_COMMAND, data.getId(), null);
            return;
        }
        if (slot == 15) {
            player.closeInventory();
            plugin.getChatInputListener().request(player, ChatInputListener.InputType.ADD_RIGHT_COMMAND, data.getId(), null);
            return;
        }
        if (slot == 48) {
            data.getLeftCommands().clear();
            plugin.getNpcManager().save(data);
            new CommandsGui(plugin, player, data).open();
            return;
        }
        if (slot == 50) {
            data.getRightCommands().clear();
            plugin.getNpcManager().save(data);
            new CommandsGui(plugin, player, data).open();
            return;
        }
        if (slot == 49) {
            new NpcEditGui(plugin, player, data).open();
            return;
        }
        if (slot >= 27 && slot < 36) {
            int index = slot - 27;
            if (index < data.getLeftCommands().size()) {
                data.getLeftCommands().remove(index);
                plugin.getNpcManager().save(data);
                new CommandsGui(plugin, player, data).open();
            }
            return;
        }
        if (slot >= 36 && slot < 45) {
            int index = slot - 36;
            if (index < data.getRightCommands().size()) {
                data.getRightCommands().remove(index);
                plugin.getNpcManager().save(data);
                new CommandsGui(plugin, player, data).open();
            }
        }
    }
}

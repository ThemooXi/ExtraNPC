package me.themoo.extranpc.gui;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.listener.ChatInputListener;
import me.themoo.extranpc.model.NpcType;
import me.themoo.extranpc.util.ItemBuilder;
import me.themoo.extranpc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class MainMenuGui {

    private final ExtraNPCPlugin plugin;
    private final Player player;

    public MainMenuGui(ExtraNPCPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        GuiHolder holder = new GuiHolder(GuiHolder.GuiType.MAIN, null);
        Inventory inv = Bukkit.createInventory(holder, 27, TextUtil.parse("<gradient:#00E5A8:#00B4D8><bold>ExtraNPC</bold></gradient>"));
        holder.setInventory(inv);

        inv.setItem(11, new ItemBuilder(Material.EMERALD)
                .name("<green><bold>Create NPC</bold></green>")
                .loreMini("<gray>Create a new NPC at your location</gray>", "<yellow>Click to start</yellow>")
                .build());

        inv.setItem(13, new ItemBuilder(Material.BOOK)
                .name("<aqua><bold>NPC List</bold></aqua>")
                .loreMini("<gray>Browse and edit all NPCs</gray>",
                        "<white>Loaded: <yellow>" + plugin.getNpcManager().getNpcs().size() + "</yellow></white>")
                .build());

        inv.setItem(15, new ItemBuilder(Material.COMPASS)
                .name("<gold><bold>Move Selected</bold></gold>")
                .loreMini("<gray>Move your selected NPC to you</gray>",
                        "<yellow>/extranpc select <id></yellow>")
                .build());

        inv.setItem(22, new ItemBuilder(Material.REDSTONE)
                .name("<red><bold>Reload</bold></red>")
                .loreMini("<gray>Reload config and NPCs</gray>")
                .build());

        player.openInventory(inv);
    }

    public static void handle(ExtraNPCPlugin plugin, Player player, int slot) {
        switch (slot) {
            case 11 -> plugin.getChatInputListener().request(player, ChatInputListener.InputType.CREATE_ID, null, (p, id) -> {
                String clean = id.toLowerCase().replaceAll("[^a-z0-9_\\-]", "");
                if (clean.isBlank()) {
                    p.sendMessage(TextUtil.parse("<red>Invalid id.</red>"));
                    return;
                }
                if (plugin.getNpcManager().exists(clean)) {
                    plugin.getMessages().send(p, "npc-exists", me.themoo.extranpc.storage.MessageService.map("id", clean));
                    return;
                }
                if (!plugin.getNpcManager().canCreatePlayerNpc()) {
                    plugin.getMessages().send(p, "npc-engine-error");
                    return;
                }
                var data = plugin.getNpcManager().create(clean, NpcType.PLAYER, p.getLocation());
                plugin.getMessages().send(p, "npc-created", me.themoo.extranpc.storage.MessageService.map("id", clean));
                new NpcEditGui(plugin, p, data).open();
            });
            case 13 -> new NpcListGui(plugin, player).open();
            case 15 -> plugin.getNpcManager().getSelected(player).ifPresentOrElse(id -> {
                plugin.getNpcManager().get(id).ifPresent(data -> {
                    plugin.getNpcManager().move(data, player.getLocation());
                    plugin.getMessages().send(player, "npc-moved", me.themoo.extranpc.storage.MessageService.map("id", id));
                });
            }, () -> player.sendMessage(TextUtil.parse("<red>No NPC selected. Use /extranpc select <id></red>")));
            case 22 -> {
                plugin.reloadAll();
                plugin.getMessages().send(player, "reload");
            }
            default -> {
            }
        }
    }
}

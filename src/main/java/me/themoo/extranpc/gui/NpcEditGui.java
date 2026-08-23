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

public final class NpcEditGui {

    private final ExtraNPCPlugin plugin;
    private final Player player;
    private final NpcData data;

    public NpcEditGui(ExtraNPCPlugin plugin, Player player, NpcData data) {
        this.plugin = plugin;
        this.player = player;
        this.data = data;
    }

    public void open() {
        GuiHolder holder = new GuiHolder(GuiHolder.GuiType.EDIT, data.getId());
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.parse("<yellow>Edit: " + data.getId() + "</yellow>"));
        holder.setInventory(inv);

        inv.setItem(10, new ItemBuilder(Material.NAME_TAG)
                .name("<green>Display Name</green>")
                .loreMini("<gray>Current:</gray> " + data.getDisplayName(), "<yellow>Click to change</yellow>")
                .build());

        inv.setItem(11, new ItemBuilder(Material.ARMOR_STAND)
                .name("<aqua>NPC Type</aqua>")
                .loreMini("<gray>Current: <white>" + data.getType().name() + "</white></gray>", "<yellow>Click to change</yellow>")
                .build());

        inv.setItem(12, new ItemBuilder(Material.PLAYER_HEAD)
                .name("<light_purple>Skin</light_purple>")
                .loreMini(
                        "<gray>Mode: <white>" + data.getSkin().getMode() + "</white></gray>",
                        "<gray>Value: <white>" + (data.getSkin().getValue().isBlank() ? "none" : data.getSkin().getValue()) + "</white></gray>",
                        "<yellow>Click to open skin menu</yellow>"
                ).build());

        inv.setItem(13, new ItemBuilder(Material.ENDER_PEARL)
                .name("<gold>Move here</gold>")
                .loreMini("<gray>Teleport this NPC to your location</gray>")
                .build());

        inv.setItem(14, new ItemBuilder(Material.COMPASS)
                .name("<blue>Teleport to NPC</blue>")
                .loreMini("<gray>Teleport yourself to this NPC</gray>")
                .build());

        inv.setItem(15, new ItemBuilder(Material.COMMAND_BLOCK)
                .name("<yellow>Commands</yellow>")
                .loreMini(
                        "<gray>Left: <white>" + data.getLeftCommands().size() + "</white></gray>",
                        "<gray>Right: <white>" + data.getRightCommands().size() + "</white></gray>",
                        "<yellow>Click to manage</yellow>"
                ).build());

        inv.setItem(16, new ItemBuilder(Material.EMERALD)
                .name("<green>Shop</green>")
                .loreMini(
                        "<gray>Enabled: <white>" + data.isShopEnabled() + "</white></gray>",
                        "<gray>Mode: <white>" + (data.isAdminShop() ? "Admin" : "Limited") + "</white></gray>",
                        "<gray>Recipes: <white>" + data.getTrades().size() + "</white></gray>",
                        "<yellow>Click to edit trades</yellow>"
                ).build());

        inv.setItem(19, new ItemBuilder(Material.REDSTONE_TORCH)
                .name("<red>Settings</red>")
                .loreMini("<gray>Look, glow, gravity, baby, cooldown...</gray>")
                .build());

        inv.setItem(20, new ItemBuilder(Material.OAK_SIGN)
                .name("<aqua>Hologram Lines</aqua>")
                .loreMini(
                        "<gray>Lines: <white>" + data.getHologramLines().size() + "</white></gray>",
                        "<green>Left-click: add line</green>",
                        "<red>Right-click: clear</red>"
                ).build());

        inv.setItem(21, new ItemBuilder(Material.BLAZE_POWDER)
                .name("<gold>Particles</gold>")
                .loreMini("<gray>Current: <white>" + data.getParticle() + "</white></gray>")
                .build());

        inv.setItem(22, new ItemBuilder(data.isShowName() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name("<white>Show Name: " + (data.isShowName() ? "<green>ON" : "<red>OFF") + "</white>")
                .build());

        inv.setItem(24, new ItemBuilder(Material.PAPER)
                .name("<yellow>Permission</yellow>")
                .loreMini("<gray>" + (data.getPermission().isBlank() ? "none" : data.getPermission()) + "</gray>")
                .build());

        inv.setItem(31, new ItemBuilder(Material.WRITABLE_BOOK)
                .name("<green>Save / Respawn</green>")
                .loreMini("<gray>Apply visuals and save to disk</gray>")
                .build());

        inv.setItem(33, new ItemBuilder(Material.TNT)
                .name("<dark_red><bold>Delete NPC</bold></dark_red>")
                .loreMini("<red>Shift-click to confirm delete</red>")
                .build());

        inv.setItem(49, new ItemBuilder(Material.ARROW).name("<gray>Back</gray>").build());
        player.openInventory(inv);
    }

    public static void handle(ExtraNPCPlugin plugin, Player player, NpcData data, int slot, boolean left, boolean shift) {
        switch (slot) {
            case 10 -> {
                player.closeInventory();
                plugin.getChatInputListener().request(player, ChatInputListener.InputType.DISPLAY_NAME, data.getId(), null);
            }
            case 11 -> new TypeSelectGui(plugin, player, data).open();
            case 12 -> new SkinGui(plugin, player, data).open();
            case 13 -> {
                plugin.getNpcManager().move(data, player.getLocation());
                plugin.getMessages().send(player, "npc-moved", me.themoo.extranpc.storage.MessageService.map("id", data.getId()));
                new NpcEditGui(plugin, player, data).open();
            }
            case 14 -> {
                if (data.getLocation() != null) {
                    player.teleport(data.getLocation());
                    plugin.getMessages().send(player, "npc-teleported", me.themoo.extranpc.storage.MessageService.map("id", data.getId()));
                }
            }
            case 15 -> new CommandsGui(plugin, player, data).open();
            case 16 -> new ShopEditorGui(plugin, player, data).open();
            case 19 -> new SettingsGui(plugin, player, data).open();
            case 20 -> {
                if (!left) {
                    data.getHologramLines().clear();
                    plugin.getNpcManager().refreshHolograms(data);
                    plugin.getNpcManager().save(data);
                    plugin.getMessages().send(player, "saved");
                    new NpcEditGui(plugin, player, data).open();
                } else {
                    player.closeInventory();
                    plugin.getChatInputListener().request(player, ChatInputListener.InputType.HOLOGRAM_LINE, data.getId(), null);
                }
            }
            case 21 -> new ParticleGui(plugin, player, data).open();
            case 22 -> {
                data.setShowName(!data.isShowName());
                plugin.getNpcManager().respawn(data);
                new NpcEditGui(plugin, player, data).open();
            }
            case 24 -> {
                player.closeInventory();
                plugin.getChatInputListener().request(player, ChatInputListener.InputType.PERMISSION, data.getId(), null);
            }
            case 31 -> {
                plugin.getNpcManager().respawn(data);
                plugin.getMessages().send(player, "saved");
                new NpcEditGui(plugin, player, data).open();
            }
            case 33 -> {
                if (shift) {
                    plugin.getNpcManager().delete(data.getId());
                    plugin.getMessages().send(player, "npc-deleted", me.themoo.extranpc.storage.MessageService.map("id", data.getId()));
                    new MainMenuGui(plugin, player).open();
                }
            }
            case 49 -> new MainMenuGui(plugin, player).open();
            default -> {
            }
        }
    }
}

package me.themoo.extranpc.command;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.gui.MainMenuGui;
import me.themoo.extranpc.gui.NpcEditGui;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.model.NpcType;
import me.themoo.extranpc.storage.MessageService;
import me.themoo.extranpc.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class NpcCommand implements CommandExecutor {

    private final ExtraNPCPlugin plugin;

    public NpcCommand(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("extranpc.admin")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player player) {
                new MainMenuGui(plugin, player).open();
            } else {
                plugin.getMessages().sendHelp(sender);
            }
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help" -> plugin.getMessages().sendHelp(sender);
            case "about", "credits", "info" -> plugin.getMessages().sendAbout(sender);
            case "gui", "menu" -> {
                if (!(sender instanceof Player player)) {
                    plugin.getMessages().send(sender, "player-only");
                    return true;
                }
                new MainMenuGui(plugin, player).open();
            }
            case "create" -> {
                if (!(sender instanceof Player player)) {
                    plugin.getMessages().send(sender, "player-only");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(TextUtil.parse("<red>Usage: /extranpc create <id> [type]</red>"));
                    return true;
                }
                String id = args[1].toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_\\-]", "");
                if (id.isBlank()) {
                    player.sendMessage(TextUtil.parse("<red>Invalid id.</red>"));
                    return true;
                }
                if (plugin.getNpcManager().exists(id)) {
                    plugin.getMessages().send(player, "npc-exists", MessageService.map("id", id));
                    return true;
                }
                NpcType type = NpcType.PLAYER;
                if (args.length >= 3) {
                    type = NpcType.fromString(args[2]).orElse(null);
                    if (type == null) {
                        plugin.getMessages().send(player, "invalid-type");
                        return true;
                    }
                }
                if (type.isPlayerLike() && !plugin.getNpcManager().canCreatePlayerNpc()) {
                    plugin.getMessages().send(player, "npc-engine-error");
                    return true;
                }
                NpcData data = plugin.getNpcManager().create(id, type, player.getLocation());
                plugin.getMessages().send(player, "npc-created", MessageService.map("id", id));
                new NpcEditGui(plugin, player, data).open();
            }
            case "edit" -> {
                if (!(sender instanceof Player player)) {
                    plugin.getMessages().send(sender, "player-only");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(TextUtil.parse("<red>Usage: /extranpc edit <id></red>"));
                    return true;
                }
                plugin.getNpcManager().get(args[1]).ifPresentOrElse(
                        data -> new NpcEditGui(plugin, player, data).open(),
                        () -> plugin.getMessages().send(player, "npc-not-found", MessageService.map("id", args[1]))
                );
            }
            case "delete", "remove" -> {
                if (args.length < 2) {
                    sender.sendMessage(TextUtil.parse("<red>Usage: /extranpc delete <id></red>"));
                    return true;
                }
                if (!plugin.getNpcManager().exists(args[1])) {
                    plugin.getMessages().send(sender, "npc-not-found", MessageService.map("id", args[1]));
                    return true;
                }
                plugin.getNpcManager().delete(args[1]);
                plugin.getMessages().send(sender, "npc-deleted", MessageService.map("id", args[1].toLowerCase(Locale.ROOT)));
            }
            case "list" -> {
                plugin.getMessages().send(sender, "list-header", MessageService.map("count", String.valueOf(plugin.getNpcManager().getNpcs().size())));
                for (NpcData data : plugin.getNpcManager().getNpcs()) {
                    sender.sendMessage(plugin.getMessages().component("list-entry", MessageService.map(
                            "id", data.getId(),
                            "name", data.getDisplayName(),
                            "type", data.getType().name()
                    )));
                }
            }
            case "move" -> {
                if (!(sender instanceof Player player)) {
                    plugin.getMessages().send(sender, "player-only");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(TextUtil.parse("<red>Usage: /extranpc move <id></red>"));
                    return true;
                }
                plugin.getNpcManager().get(args[1]).ifPresentOrElse(data -> {
                    plugin.getNpcManager().move(data, player.getLocation());
                    plugin.getMessages().send(player, "npc-moved", MessageService.map("id", data.getId()));
                }, () -> plugin.getMessages().send(player, "npc-not-found", MessageService.map("id", args[1])));
            }
            case "here" -> {
                if (!(sender instanceof Player player)) {
                    plugin.getMessages().send(sender, "player-only");
                    return true;
                }
                plugin.getNpcManager().getSelected(player).ifPresentOrElse(id -> plugin.getNpcManager().get(id).ifPresent(data -> {
                    plugin.getNpcManager().move(data, player.getLocation());
                    plugin.getMessages().send(player, "npc-moved", MessageService.map("id", id));
                }), () -> player.sendMessage(TextUtil.parse("<red>No NPC selected.</red>")));
            }
            case "tp", "teleport" -> {
                if (!(sender instanceof Player player)) {
                    plugin.getMessages().send(sender, "player-only");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(TextUtil.parse("<red>Usage: /extranpc tp <id></red>"));
                    return true;
                }
                plugin.getNpcManager().get(args[1]).ifPresentOrElse(data -> {
                    if (data.getLocation() != null) {
                        player.teleport(data.getLocation());
                        plugin.getMessages().send(player, "npc-teleported", MessageService.map("id", data.getId()));
                    }
                }, () -> plugin.getMessages().send(player, "npc-not-found", MessageService.map("id", args[1])));
            }
            case "select" -> {
                if (!(sender instanceof Player player)) {
                    plugin.getMessages().send(sender, "player-only");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(TextUtil.parse("<red>Usage: /extranpc select <id></red>"));
                    return true;
                }
                plugin.getNpcManager().get(args[1]).ifPresentOrElse(data -> {
                    plugin.getNpcManager().setSelected(player, data.getId());
                    plugin.getMessages().send(player, "npc-selected", MessageService.map("id", data.getId()));
                }, () -> plugin.getMessages().send(player, "npc-not-found", MessageService.map("id", args[1])));
            }
            case "reload" -> {
                plugin.reloadAll();
                plugin.getMessages().send(sender, "reload");
            }
            default -> plugin.getMessages().send(sender, "unknown-command");
        }
        return true;
    }
}

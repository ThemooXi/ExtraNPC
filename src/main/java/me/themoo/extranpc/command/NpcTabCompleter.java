package me.themoo.extranpc.command;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.model.NpcType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class NpcTabCompleter implements TabCompleter {

    private static final List<String> SUBS = List.of(
            "help", "about", "gui", "create", "edit", "delete", "list", "move", "here", "tp", "select", "reload"
    );

    private final ExtraNPCPlugin plugin;

    public NpcTabCompleter(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("extranpc.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            return filter(SUBS, args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (List.of("edit", "delete", "move", "tp", "select", "remove", "teleport").contains(sub)) {
                return filter(plugin.getNpcManager().getNpcs().stream().map(NpcData::getId).collect(Collectors.toList()), args[1]);
            }
            if (sub.equals("create")) {
                return filter(List.of("<id>"), args[1]);
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return filter(Arrays.stream(NpcType.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toList()), args[2]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }
}

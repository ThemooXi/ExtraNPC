package me.themoo.extranpc.listener;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.gui.NpcEditGui;
import me.themoo.extranpc.model.NpcData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class ChatInputListener implements Listener {

    public enum InputType {
        CREATE_ID,
        DISPLAY_NAME,
        SKIN_NAME,
        SKIN_URL,
        ADD_LEFT_COMMAND,
        ADD_RIGHT_COMMAND,
        PERMISSION,
        HOLOGRAM_LINE
    }

    private final ExtraNPCPlugin plugin;
    private final Map<UUID, PendingInput> pending = new HashMap<>();

    public ChatInputListener(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
    }

    public void request(Player player, InputType type, String npcId, BiConsumer<Player, String> handler) {
        pending.put(player.getUniqueId(), new PendingInput(type, npcId, handler));
        plugin.getMessages().send(player, "input-cancel");
        switch (type) {
            case CREATE_ID -> plugin.getMessages().send(player, "input-id");
            case DISPLAY_NAME -> plugin.getMessages().send(player, "input-name");
            case SKIN_NAME -> plugin.getMessages().send(player, "input-skin-name");
            case SKIN_URL -> plugin.getMessages().send(player, "input-skin-url");
            case ADD_LEFT_COMMAND, ADD_RIGHT_COMMAND -> plugin.getMessages().send(player, "input-command");
            case PERMISSION -> plugin.getMessages().send(player, "input-permission");
            case HOLOGRAM_LINE -> player.sendMessage(me.themoo.extranpc.util.TextUtil.parse(
                    plugin.getConfig().getString("settings.prefix", "") + "<green>Type a hologram line:</green>"));
        }
    }

    public boolean hasPending(Player player) {
        return pending.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        PendingInput input = pending.get(event.getPlayer().getUniqueId());
        if (input == null) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage().trim();
        Player player = event.getPlayer();

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (message.equalsIgnoreCase("cancel")) {
                pending.remove(player.getUniqueId());
                plugin.getMessages().send(player, "cancelled");
                return;
            }
            pending.remove(player.getUniqueId());
            if (input.handler != null) {
                input.handler.accept(player, message);
                return;
            }
            handleDefault(player, input, message);
        });
    }

    private void handleDefault(Player player, PendingInput input, String message) {
        if (input.npcId == null) {
            return;
        }
        plugin.getNpcManager().get(input.npcId).ifPresent(data -> {
            switch (input.type) {
                case DISPLAY_NAME -> {
                    data.setDisplayName(message);
                    plugin.getNpcManager().respawn(data);
                    plugin.getMessages().send(player, "saved");
                    new NpcEditGui(plugin, player, data).open();
                }
                case SKIN_NAME -> plugin.getSkinManager().fetchByPlayerName(message, skin -> {
                    if (skin == null) {
                        player.sendMessage(me.themoo.extranpc.util.TextUtil.parse("<red>Skin not found.</red>"));
                        return;
                    }
                    data.setSkin(skin);
                    plugin.getNpcManager().respawn(data);
                    plugin.getMessages().send(player, "saved");
                    new NpcEditGui(plugin, player, data).open();
                });
                case SKIN_URL -> plugin.getSkinManager().applyUrlSkin(message, skin -> {
                    if (skin == null) {
                        player.sendMessage(me.themoo.extranpc.util.TextUtil.parse("<red>Invalid skin URL.</red>"));
                        return;
                    }
                    data.setSkin(skin);
                    plugin.getNpcManager().respawn(data);
                    plugin.getMessages().send(player, "saved");
                    new NpcEditGui(plugin, player, data).open();
                });
                case ADD_LEFT_COMMAND -> {
                    data.getLeftCommands().add(message);
                    plugin.getNpcManager().save(data);
                    plugin.getMessages().send(player, "saved");
                    new NpcEditGui(plugin, player, data).open();
                }
                case ADD_RIGHT_COMMAND -> {
                    data.getRightCommands().add(message);
                    plugin.getNpcManager().save(data);
                    plugin.getMessages().send(player, "saved");
                    new NpcEditGui(plugin, player, data).open();
                }
                case PERMISSION -> {
                    data.setPermission(message.equalsIgnoreCase("none") ? "" : message);
                    plugin.getNpcManager().save(data);
                    plugin.getMessages().send(player, "saved");
                    new NpcEditGui(plugin, player, data).open();
                }
                case HOLOGRAM_LINE -> {
                    data.getHologramLines().add(message);
                    plugin.getNpcManager().refreshHolograms(data);
                    plugin.getNpcManager().save(data);
                    plugin.getMessages().send(player, "saved");
                    new NpcEditGui(plugin, player, data).open();
                }
                default -> {
                }
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
    }

    public record PendingInput(InputType type, String npcId, BiConsumer<Player, String> handler) {
    }
}

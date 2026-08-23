package me.themoo.extranpc.listener;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.gui.NpcEditGui;
import me.themoo.extranpc.model.NpcData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

public final class NpcListener implements Listener {

    private final ExtraNPCPlugin plugin;

    public NpcListener(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Entity entity = event.getRightClicked();
        plugin.getNpcManager().byEntity(entity).ifPresent(data -> {
            event.setCancelled(true);
            handleInteract(event.getPlayer(), data, false);
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeftClick(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        plugin.getNpcManager().byEntity(event.getEntity()).ifPresent(data -> {
            event.setCancelled(true);
            handleInteract(player, data, true);
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAnyDamage(EntityDamageEvent event) {
        plugin.getNpcManager().byEntity(event.getEntity()).ifPresent(data -> {
            if (data.isInvulnerable()) {
                event.setCancelled(true);
            }
        });
    }

    private void handleInteract(Player player, NpcData data, boolean leftClick) {
        if (player.isSneaking()
                && player.hasPermission("extranpc.admin")
                && plugin.getConfig().getBoolean("settings.sneak-edit", true)) {
            new NpcEditGui(plugin, player, data).open();
            return;
        }

        if (!player.hasPermission("extranpc.use") && !player.hasPermission("extranpc.admin")) {
            plugin.getMessages().send(player, "no-permission");
            return;
        }

        if (data.getPermission() != null && !data.getPermission().isBlank()
                && !player.hasPermission(data.getPermission())
                && !player.hasPermission("extranpc.bypass")) {
            plugin.getMessages().send(player, "no-interact-perm");
            return;
        }

        if (!plugin.getNpcManager().checkCooldown(player, data)) {
            return;
        }

        List<String> commands = leftClick ? data.getLeftCommands() : data.getRightCommands();
        boolean ranCommand = false;
        for (String raw : commands) {
            runCommand(player, raw);
            ranCommand = true;
        }

        if (!leftClick && data.isShopEnabled()) {
            plugin.getNpcManager().openShop(player, data);
            return;
        }

        if (!ranCommand && !leftClick && !data.isShopEnabled()) {
            // no-op default
        }
    }

    private void runCommand(Player player, String raw) {
        if (raw == null || raw.isBlank()) {
            return;
        }
        String command = plugin.getPlaceholderHook().apply(player, raw.trim());
        if (command.startsWith("op:") || command.startsWith("console:")) {
            String cmd = command.substring(command.indexOf(':') + 1).trim();
            if (cmd.startsWith("/")) {
                cmd = cmd.substring(1);
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            return;
        }
        if (command.startsWith("player:") || command.startsWith("self:")) {
            String cmd = command.substring(command.indexOf(':') + 1).trim();
            if (cmd.startsWith("/")) {
                cmd = cmd.substring(1);
            }
            player.performCommand(cmd);
            return;
        }
        if (command.startsWith("message:") || command.startsWith("msg:")) {
            String msg = command.substring(command.indexOf(':') + 1).trim();
            player.sendMessage(me.themoo.extranpc.util.TextUtil.parse(
                    plugin.getPlaceholderHook().apply(player, msg)));
            return;
        }
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        player.performCommand(command);
    }
}

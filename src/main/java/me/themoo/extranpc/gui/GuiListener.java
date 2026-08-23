package me.themoo.extranpc.gui;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.model.NpcType;
import me.themoo.extranpc.storage.MessageService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public final class GuiListener implements Listener {

    private final ExtraNPCPlugin plugin;

    public GuiListener(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof GuiHolder holder)) {
            return;
        }

        int slot = event.getRawSlot();
        boolean top = slot < event.getView().getTopInventory().getSize();

        // Shopkeepers-style: allow placing/taking items in recipe item slots
        if (holder.getType() == GuiHolder.GuiType.SHOP && top && ShopEditorGui.isEditableSlot(slot)) {
            ItemStack current = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            boolean cursorEmpty = cursor == null || cursor.getType().isAir();
            if (ShopEditorGui.isPlaceholder(current) && cursorEmpty) {
                event.setCancelled(true);
                return;
            }
            if (ShopEditorGui.isPlaceholder(current) && !cursorEmpty) {
                event.setCurrentItem(null);
            }
            return;
        }

        // Prevent shift-click dumping into decorative panes
        if (holder.getType() == GuiHolder.GuiType.SHOP && !top) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
            }
            return;
        }

        if (top) {
            event.setCancelled(true);
        } else {
            return;
        }

        boolean left = event.isLeftClick();
        boolean shift = event.isShiftClick();
        NpcData data = holder.getNpcId() == null ? null : plugin.getNpcManager().get(holder.getNpcId()).orElse(null);

        switch (holder.getType()) {
            case MAIN -> MainMenuGui.handle(plugin, player, slot);
            case LIST -> handleList(player, event, slot, left, shift);
            case EDIT -> {
                if (data != null) {
                    NpcEditGui.handle(plugin, player, data, slot, left, shift);
                }
            }
            case TYPE_SELECT -> {
                if (data == null) {
                    return;
                }
                if (slot == 49) {
                    new NpcEditGui(plugin, player, data).open();
                    return;
                }
                ItemStack item = event.getCurrentItem();
                if (item == null || item.getItemMeta() == null) {
                    return;
                }
                String typeName = item.getItemMeta().getPersistentDataContainer()
                        .get(new org.bukkit.NamespacedKey(plugin, "npc-type"), PersistentDataType.STRING);
                if (typeName == null) {
                    return;
                }
                NpcType.fromString(typeName).ifPresent(type -> {
                    if (type.isPlayerLike() && !plugin.getNpcManager().canCreatePlayerNpc()) {
                        plugin.getMessages().send(player, "npc-engine-error");
                        return;
                    }
                    data.setType(type);
                    plugin.getNpcManager().respawn(data);
                    plugin.getMessages().send(player, "saved");
                    new NpcEditGui(plugin, player, data).open();
                });
            }
            case SKIN -> {
                if (data != null) {
                    SkinGui.handle(plugin, player, data, slot);
                }
            }
            case COMMANDS -> {
                if (data != null) {
                    CommandsGui.handle(plugin, player, data, slot);
                }
            }
            case SHOP -> {
                if (data != null) {
                    ShopEditorGui.handle(plugin, player, data, event.getInventory(), holder, slot, left, shift);
                }
            }
            case SETTINGS -> {
                if (data != null) {
                    SettingsGui.handle(plugin, player, data, slot, left, shift);
                }
            }
            case PARTICLES -> {
                if (data == null) {
                    return;
                }
                if (slot == 22) {
                    new NpcEditGui(plugin, player, data).open();
                    return;
                }
                ItemStack item = event.getCurrentItem();
                if (item == null || item.getItemMeta() == null) {
                    return;
                }
                String particle = item.getItemMeta().getPersistentDataContainer()
                        .get(new org.bukkit.NamespacedKey(plugin, "particle"), PersistentDataType.STRING);
                if (particle != null) {
                    data.setParticle(particle);
                    plugin.getNpcManager().save(data);
                    new ParticleGui(plugin, player, data).open();
                }
            }
            default -> {
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof GuiHolder holder)) {
            return;
        }
        if (holder.getType() != GuiHolder.GuiType.SHOP || holder.getNpcId() == null) {
            return;
        }
        plugin.getNpcManager().get(holder.getNpcId()).ifPresent(data ->
                ShopEditorGui.handleClose(plugin, player, data, event.getInventory(), holder.getPage()));
    }

    private void handleList(Player player, InventoryClickEvent event, int slot, boolean left, boolean shift) {
        if (slot == 49) {
            new MainMenuGui(plugin, player).open();
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null) {
            return;
        }
        String id = item.getItemMeta().getPersistentDataContainer()
                .get(new org.bukkit.NamespacedKey(plugin, "list-npc"), PersistentDataType.STRING);
        if (id == null) {
            return;
        }
        plugin.getNpcManager().get(id).ifPresent(data -> {
            if (shift) {
                if (data.getLocation() != null) {
                    player.teleport(data.getLocation());
                    plugin.getMessages().send(player, "npc-teleported", MessageService.map("id", id));
                }
                return;
            }
            if (event.getClick() == ClickType.RIGHT) {
                plugin.getNpcManager().setSelected(player, id);
                plugin.getMessages().send(player, "npc-selected", MessageService.map("id", id));
                return;
            }
            if (left) {
                new NpcEditGui(plugin, player, data).open();
            }
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof GuiHolder holder)) {
            return;
        }
        if (holder.getType() == GuiHolder.GuiType.SHOP) {
            for (int slot : event.getRawSlots()) {
                if (slot < event.getView().getTopInventory().getSize() && !ShopEditorGui.isEditableSlot(slot)) {
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        event.setCancelled(true);
    }
}

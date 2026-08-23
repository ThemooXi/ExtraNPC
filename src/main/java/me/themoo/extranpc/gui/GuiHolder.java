package me.themoo.extranpc.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class GuiHolder implements InventoryHolder {

    public enum GuiType {
        MAIN,
        LIST,
        EDIT,
        TYPE_SELECT,
        COMMANDS,
        SHOP,
        SETTINGS,
        SKIN,
        PARTICLES
    }

    private final GuiType type;
    private final String npcId;
    private int page;
    private Inventory inventory;

    public GuiHolder(GuiType type, String npcId) {
        this(type, npcId, 0);
    }

    public GuiHolder(GuiType type, String npcId, int page) {
        this.type = type;
        this.npcId = npcId;
        this.page = Math.max(0, page);
    }

    public GuiType getType() {
        return type;
    }

    public String getNpcId() {
        return npcId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

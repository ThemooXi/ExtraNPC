package me.themoo.extranpc.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ItemBuilder {

    private final ItemStack item;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
    }

    public ItemBuilder name(Component name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder name(String miniMessage) {
        return name(TextUtil.parse(miniMessage));
    }

    public ItemBuilder lore(Component... lines) {
        return lore(Arrays.asList(lines));
    }

    public ItemBuilder lore(List<Component> lines) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.lore(lines);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder loreMini(String... lines) {
        List<Component> components = new ArrayList<>();
        for (String line : lines) {
            components.add(TextUtil.parse(line));
        }
        return lore(components);
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(Math.max(1, amount));
        return this;
    }

    public ItemStack build() {
        return item;
    }
}

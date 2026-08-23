package me.themoo.extranpc.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public final class ShopTrade {

    private ItemStack result;
    private ItemStack ingredient1;
    private ItemStack ingredient2;
    private int maxUses;
    private int experience;
    private float priceMultiplier;

    public ShopTrade(ItemStack result, ItemStack ingredient1, ItemStack ingredient2, int maxUses) {
        this.result = result;
        this.ingredient1 = ingredient1;
        this.ingredient2 = ingredient2;
        this.maxUses = Math.max(1, maxUses);
        this.experience = 0;
        this.priceMultiplier = 0.0f;
    }

    public ItemStack getResult() {
        return result == null ? null : result.clone();
    }

    public void setResult(ItemStack result) {
        this.result = result == null ? null : result.clone();
    }

    public ItemStack getIngredient1() {
        return ingredient1 == null ? null : ingredient1.clone();
    }

    public void setIngredient1(ItemStack ingredient1) {
        this.ingredient1 = ingredient1 == null ? null : ingredient1.clone();
    }

    public ItemStack getIngredient2() {
        return ingredient2 == null ? null : ingredient2.clone();
    }

    public void setIngredient2(ItemStack ingredient2) {
        this.ingredient2 = ingredient2 == null ? null : ingredient2.clone();
    }

    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = Math.max(1, maxUses);
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = Math.max(0, experience);
    }

    public float getPriceMultiplier() {
        return priceMultiplier;
    }

    public void setPriceMultiplier(float priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }

    public boolean isValid() {
        return result != null && !result.getType().isAir() && ingredient1 != null && !ingredient1.getType().isAir();
    }

    public void save(ConfigurationSection section) {
        section.set("result", result);
        section.set("ingredient1", ingredient1);
        section.set("ingredient2", ingredient2);
        section.set("max-uses", maxUses);
        section.set("experience", experience);
        section.set("price-multiplier", priceMultiplier);
    }

    public static ShopTrade load(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        ItemStack result = section.getItemStack("result");
        ItemStack in1 = section.getItemStack("ingredient1");
        ItemStack in2 = section.getItemStack("ingredient2");
        int maxUses = section.getInt("max-uses", 9999);
        ShopTrade trade = new ShopTrade(result, in1, in2, maxUses);
        trade.setExperience(section.getInt("experience", 0));
        trade.setPriceMultiplier((float) section.getDouble("price-multiplier", 0.0d));
        return trade;
    }
}

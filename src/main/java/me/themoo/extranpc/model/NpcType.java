package me.themoo.extranpc.model;

import me.themoo.extranpc.util.ServerCompat;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * NPC kinds resolved by name so ExtraNPC can run on Paper 1.21.x and 26.x
 * without crashing when a mob was added or removed.
 */
public enum NpcType {
    PLAYER("PLAYER", true, "PLAYER_HEAD"),
    VILLAGER("VILLAGER", false, "EMERALD"),
    COW("COW", false, "BEEF"),
    PIG("PIG", false, "PORKCHOP"),
    SHEEP("SHEEP", false, "WHITE_WOOL"),
    CHICKEN("CHICKEN", false, "EGG"),
    WOLF("WOLF", false, "BONE"),
    CAT("CAT", false, "COD"),
    HORSE("HORSE", false, "SADDLE"),
    FOX("FOX", false, "SWEET_BERRIES"),
    RABBIT("RABBIT", false, "RABBIT_FOOT"),
    BEE("BEE", false, "HONEYCOMB"),
    PANDA("PANDA", false, "BAMBOO"),
    IRON_GOLEM("IRON_GOLEM", false, "IRON_BLOCK"),
    SNOW_GOLEM("SNOW_GOLEM", false, "SNOWBALL"),
    ZOMBIE("ZOMBIE", false, "ROTTEN_FLESH"),
    SKELETON("SKELETON", false, "BONE"),
    CREEPER("CREEPER", false, "GUNPOWDER"),
    ENDERMAN("ENDERMAN", false, "ENDER_PEARL"),
    BLAZE("BLAZE", false, "BLAZE_ROD"),
    WITCH("WITCH", false, "GLASS_BOTTLE"),
    PILLAGER("PILLAGER", false, "CROSSBOW"),
    ALLAY("ALLAY", false, "AMETHYST_SHARD"),
    CAMEL("CAMEL", false, "CACTUS"),
    SNIFFER("SNIFFER", false, "TORCHFLOWER_SEEDS"),
    ARMADILLO("ARMADILLO", false, "ARMADILLO_SCUTE"),
    HAPPY_GHAST("HAPPY_GHAST", false, "GHAST_TEAR"),
    COPPER_GOLEM("COPPER_GOLEM", false, "COPPER_INGOT"),
    SULFUR_CUBE("SULFUR_CUBE", false, "SLIME_BALL");

    private final String entityName;
    private final boolean playerLike;
    private final String iconName;

    NpcType(String entityName, boolean playerLike, String iconName) {
        this.entityName = entityName;
        this.playerLike = playerLike;
        this.iconName = iconName;
    }

    public EntityType getEntityType() {
        return ServerCompat.entityType(entityName);
    }

    public Class<? extends Entity> getEntityClass() {
        EntityType type = getEntityType();
        return type == null ? null : type.getEntityClass();
    }

    public boolean isPlayerLike() {
        return playerLike;
    }

    /**
     * Player NPCs always have a fallback. Mob types require a spawnable entity on this server.
     */
    public boolean isAvailable() {
        if (playerLike) {
            return true;
        }
        EntityType type = getEntityType();
        if (type == null || type.getEntityClass() == null) {
            return false;
        }
        try {
            return type.isSpawnable();
        } catch (Throwable ignored) {
            return true;
        }
    }

    public Material icon() {
        return ServerCompat.material(iconName, Material.STONE);
    }

    public static Optional<NpcType> fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String key = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return Arrays.stream(values()).filter(t -> t.name().equals(key)).findFirst();
    }
}

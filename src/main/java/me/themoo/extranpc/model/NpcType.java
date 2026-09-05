package me.themoo.extranpc.model;

import me.themoo.extranpc.compat.BukkitCompat;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
    SNIFFER("SNIFFER", false, "TORCHFLOWER_SEEDS", "FLOWER_POT"),
    ARMADILLO("ARMADILLO", false, "ARMADILLO_SCUTE", "TURTLE_SCUTE", "BOWL");

    private final String entityTypeName;
    private final boolean playerLike;
    private final String[] iconNames;

    NpcType(String entityTypeName, boolean playerLike, String... iconNames) {
        this.entityTypeName = entityTypeName;
        this.playerLike = playerLike;
        this.iconNames = iconNames;
    }

    public String getEntityTypeName() {
        return entityTypeName;
    }

    public EntityType getEntityType() {
        return BukkitCompat.entityType(entityTypeName);
    }

    public Material icon() {
        return BukkitCompat.material(iconNames);
    }

    public boolean isPlayerLike() {
        return playerLike;
    }

    /**
     * Player types are always listed; mob types only if this server has the entity.
     */
    public boolean isAvailable() {
        return playerLike || getEntityType() != null;
    }

    public static List<NpcType> available() {
        return Arrays.stream(values()).filter(NpcType::isAvailable).toList();
    }

    public static Optional<NpcType> fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String key = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return Arrays.stream(values()).filter(t -> t.name().equals(key)).findFirst();
    }
}

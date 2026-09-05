package me.themoo.extranpc.model;

import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum NpcType {
    PLAYER(true),
    VILLAGER(false),
    COW(false),
    PIG(false),
    SHEEP(false),
    CHICKEN(false),
    WOLF(false),
    CAT(false),
    HORSE(false),
    FOX(false),
    RABBIT(false),
    BEE(false),
    PANDA(false),
    IRON_GOLEM(false),
    SNOW_GOLEM(false),
    ZOMBIE(false),
    SKELETON(false),
    CREEPER(false),
    ENDERMAN(false),
    BLAZE(false),
    WITCH(false),
    PILLAGER(false),
    ALLAY(false),
    CAMEL(false),
    SNIFFER(false),
    ARMADILLO(false);

    private final String entityTypeName;
    private final boolean playerLike;

    NpcType(boolean playerLike) {
        this.entityTypeName = name();
        this.playerLike = playerLike;
    }

    public EntityType getEntityType() {
        try {
            return EntityType.valueOf(entityTypeName);
        } catch (IllegalArgumentException ignored) {
            // New entity types are not present on every supported server version.
            return null;
        }
    }

    public boolean isAvailable() {
        return getEntityType() != null;
    }

    public boolean isPlayerLike() {
        return playerLike;
    }

    public static Optional<NpcType> fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String key = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return Arrays.stream(values()).filter(t -> t.name().equals(key)).findFirst();
    }
}

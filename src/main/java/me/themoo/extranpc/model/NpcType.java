package me.themoo.extranpc.model;

import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum NpcType {
    PLAYER(EntityType.PLAYER, true),
    VILLAGER(EntityType.VILLAGER, false),
    COW(EntityType.COW, false),
    PIG(EntityType.PIG, false),
    SHEEP(EntityType.SHEEP, false),
    CHICKEN(EntityType.CHICKEN, false),
    WOLF(EntityType.WOLF, false),
    CAT(EntityType.CAT, false),
    HORSE(EntityType.HORSE, false),
    FOX(EntityType.FOX, false),
    RABBIT(EntityType.RABBIT, false),
    BEE(EntityType.BEE, false),
    PANDA(EntityType.PANDA, false),
    IRON_GOLEM(EntityType.IRON_GOLEM, false),
    SNOW_GOLEM(EntityType.SNOW_GOLEM, false),
    ZOMBIE(EntityType.ZOMBIE, false),
    SKELETON(EntityType.SKELETON, false),
    CREEPER(EntityType.CREEPER, false),
    ENDERMAN(EntityType.ENDERMAN, false),
    BLAZE(EntityType.BLAZE, false),
    WITCH(EntityType.WITCH, false),
    PILLAGER(EntityType.PILLAGER, false),
    ALLAY(EntityType.ALLAY, false),
    CAMEL(EntityType.CAMEL, false),
    SNIFFER(EntityType.SNIFFER, false),
    ARMADILLO(EntityType.ARMADILLO, false);

    private final EntityType entityType;
    private final boolean playerLike;

    NpcType(EntityType entityType, boolean playerLike) {
        this.entityType = entityType;
        this.playerLike = playerLike;
    }

    public EntityType getEntityType() {
        return entityType;
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

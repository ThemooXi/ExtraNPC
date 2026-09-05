package me.themoo.extranpc.model;

import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum NpcType {
    PLAYER("PLAYER", true),
    VILLAGER("VILLAGER", false),
    COW("COW", false),
    PIG("PIG", false),
    SHEEP("SHEEP", false),
    CHICKEN("CHICKEN", false),
    WOLF("WOLF", false),
    CAT("CAT", false),
    HORSE("HORSE", false),
    FOX("FOX", false),
    RABBIT("RABBIT", false),
    BEE("BEE", false),
    PANDA("PANDA", false),
    IRON_GOLEM("IRON_GOLEM", false),
    SNOW_GOLEM("SNOW_GOLEM", false),
    ZOMBIE("ZOMBIE", false),
    SKELETON("SKELETON", false),
    CREEPER("CREEPER", false),
    ENDERMAN("ENDERMAN", false),
    BLAZE("BLAZE", false),
    WITCH("WITCH", false),
    PILLAGER("PILLAGER", false),
    ALLAY("ALLAY", false),
    CAMEL("CAMEL", false),
    SNIFFER("SNIFFER", false),
    ARMADILLO("ARMADILLO", false);

    private final String entityTypeName;
    private final boolean playerLike;

    NpcType(String entityTypeName, boolean playerLike) {
        this.entityTypeName = entityTypeName;
        this.playerLike = playerLike;
    }

    public EntityType getEntityType() {
        try {
            Object value = EntityType.class.getField(entityTypeName).get(null);
            return EntityType.class.isInstance(value) ? EntityType.class.cast(value) : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    public boolean isSupported() {
        return playerLike || getEntityType() != null;
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

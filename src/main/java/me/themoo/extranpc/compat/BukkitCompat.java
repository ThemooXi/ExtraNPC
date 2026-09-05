package me.themoo.extranpc.compat;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Safe lookups for enums / registry entries that were renamed or added
 * between 1.21.x and Paper 26.1 / 26.2.
 */
public final class BukkitCompat {

    private BukkitCompat() {
    }

    public static EntityType entityType(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String key = ServerVersionParser.normalizeKey(name);
        EntityType fromEnum = enumConstant(EntityType.class, key);
        if (fromEnum != null) {
            return fromEnum;
        }
        return registryGet(entityTypeRegistry(), key, "minecraft");
    }

    public static Material material(String... names) {
        if (names == null) {
            return Material.STONE;
        }
        for (String name : names) {
            if (name == null || name.isBlank()) {
                continue;
            }
            String key = ServerVersionParser.normalizeKey(name);
            Material fromEnum = enumConstant(Material.class, key);
            if (fromEnum != null) {
                return fromEnum;
            }
            Material fromRegistry = registryGet(materialRegistry(), key, "minecraft");
            if (fromRegistry != null) {
                return fromRegistry;
            }
        }
        return Material.STONE;
    }

    public static Particle particle(String name) {
        if (name == null || name.isBlank() || "NONE".equalsIgnoreCase(name)) {
            return null;
        }
        String key = ServerVersionParser.normalizeKey(name);
        Particle fromEnum = enumConstant(Particle.class, key);
        if (fromEnum != null) {
            return fromEnum;
        }
        Particle remapped = enumConstant(Particle.class, remapParticle(key));
        if (remapped != null) {
            return remapped;
        }
        return registryGet(particleRegistry(), key, "minecraft");
    }

    public static boolean hasParticle(String name) {
        return "NONE".equalsIgnoreCase(name) || particle(name) != null;
    }

    public static void freezeMovement(LivingEntity living) {
        Attribute attribute = attribute("MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED");
        if (attribute == null) {
            return;
        }
        try {
            AttributeInstance instance = living.getAttribute(attribute);
            if (instance != null) {
                instance.setBaseValue(0.0);
            }
        } catch (Throwable ignored) {
        }
    }

    public static Attribute attribute(String... names) {
        if (names == null) {
            return null;
        }
        for (String name : names) {
            if (name == null || name.isBlank()) {
                continue;
            }
            String key = ServerVersionParser.normalizeKey(name);
            Attribute fromField = staticField(Attribute.class, key);
            if (fromField != null) {
                return fromField;
            }
            Attribute fromEnum = enumConstant(Attribute.class, key);
            if (fromEnum != null) {
                return fromEnum;
            }
            String registryName = key.toLowerCase(Locale.ROOT);
            if (registryName.startsWith("generic_")) {
                registryName = registryName.substring("generic_".length());
            }
            Attribute fromRegistry = registryGet(attributeRegistry(), registryName, "minecraft");
            if (fromRegistry != null) {
                return fromRegistry;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T enumConstant(Class<?> type, String name) {
        try {
            Method valueOf = type.getMethod("valueOf", String.class);
            Object value = valueOf.invoke(null, name);
            if (type.isInstance(value)) {
                return (T) value;
            }
        } catch (Throwable ignored) {
        }
        return staticField(type, name);
    }

    @SuppressWarnings("unchecked")
    private static <T> T staticField(Class<?> type, String name) {
        try {
            Field field = type.getField(name);
            Object value = field.get(null);
            if (type.isInstance(value) || (value != null && type.isAssignableFrom(value.getClass()))) {
                return (T) value;
            }
            if (value != null) {
                return (T) value;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T registryGet(Object registry, String name, String namespace) {
        if (registry == null || name == null) {
            return null;
        }
        try {
            NamespacedKey key = new NamespacedKey(namespace, name.toLowerCase(Locale.ROOT));
            try {
                Method get = registry.getClass().getMethod("get", NamespacedKey.class);
                return (T) get.invoke(registry, key);
            } catch (NoSuchMethodException ignored) {
            }
            try {
                Method get = registry.getClass().getMethod("get", org.bukkit.NamespacedKey.class);
                return (T) get.invoke(registry, key);
            } catch (NoSuchMethodException ignored) {
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static Object entityTypeRegistry() {
        return staticField(Registry.class, "ENTITY_TYPE");
    }

    private static Object materialRegistry() {
        return staticField(Registry.class, "MATERIAL");
    }

    private static Object particleRegistry() {
        Object registry = staticField(Registry.class, "PARTICLE_TYPE");
        return registry != null ? registry : staticField(Registry.class, "PARTICLE");
    }

    private static Object attributeRegistry() {
        return staticField(Registry.class, "ATTRIBUTE");
    }

    private static String remapParticle(String key) {
        return switch (key) {
            case "HAPPY_VILLAGER", "VILLAGER_HAPPY" -> "HAPPY_VILLAGER";
            case "ENCHANT", "ENCHANTMENT_TABLE" -> "ENCHANT";
            case "SMOKE", "SMOKE_NORMAL" -> "SMOKE";
            case "CRIT", "CRIT_MAGIC" -> "CRIT";
            default -> key;
        };
    }
}

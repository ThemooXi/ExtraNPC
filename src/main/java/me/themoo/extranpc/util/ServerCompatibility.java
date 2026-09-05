package me.themoo.extranpc.util;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

import java.lang.reflect.Field;

/**
 * Resolves API constants whose names changed between Minecraft generations
 * without creating hard class-linkage requirements.
 */
public final class ServerCompatibility {

    private ServerCompatibility() {
    }

    public static Attribute movementSpeedAttribute() {
        return staticConstant(Attribute.class, "GENERIC_MOVEMENT_SPEED", "MOVEMENT_SPEED");
    }

    public static Material material(String name, Material fallback) {
        Material material = staticConstant(Material.class, name);
        return material == null ? fallback : material;
    }

    private static <T> T staticConstant(Class<T> type, String... names) {
        for (String name : names) {
            try {
                Field field = type.getField(name);
                Object value = field.get(null);
                if (type.isInstance(value)) {
                    return type.cast(value);
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Try the name used by another supported API generation.
            }
        }
        return null;
    }
}

package me.themoo.extranpc.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runtime helpers so ExtraNPC can load on Paper 1.21.x and the 26.x line (26.1, 26.2, …).
 */
public final class ServerCompat {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

    private static final int[] DETECTED = detect();

    private ServerCompat() {
    }

    /**
     * @return {major, minor, patch} — e.g. 1.21.11 → [1,21,11], 26.1.2 → [26,1,2]
     */
    public static int[] minecraftVersion() {
        return DETECTED.clone();
    }

    public static String minecraftVersionLabel() {
        int[] v = DETECTED;
        if (v[2] > 0) {
            return v[0] + "." + v[1] + "." + v[2];
        }
        return v[0] + "." + v[1];
    }

    /**
     * True for Minecraft 26.1 and every later modern drop (26.2, 27.x, …).
     */
    public static boolean isModern26() {
        return DETECTED[0] >= 26;
    }

    public static boolean isAtLeast(int major, int minor, int patch) {
        int[] v = DETECTED;
        if (v[0] != major) {
            return v[0] > major;
        }
        if (v[1] != minor) {
            return v[1] > minor;
        }
        return v[2] >= patch;
    }

    public static boolean hasMannequin() {
        try {
            Class.forName("org.bukkit.entity.Mannequin");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public static boolean hasTextDisplay() {
        try {
            Class.forName("org.bukkit.entity.TextDisplay");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public static EntityType entityType(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String key = normalize(name);
        try {
            return EntityType.valueOf(key);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            EntityType named = EntityType.fromName(key.toLowerCase(Locale.ROOT));
            if (named != null) {
                return named;
            }
        } catch (Throwable ignored) {
        }
        try {
            Object found = registryGet("ENTITY_TYPE", key.toLowerCase(Locale.ROOT));
            if (found instanceof EntityType type) {
                return type;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static Material material(String name, Material fallback) {
        if (name == null || name.isBlank()) {
            return fallback;
        }
        Material match = Material.matchMaterial(name);
        return match != null ? match : fallback;
    }

    public static Particle particle(String name) {
        if (name == null || name.isBlank() || "NONE".equalsIgnoreCase(name)) {
            return null;
        }
        String key = normalize(name);
        try {
            return Particle.valueOf(key);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            Object found = registryGet("PARTICLE_TYPE", key.toLowerCase(Locale.ROOT));
            if (found instanceof Particle particle) {
                return particle;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static Attribute movementSpeedAttribute() {
        Attribute direct = attributeByName("MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED");
        return direct;
    }

    public static Attribute attributeByName(String... names) {
        for (String name : names) {
            if (name == null || name.isBlank()) {
                continue;
            }
            String key = normalize(name);
            try {
                Object field = Attribute.class.getField(key).get(null);
                if (field instanceof Attribute attribute) {
                    return attribute;
                }
            } catch (Throwable ignored) {
            }
            try {
                Method method = Attribute.class.getMethod("valueOf", String.class);
                Object value = method.invoke(null, key);
                if (value instanceof Attribute attribute) {
                    return attribute;
                }
            } catch (Throwable ignored) {
            }
            try {
                Object found = registryGet("ATTRIBUTE", key.toLowerCase(Locale.ROOT));
                if (found instanceof Attribute attribute) {
                    return attribute;
                }
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    public static String pluginVersion(Plugin plugin) {
        try {
            Method meta = plugin.getClass().getMethod("getPluginMeta");
            Object pluginMeta = meta.invoke(plugin);
            Method version = pluginMeta.getClass().getMethod("getVersion");
            Object value = version.invoke(pluginMeta);
            if (value != null) {
                return value.toString();
            }
        } catch (Throwable ignored) {
        }
        try {
            return plugin.getDescription().getVersion();
        } catch (Throwable ignored) {
            return "unknown";
        }
    }

    public static String playerNpcEngineName() {
        return hasMannequin() ? "Mannequin" : "ArmorStand";
    }

    private static Object registryGet(String fieldName, String minecraftKey) throws Exception {
        Object registry = Registry.class.getField(fieldName).get(null);
        Method get = registry.getClass().getMethod("get", NamespacedKey.class);
        return get.invoke(registry, NamespacedKey.minecraft(minecraftKey));
    }

    private static String normalize(String raw) {
        return raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }

    private static int[] detect() {
        String[] candidates = {
                safeCall("getMinecraftVersion"),
                safeBukkitVersion(),
                Bukkit.getVersion(),
                Bukkit.getBukkitVersion()
        };
        for (String candidate : candidates) {
            int[] parsed = parse(candidate);
            if (parsed != null) {
                return parsed;
            }
        }
        return new int[]{1, 21, 0};
    }

    private static String safeCall(String method) {
        try {
            Method m = Bukkit.class.getMethod(method);
            Object value = m.invoke(null);
            return value != null ? value.toString() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String safeBukkitVersion() {
        try {
            return Bukkit.getBukkitVersion();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static int[] parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        Matcher matcher = VERSION_PATTERN.matcher(raw);
        int[] best = null;
        while (matcher.find()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            int patch = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
            if (major == 1 && minor >= 8 && minor <= 21) {
                best = new int[]{major, minor, patch};
            } else if (major >= 26 && major < 100) {
                return new int[]{major, minor, patch};
            }
        }
        return best;
    }
}

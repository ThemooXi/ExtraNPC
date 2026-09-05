package me.themoo.extranpc.compat;

import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Runtime server detection for Paper 1.21.x through 26.1 / 26.2+.
 */
public final class ServerCompat {

    private static final ServerVersionParser.Version VERSION = detect();
    private static final String RAW = rawVersion();

    private ServerCompat() {
    }

    public static ServerVersionParser.Version version() {
        return VERSION;
    }

    public static String rawVersion() {
        try {
            Method method = Bukkit.class.getMethod("getMinecraftVersion");
            Object value = method.invoke(null);
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value);
            }
        } catch (Throwable ignored) {
        }
        try {
            return Bukkit.getBukkitVersion();
        } catch (Throwable ignored) {
            return "unknown";
        }
    }

    public static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            return false;
        }
    }

    public static boolean hasMannequin() {
        return hasClass("org.bukkit.entity.Mannequin")
                && (hasClass("io.papermc.paper.datacomponent.item.ResolvableProfile")
                || hasClass("io.papermc.paper.profile.ResolvableProfile"));
    }

    public static boolean hasTextDisplay() {
        return hasClass("org.bukkit.entity.TextDisplay");
    }

    public static boolean supportsYearLine() {
        return VERSION.isYearLine() || VERSION.isAtLeast(26, 1, 0);
    }

    public static void logDetected(Logger logger) {
        logger.info("Server: " + RAW + " (" + ServerVersionParser.describeFamily(VERSION) + ")");
        logger.info("Player NPC backend: " + (hasMannequin() ? "Mannequin (real player model)" : "ArmorStand fallback"));
        logger.info("Supported range: Paper 1.21.x through 26.1 / 26.2+");
    }

    private static ServerVersionParser.Version detect() {
        return ServerVersionParser.parse(rawVersion());
    }
}

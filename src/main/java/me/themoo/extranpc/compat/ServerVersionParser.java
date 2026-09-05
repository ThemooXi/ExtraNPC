package me.themoo.extranpc.compat;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Paper / Minecraft version strings, including the 1.21.x line and the
 * year-based 26.x line (26.1, 26.2, …).
 */
public final class ServerVersionParser {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?");

    private ServerVersionParser() {
    }

    public static Version parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Version.UNKNOWN;
        }
        String cleaned = raw.trim();
        int dash = cleaned.indexOf('-');
        if (dash > 0) {
            cleaned = cleaned.substring(0, dash);
        }
        int space = cleaned.indexOf(' ');
        if (space > 0) {
            cleaned = cleaned.substring(0, space);
        }
        Matcher matcher = VERSION_PATTERN.matcher(cleaned);
        if (!matcher.find()) {
            return Version.UNKNOWN;
        }
        int major = Integer.parseInt(matcher.group(1));
        int minor = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
        int patch = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
        return new Version(major, minor, patch);
    }

    public static boolean isAtLeast(Version current, int major, int minor, int patch) {
        return current.compareTo(new Version(major, minor, patch)) >= 0;
    }

    public record Version(int major, int minor, int patch) implements Comparable<Version> {

        public static final Version UNKNOWN = new Version(0, 0, 0);

        public boolean isUnknown() {
            return major == 0 && minor == 0 && patch == 0;
        }

        /**
         * Year-based Minecraft / Paper line (26.1, 26.2, …).
         */
        public boolean isYearLine() {
            return major >= 26;
        }

        public boolean isAtLeast(int major, int minor, int patch) {
            return compareTo(new Version(major, minor, patch)) >= 0;
        }

        @Override
        public int compareTo(Version other) {
            int byMajor = Integer.compare(this.major, other.major);
            if (byMajor != 0) {
                return byMajor;
            }
            int byMinor = Integer.compare(this.minor, other.minor);
            if (byMinor != 0) {
                return byMinor;
            }
            return Integer.compare(this.patch, other.patch);
        }

        @Override
        public String toString() {
            if (isUnknown()) {
                return "unknown";
            }
            return major + "." + minor + "." + patch;
        }

        public String compact() {
            if (isUnknown()) {
                return "unknown";
            }
            if (patch == 0) {
                return major + "." + minor;
            }
            return toString();
        }
    }

    public static String describeFamily(Version version) {
        if (version.isUnknown()) {
            return "unknown";
        }
        if (version.isYearLine()) {
            return "Paper " + version.major + "." + version.minor;
        }
        return "Minecraft " + version.major + "." + version.minor + ".x";
    }

    public static String normalizeKey(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }
}

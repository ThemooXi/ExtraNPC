package me.themoo.extranpc.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class TextUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private TextUtil() {
    }

    public static Component parse(String input) {
        if (input == null || input.isBlank()) {
            return Component.empty();
        }
        String value = input;
        if (value.indexOf('&') >= 0 && value.indexOf('<') < 0) {
            return LEGACY.deserialize(value).decoration(TextDecoration.ITALIC, false);
        }
        return MINI.deserialize(value).decoration(TextDecoration.ITALIC, false);
    }

    public static String strip(String input) {
        if (input == null) {
            return "";
        }
        return MINI.stripTags(input.replace('&', '§')).replace('§', ' ');
    }
}

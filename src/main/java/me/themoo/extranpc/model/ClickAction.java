package me.themoo.extranpc.model;

import java.util.Locale;

public enum ClickAction {
    LEFT,
    RIGHT,
    BOTH;

    public static ClickAction fromString(String raw, ClickAction def) {
        if (raw == null || raw.isBlank()) {
            return def;
        }
        try {
            return ClickAction.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return def;
        }
    }
}

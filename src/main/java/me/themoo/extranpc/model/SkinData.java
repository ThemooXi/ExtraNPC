package me.themoo.extranpc.model;

import java.util.Locale;

public final class SkinData {

    public enum Mode {
        NONE,
        PLAYER_NAME,
        URL,
        TEXTURE
    }

    private Mode mode = Mode.NONE;
    private String value = "";
    private String texture = "";
    private String signature = "";

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode == null ? Mode.NONE : mode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value == null ? "" : value;
    }

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture == null ? "" : texture;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature == null ? "" : signature;
    }

    public boolean hasTexture() {
        return texture != null && !texture.isBlank();
    }

    public static Mode modeFromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return Mode.NONE;
        }
        try {
            return Mode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return Mode.NONE;
        }
    }
}

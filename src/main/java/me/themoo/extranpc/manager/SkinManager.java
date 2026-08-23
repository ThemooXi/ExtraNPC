package me.themoo.extranpc.manager;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.SkinData;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SkinManager {

    private static final Pattern UUID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"(.*?)\"");
    private static final Pattern VALUE_PATTERN = Pattern.compile("\"value\"\\s*:\\s*\"(.*?)\"");
    private static final Pattern SIGNATURE_PATTERN = Pattern.compile("\"signature\"\\s*:\\s*\"(.*?)\"");

    private final ExtraNPCPlugin plugin;

    public SkinManager(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
    }

    public void fetchByPlayerName(String name, Consumer<SkinData> callback) {
        CompletableFuture.runAsync(() -> {
            try {
                String uuidJson = httpGet("https://api.mojang.com/users/profiles/minecraft/" + name);
                Matcher uuidMatcher = UUID_PATTERN.matcher(uuidJson);
                if (!uuidMatcher.find()) {
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(null));
                    return;
                }
                String uuid = uuidMatcher.group(1);
                String profileJson = httpGet("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
                Matcher valueMatcher = VALUE_PATTERN.matcher(profileJson);
                Matcher signatureMatcher = SIGNATURE_PATTERN.matcher(profileJson);
                if (!valueMatcher.find()) {
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(null));
                    return;
                }
                SkinData skin = new SkinData();
                skin.setMode(SkinData.Mode.PLAYER_NAME);
                skin.setValue(name);
                skin.setTexture(valueMatcher.group(1));
                if (signatureMatcher.find()) {
                    skin.setSignature(signatureMatcher.group(1));
                }
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(skin));
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Failed to fetch skin for " + name, ex);
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(null));
            }
        });
    }

    public void applyUrlSkin(String url, Consumer<SkinData> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String payload = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
                String texture = Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
                SkinData skin = new SkinData();
                skin.setMode(SkinData.Mode.URL);
                skin.setValue(url);
                skin.setTexture(texture);
                skin.setSignature("");
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(skin));
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Failed to apply URL skin", ex);
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(null));
            }
        });
    }

    public ItemStack createSkull(SkinData skin, String fallbackName) {
        ItemStack skull = new ItemStack(org.bukkit.Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) {
            return skull;
        }
        PlayerProfile profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(("ExtraNPC:" + fallbackName).getBytes(StandardCharsets.UTF_8)), fallbackName);
        if (skin != null && skin.hasTexture()) {
            if (skin.getSignature() != null && !skin.getSignature().isBlank()) {
                profile.setProperty(new ProfileProperty("textures", skin.getTexture(), skin.getSignature()));
            } else {
                profile.setProperty(new ProfileProperty("textures", skin.getTexture()));
                try {
                    PlayerTextures textures = profile.getTextures();
                    if (skin.getMode() == SkinData.Mode.URL && skin.getValue() != null && !skin.getValue().isBlank()) {
                        textures.setSkin(URI.create(skin.getValue()).toURL());
                        profile.setTextures(textures);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        meta.setPlayerProfile(profile);
        skull.setItemMeta(meta);
        return skull;
    }

    private String httpGet(String urlString) throws Exception {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        connection.setRequestProperty("User-Agent", "ExtraNPC/1.0");
        connection.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } finally {
            connection.disconnect();
        }
    }
}

package me.themoo.extranpc.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class NpcData {

    private final String id;
    private String displayName;
    private NpcType type;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private boolean showName = true;
    private boolean invulnerable = true;
    private boolean gravity = false;
    private boolean silent = true;
    private boolean glowing = false;
    private boolean lookAtPlayers = true;
    private boolean collidable = false;
    private boolean baby = false;
    private boolean shopEnabled = false;
    private boolean adminShop = true;
    private double lookRange = 8.0;
    private int cooldownSeconds = 0;
    private String permission = "";
    private String particle = "NONE";
    private SkinData skin = new SkinData();
    private final List<String> leftCommands = new ArrayList<>();
    private final List<String> rightCommands = new ArrayList<>();
    private final List<String> hologramLines = new ArrayList<>();
    private final List<ShopTrade> trades = new ArrayList<>();
    private UUID entityUuid;
    private Integer citizensId;

    public NpcData(String id) {
        this.id = id;
        this.displayName = "<yellow>" + id + "</yellow>";
        this.type = NpcType.PLAYER;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName == null ? id : displayName;
    }

    public NpcType getType() {
        return type;
    }

    public void setType(NpcType type) {
        this.type = type == null ? NpcType.PLAYER : type;
    }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean isShowName() {
        return showName;
    }

    public void setShowName(boolean showName) {
        this.showName = showName;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public boolean isGravity() {
        return gravity;
    }

    public void setGravity(boolean gravity) {
        this.gravity = gravity;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    public boolean isLookAtPlayers() {
        return lookAtPlayers;
    }

    public void setLookAtPlayers(boolean lookAtPlayers) {
        this.lookAtPlayers = lookAtPlayers;
    }

    public boolean isCollidable() {
        return collidable;
    }

    public void setCollidable(boolean collidable) {
        this.collidable = collidable;
    }

    public boolean isBaby() {
        return baby;
    }

    public void setBaby(boolean baby) {
        this.baby = baby;
    }

    public boolean isShopEnabled() {
        return shopEnabled;
    }

    public void setShopEnabled(boolean shopEnabled) {
        this.shopEnabled = shopEnabled;
    }

    public boolean isAdminShop() {
        return adminShop;
    }

    public void setAdminShop(boolean adminShop) {
        this.adminShop = adminShop;
    }

    public double getLookRange() {
        return lookRange;
    }

    public void setLookRange(double lookRange) {
        this.lookRange = Math.max(1.0, lookRange);
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = Math.max(0, cooldownSeconds);
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission == null ? "" : permission;
    }

    public String getParticle() {
        return particle;
    }

    public void setParticle(String particle) {
        this.particle = particle == null ? "NONE" : particle;
    }

    public SkinData getSkin() {
        return skin;
    }

    public void setSkin(SkinData skin) {
        this.skin = skin == null ? new SkinData() : skin;
    }

    public List<String> getLeftCommands() {
        return leftCommands;
    }

    public List<String> getRightCommands() {
        return rightCommands;
    }

    public List<String> getHologramLines() {
        return hologramLines;
    }

    public List<ShopTrade> getTrades() {
        return trades;
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    public void setEntityUuid(UUID entityUuid) {
        this.entityUuid = entityUuid;
    }

    public Integer getCitizensId() {
        return citizensId;
    }

    public void setCitizensId(Integer citizensId) {
        this.citizensId = citizensId;
    }

    public void save(FileConfiguration config) {
        String path = "npcs." + id + ".";
        config.set(path + "name", displayName);
        config.set(path + "type", type.name());
        config.set(path + "world", worldName);
        config.set(path + "x", x);
        config.set(path + "y", y);
        config.set(path + "z", z);
        config.set(path + "yaw", yaw);
        config.set(path + "pitch", pitch);
        config.set(path + "show-name", showName);
        config.set(path + "invulnerable", invulnerable);
        config.set(path + "gravity", gravity);
        config.set(path + "silent", silent);
        config.set(path + "glowing", glowing);
        config.set(path + "look-at-players", lookAtPlayers);
        config.set(path + "collidable", collidable);
        config.set(path + "baby", baby);
        config.set(path + "shop-enabled", shopEnabled);
        config.set(path + "admin-shop", adminShop);
        config.set(path + "look-range", lookRange);
        config.set(path + "cooldown", cooldownSeconds);
        config.set(path + "permission", permission);
        config.set(path + "particle", particle);
        config.set(path + "citizens-id", citizensId);
        config.set(path + "left-commands", leftCommands);
        config.set(path + "right-commands", rightCommands);
        config.set(path + "hologram", hologramLines);
        config.set(path + "skin.mode", skin.getMode().name());
        config.set(path + "skin.value", skin.getValue());
        config.set(path + "skin.texture", skin.getTexture());
        config.set(path + "skin.signature", skin.getSignature());
        config.set(path + "trades", null);
        for (int i = 0; i < trades.size(); i++) {
            trades.get(i).save(config.createSection(path + "trades." + i));
        }
    }

    public static NpcData load(String id, ConfigurationSection section) {
        NpcData data = new NpcData(id);
        data.setDisplayName(section.getString("name", "<yellow>" + id + "</yellow>"));
        data.setType(NpcType.fromString(section.getString("type", "PLAYER")).orElse(NpcType.PLAYER));
        data.worldName = section.getString("world", "world");
        data.x = section.getDouble("x");
        data.y = section.getDouble("y");
        data.z = section.getDouble("z");
        data.yaw = (float) section.getDouble("yaw");
        data.pitch = (float) section.getDouble("pitch");
        data.showName = section.getBoolean("show-name", true);
        data.invulnerable = section.getBoolean("invulnerable", true);
        data.gravity = section.getBoolean("gravity", false);
        data.silent = section.getBoolean("silent", true);
        data.glowing = section.getBoolean("glowing", false);
        data.lookAtPlayers = section.getBoolean("look-at-players", true);
        data.collidable = section.getBoolean("collidable", false);
        data.baby = section.getBoolean("baby", false);
        data.shopEnabled = section.getBoolean("shop-enabled", false);
        data.adminShop = section.getBoolean("admin-shop", true);
        data.lookRange = section.getDouble("look-range", 8.0);
        data.cooldownSeconds = section.getInt("cooldown", 0);
        data.permission = section.getString("permission", "");
        data.particle = section.getString("particle", "NONE");
        if (section.contains("citizens-id")) {
            data.citizensId = section.getInt("citizens-id");
        }
        data.leftCommands.clear();
        data.leftCommands.addAll(section.getStringList("left-commands"));
        data.rightCommands.clear();
        data.rightCommands.addAll(section.getStringList("right-commands"));
        data.hologramLines.clear();
        data.hologramLines.addAll(section.getStringList("hologram"));

        SkinData skin = new SkinData();
        skin.setMode(SkinData.modeFromString(section.getString("skin.mode", "NONE")));
        skin.setValue(section.getString("skin.value", ""));
        skin.setTexture(section.getString("skin.texture", ""));
        skin.setSignature(section.getString("skin.signature", ""));
        data.setSkin(skin);

        ConfigurationSection tradesSection = section.getConfigurationSection("trades");
        if (tradesSection != null) {
            for (String key : tradesSection.getKeys(false)) {
                ShopTrade trade = ShopTrade.load(tradesSection.getConfigurationSection(key));
                if (trade != null && trade.isValid()) {
                    data.trades.add(trade);
                }
            }
        }
        return data;
    }
}

package me.themoo.extranpc.integration;

import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.model.SkinData;
import me.themoo.extranpc.util.TextUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Player-like NPCs using Paper's Mannequin entity (real player model, not ArmorStand).
 */
public final class NativePlayerNpcProvider implements PlayerNpcProvider {

    private final ExtraNPCPlugin plugin;
    private final boolean available;

    public NativePlayerNpcProvider(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
        boolean ok = me.themoo.extranpc.util.ServerCompat.hasMannequin();
        if (ok) {
            plugin.getLogger().info("Mannequin player NPCs ready (real player model).");
        }
        this.available = ok;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void spawn(NpcData data) {
        despawn(data);

        if (!available) {
            plugin.getLogger().severe("Cannot spawn PLAYER NPC '" + data.getId() + "' — Mannequin unavailable.");
            return;
        }

        Location location = data.getLocation();
        if (location == null || location.getWorld() == null) {
            plugin.getLogger().warning("Invalid location for NPC " + data.getId());
            return;
        }

        Mannequin mannequin = location.getWorld().spawn(location, Mannequin.class, entity -> {
            configure(entity, data);
            try {
                applyProfile(entity, data);
            } catch (Throwable ex) {
                plugin.getLogger().warning("Failed to apply skin for '" + data.getId() + "': " + ex.getMessage());
            }
        });

        data.setEntityUuid(mannequin.getUniqueId());
        plugin.getLogger().info("Spawned PLAYER NPC '" + data.getId() + "' as Mannequin");
    }

    private void configure(Mannequin entity, NpcData data) {
        entity.setPersistent(true);
        entity.setRemoveWhenFarAway(false);
        entity.setAI(false);
        entity.setImmovable(true);
        entity.setGravity(data.isGravity());
        entity.setInvulnerable(data.isInvulnerable());
        entity.setCollidable(data.isCollidable());
        entity.setSilent(data.isSilent());
        entity.setGlowing(data.isGlowing());
        entity.customName(TextUtil.parse(data.getDisplayName()));
        entity.setCustomNameVisible(data.isShowName());
        // Hide default "NPC" subtitle under the name
        entity.setDescription(null);
        entity.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "npc-id"),
                PersistentDataType.STRING,
                data.getId()
        );
        try {
            Attribute speedAttr = me.themoo.extranpc.util.ServerCompat.movementSpeedAttribute();
            if (speedAttr != null) {
                var speed = entity.getAttribute(speedAttr);
                if (speed != null) {
                    speed.setBaseValue(0.0);
                }
            }
        } catch (Throwable ignored) {
        }
        entity.setRotation(data.getLocation().getYaw(), data.getLocation().getPitch());
    }

    private void applyProfile(Mannequin entity, NpcData data) {
        SkinData skin = data.getSkin();
        String profileName = profileName(data);
        UUID uuid = UUID.nameUUIDFromBytes(("ExtraNPC:" + data.getId()).getBytes(StandardCharsets.UTF_8));

        ResolvableProfile.Builder builder = ResolvableProfile.resolvableProfile()
                .name(profileName)
                .uuid(uuid);

        if (skin != null && skin.hasTexture()) {
            if (skin.getSignature() != null && !skin.getSignature().isBlank()) {
                builder.addProperty(new ProfileProperty("textures", skin.getTexture(), skin.getSignature()));
            } else {
                builder.addProperty(new ProfileProperty("textures", skin.getTexture()));
            }
            entity.setProfile(builder.build());
            return;
        }

        // Resolve by Minecraft username if set
        if (skin != null && skin.getMode() == SkinData.Mode.PLAYER_NAME
                && skin.getValue() != null && !skin.getValue().isBlank()) {
            String name = sanitizeName(skin.getValue());
            ResolvableProfile dynamic = ResolvableProfile.resolvableProfile().name(name).build();
            entity.setProfile(dynamic);
            dynamic.resolve().thenAcceptAsync(updated -> {
                if (!entity.isValid()) {
                    return;
                }
                ResolvableProfile.Builder resolved = ResolvableProfile.resolvableProfile()
                        .name(updated.getName() != null ? sanitizeName(updated.getName()) : name)
                        .uuid(updated.getId() != null ? updated.getId() : uuid);
                for (ProfileProperty property : updated.getProperties()) {
                    resolved.addProperty(property);
                }
                entity.setProfile(resolved.build());
                // Persist textures for next spawn
                for (ProfileProperty property : updated.getProperties()) {
                    if ("textures".equalsIgnoreCase(property.getName())) {
                        skin.setTexture(property.getValue());
                        if (property.getSignature() != null) {
                            skin.setSignature(property.getSignature());
                        }
                        break;
                    }
                }
            }, runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable));
            return;
        }

        entity.setProfile(builder.build());
    }

    private String profileName(NpcData data) {
        SkinData skin = data.getSkin();
        if (skin != null && skin.getMode() == SkinData.Mode.PLAYER_NAME
                && skin.getValue() != null && !skin.getValue().isBlank()) {
            return sanitizeName(skin.getValue());
        }
        String plain = PlainTextComponentSerializer.plainText().serialize(TextUtil.parse(data.getDisplayName()));
        if (!plain.isBlank()) {
            return sanitizeName(plain);
        }
        return sanitizeName(data.getId());
    }

    private static String sanitizeName(String raw) {
        if (raw == null || raw.isBlank()) {
            return "NPC";
        }
        String clean = raw.replaceAll("[^A-Za-z0-9_]", "");
        if (clean.isBlank()) {
            clean = "NPC";
        }
        return clean.length() > 16 ? clean.substring(0, 16) : clean;
    }

    @Override
    public void despawn(NpcData data) {
        if (data.getEntityUuid() != null) {
            Entity entity = plugin.getServer().getEntity(data.getEntityUuid());
            if (entity != null) {
                entity.remove();
            }
        }
        data.setEntityUuid(null);
        data.setCitizensId(null);
    }

    @Override
    public void move(NpcData data, Location location) {
        data.setLocation(location);
        Entity entity = getEntity(data);
        if (entity != null) {
            entity.teleport(location);
        } else {
            spawn(data);
        }
    }

    @Override
    public void applySkin(NpcData data) {
        Entity entity = getEntity(data);
        if (entity instanceof Mannequin mannequin) {
            applyProfile(mannequin, data);
        } else {
            spawn(data);
        }
    }

    @Override
    public void applySettings(NpcData data) {
        Entity entity = getEntity(data);
        if (!(entity instanceof Mannequin mannequin)) {
            spawn(data);
            return;
        }
        mannequin.customName(TextUtil.parse(data.getDisplayName()));
        mannequin.setCustomNameVisible(data.isShowName());
        mannequin.setGlowing(data.isGlowing());
        mannequin.setGravity(data.isGravity());
        mannequin.setSilent(data.isSilent());
        mannequin.setInvulnerable(data.isInvulnerable());
        mannequin.setCollidable(data.isCollidable());
        mannequin.setImmovable(true);
        mannequin.setDescription(null);
    }

    @Override
    public Entity getEntity(NpcData data) {
        if (data.getEntityUuid() == null) {
            return null;
        }
        return plugin.getServer().getEntity(data.getEntityUuid());
    }

    @Override
    public void tickLook(NpcData data) {
        if (!data.isLookAtPlayers()) {
            return;
        }
        Entity entity = getEntity(data);
        if (entity == null || data.getLocation() == null) {
            return;
        }
        Location base = entity.getLocation();
        Player nearest = null;
        double best = data.getLookRange() * data.getLookRange();
        for (Player player : base.getWorld().getPlayers()) {
            double d = player.getLocation().distanceSquared(base);
            if (d <= best) {
                best = d;
                nearest = player;
            }
        }
        if (nearest == null) {
            return;
        }
        Vector dir = nearest.getEyeLocation().toVector().subtract(base.clone().add(0, 1.6, 0).toVector());
        Location look = base.clone().setDirection(dir);
        entity.setRotation(look.getYaw(), look.getPitch());
    }
}

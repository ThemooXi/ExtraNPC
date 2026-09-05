package me.themoo.extranpc.integration;

import com.destroystokyo.paper.profile.ProfileProperty;
import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.model.SkinData;
import me.themoo.extranpc.util.TextUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Uses Paper's Mannequin when the running server provides it and a head-only
 * ArmorStand fallback on older supported releases. New APIs stay behind
 * reflection so this class can load on every Paper 1.21+ server.
 */
public final class NativePlayerNpcProvider implements PlayerNpcProvider {

    private final ExtraNPCPlugin plugin;
    private final Class<? extends Entity> mannequinClass;

    public NativePlayerNpcProvider(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
        Class<? extends Entity> detected = null;
        try {
            Class<? extends Entity> candidate =
                    Class.forName("org.bukkit.entity.Mannequin").asSubclass(Entity.class);
            verifyMannequinApi(candidate);
            detected = candidate;
            plugin.getLogger().info("Mannequin player NPCs ready (real player model).");
        } catch (ReflectiveOperationException | LinkageError ex) {
            plugin.getLogger().warning(
                    "Mannequin API is unavailable; using the legacy player-head NPC fallback."
            );
        }
        this.mannequinClass = detected;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void spawn(NpcData data) {
        despawn(data);

        Location location = data.getLocation();
        if (location == null || location.getWorld() == null) {
            plugin.getLogger().warning("Invalid location for NPC " + data.getId());
            return;
        }

        Entity entity;
        if (mannequinClass != null) {
            entity = spawnMannequin(location, mannequinClass, data);
            plugin.getLogger().info("Spawned PLAYER NPC '" + data.getId() + "' as Mannequin");
        } else {
            entity = spawnLegacy(location, data);
            plugin.getLogger().info("Spawned PLAYER NPC '" + data.getId() + "' in legacy fallback mode");
        }
        data.setEntityUuid(entity.getUniqueId());
    }

    private <T extends Entity> T spawnMannequin(Location location, Class<T> type, NpcData data) {
        return location.getWorld().spawn(location, type, entity -> {
            configure((LivingEntity) entity, data);
            configureMannequin(entity);
            applyProfile(entity, data);
        });
    }

    private ArmorStand spawnLegacy(Location location, NpcData data) {
        return location.getWorld().spawn(location, ArmorStand.class, entity -> {
            configure(entity, data);
            entity.setVisible(false);
            entity.setBasePlate(false);
            entity.setArms(false);
            entity.setSmall(false);
            entity.setMarker(false);
            entity.setCanPickupItems(false);
            applyLegacySkin(entity, data);
        });
    }

    private void configure(LivingEntity entity, NpcData data) {
        entity.setPersistent(true);
        entity.setRemoveWhenFarAway(false);
        entity.setAI(false);
        entity.setGravity(data.isGravity());
        entity.setInvulnerable(data.isInvulnerable());
        entity.setCollidable(data.isCollidable());
        entity.setSilent(data.isSilent());
        entity.setGlowing(data.isGlowing());
        entity.customName(TextUtil.parse(data.getDisplayName()));
        entity.setCustomNameVisible(data.isShowName());
        entity.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "npc-id"),
                PersistentDataType.STRING,
                data.getId()
        );
        entity.setRotation(data.getLocation().getYaw(), data.getLocation().getPitch());
    }

    private void configureMannequin(Entity entity) {
        try {
            mannequinClass.getMethod("setImmovable", boolean.class).invoke(entity, true);
            mannequinClass.getMethod("setDescription", net.kyori.adventure.text.Component.class)
                    .invoke(entity, new Object[]{null});
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(
                    java.util.logging.Level.WARNING,
                    "Failed to configure the Mannequin NPC",
                    ex
            );
        }
    }

    private void applyProfile(Entity entity, NpcData data) {
        SkinData skin = data.getSkin();
        String profileName = profileName(data);
        UUID uuid = UUID.nameUUIDFromBytes(("ExtraNPC:" + data.getId()).getBytes(StandardCharsets.UTF_8));

        try {
            Class<?> profileType = Class.forName("io.papermc.paper.datacomponent.item.ResolvableProfile");
            Class<?> builderType = Class.forName(
                    "io.papermc.paper.datacomponent.item.ResolvableProfile$Builder"
            );
            Object builder = profileType.getMethod("resolvableProfile").invoke(null);
            builderType.getMethod("name", String.class).invoke(builder, profileName);
            builderType.getMethod("uuid", UUID.class).invoke(builder, uuid);

            if (skin != null && skin.hasTexture()) {
                ProfileProperty property;
                if (skin.getSignature() != null && !skin.getSignature().isBlank()) {
                    property = new ProfileProperty("textures", skin.getTexture(), skin.getSignature());
                } else {
                    property = new ProfileProperty("textures", skin.getTexture());
                }
                builderType.getMethod("addProperty", ProfileProperty.class).invoke(builder, property);
            }

            Object profile = builderType.getMethod("build").invoke(builder);
            mannequinClass.getMethod("setProfile", profileType).invoke(entity, profile);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(
                    java.util.logging.Level.WARNING,
                    "Failed to apply the profile for NPC '" + data.getId() + "'",
                    ex
            );
            return;
        }

        if (skin != null && skin.getMode() == SkinData.Mode.PLAYER_NAME
                && !skin.hasTexture()
                && skin.getValue() != null && !skin.getValue().isBlank()) {
            plugin.getSkinManager().fetchByPlayerName(skin.getValue(), resolved -> {
                if (resolved == null || !entity.isValid()) {
                    return;
                }
                data.setSkin(resolved);
                applyProfile(entity, data);
                plugin.getNpcManager().save(data);
            });
        }
    }

    private void applyLegacySkin(ArmorStand entity, NpcData data) {
        if (entity.getEquipment() != null) {
            entity.getEquipment().setHelmet(
                    plugin.getSkinManager().createSkull(data.getSkin(), profileName(data))
            );
        }
    }

    private static void verifyMannequinApi(Class<? extends Entity> type)
            throws ReflectiveOperationException {
        Class<?> profileType = Class.forName("io.papermc.paper.datacomponent.item.ResolvableProfile");
        Class<?> builderType = Class.forName(
                "io.papermc.paper.datacomponent.item.ResolvableProfile$Builder"
        );
        profileType.getMethod("resolvableProfile");
        builderType.getMethod("name", String.class);
        builderType.getMethod("uuid", UUID.class);
        builderType.getMethod("addProperty", ProfileProperty.class);
        builderType.getMethod("build");
        type.getMethod("setProfile", profileType);
        type.getMethod("setImmovable", boolean.class);
        type.getMethod("setDescription", net.kyori.adventure.text.Component.class);
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
        if (entity == null) {
            spawn(data);
        } else if (mannequinClass != null && mannequinClass.isInstance(entity)) {
            applyProfile(entity, data);
        } else if (entity instanceof ArmorStand armorStand) {
            applyLegacySkin(armorStand, data);
        } else {
            spawn(data);
        }
    }

    @Override
    public void applySettings(NpcData data) {
        Entity entity = getEntity(data);
        if (!(entity instanceof LivingEntity living)) {
            spawn(data);
            return;
        }
        configure(living, data);
        if (mannequinClass != null && mannequinClass.isInstance(entity)) {
            configureMannequin(entity);
        } else if (entity instanceof ArmorStand armorStand) {
            armorStand.setVisible(false);
            armorStand.setBasePlate(false);
            armorStand.setMarker(false);
        }
    }

    @Override
    public Entity getEntity(NpcData data) {
        if (data.getEntityUuid() == null) {
            return null;
        }
        return plugin.getServer().getEntity(data.getEntityUuid());
    }

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

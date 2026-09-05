package me.themoo.extranpc.integration;

import com.destroystokyo.paper.profile.ProfileProperty;
import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.model.SkinData;
import me.themoo.extranpc.util.ServerCompatibility;
import me.themoo.extranpc.util.TextUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Player-like NPCs. Paper's Mannequin is loaded reflectively on versions that
 * provide it; older supported versions use an ArmorStand with the selected skin.
 */
public final class NativePlayerNpcProvider implements PlayerNpcProvider {

    private final ExtraNPCPlugin plugin;
    private final Class<? extends Entity> mannequinClass;
    private boolean profileWarningLogged;

    @SuppressWarnings("unchecked")
    public NativePlayerNpcProvider(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
        Class<? extends Entity> detected = null;
        try {
            Class<?> type = Class.forName("org.bukkit.entity.Mannequin", false, getClass().getClassLoader());
            if (Entity.class.isAssignableFrom(type)) {
                detected = (Class<? extends Entity>) type;
            }
        } catch (ClassNotFoundException | LinkageError ignored) {
            // Expected on Paper versions released before Mannequin.
        }
        this.mannequinClass = detected;
        if (mannequinClass != null) {
            plugin.getLogger().info("Player NPC backend: native Paper Mannequin.");
        } else {
            plugin.getLogger().info("Player NPC backend: compatible ArmorStand fallback (Mannequin is unavailable).");
        }
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

        Entity entity = spawnMannequin(location, data);
        if (entity == null) {
            entity = location.getWorld().spawn(location, ArmorStand.class, armorStand -> {
                configure(armorStand, data);
                configureFallback(armorStand, data);
            });
        }

        data.setEntityUuid(entity.getUniqueId());
    }

    private Entity spawnMannequin(Location location, NpcData data) {
        if (mannequinClass == null) {
            return null;
        }
        try {
            return location.getWorld().spawn(location, mannequinClass, entity -> {
                configure(entity, data);
                invokeOptional(entity, "setImmovable", boolean.class, true);
                invokeNullableSetter(entity, "setDescription");
                applyProfile(entity, data);
            });
        } catch (RuntimeException | LinkageError ex) {
            plugin.getLogger().warning("Could not spawn a Mannequin for '" + data.getId()
                    + "'; using the compatible fallback: " + ex.getMessage());
            return null;
        }
    }

    private void configure(Entity entity, NpcData data) {
        entity.setPersistent(true);
        entity.setGravity(data.isGravity());
        entity.setInvulnerable(data.isInvulnerable());
        entity.setSilent(data.isSilent());
        entity.setGlowing(data.isGlowing());
        entity.customName(TextUtil.parse(data.getDisplayName()));
        entity.setCustomNameVisible(data.isShowName());
        entity.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "npc-id"),
                PersistentDataType.STRING,
                data.getId()
        );
        if (entity instanceof LivingEntity living) {
            living.setRemoveWhenFarAway(false);
            living.setAI(false);
            living.setCollidable(data.isCollidable());
            Attribute movementSpeed = ServerCompatibility.movementSpeedAttribute();
            if (movementSpeed != null) {
                var speed = living.getAttribute(movementSpeed);
                if (speed != null) {
                    speed.setBaseValue(0.0);
                }
            }
        }
        entity.setRotation(data.getLocation().getYaw(), data.getLocation().getPitch());
    }

    private void configureFallback(ArmorStand armorStand, NpcData data) {
        armorStand.setVisible(false);
        armorStand.setArms(true);
        armorStand.setBasePlate(false);
        armorStand.setMarker(false);
        EntityEquipment equipment = armorStand.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(plugin.getSkinManager().createSkull(data.getSkin(), profileName(data)));
            equipment.setHelmetDropChance(0.0f);
        }
    }

    private void applyProfile(Entity entity, NpcData data) {
        if (!isMannequin(entity)) {
            return;
        }
        SkinData skin = data.getSkin();
        String profileName = profileName(data);
        UUID uuid = UUID.nameUUIDFromBytes(("ExtraNPC:" + data.getId()).getBytes(StandardCharsets.UTF_8));

        try {
            Class<?> profileType = Class.forName("io.papermc.paper.datacomponent.item.ResolvableProfile");
            Object builder = profileType.getMethod("resolvableProfile").invoke(null);
            invokeBuilder(builder, "name", profileName);
            if (skin == null || skin.hasTexture() || skin.getMode() != SkinData.Mode.PLAYER_NAME) {
                invokeBuilder(builder, "uuid", uuid);
            }
            if (skin != null && skin.hasTexture()) {
                ProfileProperty property;
                if (skin.getSignature() != null && !skin.getSignature().isBlank()) {
                    property = new ProfileProperty("textures", skin.getTexture(), skin.getSignature());
                } else {
                    property = new ProfileProperty("textures", skin.getTexture());
                }
                invokeBuilder(builder, "addProperty", property);
            }
            Object profile = builder.getClass().getMethod("build").invoke(builder);
            invokeCompatibleSetter(entity, "setProfile", profile);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ex) {
            if (!profileWarningLogged) {
                profileWarningLogged = true;
                plugin.getLogger().warning("The server's Mannequin profile API is incompatible; "
                        + "player NPCs will use the default skin: " + ex.getMessage());
            }
        }
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
        if (isMannequin(entity)) {
            applyProfile(entity, data);
        } else if (entity instanceof ArmorStand armorStand) {
            configureFallback(armorStand, data);
        } else {
            spawn(data);
        }
    }

    @Override
    public void applySettings(NpcData data) {
        Entity entity = getEntity(data);
        if (entity == null) {
            spawn(data);
            return;
        }
        configure(entity, data);
        if (isMannequin(entity)) {
            invokeOptional(entity, "setImmovable", boolean.class, true);
            invokeNullableSetter(entity, "setDescription");
        } else if (entity instanceof ArmorStand armorStand) {
            configureFallback(armorStand, data);
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

    private boolean isMannequin(Entity entity) {
        return entity != null && mannequinClass != null && mannequinClass.isInstance(entity);
    }

    private static void invokeBuilder(Object target, String methodName, Object value)
            throws ReflectiveOperationException {
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(methodName)
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0].isAssignableFrom(value.getClass())) {
                method.invoke(target, value);
                return;
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    private static void invokeCompatibleSetter(Object target, String methodName, Object value)
            throws ReflectiveOperationException {
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(methodName)
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0].isAssignableFrom(value.getClass())) {
                method.invoke(target, value);
                return;
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    private static void invokeOptional(Object target, String methodName, Class<?> parameterType, Object value) {
        try {
            target.getClass().getMethod(methodName, parameterType).invoke(target, value);
        } catch (ReflectiveOperationException ignored) {
            // Capability is optional across supported server versions.
        }
    }

    private static void invokeNullableSetter(Object target, String methodName) {
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                try {
                    method.invoke(target, new Object[]{null});
                } catch (ReflectiveOperationException ignored) {
                    // Capability is optional across supported server versions.
                }
                return;
            }
        }
    }
}

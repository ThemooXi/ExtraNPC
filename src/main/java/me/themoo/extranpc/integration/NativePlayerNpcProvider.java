package me.themoo.extranpc.integration;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.model.SkinData;
import me.themoo.extranpc.util.TextUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Player-like NPCs using Paper's Mannequin entity when available.
 *
 * <p>The Mannequin API was added after the first 1.21 releases. It is
 * intentionally accessed through reflection so one jar can also run on older
 * Paper versions. Those versions use an interactive ArmorStand fallback.</p>
 */
public final class NativePlayerNpcProvider implements PlayerNpcProvider {

    private final ExtraNPCPlugin plugin;
    private final Class<?> mannequinClass;

    public NativePlayerNpcProvider(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
        Class<?> detected = null;
        try {
            detected = Class.forName("org.bukkit.entity.Mannequin", false,
                    plugin.getClass().getClassLoader());
            plugin.getLogger().info("Mannequin player NPCs ready (real player model).");
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().info("Mannequin is not available on this server; using ArmorStand player NPC fallback.");
        }
        this.mannequinClass = detected;
    }

    @Override
    public boolean isAvailable() {
        // ArmorStand exists on every supported Bukkit/Paper version.
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

        Entity entity = mannequinClass == null
                ? spawnArmorStand(location, data)
                : spawnMannequin(location, data);
        if (entity != null) {
            data.setEntityUuid(entity.getUniqueId());
            plugin.getLogger().info("Spawned PLAYER NPC '" + data.getId() + "' as "
                    + (isMannequin(entity) ? "Mannequin" : "ArmorStand fallback"));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Entity spawnMannequin(Location location, NpcData data) {
        try {
            return location.getWorld().spawn(location, (Class) mannequinClass, spawned -> {
                Entity entity = (Entity) spawned;
                configureMannequin(entity, data);
                applyProfile(entity, data);
            });
        } catch (Throwable ex) {
            plugin.getLogger().log(java.util.logging.Level.WARNING,
                    "Failed to spawn a Mannequin; using ArmorStand fallback.", ex);
            return spawnArmorStand(location, data);
        }
    }

    private Entity spawnArmorStand(Location location, NpcData data) {
        return location.getWorld().spawn(location, ArmorStand.class,
                entity -> configureArmorStand(entity, data));
    }

    private void configureMannequin(Entity entity, NpcData data) {
        configureLivingEntity((LivingEntity) entity, data);
        invokeCompatible(entity, "setImmovable", true);
        // Hide the built-in "NPC" subtitle where this API is present.
        invokeCompatible(entity, "setDescription", (Object) null);
    }

    private void configureArmorStand(ArmorStand entity, NpcData data) {
        configureLivingEntity(entity, data);
        entity.setVisible(true);
        entity.setArms(true);
        entity.setBasePlate(false);
        entity.setMarker(false);
        if (entity.getEquipment() != null) {
            entity.getEquipment().setHelmet(
                    plugin.getSkinManager().createSkull(data.getSkin(), data.getId()));
        }
    }

    private void configureLivingEntity(LivingEntity entity, NpcData data) {
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
        setMovementSpeedToZero(entity);
        entity.setRotation(data.getLocation().getYaw(), data.getLocation().getPitch());
    }

    private void setMovementSpeedToZero(LivingEntity entity) {
        try {
            var speed = entity.getAttribute(Attribute.MOVEMENT_SPEED);
            if (speed != null) {
                speed.setBaseValue(0.0);
            }
        } catch (Throwable ignored) {
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
            if (skin != null && skin.hasTexture()) {
                Object profile = buildResolvableProfile(profileName, uuid, skin);
                invokeCompatible(entity, "setProfile", profile);
                return;
            }

            // Resolve by Minecraft username if set. This API is optional and
            // is only reached on servers that expose Mannequin.
            String name = skin != null && skin.getMode() == SkinData.Mode.PLAYER_NAME
                    && skin.getValue() != null && !skin.getValue().isBlank()
                    ? sanitizeName(skin.getValue())
                    : profileName;
            Object dynamic = buildResolvableProfile(name, null, null);
            invokeCompatible(entity, "setProfile", dynamic);
            Object future = invokeCompatible(dynamic, "resolve");
            if (future instanceof CompletableFuture<?> completableFuture) {
                completableFuture.thenAcceptAsync(updated -> applyResolvedProfile(entity, updated, uuid, name),
                        runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable));
            }
        } catch (Throwable ex) {
            plugin.getLogger().warning("Unable to apply profile to PLAYER NPC '" + data.getId()
                    + "' on this server: " + ex.getMessage());
        }
    }

    private Object buildResolvableProfile(String name, UUID uuid, SkinData skin) throws Exception {
        Class<?> profileType = Class.forName("io.papermc.paper.datacomponent.item.ResolvableProfile");
        Object builder = profileType.getMethod("resolvableProfile").invoke(null);
        invokeCompatible(builder, "name", name);
        if (uuid != null) {
            invokeCompatible(builder, "uuid", uuid);
        }
        if (skin != null && skin.hasTexture()) {
            Object property = createProfileProperty(skin);
            if (property != null) {
                invokeCompatible(builder, "addProperty", property);
            }
        }
        return invokeCompatible(builder, "build");
    }

    private Object createProfileProperty(SkinData skin) throws Exception {
        Class<?> propertyType = Class.forName("com.destroystokyo.paper.profile.ProfileProperty");
        String signature = skin.getSignature();
        for (Constructor<?> constructor : propertyType.getConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length == 3) {
                return constructor.newInstance("textures", skin.getTexture(), signature);
            }
            if (parameters.length == 2) {
                return constructor.newInstance("textures", skin.getTexture());
            }
        }
        return null;
    }

    private void applyResolvedProfile(Entity entity, Object updated, UUID fallbackUuid, String fallbackName) {
        if (!entity.isValid()) {
            return;
        }
        try {
            String name = sanitizeName(stringValue(updated, "getName", "name"));
            if (name.equals("NPC")) {
                name = fallbackName;
            }
            Object uuidValue = value(updated, "getId", "uuid");
            UUID uuid = uuidValue instanceof UUID ? (UUID) uuidValue : fallbackUuid;
            Object builderProfile = buildResolvableProfile(name, uuid, null);
            Object properties = value(updated, "getProperties", "properties");
            if (properties instanceof Collection<?> collection) {
                for (Object property : collection) {
                    invokeCompatible(builderProfile, "addProperty", property);
                }
            }
            Object resolved = invokeCompatible(builderProfile, "build");
            invokeCompatible(entity, "setProfile", resolved);
        } catch (Throwable ex) {
            plugin.getLogger().fine("Could not apply resolved PLAYER NPC profile: " + ex.getMessage());
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
        } else if (entity instanceof ArmorStand armorStand && armorStand.getEquipment() != null) {
            armorStand.getEquipment().setHelmet(
                    plugin.getSkinManager().createSkull(data.getSkin(), data.getId()));
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
        if (entity instanceof LivingEntity living) {
            configureLivingEntity(living, data);
        }
        if (isMannequin(entity)) {
            configureMannequin(entity, data);
        } else if (entity instanceof ArmorStand armorStand) {
            configureArmorStand(armorStand, data);
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

    private static Object invokeCompatible(Object target, String methodName, Object... arguments) {
        if (target == null) {
            return null;
        }
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName)
                    || method.getParameterCount() != arguments.length
                    || Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            boolean compatible = true;
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i] != null && !wrap(parameterTypes[i]).isInstance(arguments[i])) {
                    compatible = false;
                    break;
                }
            }
            if (!compatible) {
                continue;
            }
            try {
                return method.invoke(target, arguments);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Object value(Object target, String... methodNames) {
        for (String methodName : methodNames) {
            Object value = invokeCompatible(target, methodName);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String stringValue(Object target, String... methodNames) {
        Object value = value(target, methodNames);
        return value instanceof String ? (String) value : null;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }
}

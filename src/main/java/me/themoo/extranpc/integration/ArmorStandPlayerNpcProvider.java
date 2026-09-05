package me.themoo.extranpc.integration;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * Fallback player-looking NPCs for Paper builds that do not ship {@code Mannequin}
 * (1.21.0–1.21.8). Uses an armor stand with a player-head skin.
 */
public final class ArmorStandPlayerNpcProvider implements PlayerNpcProvider {

    private final ExtraNPCPlugin plugin;

    public ArmorStandPlayerNpcProvider(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("Player NPCs using ArmorStand fallback (Mannequin not on this server).");
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

        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class, entity -> configure(entity, data));
        data.setEntityUuid(stand.getUniqueId());
        plugin.getLogger().info("Spawned PLAYER NPC '" + data.getId() + "' as ArmorStand");
    }

    private void configure(ArmorStand entity, NpcData data) {
        entity.setPersistent(true);
        entity.setRemoveWhenFarAway(false);
        entity.setGravity(data.isGravity());
        entity.setInvulnerable(data.isInvulnerable());
        entity.setCollidable(data.isCollidable());
        entity.setSilent(data.isSilent());
        entity.setGlowing(data.isGlowing());
        entity.customName(TextUtil.parse(data.getDisplayName()));
        entity.setCustomNameVisible(data.isShowName());
        entity.setBasePlate(false);
        entity.setArms(true);
        entity.setSmall(false);
        entity.setVisible(true);
        invokeIfPresent(entity, "setCanMove", new Class<?>[]{boolean.class}, false);
        try {
            java.lang.reflect.Method disabled = entity.getClass().getMethod("setDisabledSlots", EquipmentSlot[].class);
            disabled.invoke(entity, (Object) new EquipmentSlot[]{
                    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                    EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND
            });
        } catch (Throwable ignored) {
        }
        if (entity.getEquipment() != null) {
            entity.getEquipment().setHelmet(plugin.getSkinManager().createSkull(data.getSkin(), data.getId()));
            entity.getEquipment().setHelmetDropChance(0f);
        }
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
        applyHeadPose(entity, data.getLocation().getYaw(), data.getLocation().getPitch());
    }

    private static void applyHeadPose(ArmorStand stand, float yaw, float pitch) {
        stand.setHeadPose(new EulerAngle(Math.toRadians(pitch), 0, 0));
        stand.setRotation(yaw, pitch);
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
        if (entity instanceof ArmorStand stand && stand.getEquipment() != null) {
            stand.getEquipment().setHelmet(plugin.getSkinManager().createSkull(data.getSkin(), data.getId()));
        } else {
            spawn(data);
        }
    }

    @Override
    public void applySettings(NpcData data) {
        Entity entity = getEntity(data);
        if (!(entity instanceof ArmorStand stand)) {
            spawn(data);
            return;
        }
        stand.customName(TextUtil.parse(data.getDisplayName()));
        stand.setCustomNameVisible(data.isShowName());
        stand.setGlowing(data.isGlowing());
        stand.setGravity(data.isGravity());
        stand.setSilent(data.isSilent());
        stand.setInvulnerable(data.isInvulnerable());
        stand.setCollidable(data.isCollidable());
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
        if (!(entity instanceof ArmorStand stand) || data.getLocation() == null) {
            return;
        }
        Location base = stand.getLocation();
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
        applyHeadPose(stand, look.getYaw(), look.getPitch());
    }

    private static void invokeIfPresent(Object target, String method, Class<?>[] types, Object... args) {
        try {
            target.getClass().getMethod(method, types).invoke(target, args);
        } catch (Throwable ignored) {
        }
    }
}

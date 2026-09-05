package me.themoo.extranpc.integration;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.compat.BukkitCompat;
import me.themoo.extranpc.compat.LookAtHelper;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Player-like NPCs for Paper builds without Mannequin (pre-1.21.9).
 * Uses an ArmorStand wearing a skinned player head.
 */
public final class ArmorStandPlayerNpcProvider implements PlayerNpcProvider {

    private final ExtraNPCPlugin plugin;

    public ArmorStandPlayerNpcProvider(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("ArmorStand player NPCs ready (compatible fallback).");
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String backendName() {
        return "ARMOR_STAND";
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
        entity.setVisible(true);
        entity.setBasePlate(false);
        entity.setArms(true);
        entity.setSmall(false);
        entity.setMarker(false);
        entity.setCanPickupItems(false);
        entity.customName(TextUtil.parse(data.getDisplayName()));
        entity.setCustomNameVisible(data.isShowName());
        entity.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "npc-id"),
                PersistentDataType.STRING,
                data.getId()
        );
        try {
            entity.setAI(false);
        } catch (Throwable ignored) {
        }
        BukkitCompat.freezeMovement(entity);
        lockEquipment(entity);
        applyHelmet(entity, data);
        entity.setRotation(data.getLocation().getYaw(), data.getLocation().getPitch());
    }

    private void applyHelmet(ArmorStand entity, NpcData data) {
        ItemStack skull = plugin.getSkinManager().createSkull(data.getSkin(), sanitize(data.getId()));
        if (entity.getEquipment() != null) {
            entity.getEquipment().setHelmet(skull);
            entity.getEquipment().setHelmetDropChance(0f);
        }
    }

    private void lockEquipment(ArmorStand entity) {
        try {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                entity.addEquipmentLock(slot, ArmorStand.LockType.ADDING_OR_CHANGING);
                entity.addEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);
            }
        } catch (Throwable ignored) {
        }
        try {
            var disabled = entity.getClass().getMethod("setDisabledSlots", EquipmentSlot[].class);
            disabled.invoke(entity, (Object) EquipmentSlot.values());
        } catch (Throwable ignored) {
        }
    }

    private static String sanitize(String raw) {
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
        if (entity instanceof ArmorStand stand) {
            applyHelmet(stand, data);
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
        if (stand instanceof LivingEntity living) {
            BukkitCompat.freezeMovement(living);
        }
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
        LookAtHelper.tick(data, getEntity(data), 1.6);
    }
}

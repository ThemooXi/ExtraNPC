package me.themoo.extranpc.manager;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.integration.PlayerNpcProvider;
import me.themoo.extranpc.model.NpcData;
import me.themoo.extranpc.model.NpcType;
import me.themoo.extranpc.model.ShopTrade;
import me.themoo.extranpc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class NpcManager {

    private final ExtraNPCPlugin plugin;
    private final Map<String, NpcData> npcs = new HashMap<>();
    private final Map<UUID, String> entityToNpc = new HashMap<>();
    private final Map<String, List<UUID>> holograms = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, String> selected = new HashMap<>();
    private BukkitTask lookTask;
    private BukkitTask particleTask;

    public NpcManager(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
    }

    private PlayerNpcProvider players() {
        return plugin.getPlayerNpcProvider();
    }

    public void loadAll() {
        npcs.clear();
        entityToNpc.clear();
        for (NpcData data : plugin.getNpcStorage().loadAll()) {
            applyDefaults(data);
            npcs.put(data.getId().toLowerCase(Locale.ROOT), data);
            spawn(data);
        }
        startTasks();
    }

    public void saveAll() {
        plugin.getNpcStorage().saveAll(npcs.values());
    }

    public void save(NpcData data) {
        npcs.put(data.getId().toLowerCase(Locale.ROOT), data);
        if (plugin.getConfig().getBoolean("settings.auto-save", true)) {
            saveAll();
        }
    }

    private void applyDefaults(NpcData data) {
        if (data.getLookRange() <= 0) {
            data.setLookRange(plugin.getConfig().getDouble("settings.look-range", 8.0));
        }
    }

    public Collection<NpcData> getNpcs() {
        return npcs.values();
    }

    public Optional<NpcData> get(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(npcs.get(id.toLowerCase(Locale.ROOT)));
    }

    public boolean exists(String id) {
        return get(id).isPresent();
    }

    public boolean canCreatePlayerNpc() {
        return players() != null && players().isAvailable();
    }

    public NpcData create(String id, NpcType type, Location location) {
        NpcData data = new NpcData(id.toLowerCase(Locale.ROOT));
        data.setType(type);
        data.setLocation(location);
        data.setDisplayName(plugin.getConfig().getString("defaults.name", "<yellow>New NPC</yellow>"));
        data.setShowName(plugin.getConfig().getBoolean("defaults.show-name", true));
        data.setInvulnerable(plugin.getConfig().getBoolean("defaults.invulnerable", true));
        data.setGravity(plugin.getConfig().getBoolean("defaults.gravity", false));
        data.setSilent(plugin.getConfig().getBoolean("defaults.silent", true));
        data.setGlowing(plugin.getConfig().getBoolean("defaults.glowing", false));
        data.setLookAtPlayers(plugin.getConfig().getBoolean("defaults.look-at-players", true));
        data.setCollidable(plugin.getConfig().getBoolean("defaults.collidable", false));
        data.setBaby(plugin.getConfig().getBoolean("defaults.baby", false));
        data.setCooldownSeconds(plugin.getConfig().getInt("settings.default-cooldown", 0));
        data.setLookRange(plugin.getConfig().getDouble("settings.look-range", 8.0));
        npcs.put(data.getId(), data);
        spawn(data);
        save(data);
        return data;
    }

    public void delete(String id) {
        get(id).ifPresent(data -> {
            despawn(data);
            npcs.remove(data.getId().toLowerCase(Locale.ROOT));
            saveAll();
        });
    }

    public void respawn(NpcData data) {
        despawn(data);
        spawn(data);
        save(data);
    }

    public void move(NpcData data, Location location) {
        data.setLocation(location);
        if (data.getType().isPlayerLike()) {
            PlayerNpcProvider provider = players();
            if (provider != null && provider.isAvailable()) {
                provider.move(data, location);
                Entity entity = provider.getEntity(data);
                if (entity != null) {
                    entityToNpc.put(entity.getUniqueId(), data.getId());
                    data.setEntityUuid(entity.getUniqueId());
                }
                refreshHolograms(data);
                save(data);
                return;
            }
        }
        Entity entity = getEntity(data);
        if (entity != null) {
            entity.teleport(location);
            refreshHolograms(data);
        } else {
            spawn(data);
        }
        save(data);
    }

    public void despawnAll() {
        stopTasks();
        for (NpcData data : new ArrayList<>(npcs.values())) {
            despawn(data);
        }
    }

    public void despawn(NpcData data) {
        removeHolograms(data.getId());
        if (data.getType().isPlayerLike()) {
            PlayerNpcProvider provider = players();
            if (provider != null) {
                Entity entity = provider.getEntity(data);
                if (entity != null) {
                    entityToNpc.remove(entity.getUniqueId());
                }
                provider.despawn(data);
                return;
            }
        }
        Entity entity = getEntity(data);
        if (entity != null) {
            entityToNpc.remove(entity.getUniqueId());
            entity.remove();
        }
        data.setEntityUuid(null);
    }

    public Entity getEntity(NpcData data) {
        if (data.getType().isPlayerLike()) {
            PlayerNpcProvider provider = players();
            if (provider != null) {
                Entity entity = provider.getEntity(data);
                if (entity != null) {
                    return entity;
                }
            }
        }
        if (data.getEntityUuid() == null) {
            return null;
        }
        return Bukkit.getEntity(data.getEntityUuid());
    }

    public Optional<NpcData> byEntity(Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        String id = entityToNpc.get(entity.getUniqueId());
        if (id == null) {
            String stored = entity.getPersistentDataContainer().get(pluginKey(), PersistentDataType.STRING);
            if (stored != null) {
                id = stored;
            }
        }
        return id == null ? Optional.empty() : get(id);
    }

    public void setSelected(Player player, String id) {
        selected.put(player.getUniqueId(), id.toLowerCase(Locale.ROOT));
    }

    public Optional<String> getSelected(Player player) {
        return Optional.ofNullable(selected.get(player.getUniqueId()));
    }

    public void spawn(NpcData data) {
        Location location = data.getLocation();
        if (location == null || location.getWorld() == null) {
            plugin.getLogger().warning("Cannot spawn NPC " + data.getId() + " — invalid world.");
            return;
        }

        if (data.getType().isPlayerLike()) {
            spawnPlayerNpc(data);
            return;
        }
        spawnMobNpc(data, location);
    }

    private void spawnPlayerNpc(NpcData data) {
        PlayerNpcProvider provider = players();
        if (provider == null || !provider.isAvailable()) {
            plugin.getLogger().severe("Player NPC engine unavailable for '" + data.getId() + "'.");
            return;
        }
        provider.spawn(data);
        Entity entity = provider.getEntity(data);
        if (entity != null) {
            entityToNpc.put(entity.getUniqueId(), data.getId());
            data.setEntityUuid(entity.getUniqueId());
        }
        refreshHolograms(data);
    }

    private void spawnMobNpc(NpcData data, Location location) {
        Entity entity = location.getWorld().spawn(location, data.getType().getEntityType().getEntityClass(), spawned -> {
            spawned.getPersistentDataContainer().set(pluginKey(), PersistentDataType.STRING, data.getId());
            spawned.customName(TextUtil.parse(data.getDisplayName()));
            spawned.setCustomNameVisible(data.isShowName());
            spawned.setSilent(data.isSilent());
            spawned.setGlowing(data.isGlowing());
            spawned.setGravity(data.isGravity());
            spawned.setInvulnerable(data.isInvulnerable());

            if (spawned instanceof LivingEntity living) {
                living.setCollidable(data.isCollidable());
                living.setRemoveWhenFarAway(false);
                living.setPersistent(true);
                living.setAI(false);
                if (living instanceof Mob && living.getEquipment() != null) {
                    living.getEquipment().setHelmetDropChance(0f);
                    living.getEquipment().setChestplateDropChance(0f);
                    living.getEquipment().setLeggingsDropChance(0f);
                    living.getEquipment().setBootsDropChance(0f);
                }
                try {
                    var attr = living.getAttribute(Attribute.MOVEMENT_SPEED);
                    if (attr != null) {
                        attr.setBaseValue(0);
                    }
                } catch (Throwable ignored) {
                }
            }

            if (spawned instanceof Ageable ageable) {
                if (data.isBaby()) {
                    ageable.setBaby();
                } else {
                    ageable.setAdult();
                }
                ageable.setAgeLock(true);
            }

            if (spawned instanceof Villager villager) {
                villager.setProfession(Villager.Profession.NONE);
                villager.setVillagerLevel(1);
            }
        });

        data.setEntityUuid(entity.getUniqueId());
        entityToNpc.put(entity.getUniqueId(), data.getId());
        refreshHolograms(data);
    }

    public void refreshHolograms(NpcData data) {
        removeHolograms(data.getId());
        Location base = data.getLocation();
        if (base == null || base.getWorld() == null || data.getHologramLines().isEmpty()) {
            return;
        }
        List<UUID> ids = new ArrayList<>();
        double offset = 0.35 * data.getHologramLines().size() + 2.1;
        for (String line : data.getHologramLines()) {
            Location holoLoc = base.clone().add(0, offset, 0);
            TextDisplay display = base.getWorld().spawn(holoLoc, TextDisplay.class, text -> {
                text.text(TextUtil.parse(line));
                text.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
                text.setSeeThrough(true);
                text.setShadowed(true);
                text.setDefaultBackground(false);
                text.setPersistent(true);
                text.setGravity(false);
                text.getPersistentDataContainer().set(pluginKey(), PersistentDataType.STRING, "holo:" + data.getId());
            });
            ids.add(display.getUniqueId());
            offset -= 0.35;
        }
        holograms.put(data.getId().toLowerCase(Locale.ROOT), ids);
    }

    private void removeHolograms(String id) {
        List<UUID> ids = holograms.remove(id.toLowerCase(Locale.ROOT));
        if (ids == null) {
            return;
        }
        for (UUID uuid : ids) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                entity.remove();
            }
        }
    }

    public boolean checkCooldown(Player player, NpcData data) {
        if (data.getCooldownSeconds() <= 0 || player.hasPermission("extranpc.bypass")) {
            return true;
        }
        long now = System.currentTimeMillis();
        Long last = cooldowns.get(player.getUniqueId());
        if (last != null) {
            long left = (last + data.getCooldownSeconds() * 1000L) - now;
            if (left > 0) {
                plugin.getMessages().send(player, "cooldown", MessageServiceMap.seconds(String.valueOf((left / 1000L) + 1)));
                return false;
            }
        }
        cooldowns.put(player.getUniqueId(), now);
        return true;
    }

    public void openShop(Player player, NpcData data) {
        List<ShopTrade> trades = data.getTrades().stream().filter(ShopTrade::isValid).toList();
        if (trades.isEmpty()) {
            plugin.getMessages().send(player, "shop-empty");
            return;
        }
        Merchant merchant = Bukkit.createMerchant(TextUtil.parse(data.getDisplayName()));
        List<MerchantRecipe> recipes = new ArrayList<>();
        for (ShopTrade trade : trades) {
            int uses = data.isAdminShop() ? 999999 : trade.getMaxUses();
            MerchantRecipe recipe = new MerchantRecipe(trade.getResult(), 0, uses, false);
            recipe.setExperienceReward(false);
            recipe.setVillagerExperience(0);
            recipe.setPriceMultiplier(0.0f);
            // Ignore demand/special price so admin shops stay stable
            try {
                recipe.setDemand(0);
                recipe.setSpecialPrice(0);
            } catch (Throwable ignored) {
            }
            recipe.addIngredient(trade.getIngredient1());
            if (trade.getIngredient2() != null && !trade.getIngredient2().getType().isAir()) {
                recipe.addIngredient(trade.getIngredient2());
            }
            recipes.add(recipe);
        }
        merchant.setRecipes(recipes);
        player.openMerchant(merchant, true);
    }

    private void startTasks() {
        stopTasks();
        int lookInterval = Math.max(1, plugin.getConfig().getInt("settings.look-interval", 5));
        lookTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickLook, lookInterval, lookInterval);
        particleTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickParticles, 20L, 20L);
    }

    private void stopTasks() {
        if (lookTask != null) {
            lookTask.cancel();
            lookTask = null;
        }
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
    }

    private void tickLook() {
        for (NpcData data : npcs.values()) {
            if (!data.isLookAtPlayers()) {
                continue;
            }
            if (data.getType().isPlayerLike()) {
                PlayerNpcProvider provider = players();
                if (provider != null) {
                    provider.tickLook(data);
                }
                continue;
            }
            Entity entity = getEntity(data);
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            Player nearest = null;
            double best = data.getLookRange();
            for (Player player : living.getWorld().getPlayers()) {
                double dist = player.getLocation().distance(living.getLocation());
                if (dist <= best) {
                    best = dist;
                    nearest = player;
                }
            }
            if (nearest != null) {
                Location eye = living.getLocation();
                Vector direction = nearest.getEyeLocation().toVector().subtract(eye.toVector());
                eye.setDirection(direction);
                living.setRotation(eye.getYaw(), eye.getPitch());
            }
        }
    }

    private void tickParticles() {
        for (NpcData data : npcs.values()) {
            if (data.getParticle() == null || data.getParticle().equalsIgnoreCase("NONE")) {
                continue;
            }
            Entity entity = getEntity(data);
            if (entity == null) {
                continue;
            }
            try {
                Particle particle = Particle.valueOf(data.getParticle().toUpperCase(Locale.ROOT));
                entity.getWorld().spawnParticle(particle, entity.getLocation().add(0, 1.2, 0), 4, 0.25, 0.35, 0.25, 0.01);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private org.bukkit.NamespacedKey pluginKey() {
        return new org.bukkit.NamespacedKey(plugin, "npc-id");
    }

    private static final class MessageServiceMap {
        static Map<String, String> seconds(String value) {
            Map<String, String> map = new HashMap<>();
            map.put("seconds", value);
            return map;
        }
    }
}

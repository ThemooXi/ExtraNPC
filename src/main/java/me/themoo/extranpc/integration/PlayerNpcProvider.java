package me.themoo.extranpc.integration;

import me.themoo.extranpc.model.NpcData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Backend for real player-looking NPCs (standalone / native).
 */
public interface PlayerNpcProvider {

    boolean isAvailable();

    void spawn(NpcData data);

    void despawn(NpcData data);

    void move(NpcData data, Location location);

    void applySkin(NpcData data);

    void applySettings(NpcData data);

    Entity getEntity(NpcData data);
}

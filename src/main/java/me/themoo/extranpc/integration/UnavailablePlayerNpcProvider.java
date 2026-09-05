package me.themoo.extranpc.integration;

import me.themoo.extranpc.ExtraNPCPlugin;
import me.themoo.extranpc.model.NpcData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Placeholder backend when Mannequin is unavailable on the running server.
 */
public final class UnavailablePlayerNpcProvider implements PlayerNpcProvider {

    private final ExtraNPCPlugin plugin;

    public UnavailablePlayerNpcProvider(ExtraNPCPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getEngineName() {
        return "Unavailable";
    }

    @Override
    public void spawn(NpcData data) {
        plugin.getLogger().severe("Cannot spawn PLAYER NPC '" + data.getId()
                + "' — Mannequin requires Paper 1.21.9+ or 26.1+.");
    }

    @Override
    public void despawn(NpcData data) {
        data.setEntityUuid(null);
        data.setCitizensId(null);
    }

    @Override
    public void move(NpcData data, Location location) {
        data.setLocation(location);
    }

    @Override
    public void applySkin(NpcData data) {
    }

    @Override
    public void applySettings(NpcData data) {
    }

    @Override
    public Entity getEntity(NpcData data) {
        return null;
    }

    @Override
    public void tickLook(NpcData data) {
    }
}

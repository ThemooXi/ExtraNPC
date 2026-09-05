package me.themoo.extranpc.compat;

import me.themoo.extranpc.model.NpcData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class LookAtHelper {

    private LookAtHelper() {
    }

    public static void tick(NpcData data, Entity entity, double eyeHeight) {
        if (data == null || entity == null || !data.isLookAtPlayers() || data.getLocation() == null) {
            return;
        }
        Location base = entity.getLocation();
        if (base.getWorld() == null) {
            return;
        }
        Player nearest = null;
        double best = data.getLookRange() * data.getLookRange();
        for (Player player : base.getWorld().getPlayers()) {
            double distance = player.getLocation().distanceSquared(base);
            if (distance <= best) {
                best = distance;
                nearest = player;
            }
        }
        if (nearest == null) {
            return;
        }
        Vector direction = nearest.getEyeLocation().toVector().subtract(base.clone().add(0, eyeHeight, 0).toVector());
        if (direction.lengthSquared() < 1.0E-6) {
            return;
        }
        Location look = base.clone().setDirection(direction);
        try {
            entity.setRotation(look.getYaw(), look.getPitch());
        } catch (Throwable ignored) {
            entity.teleport(look);
        }
    }
}

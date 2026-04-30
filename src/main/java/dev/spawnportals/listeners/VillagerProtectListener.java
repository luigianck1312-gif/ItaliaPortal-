package dev.spawnportals.listeners;

import dev.spawnportals.SpawnPortals;
import dev.spawnportals.managers.PortalManager;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class VillagerProtectListener implements Listener {

    private final PortalManager portalManager;

    public VillagerProtectListener(SpawnPortals plugin) {
        this.portalManager = plugin.getPortalManager();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.VILLAGER) return;
        if (portalManager.isPortalVillager(event.getEntity().getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        event.setCancelled(true);
    }
}

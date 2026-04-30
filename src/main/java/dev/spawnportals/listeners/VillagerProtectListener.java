package dev.spawnportals.listeners;

import dev.spawnportals.SpawnPortals;
import dev.spawnportals.managers.PortalManager;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.world.EntitiesLoadEvent;

public class VillagerProtectListener implements Listener {

    private final SpawnPortals plugin;
    private final PortalManager portalManager;

    public VillagerProtectListener(SpawnPortals plugin) {
        this.plugin = plugin;
        this.portalManager = plugin.getPortalManager();
    }

    /** Impedisce di danneggiare i villager portale */
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.VILLAGER
                && event.getEntityType() != EntityType.ARMOR_STAND) return;
        if (portalManager.isPortalVillager(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    /** Impedisce di manipolare gli armor stand ologramma */
    @EventHandler
    public void onArmorStand(PlayerArmorStandManipulateEvent event) {
        event.setCancelled(true); // protegge tutti gli armor stand invisibili
    }
}

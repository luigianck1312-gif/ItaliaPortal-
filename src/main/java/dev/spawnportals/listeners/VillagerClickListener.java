package dev.spawnportals.listeners;

import dev.spawnportals.SpawnPortals;
import dev.spawnportals.managers.PortalManager;
import dev.spawnportals.models.PortalType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class VillagerClickListener implements Listener {

    private final SpawnPortals plugin;
    private final PortalManager portalManager;

    public VillagerClickListener(SpawnPortals plugin) {
        this.plugin = plugin;
        this.portalManager = plugin.getPortalManager();
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getRightClicked().getType() != EntityType.VILLAGER) return;

        var uuid = event.getRightClicked().getUniqueId();
        if (!portalManager.isPortalVillager(uuid)) return;

        event.setCancelled(true); // niente GUI villager

        Player player = event.getPlayer();
        PortalType type = portalManager.getTypeByVillager(uuid);

        if (portalManager.isOnCooldown(player)) {
            long rem = portalManager.getRemainingCooldown(player);
            player.sendMessage(PortalManager.color(
                plugin.getConfig().getString("messages.cooldown",
                    "&cAspetta ancora &e%seconds%s&c!")
                .replace("%seconds%", String.valueOf(rem))));
            return;
        }

        portalManager.teleportPlayer(player, type);
    }
}

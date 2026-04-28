package dev.spawnportals.listeners;

import dev.spawnportals.SpawnPortals;
import dev.spawnportals.managers.PortalManager;
import dev.spawnportals.models.PortalType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PortalEnterListener implements Listener {

    private final SpawnPortals plugin;
    private final PortalManager portalManager;

    public PortalEnterListener(SpawnPortals plugin) {
        this.plugin = plugin;
        this.portalManager = plugin.getPortalManager();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only process if the player moved to a different block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("spawnportals.use")) return;

        PortalType type = portalManager.getPortalAt(event.getTo());
        if (type == null) return;

        if (portalManager.isOnCooldown(player)) {
            long remaining = portalManager.getRemainingCooldown(player);
            String msg = plugin.getConfig().getString("messages.cooldown",
                    "&cAspetta ancora %seconds% secondi prima di usare un portale!")
                    .replace("%seconds%", String.valueOf(remaining));
            player.sendMessage(PortalManager.color(msg));
            return;
        }

        portalManager.teleportPlayer(player, type);
    }
}

package dev.spawnportals.listeners;

import dev.spawnportals.SpawnPortals;
import dev.spawnportals.managers.PortalManager;
import dev.spawnportals.models.PortalType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class PortalEnterListener implements Listener {

    private final SpawnPortals plugin;
    private final PortalManager portalManager;

    public PortalEnterListener(SpawnPortals plugin) {
        this.plugin = plugin;
        this.portalManager = plugin.getPortalManager();
    }

    /**
     * Blocca il teletrasporto nativo del Nether/End solo se il giocatore
     * si trova dentro uno dei portali registrati, e gestisce noi il tp.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        PortalType type = portalManager.getPortalAt(player.getLocation());
        if (type == null) return; // non è un nostro portale, lascia fare a Minecraft

        // Cancella il comportamento nativo
        event.setCancelled(true);

        // Gestisci cooldown e teletrasporto
        if (portalManager.isOnCooldown(player)) {
            long remaining = portalManager.getRemainingCooldown(player);
            String msg = plugin.getConfig().getString("messages.cooldown",
                    "&cAspetta ancora %seconds% secondi!")
                    .replace("%seconds%", String.valueOf(remaining));
            player.sendMessage(PortalManager.color(msg));
            return;
        }

        portalManager.teleportPlayer(player, type);
    }

    /**
     * Fallback via movimento (per sicurezza, nel caso PlayerPortalEvent
     * non scatti abbastanza velocemente).
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Solo se si è spostati di blocco
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("spawnportals.use")) return;

        // Se il giocatore è già in cooldown non fare nulla (evita spam)
        if (portalManager.isOnCooldown(player)) return;

        PortalType type = portalManager.getPortalAt(event.getTo());
        if (type == null) return;

        portalManager.teleportPlayer(player, type);
    }
}

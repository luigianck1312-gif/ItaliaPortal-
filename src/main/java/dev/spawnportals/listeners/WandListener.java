package dev.spawnportals.listeners;

import dev.spawnportals.SpawnPortals;
import dev.spawnportals.managers.PortalManager;
import dev.spawnportals.managers.SelectionManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

public class WandListener implements Listener {

    private final SpawnPortals plugin;
    private final SelectionManager selection;
    public static final NamespacedKey WAND_KEY = new NamespacedKey("spawnportals", "portal_wand");

    public WandListener(SpawnPortals plugin) {
        this.plugin = plugin;
        this.selection = plugin.getSelectionManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        Player player = event.getPlayer();

        if (!player.hasPermission("spawnportals.admin")) return;

        var item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.GOLDEN_AXE) return;

        // Check if this is our wand
        var meta = item.getItemMeta();
        if (meta == null) return;
        if (!meta.getPersistentDataContainer().has(WAND_KEY, PersistentDataType.BYTE)) return;

        event.setCancelled(true);

        if (event.getClickedBlock() == null) return;
        var loc = event.getClickedBlock().getLocation();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selection.setPos1(player.getUniqueId(), loc);
            player.sendMessage(PortalManager.color(
                "&a[Portale] &fPrimo angolo impostato: &e" + formatLoc(loc)));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            selection.setPos2(player.getUniqueId(), loc);
            player.sendMessage(PortalManager.color(
                "&a[Portale] &fSecondo angolo impostato: &e" + formatLoc(loc)));
        }

        if (selection.hasFullSelection(player.getUniqueId())) {
            player.sendMessage(PortalManager.color(
                "&a[Portale] &fSelezione completa! Usa &e/place <overworld|nether|end>&f per creare il portale."));
        }
    }

    private String formatLoc(org.bukkit.Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }
}

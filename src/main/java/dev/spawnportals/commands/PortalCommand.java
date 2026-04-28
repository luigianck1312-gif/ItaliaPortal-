package dev.spawnportals.commands;

import dev.spawnportals.SpawnPortals;
import dev.spawnportals.managers.PortalBuilder;
import dev.spawnportals.managers.PortalManager;
import dev.spawnportals.managers.SelectionManager;
import dev.spawnportals.models.PortalRegion;
import dev.spawnportals.models.PortalType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PortalCommand implements CommandExecutor, TabCompleter {

    private final SpawnPortals plugin;
    private final PortalManager portalManager;
    private final SelectionManager selectionManager;

    public PortalCommand(SpawnPortals plugin) {
        this.plugin = plugin;
        this.portalManager = plugin.getPortalManager();
        this.selectionManager = plugin.getSelectionManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo i giocatori possono usare questo comando.");
            return true;
        }

        if (!player.hasPermission("spawnportals.admin")) {
            player.sendMessage(PortalManager.color(
                    plugin.getConfig().getString("messages.no-permission", "&cNon hai il permesso!")));
            return true;
        }

        String cmdName = cmd.getName().toLowerCase();

        switch (cmdName) {
            case "portalwand" -> handleWand(player);
            case "place" -> handlePlace(player, args);
            case "remove" -> handleRemove(player, args);
            case "portals" -> handleList(player);
        }

        return true;
    }

    private void handleWand(Player player) {
        var wand = plugin.createWand();
        player.getInventory().addItem(wand);
        player.sendMessage(PortalManager.color(
                plugin.getConfig().getString("messages.wand-given",
                        "&aHai ricevuto la bacchetta portale!")));
    }

    private void handlePlace(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(PortalManager.color("&cUso: /place <overworld|nether|end>"));
            return;
        }

        PortalType type = PortalType.fromString(args[0]);
        if (type == null) {
            player.sendMessage(PortalManager.color("&cTipo non valido! Usa: overworld, nether, end"));
            return;
        }

        if (!selectionManager.hasFullSelection(player.getUniqueId())) {
            player.sendMessage(PortalManager.color(
                    plugin.getConfig().getString("messages.no-selection",
                            "&cDevi prima selezionare una regione con /portalwand!")));
            return;
        }

        Location pos1 = selectionManager.getPos1(player.getUniqueId());
        Location pos2 = selectionManager.getPos2(player.getUniqueId());

        // Build the visual portal at pos1 (bottom-left corner)
        Location buildOrigin = new Location(
                pos1.getWorld(),
                Math.min(pos1.getBlockX(), pos2.getBlockX()),
                Math.min(pos1.getBlockY(), pos2.getBlockY()),
                Math.min(pos1.getBlockZ(), pos2.getBlockZ())
        );

        PortalBuilder.buildPortal(buildOrigin, type);

        // Register the region
        PortalRegion region = new PortalRegion(type, pos1, pos2);
        portalManager.registerPortal(region);

        selectionManager.clearSelection(player.getUniqueId());

        String msg = plugin.getConfig().getString("messages.portal-placed",
                "&aPortale %type% creato con successo!")
                .replace("%type%", type.getDisplayName());
        player.sendMessage(PortalManager.color(msg));
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(PortalManager.color("&cUso: /remove <overworld|nether|end>"));
            return;
        }

        PortalType type = PortalType.fromString(args[0]);
        if (type == null) {
            player.sendMessage(PortalManager.color("&cTipo non valido! Usa: overworld, nether, end"));
            return;
        }

        if (!portalManager.hasPortal(type)) {
            String msg = plugin.getConfig().getString("messages.portal-not-found",
                    "&cNessun portale %type% trovato!")
                    .replace("%type%", type.getDisplayName());
            player.sendMessage(PortalManager.color(msg));
            return;
        }

        var region = portalManager.getPortal(type);
        World world = plugin.getServer().getWorld(region.getWorldName());
        if (world != null) {
            Location origin = new Location(world, region.getMinX(), region.getMinY(), region.getMinZ());
            PortalBuilder.removePortal(origin, PortalBuilder.WIDTH, PortalBuilder.HEIGHT, world);
        }

        portalManager.removePortal(type);

        String msg = plugin.getConfig().getString("messages.portal-removed",
                "&cPortale %type% rimosso!")
                .replace("%type%", type.getDisplayName());
        player.sendMessage(PortalManager.color(msg));
    }

    private void handleList(Player player) {
        var portals = portalManager.getAllPortals();
        if (portals.isEmpty()) {
            player.sendMessage(PortalManager.color("&eNessun portale attivo."));
            return;
        }
        player.sendMessage(PortalManager.color("&6=== Portali Attivi ==="));
        for (var region : portals) {
            player.sendMessage(PortalManager.color(
                "&a" + region.getType().getDisplayName() + " &7→ " +
                region.getWorldName() + " [" +
                region.getMinX() + "," + region.getMinY() + "," + region.getMinZ() +
                "] → [" +
                region.getMaxX() + "," + region.getMaxY() + "," + region.getMaxZ() + "]"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> types = Arrays.stream(PortalType.values())
                    .map(PortalType::getId)
                    .collect(Collectors.toList());
            return types.stream()
                    .filter(t -> t.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}

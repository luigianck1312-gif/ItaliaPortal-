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

    private static final List<String> SUBCOMMANDS = List.of("wand", "place", "remove", "list");
    private static final List<String> TYPES = Arrays.stream(PortalType.values())
            .map(PortalType::getId).collect(Collectors.toList());

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

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "wand"   -> handleWand(player);
            case "place"  -> handlePlace(player, args);
            case "remove" -> handleRemove(player, args);
            case "list"   -> handleList(player);
            default       -> sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(PortalManager.color("&6=== SpawnPortals ==="));
        player.sendMessage(PortalManager.color("&e/portale wand &7- Prendi la bacchetta selezione"));
        player.sendMessage(PortalManager.color("&e/portale place <overworld|nether|end> &7- Crea portale"));
        player.sendMessage(PortalManager.color("&e/portale remove <overworld|nether|end> &7- Rimuovi portale"));
        player.sendMessage(PortalManager.color("&e/portale list &7- Lista portali attivi"));
    }

    private void handleWand(Player player) {
        var wand = plugin.createWand();
        player.getInventory().addItem(wand);
        player.sendMessage(PortalManager.color(
                plugin.getConfig().getString("messages.wand-given",
                        "&aHai ricevuto la bacchetta portale! &7Click SX = angolo 1, Click DX = angolo 2")));
    }

    private void handlePlace(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(PortalManager.color("&cUso: /portale place <overworld|nether|end>"));
            return;
        }

        PortalType type = PortalType.fromString(args[1]);
        if (type == null) {
            player.sendMessage(PortalManager.color("&cTipo non valido! Usa: overworld, nether, end"));
            return;
        }

        if (!selectionManager.hasFullSelection(player.getUniqueId())) {
            player.sendMessage(PortalManager.color(
                    plugin.getConfig().getString("messages.no-selection",
                            "&cDevi prima selezionare una regione con &e/portale wand&c!")));
            return;
        }

        Location pos1 = selectionManager.getPos1(player.getUniqueId());
        Location pos2 = selectionManager.getPos2(player.getUniqueId());

        Location buildOrigin = new Location(
                pos1.getWorld(),
                Math.min(pos1.getBlockX(), pos2.getBlockX()),
                Math.min(pos1.getBlockY(), pos2.getBlockY()),
                Math.min(pos1.getBlockZ(), pos2.getBlockZ())
        );

        PortalBuilder.buildPortal(buildOrigin, type);

        PortalRegion region = new PortalRegion(type, pos1, pos2);
        portalManager.registerPortal(region);
        selectionManager.clearSelection(player.getUniqueId());

        String msg = plugin.getConfig().getString("messages.portal-placed",
                "&aPortale %type% creato con successo!")
                .replace("%type%", type.getDisplayName());
        player.sendMessage(PortalManager.color(msg));
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(PortalManager.color("&cUso: /portale remove <overworld|nether|end>"));
            return;
        }

        PortalType type = PortalType.fromString(args[1]);
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
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("place") || args[0].equalsIgnoreCase("remove"))) {
            return TYPES.stream()
                    .filter(t -> t.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}

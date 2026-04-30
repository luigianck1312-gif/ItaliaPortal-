package dev.spawnportals.commands;

import dev.spawnportals.SpawnPortals;
import dev.spawnportals.managers.PortalManager;
import dev.spawnportals.models.PortalType;
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

    private static final List<String> SUBS  = List.of("spawn", "remove", "list");
    private static final List<String> TYPES = Arrays.stream(PortalType.values())
            .map(PortalType::getId).collect(Collectors.toList());

    public PortalCommand(SpawnPortals plugin) {
        this.plugin = plugin;
        this.portalManager = plugin.getPortalManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo i giocatori possono usare questo comando.");
            return true;
        }
        if (!player.hasPermission("spawnportals.admin")) {
            player.sendMessage(PortalManager.color("&cNon hai il permesso!"));
            return true;
        }
        if (args.length == 0) { sendHelp(player); return true; }

        switch (args[0].toLowerCase()) {
            case "spawn"  -> handleSpawn(player, args);
            case "remove" -> handleRemove(player, args);
            case "list"   -> handleList(player);
            default       -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player p) {
        p.sendMessage(PortalManager.color("&6=== SpawnPortals ==="));
        p.sendMessage(PortalManager.color("&e/portale spawn <overworld|nether|end> &7- Spawna il villager portale dove sei"));
        p.sendMessage(PortalManager.color("&e/portale remove <overworld|nether|end> &7- Rimuovi il villager portale"));
        p.sendMessage(PortalManager.color("&e/portale list &7- Lista portali attivi"));
    }

    private void handleSpawn(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(PortalManager.color("&cUso: /portale spawn <overworld|nether|end>"));
            return;
        }
        PortalType type = PortalType.fromString(args[1]);
        if (type == null) {
            player.sendMessage(PortalManager.color("&cTipo non valido! Usa: overworld, nether, end"));
            return;
        }
        portalManager.spawnPortalVillager(player.getLocation(), type);
        player.sendMessage(PortalManager.color("&aVillager &e" + type.getId().toUpperCase() + " &aspawnato!"));
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
        portalManager.removePortalVillager(type);
        player.sendMessage(PortalManager.color("&cVillager &e" + type.getId().toUpperCase() + " &crimosso!"));
    }

    private void handleList(Player player) {
        var map = portalManager.getTypeToVillager();
        if (map.isEmpty()) {
            player.sendMessage(PortalManager.color("&eNessun villager portale attivo."));
            return;
        }
        player.sendMessage(PortalManager.color("&6=== Villager Portali Attivi ==="));
        for (var entry : map.entrySet())
            player.sendMessage(PortalManager.color("&a" + entry.getKey().getId().toUpperCase() + " &7\u2192 UUID: " + entry.getValue()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1)
            return SUBS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        if (args.length == 2 && (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("remove")))
            return TYPES.stream().filter(t -> t.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        return List.of();
    }
}

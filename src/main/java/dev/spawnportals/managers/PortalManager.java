package dev.spawnportals.managers;

import dev.spawnportals.SpawnPortals;
import dev.spawnportals.models.PortalRegion;
import dev.spawnportals.models.PortalType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class PortalManager {

    private final SpawnPortals plugin;
    private final Map<PortalType, PortalRegion> portals = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public PortalManager(SpawnPortals plugin) {
        this.plugin = plugin;
        loadPortals();
    }

    // ── Registration ──────────────────────────────────────────────────────────

    public void registerPortal(PortalRegion region) {
        portals.put(region.getType(), region);
        savePortals();
    }

    public void removePortal(PortalType type) {
        portals.remove(type);
        savePortals();
    }

    public boolean hasPortal(PortalType type) {
        return portals.containsKey(type);
    }

    public PortalRegion getPortal(PortalType type) {
        return portals.get(type);
    }

    public Collection<PortalRegion> getAllPortals() {
        return portals.values();
    }

    // ── Teleportation ─────────────────────────────────────────────────────────

    public PortalType getPortalAt(Location loc) {
        for (PortalRegion region : portals.values()) {
            if (region.contains(loc)) return region.getType();
        }
        return null;
    }

    public boolean isOnCooldown(Player player) {
        long cooldownMs = plugin.getConfig().getLong("settings.teleport-cooldown", 5) * 1000L;
        long last = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        return (System.currentTimeMillis() - last) < cooldownMs;
    }

    public long getRemainingCooldown(Player player) {
        long cooldownMs = plugin.getConfig().getLong("settings.teleport-cooldown", 5) * 1000L;
        long last = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long remaining = cooldownMs - (System.currentTimeMillis() - last);
        return Math.max(0, remaining / 1000);
    }

    public void teleportPlayer(Player player, PortalType type) {
        World targetWorld = getWorldForType(type);
        if (targetWorld == null) {
            player.sendMessage(color("&cIl mondo di destinazione non esiste!"));
            return;
        }

        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        String msg = plugin.getConfig().getString("messages.teleporting", "&aTeletrasporto in corso verso %world%...")
                .replace("%world%", type.getDisplayName());
        player.sendMessage(color(msg));

        // Run async safe-spot search
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Location safe = findSafeLocation(targetWorld, type);
            if (safe == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(color(plugin.getConfig().getString(
                            "messages.safe-spot-not-found", "&cNon riesco a trovare un posto sicuro, riprova!")));
                });
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.teleport(safe);
                sendTeleportEffect(player, type);
            });
        });
    }

    private World getWorldForType(PortalType type) {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == type.getTargetEnvironment()) return w;
        }
        return null;
    }

    private Location findSafeLocation(World world, PortalType type) {
        int range = (int) plugin.getConfig().getLong("settings.teleport-range", 30000);
        int maxAttempts = (int) plugin.getConfig().getLong("settings.max-safe-attempts", 50);
        Random random = new Random();

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = random.nextInt(range * 2) - range;
            int z = random.nextInt(range * 2) - range;

            Location candidate = findSafeY(world, x, z, type);
            if (candidate != null) return candidate;
        }
        return null;
    }

    private Location findSafeY(World world, int x, int z, PortalType type) {
        Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
        if (!chunk.isLoaded()) chunk.load();

        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 2;

        for (int y = maxY; y > minY; y--) {
            Block ground      = world.getBlockAt(x, y, z);
            Block standHere   = world.getBlockAt(x, y + 1, z);
            Block headHere    = world.getBlockAt(x, y + 2, z);

            // il blocco su cui stare in piedi deve essere solido e non pericoloso
            if (ground.isPassable()) continue;
            if (isDangerous(ground.getType())) continue;

            // i 2 blocchi dove il giocatore si trova devono essere liberi e non acqua
            if (!standHere.isPassable()) continue;
            if (!headHere.isPassable()) continue;
            if (isDangerousAir(standHere.getType())) continue;
            if (isDangerousAir(headHere.getType())) continue;

            // extra check End: non mandare in zone basse nel vuoto
            if (type == PortalType.END && y < 40) continue;

            return new Location(world, x + 0.5, y + 1, z + 0.5, 0f, 0f);
        }
        return null;
    }

    /** Blocchi che NON devono trovarsi nei 2 slot dove il giocatore si trova */
    private boolean isDangerousAir(Material mat) {
        return mat == Material.WATER
            || mat == Material.LAVA
            || mat == Material.FIRE
            || mat == Material.KELP
            || mat == Material.KELP_PLANT
            || mat == Material.SEAGRASS
            || mat == Material.TALL_SEAGRASS
            || mat == Material.BUBBLE_COLUMN;
    }

    private boolean isDangerous(Material mat) {
        return mat == Material.LAVA
            || mat == Material.FIRE
            || mat == Material.MAGMA_BLOCK
            || mat == Material.CACTUS
            || mat == Material.WITHER_ROSE
            || mat == Material.SWEET_BERRY_BUSH
            || mat == Material.WATER
            || mat == Material.KELP
            || mat == Material.KELP_PLANT
            || mat == Material.SEAGRASS
            || mat == Material.TALL_SEAGRASS;
    }

    private void sendTeleportEffect(Player player, PortalType type) {
        Color particleColor = switch (type) {
            case OVERWORLD -> Color.LIME;
            case NETHER -> Color.RED;
            case END -> Color.GRAY;
        };
        player.spawnParticle(Particle.DUST,
                player.getLocation().add(0, 1, 0),
                40,
                new Particle.DustOptions(particleColor, 1.5f));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    private void savePortals() {
        plugin.getConfig().set("portals", null);
        for (PortalRegion r : portals.values()) {
            String key = "portals." + r.getType().getId();
            plugin.getConfig().set(key + ".world", r.getWorldName());
            plugin.getConfig().set(key + ".minX", r.getMinX());
            plugin.getConfig().set(key + ".minY", r.getMinY());
            plugin.getConfig().set(key + ".minZ", r.getMinZ());
            plugin.getConfig().set(key + ".maxX", r.getMaxX());
            plugin.getConfig().set(key + ".maxY", r.getMaxY());
            plugin.getConfig().set(key + ".maxZ", r.getMaxZ());
        }
        plugin.saveConfig();
    }

    private void loadPortals() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("portals");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            PortalType type = PortalType.fromString(key);
            if (type == null) continue;
            String world = section.getString(key + ".world");
            int minX = section.getInt(key + ".minX");
            int minY = section.getInt(key + ".minY");
            int minZ = section.getInt(key + ".minZ");
            int maxX = section.getInt(key + ".maxX");
            int maxY = section.getInt(key + ".maxY");
            int maxZ = section.getInt(key + ".maxZ");
            portals.put(type, new PortalRegion(type, world, minX, minY, minZ, maxX, maxY, maxZ));
        }
        plugin.getLogger().info("Caricati " + portals.size() + " portali.");
    }

    // ── Utils ─────────────────────────────────────────────────────────────────

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}

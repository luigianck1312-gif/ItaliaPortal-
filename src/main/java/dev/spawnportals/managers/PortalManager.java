package dev.spawnportals.managers;

import dev.spawnportals.SpawnPortals;
import dev.spawnportals.models.PortalType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;

public class PortalManager {

    private final SpawnPortals plugin;

    // villagerUUID -> tipo portale
    private final Map<UUID, PortalType> villagerMap = new HashMap<>();
    // tipo -> UUID villager (per lookup inverso)
    private final Map<PortalType, UUID> typeToVillager = new HashMap<>();
    // tipo -> UUID armor stand ologramma (riga 1 = titolo, riga 2 = sottotitolo)
    private final Map<PortalType, List<UUID>> holoStands = new HashMap<>();

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public PortalManager(SpawnPortals plugin) {
        this.plugin = plugin;
    }

    // ── Spawn ────────────────────────────────────────────────────────────────

    public void spawnPortalVillager(Location loc, PortalType type) {
        World world = loc.getWorld();

        // Rimuovi eventuale precedente
        removePortalVillager(type);

        // Spawna villager
        Villager v = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
        v.setCustomName("§8[§" + colorChar(type) + type.getId().toUpperCase() + "§8]");
        v.setCustomNameVisible(false);
        v.setAI(false);
        v.setInvulnerable(true);
        v.setSilent(true);
        v.setVillagerType(villagerSkin(type));
        v.setProfession(Villager.Profession.NONE);
        v.setPersistent(true);

        villagerMap.put(v.getUniqueId(), type);
        typeToVillager.put(type, v.getUniqueId());

        // Spawna ologrammi sopra
        spawnHologram(loc, type, v.getUniqueId());

        // Salva
        saveVillager(type, v.getUniqueId(), loc);
    }

    private void spawnHologram(Location baseLoc, PortalType type, UUID villagerUuid) {
        World world = baseLoc.getWorld();
        List<UUID> stands = new ArrayList<>();

        // Riga 2 (più in basso): sottotitolo
        ArmorStand as2 = spawnStand(world,
                baseLoc.clone().add(0, 2.1, 0),
                type.getHoloLine2());
        stands.add(as2.getUniqueId());

        // Riga 1 (più in alto): titolo colorato
        ArmorStand as1 = spawnStand(world,
                baseLoc.clone().add(0, 2.55, 0),
                type.getHoloLine1());
        stands.add(as1.getUniqueId());

        holoStands.put(type, stands);

        // Salva UUID stand nel config
        plugin.getConfig().set("portals." + type.getId() + ".holo1", as1.getUniqueId().toString());
        plugin.getConfig().set("portals." + type.getId() + ".holo2", as2.getUniqueId().toString());
        plugin.saveConfig();
    }

    private ArmorStand spawnStand(World world, Location loc, String name) {
        ArmorStand as = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        as.setCustomName(name);
        as.setCustomNameVisible(true);
        as.setVisible(false);
        as.setGravity(false);
        as.setInvulnerable(true);
        as.setSmall(true);
        as.setSilent(true);
        as.setPersistent(true);
        as.setBasePlate(false);
        as.setArms(false);
        return as;
    }

    public void removePortalVillager(PortalType type) {
        // Rimuovi villager
        UUID vid = typeToVillager.remove(type);
        if (vid != null) {
            villagerMap.remove(vid);
            findAndRemoveEntity(vid);
        }
        // Rimuovi ologrammi
        List<UUID> stands = holoStands.remove(type);
        if (stands != null) {
            for (UUID uid : stands) findAndRemoveEntity(uid);
        }
        plugin.getConfig().set("portals." + type.getId(), null);
        plugin.saveConfig();
    }

    private void findAndRemoveEntity(UUID uuid) {
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e.getUniqueId().equals(uuid)) {
                    e.remove();
                    return;
                }
            }
        }
    }

    // ── Lookup ───────────────────────────────────────────────────────────────

    public PortalType getTypeByVillager(UUID uuid) {
        return villagerMap.get(uuid);
    }

    public boolean isPortalVillager(UUID uuid) {
        return villagerMap.containsKey(uuid);
    }

    // ── Teleport ─────────────────────────────────────────────────────────────

    public boolean isOnCooldown(Player player) {
        long cd = plugin.getConfig().getLong("settings.teleport-cooldown", 5) * 1000L;
        return System.currentTimeMillis() - cooldowns.getOrDefault(player.getUniqueId(), 0L) < cd;
    }

    public long getRemainingCooldown(Player player) {
        long cd = plugin.getConfig().getLong("settings.teleport-cooldown", 5) * 1000L;
        return Math.max(0, (cd - (System.currentTimeMillis() - cooldowns.getOrDefault(player.getUniqueId(), 0L))) / 1000);
    }

    public void teleportPlayer(Player player, PortalType type) {
        World target = getWorldForType(type);
        if (target == null) {
            player.sendMessage(color("&cIl mondo di destinazione non esiste!"));
            return;
        }

        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        String msg = plugin.getConfig().getString("messages.teleporting", "&aTeletrasporto verso &e%world%&a...")
                .replace("%world%", type.getId().toUpperCase());
        player.sendMessage(color(msg));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Location safe = findSafe(target, type);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (safe == null) {
                    player.sendMessage(color(plugin.getConfig().getString(
                            "messages.safe-spot-not-found", "&cPosto sicuro non trovato, riprova!")));
                    return;
                }
                player.teleport(safe, PlayerTeleportEvent.TeleportCause.PLUGIN);
                player.spawnParticle(Particle.PORTAL, player.getLocation().add(0,1,0), 60);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            });
        });
    }

    private World getWorldForType(PortalType type) {
        for (World w : Bukkit.getWorlds())
            if (w.getEnvironment() == type.getTargetEnvironment()) return w;
        return null;
    }

    private Location findSafe(World world, PortalType type) {
        int range = (int) plugin.getConfig().getLong("settings.teleport-range", 30000);
        int attempts = (int) plugin.getConfig().getLong("settings.max-safe-attempts", 50);
        Random rnd = new Random();

        for (int i = 0; i < attempts; i++) {
            int x = rnd.nextInt(range * 2) - range;
            int z = rnd.nextInt(range * 2) - range;
            Location loc = findSafeY(world, x, z, type);
            if (loc != null) return loc;
        }
        return null;
    }

    private Location findSafeY(World world, int x, int z, PortalType type) {
        if (!world.getChunkAt(x >> 4, z >> 4).isLoaded())
            world.getChunkAt(x >> 4, z >> 4).load();

        for (int y = world.getMaxHeight() - 2; y > world.getMinHeight(); y--) {
            Block ground  = world.getBlockAt(x, y, z);
            Block stand1  = world.getBlockAt(x, y + 1, z);
            Block stand2  = world.getBlockAt(x, y + 2, z);

            if (ground.isPassable()) continue;
            if (isDangerous(ground.getType())) continue;
            if (!stand1.isPassable()) continue;
            if (!stand2.isPassable()) continue;
            if (isUnsafeAir(stand1.getType())) continue;
            if (isUnsafeAir(stand2.getType())) continue;
            if (type == PortalType.END && y < 40) continue;

            return new Location(world, x + 0.5, y + 1, z + 0.5);
        }
        return null;
    }

    private boolean isDangerous(Material m) {
        return m == Material.LAVA || m == Material.MAGMA_BLOCK || m == Material.FIRE
            || m == Material.CACTUS || m == Material.WITHER_ROSE || m == Material.SWEET_BERRY_BUSH;
    }

    private boolean isUnsafeAir(Material m) {
        return m == Material.WATER || m == Material.LAVA || m == Material.FIRE
            || m == Material.KELP || m == Material.KELP_PLANT
            || m == Material.SEAGRASS || m == Material.TALL_SEAGRASS
            || m == Material.BUBBLE_COLUMN;
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void loadAll() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("portals");
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            PortalType type = PortalType.fromString(key);
            if (type == null) continue;

            String vidStr   = sec.getString(key + ".villager");
            String holo1Str = sec.getString(key + ".holo1");
            String holo2Str = sec.getString(key + ".holo2");
            if (vidStr == null) continue;

            UUID vid = UUID.fromString(vidStr);
            villagerMap.put(vid, type);
            typeToVillager.put(type, vid);

            List<UUID> stands = new ArrayList<>();
            if (holo1Str != null) stands.add(UUID.fromString(holo1Str));
            if (holo2Str != null) stands.add(UUID.fromString(holo2Str));
            holoStands.put(type, stands);
        }
        plugin.getLogger().info("Caricati " + typeToVillager.size() + " portali villager.");
    }

    private void saveVillager(PortalType type, UUID villagerUuid, Location loc) {
        String base = "portals." + type.getId();
        plugin.getConfig().set(base + ".villager", villagerUuid.toString());
        plugin.getConfig().set(base + ".world", loc.getWorld().getName());
        plugin.getConfig().set(base + ".x", loc.getX());
        plugin.getConfig().set(base + ".y", loc.getY());
        plugin.getConfig().set(base + ".z", loc.getZ());
        plugin.saveConfig();
    }

    // ── Util ─────────────────────────────────────────────────────────────────

    private char colorChar(PortalType type) {
        return switch (type) {
            case OVERWORLD -> 'a';
            case NETHER    -> 'c';
            case END       -> 'd';
        };
    }

    private Villager.Type villagerSkin(PortalType type) {
        return switch (type) {
            case OVERWORLD -> Villager.Type.PLAINS;
            case NETHER    -> Villager.Type.SAVANNA;
            case END       -> Villager.Type.SNOW;
        };
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public Map<PortalType, UUID> getTypeToVillager() { return typeToVillager; }
}

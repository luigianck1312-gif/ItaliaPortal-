package dev.spawnportals.models;

import org.bukkit.Location;
import org.bukkit.World;

public class PortalRegion {

    private final PortalType type;
    private final String worldName;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;

    public PortalRegion(PortalType type, Location pos1, Location pos2) {
        this.type = type;
        this.worldName = pos1.getWorld().getName();
        this.minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        this.maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
    }

    public PortalRegion(PortalType type, String worldName,
                        int minX, int minY, int minZ,
                        int maxX, int maxY, int maxZ) {
        this.type = type;
        this.worldName = worldName;
        this.minX = minX; this.minY = minY; this.minZ = minZ;
        this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
    }

    public boolean contains(Location loc) {
        if (!loc.getWorld().getName().equals(worldName)) return false;
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        return x >= minX && x <= maxX
            && y >= minY && y <= maxY
            && z >= minZ && z <= maxZ;
    }

    public PortalType getType() { return type; }
    public String getWorldName() { return worldName; }
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }
}

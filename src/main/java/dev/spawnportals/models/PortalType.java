package dev.spawnportals.models;

import org.bukkit.Material;
import org.bukkit.World;

public enum PortalType {

    OVERWORLD("overworld", "Overworld", Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS,
            Material.CRACKED_STONE_BRICKS, Material.CHISELED_STONE_BRICKS,
            Material.NETHER_PORTAL, World.Environment.NORMAL),

    NETHER("nether", "Nether", Material.OBSIDIAN, Material.CRYING_OBSIDIAN,
            Material.BLACKSTONE, Material.GILDED_BLACKSTONE,
            Material.NETHER_PORTAL, World.Environment.NETHER),

    END("end", "End", Material.END_STONE_BRICKS, Material.PURPUR_BLOCK,
            Material.PURPUR_PILLAR, Material.END_PORTAL_FRAME,
            Material.NETHER_PORTAL, World.Environment.THE_END);

    private final String id;
    private final String displayName;
    private final Material cornerMaterial;       // angoli della cornice
    private final Material frameMaterial;        // lati della cornice
    private final Material accentMaterial;       // dettagli della cornice
    private final Material rareMaterial;         // blocco speciale (raro)
    private final Material portalLightMaterial;  // luce interna colorata
    private final World.Environment targetEnvironment;

    PortalType(String id, String displayName,
               Material cornerMaterial, Material frameMaterial,
               Material accentMaterial, Material rareMaterial,
               Material portalLightMaterial, World.Environment targetEnvironment) {
        this.id = id;
        this.displayName = displayName;
        this.cornerMaterial = cornerMaterial;
        this.frameMaterial = frameMaterial;
        this.accentMaterial = accentMaterial;
        this.rareMaterial = rareMaterial;
        this.portalLightMaterial = portalLightMaterial;
        this.targetEnvironment = targetEnvironment;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getCornerMaterial() { return cornerMaterial; }
    public Material getFrameMaterial() { return frameMaterial; }
    public Material getAccentMaterial() { return accentMaterial; }
    public Material getRareMaterial() { return rareMaterial; }
    public Material getPortalLightMaterial() { return portalLightMaterial; }
    public World.Environment getTargetEnvironment() { return targetEnvironment; }

    public static PortalType fromString(String s) {
        for (PortalType t : values()) {
            if (t.id.equalsIgnoreCase(s) || t.displayName.equalsIgnoreCase(s)) return t;
        }
        return null;
    }
}

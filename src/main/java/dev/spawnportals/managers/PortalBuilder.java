package dev.spawnportals.managers;

import dev.spawnportals.models.PortalType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Builds a vertical Nether-portal-like structure for each portal type.
 *
 * Schema (width=4, height=5, facing North/South):
 *
 *   [C][F][F][C]    <-- top row
 *   [F][L][L][F]    <-- inner rows (L = glass light)
 *   [A][L][L][A]
 *   [F][L][L][F]
 *   [C][F][F][C]    <-- bottom row
 *
 *  Above center top: Oak/Spruce wall sign
 *
 * C = corner, F = frame, A = accent, L = portal light glass
 */
public class PortalBuilder {

    // Portal dimensions
    public static final int WIDTH = 4;
    public static final int HEIGHT = 5;

    /**
     * Builds the portal starting at the given bottom-left corner (min X, min Y, same Z).
     * The portal is built along the X axis (East-West face).
     */
    public static void buildPortal(Location origin, PortalType type) {
        World world = origin.getWorld();

        Material corner = type.getCornerMaterial();
        Material frame = type.getFrameMaterial();
        Material accent = type.getAccentMaterial();
        Material light = type.getPortalLightMaterial();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Location loc = origin.clone().add(x, y, 0);
                Block block = world.getBlockAt(loc);

                boolean isTopOrBottom = (y == 0 || y == HEIGHT - 1);
                boolean isLeftOrRight = (x == 0 || x == WIDTH - 1);
                boolean isCorner = isTopOrBottom && isLeftOrRight;

                Material mat;
                if (isCorner) {
                    mat = corner;
                } else if (isTopOrBottom) {
                    // Alternate frame and accent on top/bottom rows
                    mat = (x % 2 == 0) ? frame : accent;
                } else if (isLeftOrRight) {
                    // Alternate frame and accent on sides
                    mat = (y % 2 == 1) ? frame : accent;
                } else {
                    // Interior = portal light glass
                    mat = light;
                }

                block.setType(mat);
            }
        }

        // Place the sign above the portal, centered
        placeSign(origin, type, world);
    }

    private static void placeSign(Location origin, PortalType type, World world) {
        // Center X of the 4-wide portal is between block 1 and 2, so use block 1
        int signX = origin.getBlockX() + 1;
        int signY = origin.getBlockY() + HEIGHT; // one block above the portal
        int signZ = origin.getBlockZ();

        Block signBlock = world.getBlockAt(signX, signY, signZ);

        // Use oak sign as wall sign (hanging on south face since portal faces N/S)
        // We need a block behind (north face) to attach — but portal top is there, so
        // instead we place a sign on TOP of the portal top block at center.
        // Place the sign on top of the top-center frame block.
        Block topCenter = world.getBlockAt(signX, origin.getBlockY() + HEIGHT - 1, signZ);
        Block above = topCenter.getRelative(BlockFace.UP);
        above.setType(Material.OAK_SIGN);

        BlockData bd = above.getBlockData();
        if (bd instanceof org.bukkit.block.data.type.Sign signData) {
            // Rotate sign to face East (so it's readable from front)
            signData.setRotation(BlockFace.SOUTH);
            above.setBlockData(signData);
        }

        if (above.getState() instanceof Sign sign) {
            var sideFront = sign.getSide(Side.FRONT);

            Component title;
            Component cmd;
            NamedTextColor color;

            switch (type) {
                case OVERWORLD -> {
                    color = NamedTextColor.GREEN;
                    title = Component.text("✦ OVERWORLD ✦", color, TextDecoration.BOLD);
                    cmd = Component.text("/overworld", NamedTextColor.WHITE);
                }
                case NETHER -> {
                    color = NamedTextColor.RED;
                    title = Component.text("✦ NETHER ✦", color, TextDecoration.BOLD);
                    cmd = Component.text("/nether", NamedTextColor.WHITE);
                }
                case END -> {
                    color = NamedTextColor.GRAY;
                    title = Component.text("✦ END ✦", color, TextDecoration.BOLD);
                    cmd = Component.text("/end", NamedTextColor.WHITE);
                }
                default -> {
                    color = NamedTextColor.WHITE;
                    title = Component.text(type.getDisplayName());
                    cmd = Component.text("/" + type.getId());
                }
            }

            sideFront.line(0, Component.text(""));
            sideFront.line(1, title);
            sideFront.line(2, cmd);
            sideFront.line(3, Component.text(""));
            sign.update();
        }
    }

    /**
     * Removes the portal blocks (replaces with AIR) in a region.
     */
    public static void removePortal(Location origin, int width, int height, World world) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                world.getBlockAt(origin.clone().add(x, y, 0)).setType(Material.AIR);
            }
        }
        // Also remove sign above
        int signX = origin.getBlockX() + 1;
        int signY = origin.getBlockY() + height;
        world.getBlockAt(signX, signY, origin.getBlockZ()).setType(Material.AIR);
    }
}

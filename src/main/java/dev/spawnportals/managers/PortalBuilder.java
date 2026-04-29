package dev.spawnportals.managers;

import dev.spawnportals.models.PortalType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.sign.Side;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Schema IDENTICO al portale del Nether vanilla (4 largo x 5 alto):
 *
 *   [F][F][F][F]   <- top (cornice piena)
 *   [F][P][P][F]
 *   [F][P][P][F]
 *   [F][P][P][F]
 *   [F][F][F][F]   <- bottom (cornice piena)
 *
 * F = blocco cornice (1 blocco spessore su tutti i lati)
 * P = NETHER_PORTAL
 */
public class PortalBuilder {

    public static final int WIDTH  = 4;
    public static final int HEIGHT = 5;

    public static void buildPortal(Location origin, PortalType type) {
        World world = origin.getWorld();
        Material frame = type.getCornerMaterial();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Block block = world.getBlockAt(
                        origin.getBlockX() + x,
                        origin.getBlockY() + y,
                        origin.getBlockZ());

                boolean isEdgeX = (x == 0 || x == WIDTH - 1);
                boolean isEdgeY = (y == 0 || y == HEIGHT - 1);

                if (isEdgeX || isEdgeY) {
                    block.setType(frame);
                } else {
                    block.setType(Material.NETHER_PORTAL);
                    var bd = block.getBlockData();
                    if (bd instanceof Orientable orientable) {
                        orientable.setAxis(org.bukkit.Axis.X);
                        block.setBlockData(orientable);
                    }
                }
            }
        }

        placeSign(origin, type, world);
    }

    private static void placeSign(Location origin, PortalType type, World world) {
        int sx = origin.getBlockX() + 1;
        int sy = origin.getBlockY() + HEIGHT;
        int sz = origin.getBlockZ();

        Block above = world.getBlockAt(sx, sy, sz);
        above.setType(Material.OAK_SIGN);

        var bd = above.getBlockData();
        if (bd instanceof org.bukkit.block.data.type.Sign signData) {
            signData.setRotation(BlockFace.SOUTH);
            above.setBlockData(signData);
        }

        if (above.getState() instanceof Sign sign) {
            var front = sign.getSide(Side.FRONT);

            NamedTextColor color = switch (type) {
                case OVERWORLD -> NamedTextColor.GREEN;
                case NETHER    -> NamedTextColor.RED;
                case END       -> NamedTextColor.LIGHT_PURPLE;
            };
            String label = switch (type) {
                case OVERWORLD -> "* OVERWORLD *";
                case NETHER    -> "*  NETHER  *";
                case END       -> "*    END    *";
            };

            front.line(0, Component.text(""));
            front.line(1, Component.text(label, color, TextDecoration.BOLD));
            front.line(2, Component.text("Entra per teletrasportarti", NamedTextColor.WHITE));
            front.line(3, Component.text(""));
            sign.update();
        }
    }

    public static void removePortal(Location origin, int width, int height, World world) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                world.getBlockAt(
                        origin.getBlockX() + x,
                        origin.getBlockY() + y,
                        origin.getBlockZ()).setType(Material.AIR);
            }
        }
        world.getBlockAt(
                origin.getBlockX() + 1,
                origin.getBlockY() + height,
                origin.getBlockZ()).setType(Material.AIR);
    }
}

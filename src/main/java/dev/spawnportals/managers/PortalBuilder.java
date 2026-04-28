package dev.spawnportals.managers;

import dev.spawnportals.models.PortalType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.NetherPortal;
import org.bukkit.block.sign.Side;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Schema portale (WIDTH=4, HEIGHT=5):
 *
 *   [C][F][F][C]   top
 *   [F][P][P][F]
 *   [A][P][P][A]
 *   [F][P][P][F]
 *   [C][F][F][C]   bottom
 *
 * C=corner, F=frame/accent alternati, A=accent, P=NETHER_PORTAL
 * Il portale è orientato lungo X (asse X), cioè visibile da Nord/Sud.
 */
public class PortalBuilder {

    public static final int WIDTH  = 4;
    public static final int HEIGHT = 5;

    public static void buildPortal(Location origin, PortalType type) {
        World world = origin.getWorld();

        Material corner  = type.getCornerMaterial();
        Material frame   = type.getFrameMaterial();
        Material accent  = type.getAccentMaterial();
        Material rare    = type.getRareMaterial();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Block block = world.getBlockAt(
                        origin.getBlockX() + x,
                        origin.getBlockY() + y,
                        origin.getBlockZ());

                boolean isTop    = y == HEIGHT - 1;
                boolean isBottom = y == 0;
                boolean isLeft   = x == 0;
                boolean isRight  = x == WIDTH - 1;
                boolean isEdge   = isLeft || isRight;
                boolean isHEdge  = isTop || isBottom;
                boolean isCorner = isEdge && isHEdge;
                boolean isInner  = !isEdge && !isHEdge;

                if (isCorner) {
                    block.setType(corner);
                } else if (isHEdge) {
                    // top/bottom row: alterna frame e rare per un effetto carino
                    block.setType(x == 1 ? frame : rare);
                } else if (isEdge) {
                    // lati: alterna frame e accent
                    block.setType(y % 2 == 1 ? frame : accent);
                } else if (isInner) {
                    // interno: NETHER_PORTAL orientato lungo X
                    block.setType(Material.NETHER_PORTAL);
                    var bd = block.getBlockData();
                    if (bd instanceof NetherPortal np) {
                        np.setAxis(org.bukkit.Axis.X);
                        block.setBlockData(np);
                    }
                }
            }
        }

        placeSign(origin, type, world);
    }

    private static void placeSign(Location origin, PortalType type, World world) {
        // cartello sopra al centro (x+1, y+HEIGHT)
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
                case OVERWORLD -> "✦ OVERWORLD ✦";
                case NETHER    -> "✦ NETHER ✦";
                case END       -> "✦  END  ✦";
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
        // rimuovi cartello
        world.getBlockAt(
                origin.getBlockX() + 1,
                origin.getBlockY() + height,
                origin.getBlockZ()).setType(Material.AIR);
    }
}

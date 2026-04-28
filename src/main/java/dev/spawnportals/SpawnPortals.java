package dev.spawnportals;

import dev.spawnportals.commands.PortalCommand;
import dev.spawnportals.listeners.PortalEnterListener;
import dev.spawnportals.listeners.WandListener;
import dev.spawnportals.managers.PortalManager;
import dev.spawnportals.managers.SelectionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SpawnPortals extends JavaPlugin {

    private PortalManager portalManager;
    private SelectionManager selectionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.selectionManager = new SelectionManager();
        this.portalManager = new PortalManager(this);

        // Register commands
        var portalCmd = new PortalCommand(this);
        getCommand("portale").setExecutor(portalCmd);
        getCommand("portale").setTabCompleter(portalCmd);

        // Register listeners
        getServer().getPluginManager().registerEvents(new WandListener(this), this);
        getServer().getPluginManager().registerEvents(new PortalEnterListener(this), this);

        getLogger().info("SpawnPortals v" + getDescription().getVersion() + " abilitato!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SpawnPortals disabilitato.");
    }

    public PortalManager getPortalManager() {
        return portalManager;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    /**
     * Creates the portal wand item (Golden Axe with custom NBT tag).
     */
    public ItemStack createWand() {
        ItemStack axe = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = axe.getItemMeta();

        meta.displayName(Component.text("✦ Bacchetta Portale ✦", NamedTextColor.GOLD, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(List.of(
                Component.text("Click Sinistro » Primo angolo", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Click Destro  » Secondo angolo", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Poi: /place <tipo>", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.getPersistentDataContainer().set(
                WandListener.WAND_KEY,
                PersistentDataType.BYTE,
                (byte) 1
        );

        axe.setItemMeta(meta);
        return axe;
    }
}

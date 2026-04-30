package dev.spawnportals;

import dev.spawnportals.commands.PortalCommand;
import dev.spawnportals.listeners.VillagerClickListener;
import dev.spawnportals.listeners.VillagerProtectListener;
import dev.spawnportals.managers.PortalManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnPortals extends JavaPlugin {

    private PortalManager portalManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.portalManager = new PortalManager(this);
        getServer().getScheduler().runTaskLater(this, () -> portalManager.loadAll(), 20L);

        var cmd = new PortalCommand(this);
        getCommand("portale").setExecutor(cmd);
        getCommand("portale").setTabCompleter(cmd);

        getServer().getPluginManager().registerEvents(new VillagerClickListener(this), this);
        getServer().getPluginManager().registerEvents(new VillagerProtectListener(this), this);

        getLogger().info("SpawnPortals v" + getDescription().getVersion() + " abilitato!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SpawnPortals disabilitato.");
    }

    public PortalManager getPortalManager() { return portalManager; }
}

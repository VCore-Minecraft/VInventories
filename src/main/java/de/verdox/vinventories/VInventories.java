package de.verdox.vinventories;

import de.verdox.vcorepaper.impl.plugin.VCorePaperPlugin;
import de.verdox.vcorepaper.impl.plugin.VCorePaperSubsystem;
import de.verdox.vinventories.inventories.BukkitPlayerHandler;

import java.util.List;

public final class VInventories extends VCorePaperPlugin {

    @Override
    public void onPluginEnable() {

    }

    @Override
    public void onPluginDisable() {

    }

    @Override
    public List<VCorePaperSubsystem> provideSubsystems() {
        return List.of(new BukkitPlayerHandler(this));
    }
}

/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vinventories.inventories;

import de.verdox.vcore.synchronization.pipeline.annotations.DataStorageIdentifier;
import de.verdox.vcore.synchronization.pipeline.datatypes.PlayerData;
import de.verdox.vcore.synchronization.pipeline.datatypes.ServerData;
import de.verdox.vcorepaper.impl.plugin.VCorePaperPlugin;
import de.verdox.vcorepaper.impl.plugin.VCorePaperSubsystem;
import de.verdox.vinventories.inventories.files.VInventoriesConfig;
import de.verdox.vinventories.inventories.listener.PlayerListener;
import de.verdox.vinventories.inventories.playerdata.InventoryPlayerData;

import java.util.Set;

/**
 * @version 1.0
 * @Author: Lukas Jonsson (Verdox)
 * @date 20.06.2021 00:19
 */

//TODO: Pro Server ein StandardInventory

@DataStorageIdentifier(identifier = "VInventories")
public class BukkitPlayerHandler extends VCorePaperSubsystem {

    private VInventoriesConfig vInventoriesConfig;
    public BukkitPlayerHandler(VCorePaperPlugin VCorePlugin) {
        super(VCorePlugin);
    }

    @Override
    public boolean isActivated() {
        return true;
    }

    @Override
    public void onSubsystemEnable() {
        vInventoriesConfig = new VInventoriesConfig(getVCorePlugin(), "config.yml", "//settings");
        vInventoriesConfig.init();
        getVCorePlugin().getServices().eventBus.register(new PlayerListener(this));
    }

    @Override
    public void onSubsystemDisable() {

    }

    @Override
    public Set<Class<? extends PlayerData>> playerDataClasses() {
        return Set.of(InventoryPlayerData.class);
    }

    @Override
    public Set<Class<? extends ServerData>> serverDataClasses() {
        return null;
    }

    public VInventoriesConfig getVInventoriesConfig() {
        return vInventoriesConfig;
    }
}

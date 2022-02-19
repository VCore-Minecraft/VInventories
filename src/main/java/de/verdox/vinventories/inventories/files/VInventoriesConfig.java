package de.verdox.vinventories.inventories.files;

import de.verdox.vcore.plugin.VCorePlugin;
import de.verdox.vcore.plugin.files.config.VCoreYAMLConfig;

/**
 * @version 1.0
 * @Author: Lukas Jonsson (Verdox)
 * @date 14.08.2021 22:32
 */
public class VInventoriesConfig extends VCoreYAMLConfig {

    private String standardInventoryID;

    public VInventoriesConfig(VCorePlugin<?, ?> plugin, String fileName, String pluginDirectory) {
        super(plugin, fileName, pluginDirectory);
    }

    @Override
    public void setupConfig() {
        config.addDefault("standardInventoryName", "vanilla");
        config.options().copyDefaults(true);
    }

    public String getStandardInventoryID() {
        if (standardInventoryID == null)
            standardInventoryID = config.getString("standardInventoryName");
        return standardInventoryID;
    }
}

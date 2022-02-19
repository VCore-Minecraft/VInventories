/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vinventories.inventories.playerdata;

import de.verdox.vcore.plugin.VCorePlugin;
import de.verdox.vcore.synchronization.pipeline.annotations.*;
import de.verdox.vcore.synchronization.pipeline.datatypes.PlayerData;
import de.verdox.vcorepaper.custom.pipelinedata.inventories.SerializablePlayerInventory;
import de.verdox.vinventories.inventories.BukkitPlayerHandler;
import de.verdox.vinventories.inventories.event.PlayerInventoryRestoreEvent;
import de.verdox.vinventories.inventories.event.PlayerInventorySaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @version 1.0
 * @Author: Lukas Jonsson (Verdox)
 * @date 20.06.2021 00:27
 */
@DataStorageIdentifier(identifier = "Inventories")
@RequiredSubsystemInfo(parentSubSystem = BukkitPlayerHandler.class)
@VCoreDataProperties(preloadStrategy = PreloadStrategy.LOAD_ON_NEED, dataContext = DataContext.GLOBAL, cleanOnNoUse = false, time = 30)
public class InventoryPlayerData extends PlayerData {

    private final Map<String, Map<String, Object>> inventoryCache = new ConcurrentHashMap<>();
    private String activeInventoryID = null;

    public InventoryPlayerData(VCorePlugin<?, ?> plugin, UUID playerUUID) {
        super(plugin, playerUUID);
    }

    //@Override
    //public void onSync(Map<String, Object> dataBeforeSync) {
    //    if (!dataBeforeSync.containsKey("activeInventoryID"))
    //        return;
    //    String invIdBeforeSync = (String) dataBeforeSync.get("activeInventoryID");
    //    if (invIdBeforeSync != null && invIdBeforeSync.equals(activeInventoryID))
    //        return;
    //    Player player = Bukkit.getPlayer(getObjectUUID());
    //    if (player == null)
    //        return;
    //    saveInventory(() -> player, invIdBeforeSync);
    //    restoreInventory(activeInventoryID, () -> player);
    //    getPlugin().consoleMessage("&eInventory &6" + activeInventoryID + " &eof player &b" + getObjectUUID() + " &erestored due to a &6sync&7!&b" + System.currentTimeMillis(), true);
    //}

    public void createInventory(String inventoryName) {
        if (hasInventory(inventoryName))
            return;
        Map<String, Object> invData = new SerializablePlayerInventory(inventoryName, GameMode.SURVIVAL, new ItemStack[0], new ItemStack[0], new ItemStack[0], null, 20, 20, 0, 0, new HashSet<>()).getUnderlyingMap();

        inventoryCache.put(inventoryName, invData);
        save(true);
    }

    public void saveInventory() {
        Player player = Bukkit.getPlayer(getObjectUUID());
        if (player == null)
            return;
        saveInventory(() -> player);
    }

    public void saveInventory(Supplier<Player> supplier) {
        saveInventory(supplier, activeInventoryID);
    }

    public void saveInventory(Supplier<Player> supplier, String inventoryID) {
        Player player = supplier.get();
        if (player == null)
            return;
        ItemStack[] storageContents = player.getInventory().getStorageContents();
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        ItemStack[] enderChest = player.getEnderChest().getStorageContents();

        ItemStack offHand = player.getInventory().getItemInOffHand();

        PlayerInventorySaveEvent playerInventorySaveEvent = new PlayerInventorySaveEvent(player, inventoryID);
        Bukkit.getPluginManager().callEvent(playerInventorySaveEvent);
        SerializablePlayerInventory serializablePlayerInventory = new SerializablePlayerInventory(playerInventorySaveEvent.getInventoryID(), player.getGameMode(), storageContents, armorContents, enderChest, offHand, player.getHealth(), player.getFoodLevel(), player.getLevel(), player.getTotalExperience(), new HashSet<>(player.getActivePotionEffects()));
        inventoryCache.put(inventoryID, serializablePlayerInventory.getUnderlyingMap());
        getPlugin().consoleMessage("&eInventory &6" + playerInventorySaveEvent.getInventoryID() + " &eof player &b" + getObjectUUID() + " &esaved&7! &b" + System.currentTimeMillis(), true);

        save(true);
    }

    public boolean hasInventory(@Nonnull String inventoryID) {
        return inventoryCache.containsKey(inventoryID);
    }

    public void restoreInventory(@Nonnull String inventoryID, @Nonnull Supplier<Player> supplier) {
        Player player = supplier.get();
        if (player == null)
            return;

        PlayerInventoryRestoreEvent playerInventoryRestoreEvent = new PlayerInventoryRestoreEvent(player, inventoryID);
        Bukkit.getPluginManager().callEvent(playerInventoryRestoreEvent);

        this.activeInventoryID = playerInventoryRestoreEvent.getInventoryID();
        if (!inventoryCache.containsKey(activeInventoryID)) {
            getPlugin().consoleMessage("&cInventory &e" + activeInventoryID + " &cunknown &b" + getObjectUUID(), true);
            throw new IllegalStateException("&cInventory &e" + activeInventoryID + " &cunknown &b" + getObjectUUID());
        }
        SerializablePlayerInventory serializablePlayerInventory = new SerializablePlayerInventory(inventoryCache.get(activeInventoryID));
        serializablePlayerInventory.restoreInventory(player, null);

        save(true);
        getPlugin().consoleMessage("&eInventory &6" + activeInventoryID + " &eof player &b" + getObjectUUID() + " &erestored&7! &b" + System.currentTimeMillis(), true);
    }

    @Override
    public void onCleanUp() {
        Player player = Bukkit.getPlayer(getObjectUUID());
        if (player == null)
            return;
        saveInventory(() -> player);
    }
}

/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vinventories.inventories.listener;

import com.google.common.eventbus.Subscribe;
import de.verdox.vcore.synchronization.pipeline.parts.Pipeline;
import de.verdox.vcore.synchronization.pipeline.player.events.PlayerPreSessionLoadEvent;
import de.verdox.vcore.synchronization.pipeline.player.events.PlayerSessionLoadedEvent;
import de.verdox.vcorepaper.impl.listener.VCorePaperListener;
import de.verdox.vcorepaper.utils.BukkitPlayerUtil;
import de.verdox.vinventories.inventories.BukkitPlayerHandler;
import de.verdox.vinventories.inventories.playerdata.InventoryPlayerData;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.permissions.Permission;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @Author: Lukas Jonsson (Verdox)
 * @date 20.06.2021 00:46
 */
public class PlayerListener extends VCorePaperListener {

    private final ConcurrentHashMap.KeySetView<Player, Boolean> freezedPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastSync = new ConcurrentHashMap<>();
    private final BukkitPlayerHandler subsystem;

    public PlayerListener(BukkitPlayerHandler subsystem) {
        super(subsystem);
        this.subsystem = subsystem;
    }

    @Subscribe
    public void onSessionPreLoad(PlayerPreSessionLoadEvent e) {
        if (!e.getPlugin().equals(getPlugin()))
            return;
        Player player = Bukkit.getPlayer(e.getPlayerUUID());
        freezedPlayers.add(player);
    }

    @Subscribe
    public void onSessionLoaded(PlayerSessionLoadedEvent e) {
        if (!e.getPlugin().equals(getPlugin()))
            return;
        plugin.async(() -> {
            UUID playerUUID = e.getPlayerUUID();
            InventoryPlayerData inventoryPlayerData = subsystem.getVCorePlugin().getServices().getPipeline().load(InventoryPlayerData.class, playerUUID, Pipeline.LoadingStrategy.LOAD_PIPELINE);
            if (inventoryPlayerData == null)
                return;
            Player player = Bukkit.getPlayer(e.getPlayerUUID());

            String invID = subsystem.getVInventoriesConfig().getStandardInventoryID();
            if (!inventoryPlayerData.hasInventory(invID))
                inventoryPlayerData.createInventory(invID);
            inventoryPlayerData.restoreInventory(invID, () -> player);
            if (player.getGameMode().equals(GameMode.CREATIVE)) {
                Permission permission = new Permission("vInventories.creativeAllowed");

                if (!player.hasPermission(permission))
                    getPlugin().sync(() -> player.setGameMode(GameMode.SURVIVAL));
            }
            freezedPlayers.remove(player);
        });
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        plugin.async(() -> {
            syncInv(e.getPlayer());
        });
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        plugin.async(() -> {
            syncInv(e.getPlayer());
        });
    }

    @EventHandler
    public void dropItem(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if (!freezedPlayers.contains(player))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void attemptPickupItem(PlayerAttemptPickupItemEvent e) {
        Player player = e.getPlayer();
        if (!freezedPlayers.contains(player))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void playerDeathEvent(PlayerDeathEvent e) {
        if (!freezedPlayers.contains(e.getEntity()))
            return;
        e.setKeepInventory(true);
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        getPlugin().async(() -> {
            saveInv(player);
        });
        freezedPlayers.remove(player);
    }

    @EventHandler
    public void playerKickEvent(PlayerKickEvent e) {
        Player player = e.getPlayer();
        getPlugin().async(() -> {
            saveInv(player);
        });
        freezedPlayers.remove(player);
    }

    private void saveInv(Player player) {
        // Preventing Citizens Save
        if (player.getName().length() > 16)
            return;
        InventoryPlayerData inventoryPlayerData = subsystem.getVCorePlugin().getServices().getPipeline().load(InventoryPlayerData.class, player.getUniqueId(), Pipeline.LoadingStrategy.LOAD_PIPELINE);
        if (inventoryPlayerData == null)
            return;
        inventoryPlayerData.saveInventory(() -> player);
        inventoryPlayerData.save(true);
    }

    private void syncInv(Player player) {
        // Preventing Citizens Save
        if (player.getName().length() > 16)
            return;
        if (lastSync.containsKey(player.getUniqueId())) {
            long timeStamp = lastSync.get(player.getUniqueId());
            if (System.currentTimeMillis() - timeStamp <= TimeUnit.SECONDS.toMillis(5))
                return;
        }
        lastSync.put(player.getUniqueId(), System.currentTimeMillis());
        BukkitPlayerUtil.sendPlayerMessage(player, ChatMessageType.ACTION_BAR, "&eInventar synchronisiert");
        InventoryPlayerData inventoryPlayerData = subsystem.getVCorePlugin().getServices().getPipeline().load(InventoryPlayerData.class, player.getUniqueId(), Pipeline.LoadingStrategy.LOAD_PIPELINE);
        if (inventoryPlayerData == null)
            return;
        inventoryPlayerData.save(true);
    }
}

/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vinventories.inventories.event;

import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

/**
 * @version 1.0
 * @Author: Lukas Jonsson (Verdox)
 * @date 12.07.2021 20:30
 */
public class PlayerInventorySaveEvent extends PlayerInventoryEvent{
    public PlayerInventorySaveEvent(@Nonnull Player player, @Nonnull String inventoryToRestore) {
        super(player, inventoryToRestore);
    }
}

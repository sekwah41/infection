package com.sekwah.infection.controller;

import com.sekwah.infection.InfectionMod;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class InfectedInventoryController {
    public void handleItems(ServerPlayer player) {
        var inventory = player.getInventory();
        inventory.clearContent();
        inventory.pickSlot(0);
        player.connection.send(new ClientboundSetCarriedItemPacket(0));
        // First infected starts with a better weapon
        if (player.getUUID().equals(InfectionMod.infectionController.firstInfected)) {
            inventory.setItem(0, new ItemStack(Items.IRON_SWORD));
        }

    }
}

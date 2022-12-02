package com.sekwah.infection.controller;

import com.sekwah.infection.InfectionMod;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class InfectedInventoryController {

    private int infectionStage = 0;

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

    public int getInfectionStage() {
        return infectionStage;
    }

    /**
     * Resends the inventory to the player in case of desync e.g. cancelling drops.
     * @param serverPlayer
     */
    public void resendInventory(ServerPlayer serverPlayer) {
        serverPlayer.inventoryMenu.sendAllDataToRemote();
    }
}

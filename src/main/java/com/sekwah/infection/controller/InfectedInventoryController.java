package com.sekwah.infection.controller;

import com.sekwah.infection.InfectionMod;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class InfectedInventoryController {

    public record InfectedInventory(ItemLike alphaWeapon, ItemLike defaultWeapon, ItemLike[] armor, ItemLike[] remainingHotbar) {
        public InfectedInventory(ItemLike alphaWeapon, ItemLike defaultWeapon) {
            this(alphaWeapon, defaultWeapon, new ItemLike[]{}, new ItemLike[]{});
        }

        public InfectedInventory(ItemLike alphaWeapon, ItemLike defaultWeapon, ItemLike[] armor) {
            this(alphaWeapon, defaultWeapon, armor, new ItemLike[]{});
        }
    }

    private InfectedInventory[] infectedInventories = {
            new InfectedInventory(Items.IRON_SWORD, Items.AIR)
    };

    private int infectionStage = 0;

    private int ticksToNextStage = 0;

    public void handleItems(ServerPlayer player) {
        var inventory = player.getInventory();
        inventory.clearContent();
        inventory.pickSlot(0);
        player.connection.send(new ClientboundSetCarriedItemPacket(0));
        // First infected starts with a better weapon
        if (player.getUUID().equals(InfectionMod.infectionController.firstInfected)) {
            inventory.setItem(0, infectedInventories[infectionStage].alphaWeapon.asItem().getDefaultInstance());
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

    public void tick() {
        // TODO do logic to increase the loot.
    }

    public void upgrade() {
        ticksToNextStage = 0;
    }

    public void start() {
        ticksToNextStage = 0;
        infectionStage = 0;
    }
}

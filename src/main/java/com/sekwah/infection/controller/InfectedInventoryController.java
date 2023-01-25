package com.sekwah.infection.controller;

import com.sekwah.infection.InfectionMod;
import com.sekwah.infection.scheduler.DelayedTickEvent;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class InfectedInventoryController {


    private DelayedTickEvent<MinecraftServer> upgradeTask;

    public int timeTillNextStage() {
        if(upgradeTask == null || !upgradeAvailable()) {
            return 0;
        }
        else {
            return upgradeTask.getTicksLeft();
        }
    }

    private InfectedInventory[] infectedInventories = {
            new InfectedInventory(Items.IRON_SWORD, Items.AIR),
            new InfectedInventory(Items.DIAMOND_SHOVEL, Items.WOODEN_SWORD),
            new InfectedInventory(Items.DIAMOND_SWORD, Items.WOODEN_SWORD),
            new InfectedInventory(Items.NETHERITE_SWORD, Items.DIAMOND_SWORD),
    };

    /**
     * Register the ticking events for during the game
     */
    public void register() {
        this.upgradeTask = InfectionMod.infectionController.serverTaskScheduler.scheduleIntervalTickEvent(this::upgrade, 20 * 20);//Math.round(20 * 60 * InfectionMod.infectionController.configController.getConfig().minsBetweenInfectionUpgrades));

        this.start();
    }

    public record InfectedInventory(ItemLike alphaWeapon, ItemLike defaultWeapon, ItemLike[] armor, ItemLike[] remainingHotbar) {
        public InfectedInventory(ItemLike alphaWeapon, ItemLike defaultWeapon) {
            this(alphaWeapon, defaultWeapon, new ItemLike[]{}, new ItemLike[]{});
        }

        public InfectedInventory(ItemLike alphaWeapon, ItemLike defaultWeapon, ItemLike[] armor) {
            this(alphaWeapon, defaultWeapon, armor, new ItemLike[]{});
        }
    }

    private int infectionStage = 0;


    public void handleInfectedItems(ServerPlayer player) {
        this.handleInfectedItems(player, false);
    }

    /**
     * Sort the inventory for the infecected players
     * @param player
     */
    public void handleInfectedItems(ServerPlayer player, boolean setSlot) {
        if(player.getTeam() != InfectionMod.infectionController.infectedTeam) {
            return;
        }
        var inventory = player.getInventory();
        inventory.clearContent();
        if(setSlot) {
            inventory.pickSlot(0);
            player.connection.send(new ClientboundSetCarriedItemPacket(0));
        }
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

    private boolean upgradeAvailable() {
        return infectedInventories.length - 1 > infectionStage;
    }

    public void upgrade(MinecraftServer server) {
        if(!upgradeAvailable()) {
            return;
        }

        for(ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.MASTER, Float.MAX_VALUE, 1f);
            this.handleInfectedItems(player);
        }



        infectionStage++;
    }

    public void start() {
        infectionStage = 0;
    }
}

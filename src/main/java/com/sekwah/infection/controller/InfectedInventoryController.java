package com.sekwah.infection.controller;

import com.sekwah.infection.InfectionMod;
import com.sekwah.infection.scheduler.DelayedTickEvent;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

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

    private EquipmentSlot[] armorSlotMapping = new EquipmentSlot[] {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private InfectedInventory[] infectedInventories = {
            //Stage 1 -
            //Wooden Sword
            //Infinite Red Concrete
            //Compass
            //No Armour
            //Stone Shovel/Pick
            //Wooden Axe
            new InfectedInventory(Items.IRON_SWORD, Items.WOODEN_SWORD, new ItemLike[]{},
                    new ItemLike[]{
                            Blocks.RED_CONCRETE,
                            Items.WOODEN_AXE,
                            Items.STONE_PICKAXE,
                            Items.STONE_SHOVEL,
                            Items.COMPASS
            }),
            //Stage 2 -
            //Stone Sword
            //Infinite Red Concrete
            //Compass
            //Leather Armour
            //Iron Shovel/Pick
            //Stone Axe
            new InfectedInventory(Items.IRON_SWORD, Items.STONE_SWORD,
                    new ItemLike[]{
                            Items.LEATHER_HELMET,
                            Items.LEATHER_CHESTPLATE,
                            Items.LEATHER_LEGGINGS,
                            Items.LEATHER_BOOTS
                    },
                    new ItemLike[]{
                            Blocks.RED_CONCRETE,
                            Items.STONE_AXE,
                            Items.IRON_PICKAXE,
                            Items.IRON_SHOVEL,
                            Items.COMPASS
            }),
            //Stage 3 -
            //Iron Sword
            //Infinite Red Concrete
            //Compass
            //Gold Armour
            //Iron Shovel/Pick
            //Stone Axe
            new InfectedInventory(Items.IRON_SWORD, Items.IRON_SWORD,
                    new ItemLike[]{
                            Items.GOLDEN_HELMET,
                            Items.GOLDEN_CHESTPLATE,
                            Items.GOLDEN_LEGGINGS,
                            Items.GOLDEN_BOOTS
                    },
                    new ItemLike[]{
                            Blocks.RED_CONCRETE,
                            Items.STONE_AXE,
                            Items.IRON_PICKAXE,
                            Items.IRON_SHOVEL,
                            Items.COMPASS
                    }),
            //Stage 4 -
            //Diamond Sword
            //Infinite Red Concrete
            //Compass
            //Chainmail Armour
            //Diamond Shovel/Pick
            //Iron Axe
            new InfectedInventory(Items.DIAMOND_SWORD, Items.DIAMOND_SWORD,
                    new ItemLike[]{
                            Items.CHAINMAIL_HELMET,
                            Items.CHAINMAIL_CHESTPLATE,
                            Items.CHAINMAIL_LEGGINGS,
                            Items.CHAINMAIL_BOOTS
                    },
                    new ItemLike[]{
                            Blocks.RED_CONCRETE,
                            Items.IRON_AXE,
                            Items.DIAMOND_PICKAXE,
                            Items.DIAMOND_SHOVEL,
                            Items.COMPASS
                    }),
            //Stage 5 -
            //Netherite Sword
            //Infinite Red Concrete
            //Compass
            //Iron Armour
            //Netherite Shovel/Pick
            //Iron Axe
            new InfectedInventory(Items.NETHERITE_SWORD, Items.NETHERITE_SWORD,
                    new ItemLike[]{
                            Items.IRON_HELMET,
                            Items.IRON_CHESTPLATE,
                            Items.IRON_LEGGINGS,
                            Items.IRON_BOOTS
                    },
                    new ItemLike[]{
                            Blocks.RED_CONCRETE,
                            Items.IRON_AXE,
                            Items.NETHERITE_PICKAXE,
                            Items.NETHERITE_SHOVEL,
                            Items.COMPASS
                    }),
    };

    /**
     * Register the ticking events for during the game
     */
    public void register() {
        this.upgradeTask = InfectionMod.infectionController.serverTaskScheduler.scheduleIntervalTickEvent(this::upgrade, Math.round(20f * 60f * InfectionMod.infectionController.configController.getConfig().minsBetweenInfectionUpgrades));

        this.start();
    }

    public record InfectedInventory(ItemLike alphaWeapon, ItemLike defaultWeapon, ItemLike[] armor, ItemLike[] remainingHotbar) {
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
        var firstInfected = player.getUUID().equals(InfectionMod.infectionController.firstInfected);
        var infectedInventory = infectedInventories[infectionStage];
        var primaryItem = firstInfected ? infectedInventory.alphaWeapon : infectedInventory.defaultWeapon;
        inventory.setItem(0, primaryItem.asItem().getDefaultInstance());
        for (int i = 0; i < infectedInventory.remainingHotbar.length; i++) {
            var item = infectedInventory.remainingHotbar[i].asItem().getDefaultInstance();
            if(!item.is(Items.COMPASS)) {
                item.setCount(item.getMaxStackSize());
            }
            inventory.setItem(i + 1, item);
        }
        for(int i = 0; i < infectedInventory.armor.length; i++) {
            player.setItemSlot(armorSlotMapping[i], infectedInventory.armor[i].asItem().getDefaultInstance());
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
        infectionStage++;

        for(ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.MASTER, Float.MAX_VALUE, 1f);
            this.handleInfectedItems(player);
        }
    }

    public void start() {
        infectionStage = 0;
    }
}

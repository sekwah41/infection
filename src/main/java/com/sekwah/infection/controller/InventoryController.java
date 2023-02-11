package com.sekwah.infection.controller;

import com.sekwah.infection.InfectionMod;
import com.sekwah.infection.mixin.CompassItemAccessor;
import com.sekwah.infection.scheduler.DelayedTickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.function.Function;

public class InventoryController {


    private DelayedTickEvent<MinecraftServer> upgradeTask;

    public int timeTillNextStage() {
        if (upgradeTask == null || !upgradeAvailable()) {
            return 0;
        } else {
            return upgradeTask.getTicksLeft();
        }
    }

    private EquipmentSlot[] armorSlotMapping = new EquipmentSlot[]{
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    public static final Component PLAYER_TRACKER_NAME = Component.literal("Player Tracker");
    public static final Component PORTAL_TRACKER_NAME = Component.literal("Portal Tracker");

    private List<Function<Boolean, InfectedInventory>> infectedInvetoryCreators = List.of(
            //Stage 1 -
            //Wooden Sword
            //Infinite Red Concrete
            //Compass
            //No Armour
            //Stone Shovel/Pick
            //Wooden Axe
            (Boolean isAlpha) -> new InfectedInventory(new ItemStack[]{},
                    new ItemStack[]{
                            isAlpha ? new ItemStack(Items.IRON_SWORD) : new ItemStack(Items.WOODEN_SWORD),
                            new ItemStack(Blocks.RED_CONCRETE),
                            new ItemStack(Items.WOODEN_AXE),
                            new ItemStack(Items.STONE_PICKAXE),
                            new ItemStack(Items.STONE_SHOVEL),
                            new ItemStack(Items.COMPASS).setHoverName(PLAYER_TRACKER_NAME),
                            new ItemStack(Items.AIR),
                            new ItemStack(Items.AIR),
                            new ItemStack(Items.COMPASS).setHoverName(PORTAL_TRACKER_NAME),
                    }),
            //Stage 2 -
            //Stone Sword
            //Infinite Red Concrete
            //Compass
            //Leather Armour
            //Iron Shovel/Pick
            //Stone Axe
            (Boolean isAlpha) -> new InfectedInventory(new ItemStack[]{
                    Items.LEATHER_HELMET.getDefaultInstance(),
                    Items.LEATHER_CHESTPLATE.getDefaultInstance(),
                    Items.LEATHER_LEGGINGS.getDefaultInstance(),
                    Items.LEATHER_BOOTS.getDefaultInstance()
            },
                    new ItemStack[]{
                            isAlpha ? new ItemStack(Items.IRON_SWORD) : new ItemStack(Items.STONE_SWORD),
                            new ItemStack(Blocks.RED_CONCRETE),
                            new ItemStack(Items.STONE_AXE),
                            new ItemStack(Items.IRON_PICKAXE),
                            new ItemStack(Items.IRON_SHOVEL),
                            new ItemStack(Items.COMPASS).setHoverName(PLAYER_TRACKER_NAME),
                            new ItemStack(Items.AIR),
                            new ItemStack(Items.AIR),
                            new ItemStack(Items.COMPASS).setHoverName(PORTAL_TRACKER_NAME),
                    }),
            //Stage 3 -
            //Iron Sword
            //Infinite Red Concrete
            //Compass
            //Gold Armour
            //Iron Shovel/Pick
            //Stone Axe
            (Boolean isAlpha) -> new InfectedInventory(new ItemStack[]{
                    Items.GOLDEN_HELMET.getDefaultInstance(),
                    Items.GOLDEN_CHESTPLATE.getDefaultInstance(),
                    Items.GOLDEN_LEGGINGS.getDefaultInstance(),
                    Items.GOLDEN_BOOTS.getDefaultInstance()
            },
                    new ItemStack[]{
                            isAlpha ? new ItemStack(Items.IRON_SWORD) : new ItemStack(Items.IRON_SWORD),
                            new ItemStack(Blocks.RED_CONCRETE),
                            new ItemStack(Items.STONE_AXE),
                            new ItemStack(Items.IRON_PICKAXE),
                            new ItemStack(Items.IRON_SHOVEL),
                            new ItemStack(Items.COMPASS).setHoverName(PLAYER_TRACKER_NAME),
                            new ItemStack(Items.AIR),
                            new ItemStack(Items.AIR),
                            new ItemStack(Items.COMPASS).setHoverName(PORTAL_TRACKER_NAME),
                    }),
            //Stage 4 -
            //Diamond Sword
            //Infinite Red Concrete
            //Compass
            //Chainmail Armour
            //Diamond Shovel/Pick
            //Iron Axe
            (Boolean isAlpha) -> new InfectedInventory(new ItemStack[]{
                    Items.CHAINMAIL_HELMET.getDefaultInstance(),
                    Items.CHAINMAIL_CHESTPLATE.getDefaultInstance(),
                    Items.CHAINMAIL_LEGGINGS.getDefaultInstance(),
                    Items.CHAINMAIL_BOOTS.getDefaultInstance()
            },
                    new ItemStack[]{
                            isAlpha ? new ItemStack(Items.DIAMOND_SWORD) : new ItemStack(Items.DIAMOND_SWORD),
                            new ItemStack(Blocks.RED_CONCRETE),
                            new ItemStack(Items.IRON_AXE),
                            new ItemStack(Items.DIAMOND_PICKAXE),
                            new ItemStack(Items.DIAMOND_SHOVEL),
                            new ItemStack(Items.COMPASS).setHoverName(PLAYER_TRACKER_NAME),
                            new ItemStack(Items.AIR),
                            new ItemStack(Items.AIR),
                            new ItemStack(Items.COMPASS).setHoverName(PORTAL_TRACKER_NAME),
                    }),
            //Stage 5 -
            //Netherite Sword
            //Infinite Red Concrete
            //Compass
            //Iron Armour
            //Netherite Shovel/Pick
            //Iron Axe
            (Boolean isAlpha) -> new InfectedInventory(new ItemStack[]{
                    Items.IRON_HELMET.getDefaultInstance(),
                    Items.IRON_CHESTPLATE.getDefaultInstance(),
                    Items.IRON_LEGGINGS.getDefaultInstance(),
                    Items.IRON_BOOTS.getDefaultInstance()
            },
                    new ItemStack[]{
                            isAlpha ? new ItemStack(Items.NETHERITE_SWORD) : new ItemStack(Items.NETHERITE_SWORD),
                            new ItemStack(Blocks.RED_CONCRETE),
                            new ItemStack(Items.IRON_AXE),
                            new ItemStack(Items.NETHERITE_PICKAXE),
                            new ItemStack(Items.NETHERITE_SHOVEL),
                            new ItemStack(Items.COMPASS).setHoverName(PLAYER_TRACKER_NAME),
                            new ItemStack(Items.AIR),
                            new ItemStack(Items.AIR),
                            new ItemStack(Items.COMPASS).setHoverName(PORTAL_TRACKER_NAME),
                    })
    );
//    private InfectedInventory[] infectedInventories = {
//            //Stage 1 -
//            //Wooden Sword
//            //Infinite Red Concrete
//            //Compass
//            //No Armour
//            //Stone Shovel/Pick
//            //Wooden Axe
//            new InfectedInventory(Items.IRON_SWORD, Items.WOODEN_SWORD, new ItemLike[]{},
//                    new ItemLike[]{
//                            Blocks.RED_CONCRETE,
//                            Items.WOODEN_AXE,
//                            Items.STONE_PICKAXE,
//                            Items.STONE_SHOVEL,
//                            Items.COMPASS,
//                            Items.RECOVERY_COMPASS
//                    }),
//            //Stage 2 -
//            //Stone Sword
//            //Infinite Red Concrete
//            //Compass
//            //Leather Armour
//            //Iron Shovel/Pick
//            //Stone Axe
//            new InfectedInventory(Items.IRON_SWORD, Items.STONE_SWORD,
//                    new ItemLike[]{
//                            Items.LEATHER_HELMET,
//                            Items.LEATHER_CHESTPLATE,
//                            Items.LEATHER_LEGGINGS,
//                            Items.LEATHER_BOOTS
//                    },
//                    new ItemLike[]{
//                            Blocks.RED_CONCRETE,
//                            Items.STONE_AXE,
//                            Items.IRON_PICKAXE,
//                            Items.IRON_SHOVEL,
//                            Items.COMPASS,
//                            Items.RECOVERY_COMPASS
//                    }),
//            //Stage 3 -
//            //Iron Sword
//            //Infinite Red Concrete
//            //Compass
//            //Gold Armour
//            //Iron Shovel/Pick
//            //Stone Axe
//            new InfectedInventory(Items.IRON_SWORD, Items.IRON_SWORD,
//                    new ItemLike[]{
//                            Items.GOLDEN_HELMET,
//                            Items.GOLDEN_CHESTPLATE,
//                            Items.GOLDEN_LEGGINGS,
//                            Items.GOLDEN_BOOTS
//                    },
//                    new ItemLike[]{
//                            Blocks.RED_CONCRETE,
//                            Items.STONE_AXE,
//                            Items.IRON_PICKAXE,
//                            Items.IRON_SHOVEL,
//                            Items.COMPASS,
//                            Items.RECOVERY_COMPASS
//                    }),
//            //Stage 4 -
//            //Diamond Sword
//            //Infinite Red Concrete
//            //Compass
//            //Chainmail Armour
//            //Diamond Shovel/Pick
//            //Iron Axe
//            new InfectedInventory(Items.DIAMOND_SWORD, Items.DIAMOND_SWORD,
//                    new ItemLike[]{
//                            Items.CHAINMAIL_HELMET,
//                            Items.CHAINMAIL_CHESTPLATE,
//                            Items.CHAINMAIL_LEGGINGS,
//                            Items.CHAINMAIL_BOOTS
//                    },
//                    new ItemLike[]{
//                            Blocks.RED_CONCRETE,
//                            Items.IRON_AXE,
//                            Items.DIAMOND_PICKAXE,
//                            Items.DIAMOND_SHOVEL,
//                            Items.COMPASS,
//                            Items.RECOVERY_COMPASS
//                    }),
//            //Stage 5 -
//            //Netherite Sword
//            //Infinite Red Concrete
//            //Compass
//            //Iron Armour
//            //Netherite Shovel/Pick
//            //Iron Axe
//            new InfectedInventory(Items.NETHERITE_SWORD, Items.NETHERITE_SWORD,
//                    new ItemLike[]{
//                            Items.IRON_HELMET,
//                            Items.IRON_CHESTPLATE,
//                            Items.IRON_LEGGINGS,
//                            Items.IRON_BOOTS
//                    },
//                    new ItemLike[]{
//                            Blocks.RED_CONCRETE,
//                            Items.IRON_AXE,
//                            Items.NETHERITE_PICKAXE,
//                            Items.NETHERITE_SHOVEL,
//                            Items.COMPASS,
//                            Items.RECOVERY_COMPASS
//                    }),
//    };

    /**
     * Register the ticking events for during the game
     */
    public void register() {
        this.upgradeTask = InfectionMod.infectionController.serverTaskScheduler.scheduleIntervalTickEvent(this::upgrade, Math.round(20f * 60f * InfectionMod.infectionController.configController.getConfig().minsBetweenInfectionUpgrades));

        this.start();
    }

    public record InfectedInventory(ItemStack[] armor,
                                    ItemStack[] hotbar) {
    }

    private int infectionStage = 0;


    public void handleInfectedItems(ServerPlayer player) {
        this.handleInfectedItems(player, false);
    }

    /**
     * Sort the inventory for the infecected players
     *
     * @param player
     */
    public void handleInfectedItems(ServerPlayer player, boolean setSlot) {
        if (player.getTeam() != InfectionMod.infectionController.infectedTeam) {
            return;
        }
        var inventory = player.getInventory();
        inventory.clearContent();
        if (setSlot) {
            inventory.pickSlot(0);
            player.connection.send(new ClientboundSetCarriedItemPacket(0));
        }
        var firstInfected = player.getUUID().equals(InfectionMod.infectionController.firstInfected);
        var infectedInventory = infectedInvetoryCreators.get(infectionStage).apply(firstInfected);
        for (int i = 0; i < infectedInventory.hotbar.length; i++) {
            var item = infectedInventory.hotbar[i];
            if (item.is(Items.COMPASS)) {
                CompassItemAccessor compassItem = (CompassItemAccessor) Items.COMPASS;
                compassItem.invokeAddLodestoneTags(player.getLevel().dimension(), player.blockPosition(), item.getTag());
            }
            else {
                item.setCount(item.getMaxStackSize());
                item.getOrCreateTag().putBoolean("Unbreakable", true);
            }
            inventory.setItem(i, item);
        }
        for (int i = 0; i < infectedInventory.armor.length; i++) {
            var item = infectedInventory.armor[i];
            item.getOrCreateTag().putBoolean("Unbreakable", true);
            player.setItemSlot(armorSlotMapping[i], item);
        }
    }

    public int getInfectionStage() {
        return infectionStage;
    }

    /**
     * Resends the inventory to the player in case of desync e.g. cancelling drops.
     *
     * @param serverPlayer
     */
    public void resendInventory(ServerPlayer serverPlayer) {
        serverPlayer.inventoryMenu.sendAllDataToRemote();
    }

    public void tick() {
        // TODO do logic to increase the loot.
    }

    private boolean upgradeAvailable() {
        return infectedInvetoryCreators.size() - 1 > infectionStage;
    }

    public void upgrade(MinecraftServer server) {
        if (!upgradeAvailable()) {
            return;
        }
        infectionStage++;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.MASTER, Float.MAX_VALUE, 1f);
            this.handleInfectedItems(player);
        }
        InfectionMod.infectionController.compassController.tick(server);
    }

    public void start() {
        infectionStage = 0;
    }
}

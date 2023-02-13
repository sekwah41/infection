package com.sekwah.infection.controller;

import com.sekwah.infection.InfectionMod;
import com.sekwah.infection.mixin.CompassItemAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * As distance calculation is easy to check as part of this, also handle the speed boosts if there are no players nearby
 */
public class CompassController {

    public void tick(MinecraftServer server) {
        var players = server.getPlayerList().getPlayers();

        var nearThreshold = InfectionMod.getConfig().compassNearbyThreshold;
        var farThreshold = InfectionMod.getConfig().zombieBuffDistance;

        var infectedPlayers = players.stream().filter(player -> player.getTeam() == InfectionMod.infectionController.infectedTeam).toList();
        var speedRunners = players.stream().filter(player -> player.getTeam() == InfectionMod.infectionController.speedRunnerTeam).toList();

        CompassItemAccessor compassItem = (CompassItemAccessor) Items.COMPASS;

        infectedPlayers.forEach(infected -> {
            var inventory = infected.getInventory();

            var dimension = infected.getLevel().dimension();

            // abuse this to make the compass spin
            var diffLevel = dimension == Level.OVERWORLD ? Level.NETHER : Level.OVERWORLD;

            // Try to find a player in the same world
            var pos = infected.blockPosition();

            // TODO get a location for this player to set all their compasses to.

            AtomicReference<BlockPos> compassPos = new AtomicReference<>(pos.offset(10000,0,0));
            AtomicReference<ResourceKey<Level>> targetDimension = new AtomicReference<>(diffLevel);

            speedRunners.stream().filter(survivor -> survivor.getLevel().dimension() == dimension).min((s1, s2) -> {
                var pos1 = s1.blockPosition();
                var pos2 = s2.blockPosition();
                var dist1 = pos1.distSqr(pos);
                var dist2 = pos2.distSqr(pos);
                return Double.compare(dist1, dist2);
            }).ifPresent(survivor -> {
                var accuracy = InfectionMod.getConfig().compassAccuracy;
                var rand = infected.getRandom();
                BlockPos destination = survivor.blockPosition();
                if(accuracy > 0) {
                    destination = destination.offset(
                            rand.nextInt(accuracy + 1) - accuracy / 2,
                            rand.nextInt(accuracy + 1) - accuracy / 2,
                            rand.nextInt(accuracy + 1) - accuracy / 2);
                }
                compassPos.set(destination);
                targetDimension.set(survivor.level.dimension());
            });

            var distance = compassPos.get().distSqr(pos);
            if(compassPos.get().atY(0).distSqr(pos.atY(0)) < (nearThreshold * nearThreshold)) {
                var yDiff = compassPos.get().getY() - pos.getY();
                if(yDiff > nearThreshold) {
                    infected.displayClientMessage(Component.literal("There are speed-runners above you").withStyle(ChatFormatting.GOLD), true);
                } else if(yDiff < -nearThreshold) {
                    infected.displayClientMessage(Component.literal("There are speed-runners below you").withStyle(ChatFormatting.GOLD), true);
                } else {
                    infected.displayClientMessage(Component.literal("There are speed-runners nearby").withStyle(ChatFormatting.GREEN), true);
                }
            }

            if(farThreshold * farThreshold < distance) {
                // max, range-multiplier, min
                int potionModifier = (int) Math.min(20, Math.max(Math.floor((Math.sqrt(distance) - farThreshold) / InfectionMod.getConfig().zombieBuffBlockModifier), 0));
                int potionTicks = 20 * 6 - 1;
                if(infected.isInWater()) {
                    infected.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, potionTicks, potionModifier, true, true ));
                    infected.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, potionTicks));
                }
                infected.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, potionTicks, potionModifier, true, true ));
                infected.addEffect(new MobEffectInstance(MobEffects.JUMP, potionTicks, potionModifier / 4 + 1, true, true ));
            }

            infected.setLastDeathLocation(Optional.of(GlobalPos.of(infected.level.dimension(), compassPos.get())));

            for (int i = 0; i < 9; i++) {
                var itemStack = inventory.getItem(i);
                if(itemStack.is(Items.COMPASS) && itemStack.getDisplayName().getString().contains(InventoryController.PLAYER_TRACKER_NAME.getString())) {

                    CompoundTag compoundTag = itemStack.getTag();

                    if(compoundTag == null) {
                        compoundTag = new CompoundTag();
                        itemStack.setTag(compoundTag);
                    }

                    compassItem.invokeAddLodestoneTags(targetDimension.get(), compassPos.get(), compoundTag);
                }
            }
        });
    }

}

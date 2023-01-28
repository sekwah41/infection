package com.sekwah.infection.controller;

import com.sekwah.infection.InfectionMod;
import com.sekwah.infection.mixin.CompassItemAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;

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

            var level = infected.getLevel().dimension();
            // Try to find a player in the same world

            var pos = infected.blockPosition();

            // TODO get a location for this player to set all their compasses to.

            AtomicReference<BlockPos> compassPos = new AtomicReference<>(pos);

            speedRunners.stream().filter(survivor -> survivor.getLevel().dimension() == level).min((s1, s2) -> {
                var pos1 = s1.blockPosition();
                var pos2 = s2.blockPosition();
                var dist1 = pos1.distSqr(pos);
                var dist2 = pos2.distSqr(pos);
                return Double.compare(dist1, dist2);
            }).ifPresent(survivor -> {
                var accuracy = InfectionMod.getConfig().compassAccuracy;
                var rand = infected.getRandom();
                compassPos.set(survivor.blockPosition().offset(
                        rand.nextInt(accuracy) - accuracy / 2,
                        rand.nextInt(accuracy) - accuracy / 2,
                        rand.nextInt(accuracy) - accuracy / 2));
            });

            var distance = compassPos.get().distSqr(pos);
            if(compassPos.get().atY(0).distSqr(pos.atY(0)) < nearThreshold * nearThreshold) {
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

            for (int i = 0; i < 9; i++) {
                var itemStack = inventory.getItem(i);
                if(itemStack.is(Items.COMPASS)) {

                    CompoundTag compoundTag = itemStack.getTag();

                    compassItem.invokeAddLodestoneTags(infected.getLevel().dimension(), compassPos.get(), compoundTag);
                }
            }
        });
    }

}

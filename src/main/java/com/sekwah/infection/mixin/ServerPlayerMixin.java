package com.sekwah.infection.mixin;

import com.sekwah.infection.InfectionMod;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin extends LivingEntity {

    protected ServerPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "die", at = @At("TAIL"))
    private void onPlayerDeath(DamageSource damageSource, CallbackInfo ci) {
        if(InfectionMod.infectionController.hasStarted()) {
            InfectionMod.infectionController.infectPlayer((ServerPlayer) (Object) this);
        }
    }

    @Inject(method = "drop(Z)Z", at = @At("HEAD"), cancellable = true)
    public void drop(boolean bl, CallbackInfoReturnable<Boolean> cir) {
        var serverPlayer = (ServerPlayer) (Object) this;
        if(serverPlayer.getTeam() == InfectionMod.infectionController.infectedTeam) {
            InfectionMod.infectionController.inventoryController.resendInventory(serverPlayer);
            cir.setReturnValue(false);
        }
    }

}

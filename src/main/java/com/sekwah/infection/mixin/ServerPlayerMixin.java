package com.sekwah.infection.mixin;

import com.sekwah.infection.InfectionMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}

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
abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "die", at = @At("TAIL"))
    private void onPlayerDeath(DamageSource damageSource, CallbackInfo ci) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(this.level);
        bolt.setPos(this.getX(), this.getY(), this.getZ());
        bolt.setVisualOnly(true);
        this.level.addFreshEntity(bolt);
        InfectionMod.infectionController.infectPlayer((ServerPlayer) (Object) this);
    }
}

package com.sekwah.infection.mixin;

import com.sekwah.infection.InfectionMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
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
            InfectionMod.infectionController.serverTaskScheduler.scheduleTickEvent((server) -> {
                InfectionMod.infectionController.infectPlayer((ServerPlayer) (Object) this);
            }, 0);
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

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
    public void drop(ItemStack droppedItem, boolean dropAround, boolean includeThrowerName, CallbackInfoReturnable<ItemEntity> cir) {
        var serverPlayer = (ServerPlayer) (Object) this;
        if(serverPlayer.getTeam() == InfectionMod.infectionController.infectedTeam) {
            InfectionMod.infectionController.inventoryController.resendInventory(serverPlayer);
            cir.setReturnValue(null);
        }
    }

}

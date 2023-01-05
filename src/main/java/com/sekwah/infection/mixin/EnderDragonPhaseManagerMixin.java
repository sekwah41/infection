package com.sekwah.infection.mixin;

import com.sekwah.infection.InfectionMod;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonPhaseManager.class)
public class EnderDragonPhaseManagerMixin {

    @Inject(method = "setPhase", at = @At("HEAD"))
    public void setPhase(EnderDragonPhase<?> phase, CallbackInfo ci) {
        if(phase == EnderDragonPhase.DYING) {
            InfectionMod.infectionController.endGame(true);
        }
    }

}

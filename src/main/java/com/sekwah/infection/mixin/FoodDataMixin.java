package com.sekwah.infection.mixin;

import com.sekwah.infection.InfectionMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class FoodDataMixin {


    @Shadow private int tickTimer;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(Player player, CallbackInfo ci) {
        if(player.getTeam() == InfectionMod.infectionController.infectedTeam) {
            ++this.tickTimer;
            var delay = InfectionMod.getConfig().infectionHealSpeed;
            if (this.tickTimer >= delay) {
                player.heal(1.0f);
                this.tickTimer = 0;
            }
            ci.cancel();
        }
    }


}

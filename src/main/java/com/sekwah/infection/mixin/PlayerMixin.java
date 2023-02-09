package com.sekwah.infection.mixin;

import com.sekwah.infection.InfectionMod;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract FoodData getFoodData();

    @Inject(method = "canHarmPlayer", at = @At("HEAD"), cancellable = true)
    private void canHarmPlayer(Player player, CallbackInfoReturnable<Boolean> cir) {

        var self = (Player) (Object) this;

        if(self == player) {
            cir.setReturnValue(true);
        }

        // TODO store when players were infected and make at least 3 seconds need to have passed before they can harm players
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(InfectionMod.infectionController.isGameOver() && !source.isBypassInvul()) {
            cir.setReturnValue(false);
            return;
        }
        var self = (Player) (Object) this;
        if(self.getTeam() == InfectionMod.infectionController.infectedTeam) {
            var foodData = (FoodDataAccessor) this.getFoodData();
            var config = InfectionMod.getConfig();
            foodData.setTickTimer(-config.infectionHealDelay + config.infectionHealSpeed);
            if(source.getMsgId().equals("thrown")) {

                cir.setReturnValue(super.hurt(source, amount));
            }
        }
    }
}

package com.sekwah.infection.mixin;

import com.sekwah.infection.InfectionMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowGolem.class)
public class SnowGolemMixin extends AbstractGolem {

    protected SnowGolemMixin(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    protected void registerGoals(CallbackInfo ci) {
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, 5, true, false,
                (livingEntity) -> livingEntity instanceof Player player && player.getTeam() == InfectionMod.infectionController.infectedTeam));
    }
}

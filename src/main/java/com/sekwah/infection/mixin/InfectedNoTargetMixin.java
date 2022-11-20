package com.sekwah.infection.mixin;

import com.sekwah.infection.InfectionMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class InfectedNoTargetMixin {


    @Inject(method = "isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    public void isAlliedTo(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(entity instanceof Player player && player.getTeam() == InfectionMod.infectionController.infectedTeam) {
            if((Object) this instanceof Zombie
                    || (Object) this instanceof Skeleton
                    || (Object) this instanceof Creeper
                    || (Object) this instanceof EnderMan
                    || (Object) this instanceof EnderDragon
            ) {
                cir.setReturnValue(true);
            }
        }
    }
}

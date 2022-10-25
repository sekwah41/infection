package com.sekwah.infection.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "canHarmPlayer", at = @At("HEAD"), cancellable = true)
    private void canHarmPlayer(Player player, CallbackInfoReturnable<Boolean> cir) {
        if((Player) (Object) this == player) {
            cir.setReturnValue(true);
        }
    }
}

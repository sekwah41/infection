package com.sekwah.infection.mixin;

import com.sekwah.infection.InfectionMod;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void placeNewPlayer(Connection netManager, ServerPlayer player, CallbackInfo ci) {
        if(player.getTeam() == InfectionMod.infectionController.infectedTeam) {
            InfectionMod.infectionController.switchSkin(player);
        }
    }
}

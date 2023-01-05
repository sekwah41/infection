package com.sekwah.infection.mixin;

import com.sekwah.infection.InfectionMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    public void clicked(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if(player.getTeam() == InfectionMod.infectionController.infectedTeam) {
            if(player instanceof ServerPlayer serverPlayer) {
                InfectionMod.infectionController.inventoryController.resendInventory(serverPlayer);
            }
            ci.cancel();
        }
    }
}

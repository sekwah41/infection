package com.sekwah.infection.mixin;

import com.sekwah.infection.InfectionMod;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "place", at = @At("TAIL"))
    private void place(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        var player = context.getPlayer();
        if(player != null && player.getTeam() == InfectionMod.infectionController.infectedTeam) {
            ItemStack itemStack = context.getItemInHand();
            itemStack.setCount(itemStack.getMaxStackSize());
            context.getPlayer().inventoryMenu.sendAllDataToRemote();
        }
    }
}

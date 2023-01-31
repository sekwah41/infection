package com.sekwah.infection.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedItem.class)
public class BedItemMixin {

    @Inject(method = "placeBlock", at = @At("HEAD"), cancellable = true)
    protected void placeBlock(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if(context.getLevel().dimensionTypeId().location().toString().equals("minecraft:the_end")) {
            var player = context.getPlayer();
            player.displayClientMessage(Component.literal("You cannot place beds in the End!").withStyle(ChatFormatting.RED), true);
            player.inventoryMenu.sendAllDataToRemote();
            cir.setReturnValue(false);
        }
    }
}

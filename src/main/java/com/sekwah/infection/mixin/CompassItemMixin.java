package com.sekwah.infection.mixin;

import com.sekwah.infection.InfectionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CompassItem.class)
public class CompassItemMixin {


    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected, CallbackInfo ci) {
        if(entity.getTeam() == InfectionMod.infectionController.infectedTeam) {
            ci.cancel();
        }
    }

}

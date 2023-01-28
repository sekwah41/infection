package com.sekwah.infection.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CompassItem.class)
public interface CompassItemAccessor {

    // Giving it the same name causes a stackoverflow error
    @Invoker("addLodestoneTags")
    void invokeAddLodestoneTags(ResourceKey<Level> lodestoneDimension, BlockPos lodestonePos, CompoundTag compoundTag);

}

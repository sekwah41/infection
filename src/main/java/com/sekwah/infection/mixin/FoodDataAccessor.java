package com.sekwah.infection.mixin;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodData.class)
public interface FoodDataAccessor {

    @Accessor("tickTimer")
    void setTickTimer(int tickTimer);

    @Accessor("lastFoodLevel")
    void setLastFoodLevel(int lastFoodLevel);
}

package com.sekwah.infection.mixin;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodData.class)
public interface FoodDataMixin {

    @Accessor("foodLevel")
    void setFoodLevel(int foodLevel);

    @Accessor("saturationLevel")
    void setSaturationLevel(float saturationLevel);

    @Accessor("exhaustionLevel")
    void setExhaustionLevel(float saturationLevel);

    @Accessor("tickTimer")
    void setTickTimer(int tickTimer);

    @Accessor("lastFoodLevel")
    void setLastFoodLevel(int lastFoodLevel);

}

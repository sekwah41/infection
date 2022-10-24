package com.sekwah.whilewewait.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class GameStartMixin {

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void startStopMusic(Screen screen, CallbackInfo ci) {
        System.out.println("Example");
    }
}

package com.sekwah.infection.mixin;

import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Inject(method = "handlePickItem", at = @At("HEAD"), cancellable = true)
    public void handlePickItem(ServerboundPickItemPacket packet, CallbackInfo ci) {
        ci.cancel();
    }
}

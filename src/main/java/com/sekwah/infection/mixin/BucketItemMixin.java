package com.sekwah.infection.mixin;

import com.sekwah.infection.InfectionMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BucketItem.class)
public class BucketItemMixin {

    @Shadow @Final private Fluid content;

    @Inject(method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/BlockHitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void placeDetector(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, ItemStack itemStack, BlockHitResult blockHitResult) {
        if(player != null && player.getTeam() == InfectionMod.infectionController.speedRunnerTeam && this.content == Fluids.LAVA) {
            if(blockHitResult.getType() == BlockHitResult.Type.BLOCK) {
                BlockPos blockPos = blockHitResult.getBlockPos();
                var entitiesNear = level.getEntities(player, new AABB(blockPos).inflate(3));
                var isSpeedrunnerNear = entitiesNear.stream().filter(entity -> entity.getTeam() == InfectionMod.infectionController.speedRunnerTeam).count() > 0;
                if(isSpeedrunnerNear) {
                    player.displayClientMessage(Component.literal("You cannot place lava near other speed-runners!").withStyle(ChatFormatting.RED), true);
                    player.inventoryMenu.sendAllDataToRemote();
                    cir.setReturnValue(InteractionResultHolder.pass(itemStack));
                }
            }
        }
    }
}

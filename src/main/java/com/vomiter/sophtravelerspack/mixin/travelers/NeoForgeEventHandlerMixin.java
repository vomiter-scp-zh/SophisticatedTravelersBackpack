package com.vomiter.sophtravelerspack.mixin.travelers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tiviacz.travelersbackpack.handlers.NeoForgeEventHandler;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = NeoForgeEventHandler.class, remap = false)
public class NeoForgeEventHandlerMixin {
    @WrapOperation(
            method = "playerTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tiviacz/travelersbackpack/capability/CapabilityUtils;" +
                            "isWearingBackpack(Lnet/minecraft/world/entity/player/Player;)Z"
            )
    )
    private static boolean sophtravelerspack$isWearingBackpack(
            Player player,
            Operation<Boolean> original
    ) {
        return original.call(player)
                || !TravelerUtil.getShadow(player).isEmpty();
    }

    @WrapOperation(
            method = "playerTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tiviacz/travelersbackpack/capability/CapabilityUtils;" +
                            "getWearingBackpack(Lnet/minecraft/world/entity/player/Player;)" +
                            "Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private static ItemStack sophtravelerspack$getWearingBackpack(
            Player player,
            Operation<ItemStack> original
    ) {
        ItemStack originalStack = original.call(player);
        return originalStack.isEmpty()
                ? TravelerUtil.getShadow(player)
                : originalStack;
    }
}
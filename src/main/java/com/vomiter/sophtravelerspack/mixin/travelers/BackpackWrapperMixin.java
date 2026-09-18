package com.vomiter.sophtravelerspack.mixin.travelers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BackpackWrapper.class, remap = false)
public class BackpackWrapperMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lcom/tiviacz/travelersbackpack/capability/CapabilityUtils;isWearingBackpack(Lnet/minecraft/world/entity/player/Player;)Z"))
    private static boolean sophtravelerspack$isWearingBackpack(Player player, Operation<Boolean> original){
        if (original.call(player)) return true;
        return !TravelerUtil.getShadow(player).isEmpty();
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lcom/tiviacz/travelersbackpack/capability/CapabilityUtils;getWearingBackpack(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack sophtravelerspack$getWearingBackpack(Player player, Operation<ItemStack> original){
        var originalResult = original.call(player);
        if (originalResult.isEmpty()){
            return TravelerUtil.getShadow(player);
        }
        return originalResult;
    }

}

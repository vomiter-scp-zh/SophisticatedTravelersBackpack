package com.vomiter.sophtravelerspack.mixin.travelers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tiviacz.travelersbackpack.common.BackpackAbilities;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BackpackAbilities.class, remap = false)
public class BackpackAbilitiesMixin {
    @WrapOperation(method = "checkBackpack", at = @At(value = "INVOKE", target = "Lcom/tiviacz/travelersbackpack/capability/CapabilityUtils;isWearingBackpack(Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean stbp$isWearingBackpack(Player player, Operation<Boolean> original){
        boolean originalResult = original.call(player);
        if (originalResult) return true;
        return !TravelerUtil.getShadow(player).isEmpty();
    }

    @WrapOperation(method = "checkBackpack", at = @At(value = "INVOKE", target = "Lcom/tiviacz/travelersbackpack/capability/CapabilityUtils;getWearingBackpack(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack stbp$checkBackpack(Player player, Operation<ItemStack> original){
        var originalResult = original.call(player);
        if (originalResult.isEmpty()){
            return TravelerUtil.getShadow(player);
        }
        return originalResult;
    }
}

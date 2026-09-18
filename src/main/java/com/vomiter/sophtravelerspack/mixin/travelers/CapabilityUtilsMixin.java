package com.vomiter.sophtravelerspack.mixin.travelers;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tiviacz.travelersbackpack.capability.CapabilityUtils;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = CapabilityUtils.class, remap = false)
public class CapabilityUtilsMixin {
    @WrapMethod(method = "getBackpackWrapper(Lnet/minecraft/world/entity/player/Player;)Lcom/tiviacz/travelersbackpack/inventory/BackpackWrapper;")
    private static BackpackWrapper srbp$getBackpackWrapperArtificial(Player player, Operation<BackpackWrapper> original){
        BackpackWrapper wrapper = original.call(player);
        if (wrapper == null){
            ItemStack stack = TravelerUtil.getShadow(player);
            if (stack != null && stack.getItem() instanceof TravelersBackpackItem){
                return BackpackWrapper.fromStack(stack);
            }
        }
        return wrapper;
    }

    @WrapMethod(method = "getBackpackWrapper(Lnet/minecraft/world/entity/player/Player;[I)Lcom/tiviacz/travelersbackpack/inventory/BackpackWrapper;")
    private static BackpackWrapper srbp$getBackpackWrapperArtificial(Player player, int[] dataLoad, Operation<BackpackWrapper> original){
        BackpackWrapper wrapper = original.call(player, dataLoad);
        if (wrapper == null){
            ItemStack stack = TravelerUtil.getShadow(player);
            if (stack != null && stack.getItem() instanceof TravelersBackpackItem){
                return BackpackWrapper.fromStack(stack);
            }
        }
        return wrapper;
    }

    @WrapMethod(method = "getBackpackWrapper(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;[I)Lcom/tiviacz/travelersbackpack/inventory/BackpackWrapper;")
    private static BackpackWrapper srbp$getBackpackWrapperArtificial(Player player, ItemStack stack0, int[] dataLoad, Operation<BackpackWrapper> original){
        BackpackWrapper wrapper = original.call(player, stack0, dataLoad);
        if (wrapper == null){
            ItemStack stack = TravelerUtil.getShadow(player);
            if (stack != null && stack.getItem() instanceof TravelersBackpackItem){
                return BackpackWrapper.fromStack(stack);
            }
        }
        return wrapper;
    }

    @WrapMethod(method = "getBackpackWrapper(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lcom/tiviacz/travelersbackpack/inventory/BackpackWrapper;")
    private static BackpackWrapper srbp$getBackpackWrapperArtificial(Player player, ItemStack stack0, Operation<BackpackWrapper> original){
        BackpackWrapper wrapper = original.call(player, stack0);
        if (wrapper == null){
            ItemStack stack = TravelerUtil.getShadow(player);
            if (stack != null && stack.getItem() instanceof TravelersBackpackItem){
                return BackpackWrapper.fromStack(stack);
            }
        }
        return wrapper;
    }


}

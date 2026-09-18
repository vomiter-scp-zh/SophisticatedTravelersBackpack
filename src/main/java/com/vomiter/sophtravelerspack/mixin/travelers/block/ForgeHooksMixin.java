package com.vomiter.sophtravelerspack.mixin.travelers.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tiviacz.travelersbackpack.init.ModItems;
import com.vomiter.sophtravelerspack.traveler.TravelerBlockAbilities;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ForgeHooks.class)
public class ForgeHooksMixin {
    @WrapOperation(method = "onPlaceItemIntoWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;onPlace(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"))
    private static void stbp$onPlace(BlockState instance, Level level, BlockPos pos, BlockState state, boolean b, Operation<Void> original){
        original.call(instance, level, pos, state, b);
        if (level.getBlockEntity(pos) instanceof BackpackBlockEntity backpackBlockEntity){
            TravelerUtil.get(backpackBlockEntity.getBackpackWrapper().getBackpack()).ifPresent(
                    travelerTypeInstance -> {
                        if (travelerTypeInstance.createTravelerBackpack().is(ModItems.SPONGE_TRAVELERS_BACKPACK.get())){
                            TravelerBlockAbilities.tryAbsorbWater(level, pos);
                        }
                    }
            );
        }
    }
}

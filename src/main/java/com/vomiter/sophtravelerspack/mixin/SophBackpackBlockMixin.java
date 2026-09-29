package com.vomiter.sophtravelerspack.mixin;

import com.vomiter.sophtravelerspack.util.SleepingBagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedcore.controller.IControllableStorage;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackpackBlock.class)
public class SophBackpackBlockMixin {
    @Inject(method = "onRemove", at = @At("HEAD"))
    private void stbp$onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci){
        if (level.getBlockEntity(pos) instanceof BackpackBlockEntity backpackBlockEntity){
            if (!state.is(newState.getBlock())) {
                SleepingBagUtils.recoverSleepingBagIntoSBP(level, pos);
            }
        }
    }
}

package com.vomiter.sophtravelerspack.mixin.travelers;

import com.llamalad7.mixinextras.sugar.Local;
import com.tiviacz.travelersbackpack.blocks.SleepingBagBlock;
import com.vomiter.sophtravelerspack.util.SleepingBagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SleepingBagBlock.class)
public class SleepingBagBlockMixin {
    @Inject(method = "playerWillDestroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BedBlock;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private void stbp$playerWillDestroy(
            Level level, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<BlockState> cir, @Local(name = "var8") BlockEntity backpack
    ){
        if (backpack instanceof BackpackBlockEntity backpackBlock){
            SleepingBagUtils.recoverSleepingBagIntoSBP(level, backpackBlock.getBlockPos());
        }
    }
}

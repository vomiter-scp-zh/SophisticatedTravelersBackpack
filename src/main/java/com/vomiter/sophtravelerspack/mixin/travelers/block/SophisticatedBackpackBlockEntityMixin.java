package com.vomiter.sophtravelerspack.mixin.travelers.block;

import com.vomiter.sophtravelerspack.STBackpack;
import com.vomiter.sophtravelerspack.common.registry.ModTravelerTypeRegistry;
import com.vomiter.sophtravelerspack.traveler.TravelerType;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BackpackBlockEntity.class, remap = false)
public class SophisticatedBackpackBlockEntityMixin {
    @Unique
    int  stbp$cactusCounter = 0;

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void sbtp$backpackTick(Level level, BlockPos blockPos, BackpackBlockEntity backpackBlockEntity, CallbackInfo ci){
        if (!((Object)level.getBlockEntity(blockPos) instanceof SophisticatedBackpackBlockEntityMixin mixin)){
            return;
        }
        if (level.isClientSide()) return;
        boolean isCactus = TravelerUtil.get(backpackBlockEntity.getBackpackWrapper().getBackpack()).map(type -> ModTravelerTypeRegistry.get(TravelerType.CACTUS).equals(type)).orElse(false);
        if(!isCactus) return;
        if (mixin.stbp$cactusCounter >= 1000 && backpackBlockEntity.getBackpackWrapper().getFluidHandler().isPresent()){
            var fluidHandler = backpackBlockEntity.getBackpackWrapper().getFluidHandler().get();
            var result = fluidHandler.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            if(result > 0){
                mixin.stbp$cactusCounter = 0;
            }
        }

        if (level.getGameTime() % 100 != 0) return;
        if (!(level.isRaining() && level.canSeeSky(blockPos))) return;
        mixin.stbp$cactusCounter += 50;
    }
}

package com.vomiter.sophtravelerspack.mixin.extra_model;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.vomiter.sophtravelerspack.client.BakedSTBackpackModels;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

@Mixin(targets = {"com.tiviacz.travelersbackpack.client.model.BackpackDynamicModel$BackpackBakedModel"}, remap = false)
public class TravelerDynamicBackpackModelMixin {
    @Shadow
    private Block block;

    @WrapOperation(method = "addExtras", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object stbp$addExtras(Map instance, Object o, Operation<BakedModel> original){
        var originalResult = original.call(instance, o);
        BakedSTBackpackModels.putExtraModel(block, originalResult);
        return originalResult;
    }

    @WrapOperation(method = "addTanks", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;Lnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)Ljava/util/List;"))
    private List stbp$addTanks(BakedModel instance, BlockState state, Direction direction, RandomSource randomSource, ModelData modelData, RenderType renderType, Operation<List> original){
        BakedSTBackpackModels.putTankModel(block, instance);
        return original.call(instance, state, direction, randomSource, modelData, renderType);
    }
}

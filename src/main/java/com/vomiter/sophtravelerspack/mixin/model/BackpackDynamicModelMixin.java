package com.vomiter.sophtravelerspack.mixin.model;

import com.mojang.datafixers.util.Either;
import com.vomiter.sophtravelerspack.client.BakedSTBackpackModels;
import com.vomiter.sophtravelerspack.traveler.TravelerType;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackDynamicModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Mixin(value = BackpackDynamicModel.class, remap = false)
public abstract class BackpackDynamicModelMixin {
    @Unique
    private BlockModel stbp$createTravelerTypePartModel(
            ResourceLocation partModelLocation,
            IGeometryBakingContext context,
            TravelerType travelerType
    ) {
        Map<String, Either<Material, String>> resolvedTextures = new HashMap<>();

        if (context.hasMaterial("clips")) {
            resolvedTextures.put("clips", Either.left(context.getMaterial("clips")));
        }

        return new BlockModel(
                partModelLocation,
                Collections.emptyList(),
                resolvedTextures,
                true,
                null,
                ItemTransforms.NO_TRANSFORMS,
                Collections.emptyList()
        );
    }

    @Inject(method = "bake", at = @At("HEAD"))
    private void stbp$bakeTravelerModels(
            IGeometryBakingContext context,
            ModelBaker baker,
            Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelTransform,
            ItemOverrides overrides,
            CallbackInfoReturnable<BakedModel> cir
    ) {
        for (TravelerType travelerType : TravelerType.values()) {
            for (BakedSTBackpackModels.ModelPart modelPart
                    : BakedSTBackpackModels.ModelPart.values()) {
                ResourceLocation location =
                        BakedSTBackpackModels.getLoc(travelerType, modelPart);

                BlockModel blockModel = stbp$createTravelerTypePartModel(
                        location,
                        context,
                        travelerType
                );

                blockModel.resolveParents(baker::getModel);

                BakedModel bakedModel = blockModel.bake(
                        baker,
                        spriteGetter,
                        modelTransform
                );

                BakedSTBackpackModels.putModel(location, bakedModel);
            }
        }
    }
}
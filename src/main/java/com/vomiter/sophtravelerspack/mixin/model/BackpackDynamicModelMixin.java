package com.vomiter.sophtravelerspack.mixin.model;

import com.mojang.datafixers.util.Either;
import com.vomiter.sophtravelerspack.STBackpack;
import com.vomiter.sophtravelerspack.client.BakedSTBackpackModels;
import com.vomiter.sophtravelerspack.traveler.TravelerType;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackDynamicModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

@Mixin(value = BackpackDynamicModel.class, remap = false)
public abstract class BackpackDynamicModelMixin {
    @Shadow
    protected abstract BlockModel createPartModel(ResourceLocation partModelLocation, IGeometryBakingContext context);

    @Shadow
    @Final
    private Map<String, Either<Material, String>> textures;

    //use different clip texture
    @Unique
    private BlockModel stbp$createTravelerTypePartModel(ResourceLocation partModelLocation, IGeometryBakingContext context, TravelerType travelerType) {
        Map<String, Either<Material, String>> resolvedTextures = new HashMap(this.textures);
        this.textures.forEach((textureName, texture) -> {
            if (context.hasMaterial(textureName)) {
                resolvedTextures.put(textureName, Either.left(context.getMaterial(textureName)));
            } else {
                resolvedTextures.put(textureName, texture);
            }
        });
        ResourceLocation clipTexture = resolvedTextures.get("clips").left().get().texture();
        var pathSplit = clipTexture.getPath().split("/");
        var lastPath = pathSplit[pathSplit.length-1];
        var textureLoc = STBackpack.modLoc("block/" + travelerType.name().toLowerCase(Locale.ROOT) + "/" + lastPath);
        var texture =((BackpackDynamicModelLoaderAccessor)(Object)BackpackDynamicModel.Loader.INSTANCE)
                .stbp$parseTextureLocationOrReference(TextureAtlas.LOCATION_BLOCKS, textureLoc.toString());
        //resolvedTextures.put("clips", texture);
        return new BlockModel(partModelLocation, Collections.emptyList(), resolvedTextures, true, (BlockModel.GuiLight)null, ItemTransforms.NO_TRANSFORMS, Collections.emptyList());
    }


    @Inject(method = "<init>", at=@At("TAIL"))
    private void debugLog(Map partModelLocations, Map textures, CallbackInfo ci){
    }

    /*
    bake partial models
     */
    @Inject(method = "bake", at = @At("HEAD"))
    private void stbp$back(
            IGeometryBakingContext context,
            ModelBaker baker,
            Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelTransform,
            ItemOverrides overrides,
            ResourceLocation modelLocation,
            CallbackInfoReturnable<BakedModel> cir){
        for (TravelerType travelerType : TravelerType.values()) {
            for (BakedSTBackpackModels.ModelPart modelPart : BakedSTBackpackModels.ModelPart.values()) {
                var loc = BakedSTBackpackModels.getLoc(travelerType, modelPart);
                BlockModel blockModel = stbp$createTravelerTypePartModel(
                        loc,
                        context,
                        travelerType
                );
                blockModel.resolveParents(baker::getModel);
                BakedModel bakedModel = blockModel.bake(baker, spriteGetter, modelTransform, modelLocation);
                BakedSTBackpackModels.putModel(loc, bakedModel);
            }
        }
    }
}

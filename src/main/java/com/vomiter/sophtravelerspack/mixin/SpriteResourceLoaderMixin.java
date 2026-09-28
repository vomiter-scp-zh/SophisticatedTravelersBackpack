package com.vomiter.sophtravelerspack.mixin;

import com.mojang.logging.LogUtils;
import com.vomiter.sophtravelerspack.common.registry.ModTravelerTypeRegistry;
import com.vomiter.sophtravelerspack.traveler.TravelerType;
import com.vomiter.sophtravelerspack.traveler.TravelerTypeInstance;
import com.vomiter.sophtravelerspack.util.TravelerSophSpriteComposer;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

/**
 * Adds generated cloth sprites for every TravelerType to Minecraft 1.20.1's
 * block texture atlas.
 *
 * <p>This mixin is client-only.</p>
 */
@Mixin(SpriteResourceLoader.class)
public abstract class SpriteResourceLoaderMixin {
    @Unique
    private static final Logger SOPHTRAVELERSPACK_LOGGER = LogUtils.getLogger();

    @Unique
    private static final ResourceLocation SOPHTRAVELERSPACK_BLOCK_ATLAS_INFO = ResourceLocation.fromNamespaceAndPath("minecraft", "blocks");

    @Unique
    private static final ResourceLocation SOPHTRAVELERSPACK_SOPH_BASE_ID = ResourceLocation.fromNamespaceAndPath("sophisticatedbackpacks", "block/backpack_cloth");

    @Unique
    private static final Set<SpriteResourceLoader> SOPHTRAVELERSPACK_BLOCK_ATLAS_LOADERS =
            Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));

    @Inject(method = "load", at = @At("RETURN"))
    private static void sophtravelerspack$markBlockAtlasLoader(
            ResourceManager resourceManager,
            ResourceLocation atlasInfoId,
            CallbackInfoReturnable<SpriteResourceLoader> cir
    ) {
        if (atlasInfoId.equals(SOPHTRAVELERSPACK_BLOCK_ATLAS_INFO)) {
            SOPHTRAVELERSPACK_BLOCK_ATLAS_LOADERS.add(cir.getReturnValue());
        }
    }

    @Inject(method = "list", at = @At("RETURN"), cancellable = true)
    private void sophtravelerspack$addTravelerBackpackCloths(
            ResourceManager resourceManager,
            CallbackInfoReturnable<List<Supplier<SpriteContents>>> cir
    ) {
        SpriteResourceLoader self = (SpriteResourceLoader) (Object) this;
        if (!SOPHTRAVELERSPACK_BLOCK_ATLAS_LOADERS.contains(self)) {
            return;
        }

        List<Supplier<SpriteContents>> sprites =
                new ArrayList<>(cir.getReturnValue());

        for (TravelerTypeInstance travelerType : ModTravelerTypeRegistry.REGISTRY.get()) {
            sprites.add(() -> sophtravelerspack$createBackpackCloth(
                    resourceManager, travelerType
            ));
        }

        cir.setReturnValue(sprites);
    }

    @Unique
    @Nullable
    private static SpriteContents sophtravelerspack$createBackpackCloth(
            ResourceManager resourceManager,
            TravelerTypeInstance travelerTypeInstance
    ) {
        ResourceLocation generatedId = ResourceLocation.fromNamespaceAndPath(
                "sophtravelerspack",
                "block/" + travelerTypeInstance.getStringRepresentation() + "/backpack_cloth"
        );
        ResourceLocation travelersTextureId = travelerTypeInstance.getTextureId();

        SpriteContents sophisticatedBase = null;
        SpriteContents travelersTexture = null;

        try {
            sophisticatedBase = sophtravelerspack$loadSprite(
                    resourceManager,
                    SOPHTRAVELERSPACK_SOPH_BASE_ID
            );
            travelersTexture = sophtravelerspack$loadSprite(
                    resourceManager,
                    travelersTextureId
            );

            if (sophisticatedBase == null || travelersTexture == null) {
                SOPHTRAVELERSPACK_LOGGER.error(
                        "Cannot generate {}: unable to load {} or {}",
                        generatedId,
                        SOPHTRAVELERSPACK_SOPH_BASE_ID,
                        travelersTextureId
                );
                return null;
            }

            return TravelerSophSpriteComposer.compose(
                    sophisticatedBase,
                    travelersTexture,
                    generatedId
            );
        } catch (RuntimeException e) {
            SOPHTRAVELERSPACK_LOGGER.error(
                    "Failed to generate {}",
                    generatedId,
                    e
            );
            return null;
        } finally {
            if (sophisticatedBase != null) {
                sophisticatedBase.close();
            }
            if (travelersTexture != null) {
                travelersTexture.close();
            }
        }
    }

    @Unique
    @Nullable
    private static SpriteContents sophtravelerspack$loadSprite(
            ResourceManager resourceManager,
            ResourceLocation spriteId
    ) {
        ResourceLocation textureFile = ResourceLocation.fromNamespaceAndPath(
                spriteId.getNamespace(),
                "textures/" + spriteId.getPath() + ".png"
        );

        Resource resource = resourceManager.getResource(textureFile)
                .orElse(null);

        if (resource == null) {
            SOPHTRAVELERSPACK_LOGGER.error(
                    "Missing source texture {}",
                    textureFile
            );
            return null;
        }

        return SpriteLoader.loadSprite(spriteId, resource);
    }
}

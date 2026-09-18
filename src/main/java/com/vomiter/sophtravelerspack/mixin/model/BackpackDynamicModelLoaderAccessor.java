package com.vomiter.sophtravelerspack.mixin.model;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackDynamicModel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BackpackDynamicModel.Loader.class)
public interface BackpackDynamicModelLoaderAccessor {
    @Invoker("parseTextureLocationOrReference")
    Either<Material, String> stbp$parseTextureLocationOrReference(
            ResourceLocation location,
            @NotNull String name
    );
}

package com.vomiter.sophtravelerspack.data;

import com.vomiter.sophtravelerspack.STBackpack;
import com.vomiter.sophtravelerspack.traveler.TravelerType;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;

import java.util.List;
import java.util.Locale;

public class ModBlockModelProvider extends BlockModelProvider {
    private static ModelFile.UncheckedModelFile getBackpackVariant(String variant){
        return new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(SophisticatedBackpacks.MOD_ID, "block/backpack_"+variant));
    }

    private static final List<String> variants = List.of(
            "base",
            "front_pouch",
            "left_pouch",
            "right_pouch",
            "straps"
    );

    public ModBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, STBackpack.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (TravelerType type : TravelerType.values()) {
            generateBackpackModels(type);
        }
    }

    private void generateBackpackModels(TravelerType type) {
        String typeName = type.name().toLowerCase(Locale.ROOT);

        ResourceLocation clothTexture = modLoc(
                "block/" + typeName + "/backpack_cloth"
        );
        existingFileHelper.trackGenerated(
                clothTexture,
                PackType.CLIENT_RESOURCES,
                ".png",
                "textures"
        );

        for (String variant : variants) {
            backpackModel(
                    "block/" + typeName + "/backpack_" + variant,
                    getBackpackVariant(variant),
                    clothTexture
            );
        }
    }

    private BlockModelBuilder backpackModel(
            String modelPath,
            ModelFile parent,
            ResourceLocation clothTexture
    ) {
        return getBuilder(modelPath)
                .parent(parent)
                .texture("particle", clothTexture)
                .texture("cloth", clothTexture);
    }
}
package com.vomiter.sophtravelerspack.client;

import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import com.vomiter.sophtravelerspack.STBackpack;
import com.vomiter.sophtravelerspack.traveler.TravelerType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BakedSTBackpackModels {
    public enum ModelPart {
        RIGHT_POUCH,
        LEFT_POUCH,
        STRAPS,
        FRONT_POUCH,
        BASE,
    }

    private static final Map<ResourceLocation, BakedModel> BAKED_MODELS = new HashMap<>();
    public static void putModel(ResourceLocation key, BakedModel model){
        BAKED_MODELS.put(key, model);
    }
    public static BakedModel getModel(ResourceLocation key) {
        return BAKED_MODELS.get(key);
    }
    public static ResourceLocation getLoc(TravelerType travelerType, ModelPart part){
        return STBackpack.modLoc(
                "block/" + travelerType.name().toLowerCase(Locale.ROOT)
                        + "/backpack_" + part.name().toLowerCase(Locale.ROOT));
    }

    private static final List<Block> CACHED_TRAVELERS = new ArrayList<>();
    private static final Map<Block, BakedModel> BAKED_TRAVELER_EXTRA_MODELS = new HashMap<>();
    private static final Map<Block, BakedModel> BAKED_TRAVELER_TANK_MODELS = new HashMap<>();

    public static void putExtraModel(Block key, BakedModel model){
        BAKED_TRAVELER_EXTRA_MODELS.put(key, model);
    }
    public static BakedModel getExtraModel(Block key) {
        return BAKED_TRAVELER_EXTRA_MODELS.get(key);
    }
    public static void putTankModel(Block key, BakedModel model){
        BAKED_TRAVELER_TANK_MODELS.put(key, model);
    }

    private static void load(ItemStack travelerStack){
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BakedModel travelerModel = itemRenderer.getModel(
                travelerStack,
                minecraft.level,
                minecraft.player,
                0
        );

        /*
         * 強制進入 BackpackBakedModel#getQuads -> addExtras。
         * 回傳的 quads 不會送到 VertexConsumer，因此不會真的畫出
         */
        travelerModel.getQuads(
                null,
                null,
                RandomSource.create(42L),
                ModelData.EMPTY,
                null
        );
    }

    @Nullable
    public static BakedModel getOrLoadExtraModel(ItemStack travelerStack) {
        if (!(travelerStack.getItem() instanceof TravelersBackpackItem travelersBackpackItem)) {
            return null;
        }
        Block block = travelersBackpackItem.getBlock();
        BakedModel cached = BAKED_TRAVELER_EXTRA_MODELS.get(block);
        if (cached != null) {
            return cached;
        }
        if (CACHED_TRAVELERS.contains(block)) return null;
        load(travelerStack);
        CACHED_TRAVELERS.add(block);
        return BAKED_TRAVELER_EXTRA_MODELS.get(block);
    }

    @Nullable
    public static BakedModel getOrLoadTankModel(ItemStack travelerStack) {
        if (!(travelerStack.getItem() instanceof TravelersBackpackItem travelersBackpackItem)) {
            return null;
        }
        Block block = travelersBackpackItem.getBlock();
        BakedModel cached = BAKED_TRAVELER_TANK_MODELS.get(block);
        if (cached != null) {
            return cached;
        }
        if (CACHED_TRAVELERS.contains(block)) return null;
        load(travelerStack);
        CACHED_TRAVELERS.add(block);
        return BAKED_TRAVELER_TANK_MODELS.get(block);
    }
}

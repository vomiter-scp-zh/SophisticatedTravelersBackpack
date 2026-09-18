package com.vomiter.sophtravelerspack.mixin.model;

import com.google.common.collect.ImmutableMap;
import com.vomiter.sophtravelerspack.client.BackpackModelsExtension;
import com.vomiter.sophtravelerspack.client.BakedSTBackpackModels;
import com.vomiter.sophtravelerspack.common.registry.ModTravelerTypeRegistry;
import com.vomiter.sophtravelerspack.traveler.TravelerType;
import com.vomiter.sophtravelerspack.traveler.TravelerTypeInstance;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackDynamicModel;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IRenderedBatteryUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IRenderedTankUpgrade;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Mixin(BackpackDynamicModel.BackpackBakedModel.class)
public abstract class BackpackBakedModelMixin implements BackpackModelsExtension {

    @Shadow
    @Final
    @Mutable
    private Map<?, BakedModel> models;

    @Shadow
    private boolean battery;
    @Shadow
    private ItemDisplayContext lastContext;
    @Shadow
    @Nullable
    private IRenderedTankUpgrade.TankRenderInfo leftTankRenderInfo;
    @Shadow
    @Nullable
    private IRenderedTankUpgrade.TankRenderInfo rightTankRenderInfo;
    @Shadow
    @Nullable
    private IRenderedBatteryUpgrade.BatteryRenderInfo batteryRenderInfo;
    @Shadow
    private boolean tankLeft;
    @Shadow
    private boolean tankRight;
    @Unique
    private static final ThreadLocal<Boolean> stbp$isBuildingTravelerType
            = ThreadLocal.withInitial(() -> false);

    @Unique
    private Map<
            TravelerTypeInstance,
            BackpackDynamicModel.BackpackBakedModel
            > stbp$camouflages = null;

    @Override
    public BackpackDynamicModel.BackpackBakedModel stbp$get(
            TravelerTypeInstance travelerType
    ){
        return stbp$camouflages.get(travelerType);
    }


    /*
    build a camouflage map for this baked model group
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void stbp$init(
            Map models,
            Map partShapes,
            ItemTransforms itemTransforms,
            CallbackInfo ci){
        if (stbp$isBuildingTravelerType.get()) {
            stbp$isTravelerTypeVariant = true;
            return;
        }
        try{
            stbp$isBuildingTravelerType.set(true);
            ImmutableMap.Builder<TravelerTypeInstance,
                    BackpackDynamicModel.BackpackBakedModel> builder = new ImmutableMap.Builder<>();
            for (TravelerType travelerType : TravelerType.values()) {
                BackpackDynamicModel.BackpackBakedModel backpackBakedModel
                        = new BackpackDynamicModel.BackpackBakedModel(models, partShapes, itemTransforms);
                if((Object)backpackBakedModel instanceof BackpackModelsExtension extension){
                    for (BakedSTBackpackModels.ModelPart modelPart
                            : BakedSTBackpackModels.ModelPart.values()) {
                        extension.stbp$replaceModel(
                                modelPart.name(),
                                BakedSTBackpackModels.getModel(
                                        BakedSTBackpackModels.getLoc(travelerType, modelPart)
                                )
                        );
                    }
                }
                builder.put(ModTravelerTypeRegistry.get(travelerType), backpackBakedModel);
            }
            stbp$camouflages = builder.build();
        }
        finally {
            stbp$isBuildingTravelerType.remove();
        }
    }

    @Override
    public void stbp$replaceModel(
            String partName,
            BakedModel replacement
    ) {
        Map<Object, BakedModel> copy = new HashMap<>();

        for (Map.Entry<?, BakedModel> entry : models.entrySet()) {
            Object key = entry.getKey();
            BakedModel value = entry.getValue();

            if (key instanceof Enum<?> part
                    && part.name().equals(partName)) {
                value = replacement;
            }

            copy.put(key, value);
        }

        this.models = copy;
    }


    @Unique
    private static final ModelProperty<TravelerTypeInstance>
            stbp$camouflageProperty =
            new ModelProperty<>();
    @Unique
    private TravelerTypeInstance stbp$travelerTypePropertyValue = null;

    @Unique
    private boolean stbp$isTravelerTypeVariant = false;

    public Boolean stbp$isTravelerType(){
        return stbp$isTravelerTypeVariant;
    }

    /*
    pass camouflage model property into model data
    in this method, the backpack item is still accessible from the block entity
     */
    @Inject(method = "lambda$getModelData$4", at = @At(value = "RETURN"), cancellable = true, remap = false)
    private static void stbp$getModel(
            ModelData modelData,
            BackpackBlockEntity backpackBlockEntity,
            CallbackInfoReturnable<ModelData> cir){
        ItemStack itemStack = backpackBlockEntity.getBackpackWrapper().getBackpack();
        TravelerTypeInstance travelerType = TravelerType.getTravelerTypeFromBackpack(itemStack);
        if(travelerType == null) return;
        cir.setReturnValue(
                cir.getReturnValue()
                        .derive()
                        .with(stbp$camouflageProperty, travelerType)
                        .build()
        );
    }

    /*
    read the camouflage property value for this class instance
     */
    @Inject(method = "setPropertiesFromModelData", at = @At("TAIL"), remap = false)
    private void stbp$setProperties(ModelData extraData, CallbackInfo ci){
        stbp$travelerTypePropertyValue = extraData.get(stbp$camouflageProperty);
    }

    /*
    If the camouflage property value is not null in this instance,
    switch the result to the result of corresponding camouflage baked model group
     */
    @Inject(method = "getQuads",
            at = @At(value = "INVOKE",
                    target = "Lnet/p3pp3rf1y/sophisticatedbackpacks/client/render/BackpackDynamicModel$BackpackBakedModel;" +
                            "setPropertiesFromModelData" +
                            "(Lnet/minecraftforge/client/model/data/ModelData;)V",
                    shift = At.Shift.AFTER,
                    remap = false),
            cancellable = true,
            remap = false
    )
    private void stbp$getQuads(
            BlockState state,
            Direction side,
            RandomSource rand,
            ModelData extraData,
            RenderType renderType,
            CallbackInfoReturnable<List<BakedQuad>> cir){
        if (this.stbp$travelerTypePropertyValue == null) return;
        if (this.stbp$isTravelerTypeVariant) return;
        var camouflageQuads = stbp$get(this.stbp$travelerTypePropertyValue).getQuads(state, side, rand, extraData, renderType);
        cir.setReturnValue(camouflageQuads);
    }

    @Contract(pure = true)
    @Unique
    public void stbp$copy(BackpackDynamicModel.BackpackBakedModel other){
        if ((Object) other instanceof BackpackBakedModelMixin mixin){
            lastContext = mixin.lastContext;
            leftTankRenderInfo = mixin.leftTankRenderInfo;
            rightTankRenderInfo = mixin.rightTankRenderInfo;
            batteryRenderInfo = mixin.batteryRenderInfo;
            battery = mixin.battery;
            tankLeft = mixin.tankLeft;
            tankRight = mixin.tankRight;
        }
    }

}
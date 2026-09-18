package com.vomiter.sophtravelerspack.mixin.model;

import com.vomiter.sophtravelerspack.client.BackpackModelsExtension;
import com.vomiter.sophtravelerspack.traveler.TravelerType;
import com.vomiter.sophtravelerspack.traveler.TravelerTypeInstance;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackDynamicModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackDynamicModel$BackpackItemOverrideList"})
public class BackpackOverrideMixin {
    @Shadow
    @Final
    private BackpackDynamicModel.BackpackBakedModel backpackModel;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(BackpackDynamicModel.BackpackBakedModel backpackModel, CallbackInfo ci){
    }

    /*
    for item renderer. return the baked model built in BackpackBakedModelMixin#stbp$init
     */
    @Inject(method = "resolve", at = @At("HEAD"), cancellable = true)
    private void stbp$resolve(
            BakedModel model,
            ItemStack stack,
            ClientLevel world,
            LivingEntity livingEntity,
            int seed,
            CallbackInfoReturnable<BakedModel> cir){
        if((Object)backpackModel instanceof BackpackModelsExtension extension){
            if (extension.stbp$isTravelerType()) return;
            TravelerTypeInstance travelerType = TravelerType.getTravelerTypeFromBackpack(stack);
            if (travelerType == null) return;
            BackpackDynamicModel.BackpackBakedModel bakedModel = extension.stbp$get(travelerType);
            if ((Object)bakedModel instanceof BackpackModelsExtension extension1){
                extension1.stbp$copy(backpackModel);
            }
            cir.setReturnValue(bakedModel);
        }
    }
}

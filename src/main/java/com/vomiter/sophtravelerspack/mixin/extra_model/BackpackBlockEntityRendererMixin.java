package com.vomiter.sophtravelerspack.mixin.extra_model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vomiter.sophtravelerspack.client.BakedSTBackpackModels;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BackpackBlockEntityRenderer.class, remap = false)
public class BackpackBlockEntityRendererMixin {

    @Inject(
            method = "render(Lnet/p3pp3rf1y/sophisticatedbackpacks/backpack/BackpackBlockEntity;"
                    + "FLcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("TAIL")
    )
    private void sophtravelerspack$renderExtra(
            BackpackBlockEntity backpackBlockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            CallbackInfo ci) {

        ItemStack backpack = backpackBlockEntity.getBackpackWrapper().getBackpack();

        if (backpack.isEmpty()) {
            return;
        }

        ItemStack travelerStack = TravelerUtil.getShadow(backpack);
        BakedModel extra = BakedSTBackpackModels.getOrLoadExtraModel(travelerStack);
        BakedModel tank = BakedSTBackpackModels.getOrLoadTankModel(travelerStack);

        Direction facing = backpackBlockEntity
                .getBlockState()
                .getValue(BackpackBlock.FACING)
                .getOpposite();
        float rotation = -(facing.toYRot() + 180);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        if (extra != null){
            poseStack.pushPose();
            try {
                poseStack.translate(0.5f, 0, 0.5f);
                poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
                poseStack.translate(-0.5f, 0, -0.5f);
                poseStack.translate(0.0D, 0.4, 0.05);

                for (BakedModel renderPass : extra.getRenderPasses(travelerStack, true)) {

                    for (RenderType renderType : renderPass.getRenderTypes(travelerStack, true)) {
                        VertexConsumer consumer = bufferSource.getBuffer(renderType);
                        itemRenderer.renderModelLists(
                                renderPass,
                                travelerStack,
                                packedLight,
                                packedOverlay,
                                poseStack,
                                consumer
                        );
                    }
                }
            } finally {
                poseStack.popPose();
            }
        }
    }
}
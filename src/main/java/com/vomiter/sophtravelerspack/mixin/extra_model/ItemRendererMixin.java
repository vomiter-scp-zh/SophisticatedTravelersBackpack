package com.vomiter.sophtravelerspack.mixin.extra_model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vomiter.sophtravelerspack.client.BakedSTBackpackModels;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Inject(
            method = "render(Lnet/minecraft/world/item/ItemStack;"
                    + "Lnet/minecraft/world/item/ItemDisplayContext;"
                    + "ZLcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;"
                    + "IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void stbp$renderExtra(
            ItemStack itemStack,
            ItemDisplayContext context,
            boolean leftHand,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            BakedModel model,
            CallbackInfo ci) {

        if (!(itemStack.getItem() instanceof BackpackItem)) {
            return;
        }

        if (
                TravelerUtil.get(itemStack).isEmpty()
                || TravelerUtil.getShadow(itemStack) == null
                || TravelerUtil.getShadow(itemStack).isEmpty()
        ) {
            return;
        }

        ItemStack travelerStack = TravelerUtil.getShadow(itemStack);
        BakedModel extra = BakedSTBackpackModels.getOrLoadExtraModel(travelerStack);

        if (extra == null) {
            return;
        }

        poseStack.pushPose();
        try {
            //the "front" of traveler's backpack and sophisticated backpack is different
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.translate(-0.5, -0.5, -0.5);

            poseStack.translate(0, 0.4, 0.05);

            ItemRenderer renderer = (ItemRenderer) (Object) this;

            boolean solidRender = true;

            for (BakedModel renderPass : extra.getRenderPasses(travelerStack, solidRender)) {

                for (RenderType renderType : renderPass.getRenderTypes(travelerStack, solidRender)) {

                    VertexConsumer consumer =
                            ItemRenderer.getFoilBufferDirect(
                                    bufferSource,
                                    renderType,
                                    true,
                                    false
                            );

                    renderer.renderModelLists(
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
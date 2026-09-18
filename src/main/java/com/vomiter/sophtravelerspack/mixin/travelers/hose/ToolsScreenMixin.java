package com.vomiter.sophtravelerspack.mixin.travelers.hose;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tiviacz.travelersbackpack.client.screens.ToolsScreen;
import com.vomiter.sophtravelerspack.client.ClientEventHandler;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ToolsScreen.class, remap = false)
public class ToolsScreenMixin {
    @WrapOperation(method = "selectHoseAction", at = @At(value = "INVOKE", target = "Lcom/tiviacz/travelersbackpack/client/screens/ToolsScreen;getNextModeMessage(II)Lnet/minecraft/network/chat/Component;"))
    private Component stbp$selectHoseAction(int changedMode, int data, Operation<Component> original){
        if (ClientEventHandler.shouldInverseTank){
            int tank = data == 2 ? 1 : 2;
            return original.call(changedMode, tank);
        }
        return original.call(changedMode, data);
    }
}

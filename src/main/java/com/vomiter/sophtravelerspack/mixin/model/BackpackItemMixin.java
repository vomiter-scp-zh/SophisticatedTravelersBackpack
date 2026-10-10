package com.vomiter.sophtravelerspack.mixin.model;

import com.vomiter.sophtravelerspack.traveler.TravelerType;
import com.vomiter.sophtravelerspack.traveler.TravelerTypeInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackpackItem.class)
public class BackpackItemMixin {

    /*
    makes the main color more faint
     */
    @Inject(method = "getMainColor", remap = false, at = @At("RETURN"), cancellable = true)
    private static void stbp$getMainColor(ItemStack backpackStack, CallbackInfoReturnable<Integer> cir){
        TravelerTypeInstance travelerType = TravelerType.getTravelerTypeFromBackpack(backpackStack);
        if(travelerType == null) return;
        Integer mainColor = backpackStack.get(ModCoreDataComponents.MAIN_COLOR);
        if(mainColor == null){
            cir.setReturnValue(0xFFFFFFFF);
        }

        cir.setReturnValue(fadeTint(cir.getReturnValue(), 0.1f));
    }

    private static int fadeTint(int color, float strength) {
        strength = Mth.clamp(strength, 0.0F, 1.0F);

        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        red = Mth.floor(255.0F - (255.0F - red) * strength);
        green = Mth.floor(255.0F - (255.0F - green) * strength);
        blue = Mth.floor(255.0F - (255.0F - blue) * strength);

        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}

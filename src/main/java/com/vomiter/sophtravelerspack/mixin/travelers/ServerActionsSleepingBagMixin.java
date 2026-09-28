package com.vomiter.sophtravelerspack.mixin.travelers;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tiviacz.travelersbackpack.common.ServerActions;
import com.vomiter.sophtravelerspack.util.SleepingBagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ServerActions.class, remap = false)
public class ServerActionsSleepingBagMixin {
    @WrapMethod(method = "placeAndUseSleepingBag")
    private static boolean stbp$placeAndUseSleepingBag(Player player, BlockPos foot, BlockPos head,
                                                        BlockPos pos, Level level, Direction direction,
                                                        Operation<Boolean> original) {
        if (player.containerMenu instanceof BackpackContainer menu && menu.getBlockPosition().isEmpty()) {
            return SleepingBagUtils.placeAndUseSleepingBag(player, foot, head, pos, level, direction);
        }
        return original.call(player, foot, head, pos, level, direction);
    }
}

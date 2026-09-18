package com.vomiter.sophtravelerspack.mixin.travelers.block;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tiviacz.travelersbackpack.common.BackpackAbilities;
import com.tiviacz.travelersbackpack.init.ModItems;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public class BlockMixin {
    @WrapMethod(method = "playerWillDestroy")
    private void stbp$playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player, Operation<Void> original) {
        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof BackpackBlockEntity backpackBlockEntity) {
            ItemStack sophisticatedBackpack = backpackBlockEntity.getBackpackWrapper().getBackpack();
            ItemStack shadow = TravelerUtil.getShadow(sophisticatedBackpack);

            if (shadow.is(ModItems.MELON_TRAVELERS_BACKPACK.get())) {
                BackpackWrapper wrapper = BackpackWrapper.fromStack(shadow);
                if (wrapper.isAbilityEnabled() && wrapper.getCooldown() <= 0) {
                    Block.popResource(world, pos, new ItemStack(Items.MELON_SLICE, world.random.nextInt(0, 3)));

                    BackpackAbilities.setCooldown(wrapper, wrapper.getBackpackStack().getItem());

                    TravelerUtil.setShadow(sophisticatedBackpack, shadow);
                    backpackBlockEntity.setChanged();
                }
            }
        }

        original.call(world, pos, state, player);
    }}

package com.vomiter.sophtravelerspack.util;

import com.tiviacz.travelersbackpack.blocks.SleepingBagBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;

import java.util.concurrent.atomic.AtomicReference;

public class SleepingBagUtils {
    //equipment route -> call com.tiviacz.travelersbackpack.common.ServerActions#toggleSleepingBag(player, pos, true, false)
    //todo: mixin into placeAndUseSleepingBag to make sleeping bag in SBP usable

    final static String SLEEPING_BAG_DEPLOYED_KEY = "sleeping_bag_deployed";
    static void setSleepingBagDeployed(ItemStack stack, boolean b){
        var tag = stack.getOrCreateTag();
        tag.putBoolean(SLEEPING_BAG_DEPLOYED_KEY, b);
    }
    static boolean isSleepingBagDeployed(ItemStack stack){
        var tag = stack.getOrCreateTag();
        return tag.getBoolean(SLEEPING_BAG_DEPLOYED_KEY);
    }

    public static boolean deploySleepingBagFromSBP(Level level, BlockPos pos){
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BackpackBlockEntity backpackBlockEntity){
            var wrapper = backpackBlockEntity.getBackpackWrapper();
            if (isThereSleepingBag(level, pos, backpackBlockEntity.getBlockState().getValue(BackpackBlock.FACING), wrapper.getBackpack())) return false;
            if (isSleepingBagDeployed(wrapper.getBackpack())) return false;
            if (getSleepingBag(wrapper, true).isEmpty()) return false;
            if (!deploySleepingBag(level, pos, ((BlockItem)getSleepingBag(wrapper, false).getItem()).getBlock())) return false;
            setSleepingBagDeployed(wrapper.getBackpack(), true);
            return true;
        }
        return false;
    }

    public static boolean recoverSleepingBagIntoSBP(Level level, BlockPos pos){
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BackpackBlockEntity backpackBlockEntity){
            var wrapper = backpackBlockEntity.getBackpackWrapper();
            Direction direction = backpackBlockEntity.getBlockState().getValue(BackpackBlock.FACING);
            if (!isThereSleepingBag(level, pos, direction, wrapper.getBackpack())) return false;
            if (!isSleepingBagDeployed(wrapper.getBackpack())) return false;
            Block sleepingBagBlock = getSleepingBagInWorld(level, pos, direction);
            ItemStack remainder = wrapper.getInventoryHandler().insertItem(sleepingBagBlock.asItem().getDefaultInstance(), false);
            if (!remainder.isEmpty()){
                var marker = new Marker(EntityType.MARKER, level);
                marker.spawnAtLocation(remainder);
                marker.discard();
            }
            removeSleepingBag(level, pos, direction);
            setSleepingBagDeployed(wrapper.getBackpack(), false);
            return true;
        }
        return false;
    }

    public static ItemStack getSleepingBag(IBackpackWrapper backpackWrapper, boolean simulate){
        var inv = backpackWrapper.getInventoryHandler();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.extractItem(i, 1, simulate);
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SleepingBagBlock){
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getSleepingBag(Player player, boolean simulate){
        AtomicReference<ItemStack> itemRef = new AtomicReference<>(ItemStack.EMPTY);
        PlayerInventoryProvider.get()
                .runOnBackpacks(
                        player,
                        (backpack,
                         inventoryName,
                         identifier,
                         backpackSlot
                        ) -> {
                            return backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance())
                                    .map(
                                            backpackWrapper -> {
                                                var sleepingBag = getSleepingBag(backpackWrapper, simulate);
                                                if (sleepingBag.isEmpty()) return false;
                                                itemRef.set(sleepingBag);
                                                return true;
                                            }
                                    )
                                    .orElse(false);
                        });
        return itemRef.get();
    }

    public static boolean deploySleepingBag(Level level, BlockPos pos, Block sleepingBag) {
        Direction direction = level.getBlockState(pos).getValue(BackpackBlock.FACING);
        BlockPos sleepingBagPos1 = pos.relative(direction);
        BlockPos sleepingBagPos2 = sleepingBagPos1.relative(direction);
        if (com.tiviacz.travelersbackpack.blockentity.BackpackBlockEntity.canPlaceSleepingBag(sleepingBagPos1, level)
                && com.tiviacz.travelersbackpack.blockentity.BackpackBlockEntity.canPlaceSleepingBag(sleepingBagPos2, level)) {
            if (!level.getBlockState(sleepingBagPos1.below()).isAir() && !(level.getBlockState(sleepingBagPos1.below()).getBlock() instanceof LiquidBlock)) {
                level.playSound(null, sleepingBagPos2, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.5F, 1.0F);
                if (!level.isClientSide) {
                    BlockState sleepingBagState = sleepingBag.defaultBlockState();
                    level.setBlock(sleepingBagPos1, sleepingBagState.setValue(SleepingBagBlock.FACING, direction).setValue(SleepingBagBlock.PART, BedPart.FOOT).setValue(SleepingBagBlock.CAN_DROP, false), 3);
                    level.setBlock(sleepingBagPos2, sleepingBagState.setValue(SleepingBagBlock.FACING, direction).setValue(SleepingBagBlock.PART, BedPart.HEAD).setValue(SleepingBagBlock.CAN_DROP, false), 3);
                    level.updateNeighborsAt(pos, sleepingBagState.getBlock());
                    level.updateNeighborsAt(sleepingBagPos2, sleepingBagState.getBlock());
                }
                return true;
            }

            return false;
        }

        return false;
    }

    public static boolean removeSleepingBag(Level level, BlockPos pos, Direction direction) {
        BlockPos sleepingBagPos1 = pos.relative(direction);
        BlockPos sleepingBagPos2 = sleepingBagPos1.relative(direction);
        if (level.getBlockState(sleepingBagPos1).getBlock() instanceof SleepingBagBlock && level.getBlockState(sleepingBagPos2).getBlock() instanceof SleepingBagBlock) {
            level.playSound(null, sleepingBagPos2, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.5F, 1.0F);
            level.setBlock(sleepingBagPos2, Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(sleepingBagPos1, Blocks.AIR.defaultBlockState(), 3);
            return true;
        } else {
            return false;
        }
    }

    public static boolean isThereSleepingBag(Level level, BlockPos pos, Direction direction, ItemStack backpack) {
        if (level.getBlockState(pos.relative(direction)).getBlock() instanceof SleepingBagBlock && level.getBlockState(pos.relative(direction).relative(direction)).getBlock() instanceof SleepingBagBlock) {
            return true;
        } else {
            setSleepingBagDeployed(backpack, false);
            return false;
        }
    }

    static Block getSleepingBagInWorld(Level level, BlockPos pos, Direction direction){
        return level.getBlockState(pos.relative(direction)).getBlock();
    }
}

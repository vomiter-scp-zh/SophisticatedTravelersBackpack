package com.vomiter.sophtravelerspack.mixin.travelers.hose;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tiviacz.travelersbackpack.init.ModItems;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.upgrades.tanks.TanksUpgrade;
import com.tiviacz.travelersbackpack.items.HoseItem;
import com.vomiter.sophtravelerspack.mixin.SophBackpackFluidHandlerAccessor;
import com.vomiter.sophtravelerspack.util.SophisticatedUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackFluidHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HoseItem.class)
public class HoseItemMixin {
    @Shadow
    public static int getHoseTank(ItemStack stack) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    private ThreadLocal<BackpackFluidHandler> fluidHandlerThreadLocal = new ThreadLocal<>();
    private ThreadLocal<Integer> indexThreadLocal = ThreadLocal.withInitial(() -> 0);
    private ThreadLocal<FluidTank> tankThreadLocal = new ThreadLocal<>();


    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lcom/tiviacz/travelersbackpack/capability/CapabilityUtils;isWearingBackpack(Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean stbp$isWearingBackpack(Player player, Operation<Boolean> original){
        if (original.call(player)) return true;
        return !SophisticatedUtil.getSophBackpackOnBack(player).isEmpty();
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lcom/tiviacz/travelersbackpack/capability/CapabilityUtils;getBackpackWrapper(Lnet/minecraft/world/entity/player/Player;[I)Lcom/tiviacz/travelersbackpack/inventory/BackpackWrapper;"))
    private BackpackWrapper stbp$getDummyBackpackWrapper(Player player, int[] dataLoad, Operation<BackpackWrapper> original){
        ItemStack sophBackpack = SophisticatedUtil.getSophBackpackOnBack(player);
        if (sophBackpack.isEmpty()) return original.call(player, dataLoad);
        LazyOptional<IBackpackWrapper> sWrapper = sophBackpack.getCapability(CapabilityBackpackWrapper.BACKPACK_WRAPPER_CAPABILITY);
        if (!sWrapper.isPresent()) return original.call(player, dataLoad);
        if (sWrapper.resolve().get().getFluidHandler().isPresent() && sWrapper.resolve().get().getFluidHandler().get() instanceof BackpackFluidHandler backpackFluidHandler){
            ItemStack dummyStack = ModItems.STANDARD_TRAVELERS_BACKPACK.get().getDefaultInstance();
            BackpackWrapper wrapper = BackpackWrapper.fromStack(dummyStack);
            wrapper.upgrades.setStackInSlot(
                    0,
                    ModItems.TANKS_UPGRADE.get().getDefaultInstance()
            );
            fluidHandlerThreadLocal.set(backpackFluidHandler);
            return BackpackWrapper.fromStack(dummyStack);
        } else {
            ItemStack dummyStack = ModItems.STANDARD_TRAVELERS_BACKPACK.get().getDefaultInstance();
            return BackpackWrapper.fromStack(dummyStack);
        }
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lcom/tiviacz/travelersbackpack/items/HoseItem;getSelectedFluidTank(Lnet/minecraft/world/item/ItemStack;Lcom/tiviacz/travelersbackpack/inventory/upgrades/tanks/TanksUpgrade;)Lnet/minecraftforge/fluids/capability/templates/FluidTank;"))
    private FluidTank stbp$getTank(HoseItem instance, ItemStack stack, TanksUpgrade upgrade, Operation<FluidTank> original){
        if (fluidHandlerThreadLocal.get() == null) return original.call(instance, stack, upgrade);
        var fluidHandler = fluidHandlerThreadLocal.get();
        int index = getHoseTank(stack) -1;
        if (fluidHandler.getTanks() < index + 1 || index <= -1) return new FluidTank(0);
        indexThreadLocal.set(index);
        FluidStack fluidStack = fluidHandler.getFluidInTank(index);
        int capacity = fluidHandler.getTankCapacity(index);
        FluidTank tank = new FluidTank(capacity);
        tank.fill(fluidStack.copy(), IFluidHandler.FluidAction.EXECUTE);
        tankThreadLocal.set(tank);
        return tank;
    }

    @WrapMethod(method = "use")
    private InteractionResultHolder<ItemStack> stbp$wrapUse(Level level, Player player, InteractionHand hand, Operation<InteractionResultHolder<ItemStack>> original){
        try{
            if (player.isLocalPlayer()) SophisticatedUtil.requestBackpackSync(player);
            return original.call(level, player, hand);
        }
        finally {
            if (fluidHandlerThreadLocal.get() != null && tankThreadLocal.get() != null){
                var fluidHandler = fluidHandlerThreadLocal.get();
                var dummyTank = tankThreadLocal.get();
                if (fluidHandler instanceof SophBackpackFluidHandlerAccessor acc){
                    var tank = acc.stbp$getAllTanks().get(indexThreadLocal.get());
                    tank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE, true);
                    tank.fill(dummyTank.getFluid(), IFluidHandler.FluidAction.EXECUTE, true);
                }
            }
            fluidHandlerThreadLocal.remove();
            indexThreadLocal.remove();
            tankThreadLocal.remove();
        }
    }

    @WrapOperation(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lcom/tiviacz/travelersbackpack/capability/CapabilityUtils;isWearingBackpack(Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean stbp$isWearingBackpackAtFinish(Player player, Operation<Boolean> original){
        if (original.call(player)) return true;
        return !SophisticatedUtil.getSophBackpackOnBack(player).isEmpty();
    }

    @WrapOperation(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lcom/tiviacz/travelersbackpack/capability/CapabilityUtils;getBackpackWrapper(Lnet/minecraft/world/entity/player/Player;[I)Lcom/tiviacz/travelersbackpack/inventory/BackpackWrapper;"))
    private BackpackWrapper stbp$getDummyBackpackWrapperAtFinish(Player player, int[] dataLoad, Operation<BackpackWrapper> original){
        ItemStack sophBackpack = SophisticatedUtil.getSophBackpackOnBack(player);
        if (sophBackpack.isEmpty()) return original.call(player, dataLoad);
        LazyOptional<IBackpackWrapper> sWrapper = sophBackpack.getCapability(CapabilityBackpackWrapper.BACKPACK_WRAPPER_CAPABILITY);
        if (!sWrapper.isPresent()) return original.call(player, dataLoad);
        if (sWrapper.resolve().get().getFluidHandler().isPresent() && sWrapper.resolve().get().getFluidHandler().get() instanceof BackpackFluidHandler backpackFluidHandler){
            ItemStack dummyStack = ModItems.STANDARD_TRAVELERS_BACKPACK.get().getDefaultInstance();
            BackpackWrapper wrapper = BackpackWrapper.fromStack(dummyStack);
            wrapper.upgrades.setStackInSlot(
                    0,
                    ModItems.TANKS_UPGRADE.get().getDefaultInstance()
            );
            fluidHandlerThreadLocal.set(backpackFluidHandler);
            return BackpackWrapper.fromStack(dummyStack);
        }else {
            ItemStack dummyStack = ModItems.STANDARD_TRAVELERS_BACKPACK.get().getDefaultInstance();
            return BackpackWrapper.fromStack(dummyStack);
        }
    }

    @WrapOperation(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lcom/tiviacz/travelersbackpack/items/HoseItem;getSelectedFluidTank(Lnet/minecraft/world/item/ItemStack;Lcom/tiviacz/travelersbackpack/inventory/upgrades/tanks/TanksUpgrade;)Lnet/minecraftforge/fluids/capability/templates/FluidTank;"))
    private FluidTank stbp$getTankAtFinish(HoseItem instance, ItemStack stack, TanksUpgrade upgrade, Operation<FluidTank> original){
        if (fluidHandlerThreadLocal.get() == null) return original.call(instance, stack, upgrade);
        var fluidHandler = fluidHandlerThreadLocal.get();
        int index = getHoseTank(stack) -1;
        if (fluidHandler.getTanks() < index + 1 || index <= -1) return new FluidTank(0);
        indexThreadLocal.set(index);
        FluidStack fluidStack = fluidHandler.getFluidInTank(index);
        int capacity = fluidHandler.getTankCapacity(index);
        FluidTank tank = new FluidTank(capacity);
        tank.fill(fluidStack.copy(), IFluidHandler.FluidAction.EXECUTE);
        tankThreadLocal.set(tank);
        return tank;
    }

    @WrapMethod(method = "finishUsingItem")
    private ItemStack stbp$wrapFinish(ItemStack stack, Level level, LivingEntity entityLiving, Operation<ItemStack> original){
        try{
            if (entityLiving instanceof Player player && player.isLocalPlayer()) SophisticatedUtil.requestBackpackSync(player);
            return original.call(stack, level, entityLiving);
        }
        finally {
            if (fluidHandlerThreadLocal.get() != null && tankThreadLocal.get() != null){
                var fluidHandler = fluidHandlerThreadLocal.get();
                var dummyTank = tankThreadLocal.get();
                if (fluidHandler instanceof SophBackpackFluidHandlerAccessor acc){
                    var tank = acc.stbp$getAllTanks().get(indexThreadLocal.get());
                    tank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE, true);
                    tank.fill(dummyTank.getFluid(), IFluidHandler.FluidAction.EXECUTE, true);
                }
            }
            fluidHandlerThreadLocal.remove();
            indexThreadLocal.remove();
            tankThreadLocal.remove();
        }
    }

}

package com.vomiter.sophtravelerspack.traveler;

import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.upgrades.tanks.TanksUpgrade;
import com.vomiter.sophtravelerspack.common.registry.ModTravelerTypeRegistry;
import com.vomiter.sophtravelerspack.common.registry.ModDataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;

import static net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper.fromStack;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class TravelerUtil {
    public static void set(ItemStack stack, TravelerTypeInstance travelerType) {
        ResourceLocation registryId =
                ModTravelerTypeRegistry.REGISTRY.get().getKey(travelerType);

        if (registryId == null) {
            throw new IllegalArgumentException(
                    "TravelerTypeInstance is not registered: "
                            + travelerType
            );
        }

        LegacyTravelerDataMigration.migrate(stack);
        LegacyTravelerDataMigration.removeLegacyData(stack, LegacyTravelerDataMigration.TRAVELER_KEY);
        stack.set(ModDataComponents.TRAVELER_TYPE.get(), registryId);
        setShadow(stack, travelerType.createTravelerBackpack());
    }

    public static Optional<TravelerTypeInstance> get(ItemStack stack) {
        LegacyTravelerDataMigration.migrate(stack);
        ResourceLocation registryId = stack.get(ModDataComponents.TRAVELER_TYPE.get());
        return registryId == null ? Optional.empty() : ModTravelerTypeRegistry.get(registryId);
    }

    public static boolean clear(ItemStack stack) {
        LegacyTravelerDataMigration.migrate(stack);
        LegacyTravelerDataMigration.removeLegacyData(stack,
                LegacyTravelerDataMigration.TRAVELER_KEY, LegacyTravelerDataMigration.SHADOW_KEY);
        boolean present = stack.remove(ModDataComponents.TRAVELER_TYPE.get()) != null;
        stack.remove(ModDataComponents.TRAVELER_SHADOW.get());
        if ((Object) stack instanceof ITravelerDummyHolder holder) {
            holder.sophtravelerspack$setTraveler(null);
            holder.sophtravelerspack$setShadowSource(null);
        }
        return present;
    }

    public static ItemStack getShadow(ItemStack sophisticatedBackpack) {
        LegacyTravelerDataMigration.migrate(sophisticatedBackpack);
        ItemContainerContents source = sophisticatedBackpack.get(ModDataComponents.TRAVELER_SHADOW.get());
        if (source == null || source.getSlots() == 0) {
            return ItemStack.EMPTY;
        }

        if ((Object) sophisticatedBackpack instanceof ITravelerDummyHolder holder) {
            ItemStack cachedTraveler = holder.sophtravelerspack$getTraveler();
            // Component replacement (including a server sync) invalidates the mutable cache.
            if (cachedTraveler != null && holder.sophtravelerspack$getShadowSource() == source) {
                return cachedTraveler;
            }
            ItemStack traveler = source.getStackInSlot(0);
            holder.sophtravelerspack$setTraveler(traveler);
            holder.sophtravelerspack$setShadowSource(source);
            return traveler;
        }
        return source.getStackInSlot(0);
    }

    public static ItemStack getShadow(Player player) {
        AtomicReference<ItemStack> travelerStack =
                new AtomicReference<>(ItemStack.EMPTY);

        PlayerInventoryProvider.get().runOnBackpacks(
                player,
                (itemStack, handlerName, identifier, slot) -> {
                    if ("main".equals(handlerName) || "offhand".equals(handlerName)) {
                        return false;
                    }

                    ItemStack shadow = getShadow(itemStack);

                    if (!shadow.isEmpty() && travelerStack.get().isEmpty()) {
                        travelerStack.set(shadow);
                    }

                    return true;
                }
        );

        return travelerStack.get();
    }

    public static void setShadow(ItemStack sophisticatedBackpack, ItemStack travelerShadow) {
        LegacyTravelerDataMigration.removeLegacyData(sophisticatedBackpack, LegacyTravelerDataMigration.SHADOW_KEY);
        if (travelerShadow.isEmpty()) {
            sophisticatedBackpack.remove(ModDataComponents.TRAVELER_SHADOW.get());
        } else {
            sophisticatedBackpack.set(ModDataComponents.TRAVELER_SHADOW.get(),
                    ItemContainerContents.fromItems(List.of(travelerShadow.copy())));
        }
        if ((Object) sophisticatedBackpack instanceof ITravelerDummyHolder holder) {
            holder.sophtravelerspack$setTraveler(travelerShadow);
            holder.sophtravelerspack$setShadowSource(
                    sophisticatedBackpack.get(ModDataComponents.TRAVELER_SHADOW.get()));
        }
    }

    public static void saveShadow(ItemStack sophisticatedBackpack) {
        ItemStack travelerShadow = getShadow(sophisticatedBackpack);
        if (travelerShadow.isEmpty()) return;
        fromStack(sophisticatedBackpack).getFluidHandler().ifPresent(fluidHandler -> {
            TanksUpgrade upgrade = BackpackWrapper.fromStack(travelerShadow).getUpgradeManager().getUpgrade(TanksUpgrade.class).orElse(null);
            if (upgrade == null) return;
            FluidTank leftTank = upgrade.getLeftTank();
            FluidTank rightTank = upgrade.getRightTank();
            int leftAccepted = fluidHandler.fill(leftTank.getFluid().copy(), IFluidHandler.FluidAction.SIMULATE);
            if (leftAccepted > 0) {
                fluidHandler.fill(leftTank.drain(leftAccepted, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
            }
            int rightAccepted = fluidHandler.fill(rightTank.getFluid().copy(), IFluidHandler.FluidAction.SIMULATE);
            if (rightAccepted > 0) {
                fluidHandler.fill(rightTank.drain(rightAccepted, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
            }
        });
        // Persist after tank transfer so the snapshot contains the resulting state.
        setShadow(sophisticatedBackpack, travelerShadow);
    }

    public static void saveShadow(Player player){
        PlayerInventoryProvider.get().runOnBackpacks(
                player,
                (itemStack, handlerName, identifier, slot) -> {
                    if ("main".equals(handlerName) || "offhand".equals(handlerName)) {
                        return false;
                    }
                    saveShadow(itemStack);
                    return true;
                }
        );
    }


    public static boolean hasShadow(ItemStack sophisticatedBackpack) {
        return !getShadow(sophisticatedBackpack).isEmpty();
    }

    /** Replaces the item while retaining all component overrides and removals. */
    public static ItemStack changeBackpackItem(ItemStack original, Item targetItem) {
        ItemStack result = new ItemStack(targetItem, 1);
        result.applyComponents(original.getComponentsPatch());
        return result;
    }
}

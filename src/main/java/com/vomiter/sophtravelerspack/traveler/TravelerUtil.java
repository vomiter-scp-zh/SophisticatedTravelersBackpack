package com.vomiter.sophtravelerspack.traveler;

import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.upgrades.tanks.TanksUpgrade;
import com.vomiter.sophtravelerspack.common.registry.ModTravelerTypeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class TravelerUtil {
    public static final String TRAVELER_KEY = "sophtravelerspack:traveler_type";

    public static final String SHADOW_TAG = "travelersophpack:traveler_shadow";

    public static void set(ItemStack stack, TravelerTypeInstance travelerType) {
        ResourceLocation registryId =
                ModTravelerTypeRegistry.REGISTRY.get().getKey(travelerType);

        if (registryId == null) {
            throw new IllegalArgumentException(
                    "TravelerTypeInstance is not registered: "
                            + travelerType
            );
        }

        stack.getOrCreateTag().putString(TRAVELER_KEY, registryId.toString());
        setShadow(stack, travelerType.createTravelerBackpack());
    }

    /**
     * 從 NBT 保存的 registry ID 查詢 TravelerTypeInstance。
     */
    public static Optional<TravelerTypeInstance> get(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains(TRAVELER_KEY, Tag.TAG_STRING)) {
            return Optional.empty();
        }

        String value = tag.getString(TRAVELER_KEY);
        ResourceLocation registryId = ResourceLocation.tryParse(value);

        if (registryId != null) {
            TravelerTypeInstance instance =
                    ModTravelerTypeRegistry.REGISTRY.get().getValue(registryId);

            if (instance != null) {
                return Optional.of(instance);
            }
        }

        /*
         * 向下相容舊版 NBT。
         * 舊版保存的是："DIAMOND"
         * 新版保存的是："sophtravelerspack:diamond"
         */
        try {
            TravelerType legacyType = TravelerType.valueOf(value);

            return Optional.of(
                    ModTravelerTypeRegistry.get(legacyType)
            );
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    /**
     * @return 原本是否存在 traveler type NBT
     */
    public static boolean clear(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains(TRAVELER_KEY)) {
            return false;
        }

        tag.remove(TRAVELER_KEY);

        if (tag.isEmpty()) {
            stack.setTag(null);
        }

        return true;
    }

    public static ItemStack getShadow(ItemStack sophisticatedBackpack) {
        CompoundTag tag = sophisticatedBackpack.getTag();

        if (tag == null || !tag.contains(SHADOW_TAG, Tag.TAG_COMPOUND)) {
            return ItemStack.EMPTY;
        }

        if ((Object) sophisticatedBackpack instanceof ITravelerDummyHolder holder) {
            ItemStack cachedTraveler = holder.sophtravelerspack$getTraveler();

            if (cachedTraveler != null) {
                return cachedTraveler;
            }

            ItemStack traveler =
                    ItemStack.of(tag.getCompound(SHADOW_TAG));

            holder.sophtravelerspack$setTraveler(traveler);
            return traveler;
        }

        return ItemStack.of(tag.getCompound(SHADOW_TAG));
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
        CompoundTag serialized = new CompoundTag();
        travelerShadow.save(serialized);

        sophisticatedBackpack.getOrCreateTag().put(SHADOW_TAG, serialized);

        if ((Object) sophisticatedBackpack instanceof ITravelerDummyHolder holder) {
            holder.sophtravelerspack$setTraveler(travelerShadow);
        }
    }

    public static void saveShadow(ItemStack sophisticatedBackpack) {
        var travelerShadow = getShadow(sophisticatedBackpack);
        CompoundTag serialized = new CompoundTag();
        travelerShadow.save(serialized);
        sophisticatedBackpack.getOrCreateTag().put(SHADOW_TAG, serialized);
        sophisticatedBackpack.getCapability(CapabilityBackpackWrapper.BACKPACK_WRAPPER_CAPABILITY).ifPresent(
                cap -> {
                    cap.getFluidHandler().ifPresent(iStorageFluidHandler -> {
                        TanksUpgrade upgrade = BackpackWrapper.fromStack(travelerShadow).getUpgradeManager().getUpgrade(TanksUpgrade.class).orElse(null);
                        if (upgrade == null) return;
                        FluidTank leftTank = upgrade.getLeftTank();
                        FluidTank rightTank = upgrade.getRightTank();
                        iStorageFluidHandler.fill(leftTank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                        iStorageFluidHandler.fill(rightTank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                    });
                }
        );
        if ((Object) sophisticatedBackpack instanceof ITravelerDummyHolder holder) {
            holder.sophtravelerspack$setTraveler(travelerShadow);
        }
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

    /**
     * 把 Traveler's Backpack 換成另一個 backpack item，
     * 但完整保留原本的 NBT。
     */
    public static ItemStack changeBackpackItem(ItemStack original, Item targetItem) {
        ItemStack result = new ItemStack(targetItem, 1);

        if (original.hasTag()) {
            result.setTag(original.getTag().copy());
        }

        return result;
    }

}
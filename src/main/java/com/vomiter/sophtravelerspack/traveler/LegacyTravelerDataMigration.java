package com.vomiter.sophtravelerspack.traveler;

import com.mojang.logging.LogUtils;
import com.tiviacz.travelersbackpack.util.RegistryHelper;
import com.mojang.serialization.Dynamic;
import com.vomiter.sophtravelerspack.STBackpack;
import com.vomiter.sophtravelerspack.common.registry.ModDataComponents;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

import static com.tiviacz.travelersbackpack.init.ModDataComponents.ABILITY_ENABLED;
import static com.tiviacz.travelersbackpack.init.ModDataComponents.COOLDOWN;
import static com.tiviacz.travelersbackpack.init.ModDataComponents.SLEEPING_BAG_COLOR;

/** Migrates STBP's old tags after vanilla has moved them into minecraft:custom_data. */
public final class LegacyTravelerDataMigration {
    public static final String TRAVELER_KEY = "sophtravelerspack:traveler_type";
    // This spelling is intentional; it is the key written by the 1.20.1 release.
    public static final String SHADOW_KEY = "travelersophpack:traveler_shadow";
    public static final String SLEEPING_BAG_KEY = "sleeping_bag_deployed";
    private static final int DATA_VERSION_1_20_1 = 3465;
    private static final Logger LOGGER = LogUtils.getLogger();

    private LegacyTravelerDataMigration() {}

    public static void migrate(ItemStack stack) {
        migrate(stack, RegistryHelper.getRegistryAccess().orElse(null));
    }

    public static void migrate(ItemStack stack, @Nullable HolderLookup.Provider registries) {
        if (!(stack.getItem() instanceof BackpackItem)) return;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        if (!data.contains(TRAVELER_KEY) && !data.contains(SHADOW_KEY)
                && !data.contains(SLEEPING_BAG_KEY)) return;

        CompoundTag legacy = data.copyTag();
        boolean changed = false;
        if (legacy.contains(TRAVELER_KEY)) {
            if (stack.has(ModDataComponents.TRAVELER_TYPE.get())) {
                legacy.remove(TRAVELER_KEY);
                changed = true;
            } else if (legacy.contains(TRAVELER_KEY, Tag.TAG_STRING)) {
                ResourceLocation id = parseTravelerId(legacy.getString(TRAVELER_KEY));
                if (id != null) {
                    stack.set(ModDataComponents.TRAVELER_TYPE.get(), id);
                    legacy.remove(TRAVELER_KEY);
                    changed = true;
                }
            }
        }
        if (legacy.contains(SHADOW_KEY)) {
            if (stack.has(ModDataComponents.TRAVELER_SHADOW.get())) {
                legacy.remove(SHADOW_KEY);
                changed = true;
            } else if (registries != null && legacy.contains(SHADOW_KEY, Tag.TAG_COMPOUND)) {
                Optional<ItemStack> shadow = parseShadow(registries, legacy.getCompound(SHADOW_KEY));
                if (shadow.isPresent()) {
                    stack.set(ModDataComponents.TRAVELER_SHADOW.get(),
                            ItemContainerContents.fromItems(List.of(shadow.get())));
                    legacy.remove(SHADOW_KEY);
                    changed = true;
                }
            }
        }
        if (legacy.contains(SLEEPING_BAG_KEY)) {
            if (stack.has(ModDataComponents.SLEEPING_BAG_DEPLOYED.get())) {
                legacy.remove(SLEEPING_BAG_KEY);
                changed = true;
            } else if (legacy.contains(SLEEPING_BAG_KEY, Tag.TAG_BYTE)) {
                // Store false explicitly too, so this migration has an authoritative result.
                stack.set(ModDataComponents.SLEEPING_BAG_DEPLOYED.get(),
                        legacy.getBoolean(SLEEPING_BAG_KEY));
                legacy.remove(SLEEPING_BAG_KEY);
                changed = true;
            }
        }
        if (changed) writeRemainingData(stack, legacy);
    }

    @Nullable
    private static ResourceLocation parseTravelerId(String value) {
        try {
            TravelerType type = TravelerType.valueOf(value);
            return ResourceLocation.fromNamespaceAndPath(STBackpack.MODID, type.getStringRepresentation());
        } catch (IllegalArgumentException ignored) {
            // Preserve valid registry IDs even when the corresponding type is currently unavailable.
            return ResourceLocation.tryParse(value);
        }
    }

    private static Optional<ItemStack> parseShadow(HolderLookup.Provider registries, CompoundTag saved) {
        if (saved.isEmpty()) return Optional.of(ItemStack.EMPTY);
        CompoundTag converted = saved.copy();
        try {
            if (converted.contains("Count", Tag.TAG_ANY_NUMERIC) && !converted.contains("count")) {
                // Vanilla does not traverse arbitrary mod-owned tags inside CUSTOM_DATA.
                // Run its item-stack fixer explicitly for the nested 1.20.1 stack.
                Dynamic<Tag> fixed = DataFixers.getDataFixer().update(References.ITEM_STACK,
                        new Dynamic<>(NbtOps.INSTANCE, converted), DATA_VERSION_1_20_1,
                        SharedConstants.getCurrentVersion().getDataVersion().getVersion());
                if (!(fixed.getValue() instanceof CompoundTag compound)) return Optional.empty();
                converted = compound;
            }
            Optional<ItemStack> result = ItemStack.parse(registries, converted);
            result.ifPresent(LegacyTravelerDataMigration::migrateShadowState);
            return result;
        } catch (RuntimeException exception) {
            LOGGER.debug("Cannot migrate legacy STBP shadow; keeping its original NBT", exception);
            return Optional.empty();
        }
    }

    private static void migrateShadowState(ItemStack shadow) {
        CustomData data = shadow.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        CompoundTag legacy = data.copyTag();
        boolean changed = false;
        // The vanilla fixer retains mod-owned tags; TB now reads these from components.
        if (legacy.contains("Cooldown", Tag.TAG_INT)) {
            if (!shadow.has(COOLDOWN.get())) shadow.set(COOLDOWN.get(), legacy.getInt("Cooldown"));
            legacy.remove("Cooldown");
            changed = true;
        }
        if (legacy.contains("AbilityEnabled", Tag.TAG_BYTE)) {
            if (!shadow.has(ABILITY_ENABLED.get())) shadow.set(ABILITY_ENABLED.get(), legacy.getBoolean("AbilityEnabled"));
            legacy.remove("AbilityEnabled");
            changed = true;
        }
        if (legacy.contains("SleepingBagColor", Tag.TAG_INT)) {
            if (!shadow.has(SLEEPING_BAG_COLOR.get())) shadow.set(SLEEPING_BAG_COLOR.get(), legacy.getInt("SleepingBagColor"));
            legacy.remove("SleepingBagColor");
            changed = true;
        }
        if (changed) writeRemainingData(shadow, legacy);
    }

    /** Explicit writes must remove old values too, or a later migration could resurrect them. */
    public static void removeLegacyData(ItemStack stack, String... keys) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        CompoundTag legacy = data.copyTag();
        boolean changed = false;
        for (String key : keys) {
            if (legacy.contains(key)) {
                legacy.remove(key);
                changed = true;
            }
        }
        if (changed) writeRemainingData(stack, legacy);
    }

    private static void writeRemainingData(ItemStack stack, CompoundTag remaining) {
        if (remaining.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(remaining));
        }
    }
}

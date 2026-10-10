package com.vomiter.sophtravelerspack.common.registry;

import com.mojang.serialization.Codec;
import com.vomiter.sophtravelerspack.STBackpack;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModDataComponents {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, STBackpack.MODID);

    public static final Supplier<DataComponentType<ResourceLocation>> TRAVELER_TYPE =
            COMPONENTS.register("traveler_type", () -> DataComponentType.<ResourceLocation>builder()
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC)
                    .build());

    // ItemContainerContents owns a snapshot of the mutable shadow stack.
    public static final Supplier<DataComponentType<ItemContainerContents>> TRAVELER_SHADOW =
            COMPONENTS.register("traveler_shadow", () -> DataComponentType.<ItemContainerContents>builder()
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                    .build());

    public static final Supplier<DataComponentType<Boolean>> SLEEPING_BAG_DEPLOYED =
            COMPONENTS.register("sleeping_bag_deployed", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    private ModDataComponents() {}

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}

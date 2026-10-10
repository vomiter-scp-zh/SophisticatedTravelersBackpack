package com.vomiter.sophtravelerspack.common.registry;

import com.vomiter.sophtravelerspack.STBackpack;
import com.vomiter.sophtravelerspack.traveler.TravelerType;
import com.vomiter.sophtravelerspack.traveler.TravelerTypeInstance;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class ModTravelerTypeRegistry {
    public static final ResourceLocation REGISTRY_ID =
            STBackpack.modLoc("traveler_type_instance");

    public static final ResourceKey<Registry<TravelerTypeInstance>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(REGISTRY_ID);

    public static final DeferredRegister<TravelerTypeInstance> DEFERRED_REGISTER =
            DeferredRegister.create(
                    REGISTRY_KEY,
                    STBackpack.MODID
            );

    private static final Registry<TravelerTypeInstance> TRAVELER_TYPES =
            DEFERRED_REGISTER.makeRegistry(builder -> builder.sync(true));

    public static final Supplier<Registry<TravelerTypeInstance>> REGISTRY = () -> TRAVELER_TYPES;

    private static final EnumMap<TravelerType, DeferredHolder<TravelerTypeInstance, TravelerTypeInstance>> MUTABLE_ENTRIES = new EnumMap<>(TravelerType.class);

    public static final Map<TravelerType, DeferredHolder<TravelerTypeInstance, TravelerTypeInstance>> ENTRIES;

    static {
        for (TravelerType travelerType : TravelerType.values()) {
            DeferredHolder<TravelerTypeInstance, TravelerTypeInstance> registryObject =
                    DEFERRED_REGISTER.register(
                            travelerType.getStringRepresentation(),
                            () -> new TravelerTypeInstance(
                                    travelerType.getStringRepresentation(),
                                    travelerType::createItemStack,
                                    ResourceLocation.fromNamespaceAndPath(
                                    "travelersbackpack",
                                    "block/backpack/" + travelerType.getStringRepresentation()
                            ))
                    );

            MUTABLE_ENTRIES.put(travelerType, registryObject);
        }

        ENTRIES = Collections.unmodifiableMap(MUTABLE_ENTRIES);
    }

    private ModTravelerTypeRegistry() {
    }

    public static void register(IEventBus modBus) {
        DEFERRED_REGISTER.register(modBus);
    }

    public static DeferredHolder<TravelerTypeInstance, TravelerTypeInstance> getRegistryObject(
            TravelerType travelerType
    ) {
        DeferredHolder<TravelerTypeInstance, TravelerTypeInstance> registryObject =
                MUTABLE_ENTRIES.get(travelerType);

        if (registryObject == null) {
            throw new IllegalArgumentException(
                    "Unregistered traveler type: " + travelerType
            );
        }

        return registryObject;
    }

    public static TravelerTypeInstance get(TravelerType travelerType) {
        return getRegistryObject(travelerType).get();
    }

    public static Optional<TravelerTypeInstance> get(ResourceLocation id) {
        return Optional.ofNullable(REGISTRY.get().get(id));
    }

    public static Optional<TravelerTypeInstance> findByStringRepresentation(
            String representation
    ) {
        return REGISTRY.get().stream()
                .filter(instance ->
                        instance.getStringRepresentation().equals(representation)
                )
                .findFirst();
    }
}
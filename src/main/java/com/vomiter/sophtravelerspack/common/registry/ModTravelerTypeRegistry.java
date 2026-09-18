package com.vomiter.sophtravelerspack.common.registry;

import com.vomiter.sophtravelerspack.STBackpack;
import com.vomiter.sophtravelerspack.traveler.TravelerType;
import com.vomiter.sophtravelerspack.traveler.TravelerTypeInstance;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

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

    public static final Supplier<IForgeRegistry<TravelerTypeInstance>> REGISTRY =
            DEFERRED_REGISTER.makeRegistry(RegistryBuilder::new);

    private static final EnumMap<TravelerType, RegistryObject<TravelerTypeInstance>> MUTABLE_ENTRIES = new EnumMap<>(TravelerType.class);

    public static final Map<TravelerType, RegistryObject<TravelerTypeInstance>> ENTRIES;

    static {
        for (TravelerType travelerType : TravelerType.values()) {
            RegistryObject<TravelerTypeInstance> registryObject =
                    DEFERRED_REGISTER.register(
                            travelerType.getStringRepresentation(),
                            () -> new TravelerTypeInstance(
                                    travelerType.getStringRepresentation(),
                                    travelerType::createItemStack
                            )
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

    public static RegistryObject<TravelerTypeInstance> getRegistryObject(
            TravelerType travelerType
    ) {
        RegistryObject<TravelerTypeInstance> registryObject =
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
        return Optional.ofNullable(REGISTRY.get().getValue(id));
    }

    public static Optional<TravelerTypeInstance> findByStringRepresentation(
            String representation
    ) {
        return REGISTRY.get()
                .getValues()
                .stream()
                .filter(instance ->
                        instance.getStringRepresentation().equals(representation)
                )
                .findFirst();
    }
}
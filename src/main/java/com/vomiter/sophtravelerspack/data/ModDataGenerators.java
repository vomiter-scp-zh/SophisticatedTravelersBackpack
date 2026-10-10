package com.vomiter.sophtravelerspack.data;

import com.vomiter.sophtravelerspack.STBackpack;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
        modid = STBackpack.MODID,
        bus = EventBusSubscriber.Bus.MOD
)
public final class ModDataGenerators {
    private ModDataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
                event.includeClient(),
                new ModBlockModelProvider(
                        event.getGenerator().getPackOutput(),
                        event.getExistingFileHelper()
                )
        );
    }
}
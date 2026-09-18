package com.vomiter.sophtravelerspack.data;

import com.vomiter.sophtravelerspack.STBackpack;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = STBackpack.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
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
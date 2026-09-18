package com.vomiter.sophtravelerspack;

import com.mojang.logging.LogUtils;
import com.vomiter.sophtravelerspack.client.ClientEventHandler;
import com.vomiter.sophtravelerspack.client.ClientUpgradeTabs;
import com.vomiter.sophtravelerspack.common.STBPCommand;
import com.vomiter.sophtravelerspack.common.registry.*;
import com.vomiter.sophtravelerspack.network.ModNetwork;
import com.vomiter.sophtravelerspack.traveler.EventHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(STBackpack.MODID)
public class STBackpack {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "sophtravelerspack";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static ResourceLocation modLoc(String path){return ResourceLocation.fromNamespaceAndPath(MODID, path);};

    public STBackpack() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModTravelerTypeRegistry.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModTabs.TABS.register(modEventBus);
        modEventBus.addListener(ModUpgradeContainers::register);
        modEventBus.addListener(ModNetwork::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(STBPCommand::register);
        EventHandler.init();

        if (FMLEnvironment.dist.isClient()){
            modEventBus.addListener(ClientUpgradeTabs::onClientSetup);
            MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::onClientTick);
            MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::onItemTooltip);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }
}

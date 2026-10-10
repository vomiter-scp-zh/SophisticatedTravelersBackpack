package com.vomiter.sophtravelerspack;

import com.mojang.logging.LogUtils;
import com.vomiter.sophtravelerspack.client.ClientEventHandler;
import com.vomiter.sophtravelerspack.client.ClientUpgradeTabs;
import com.vomiter.sophtravelerspack.client.SleepingBagScreenButton;
import com.vomiter.sophtravelerspack.common.STBPCommand;
import com.vomiter.sophtravelerspack.common.SleepingBagCommand;
import com.vomiter.sophtravelerspack.common.registry.*;
import com.vomiter.sophtravelerspack.network.ModNetwork;
import com.vomiter.sophtravelerspack.traveler.EventHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

// The value here should match the mod ID in META-INF/neoforge.mods.toml
@Mod(STBackpack.MODID)
public class STBackpack {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "sophtravelerspack";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static ResourceLocation modLoc(String path){return ResourceLocation.fromNamespaceAndPath(MODID, path);}

    public STBackpack(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModTravelerTypeRegistry.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModTabs.TABS.register(modEventBus);
        modEventBus.addListener(ModUpgradeContainers::register);
        modEventBus.addListener(ModNetwork::registerPayloads);
        NeoForge.EVENT_BUS.addListener(STBPCommand::register);
        NeoForge.EVENT_BUS.addListener(SleepingBagCommand::register);
        EventHandler.init();

        if (FMLEnvironment.dist.isClient()){
            modEventBus.addListener(ClientUpgradeTabs::onClientSetup);
            modEventBus.addListener(ClientEventHandler::onRegisterAdditionalModels);
            modEventBus.addListener(ClientEventHandler::onBakingCompleted);
            NeoForge.EVENT_BUS.addListener(ClientEventHandler::onClientTick);
            NeoForge.EVENT_BUS.addListener(ClientEventHandler::onItemTooltip);
            NeoForge.EVENT_BUS.addListener(SleepingBagScreenButton::onInit);
            NeoForge.EVENT_BUS.addListener(SleepingBagScreenButton::onRenderPre);
            NeoForge.EVENT_BUS.addListener(SleepingBagScreenButton::onRenderPost);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }
}

package com.vomiter.sophtravelerspack.client;

import com.tiviacz.travelersbackpack.client.screens.ToolsScreen;
import com.tiviacz.travelersbackpack.config.TravelersBackpackConfig;
import com.tiviacz.travelersbackpack.handlers.ModClientEventHandler;
import com.tiviacz.travelersbackpack.init.ModItems;
import com.vomiter.sophtravelerspack.traveler.TravelerType;
import com.vomiter.sophtravelerspack.traveler.TravelerTypeInstance;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;

import java.util.concurrent.atomic.AtomicReference;

public class ClientEventHandler {
    public static boolean shouldInverseTank = false;

    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (TravelerUtil.getShadow(stack).isEmpty()) return;

        Component travelerName = TravelerUtil.getShadow(stack).getItem().getDescription().copy().withStyle(ChatFormatting.DARK_RED);

        event.getToolTip().add(
                Component.translatable(
                        "tooltip.sophtravelerspack.traveler_type",
                        travelerName
                ).withStyle(ChatFormatting.GOLD)
        );
    }


    public static void onClientTick(TickEvent.ClientTickEvent event){
        shouldInverseTank = false;
        var mc = Minecraft.getInstance();
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!player.getMainHandItem().is(ModItems.HOSE.get())) return;
        AtomicReference<ItemStack> backpackRef = new AtomicReference<>(ItemStack.EMPTY);
        PlayerInventoryProvider.get().runOnBackpacks(
                player,
                (itemStack, handlerName, identifier, slot) -> {
                    if ("main".equals(handlerName) || "offhand".equals(handlerName)) {
                        return false;
                    }
                    backpackRef.set(itemStack);
                    return true;
                }
        );
        ItemStack backpack = backpackRef.get();
        if(backpack.isEmpty()) return;
        if (mc.gameMode == null) return;
        while(ModClientEventHandler.SWAP_TOOL.consumeClick()) {
            if (mc.screen == null && !mc.options.hideGui && mc.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                if (!(Boolean) TravelersBackpackConfig.SERVER.backpackSettings.allowToolSwapping.get() && mc.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() != ModItems.HOSE.get()) {
                    return;
                }
                shouldInverseTank = true;
                mc.setScreen(new ToolsScreen());
            }
        }


    }
}

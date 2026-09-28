package com.vomiter.sophtravelerspack.client;

import com.tiviacz.travelersbackpack.client.screens.ToolsScreen;
import com.tiviacz.travelersbackpack.common.BackpackAbilities;
import com.tiviacz.travelersbackpack.config.BackpackEffect;
import com.tiviacz.travelersbackpack.config.TravelersBackpackConfig;
import com.tiviacz.travelersbackpack.handlers.ModClientEventHandler;
import com.tiviacz.travelersbackpack.init.ModItems;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import com.tiviacz.travelersbackpack.util.KeyHelper;
import com.tiviacz.travelersbackpack.util.TextUtils;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
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
        ItemStack sbpStack = event.getItemStack();
        if (TravelerUtil.getShadow(sbpStack).isEmpty()) return;

        Component travelerName = TravelerUtil.getShadow(sbpStack).getItem().getDescription().copy().withStyle(ChatFormatting.DARK_RED);

        event.getToolTip().add(
                Component.translatable(
                        "tooltip.sophtravelerspack.traveler_type",
                        travelerName
                ).withStyle(ChatFormatting.GOLD)
        );

        var tooltipComponents = event.getToolTip();
        ItemStack stack = TravelerUtil.getShadow(sbpStack);
        if (!(stack.getItem() instanceof TravelersBackpackItem travelersBackpackItem)) return;
        if (BackpackAbilities.ALLOWED_ABILITIES.contains(stack.getItem()) && (Boolean)TravelersBackpackConfig.SERVER.backpackAbilities.enableBackpackAbilities.get()) {
            if (KeyHelper.isCtrlPressed()) {
                if (BackpackAbilities.CUSTOM_DESCRIPTIONS.contains(stack.getItem())) {
                    String var10001 = stack.getDescriptionId();
                    tooltipComponents.add(Component.translatable("ability.travelersbackpack." + var10001.replaceAll("block.travelersbackpack.", "")).withStyle(ChatFormatting.BLUE));
                }

                boolean whenEquippedPresent = false;
                if (BackpackAbilities.getBackpackEffects().containsKey(stack.getItem())) {
                    tooltipComponents.add(Component.translatable("ability.travelersbackpack.when_equipped").withStyle(ChatFormatting.DARK_PURPLE));
                    whenEquippedPresent = true;
                    BackpackAbilities.getBackpackEffects().entries().stream().filter((entry) -> entry.getKey() == stack.getItem()).forEach((entry) -> {
                        MutableComponent mutablecomponent = Component.literal("- ");
                        mutablecomponent.append(Component.translatable(((BackpackEffect)entry.getValue()).effect().getDescriptionId()));
                        MobEffect mobeffect = ((BackpackEffect)entry.getValue()).effect();
                        if (((BackpackEffect)entry.getValue()).amplifier() > 0) {
                            mutablecomponent = Component.translatable("potion.withAmplifier", new Object[]{mutablecomponent, Component.translatable("potion.potency." + ((BackpackEffect)entry.getValue()).amplifier())});
                        }

                        if (BackpackAbilities.getCooldowns().containsKey(stack.getItem())) {
                            mutablecomponent.append(" " + TextUtils.getConvertedTime(((BackpackEffect)entry.getValue()).minDuration()));
                        }

                        tooltipComponents.add(mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting()));
                    });
                }

                travelersBackpackItem.addAttributeModifierTooltip(stack, tooltipComponents, whenEquippedPresent);
                if (BackpackAbilities.isOnList(BackpackAbilities.BLOCK_ABILITIES_LIST, stack) && BackpackAbilities.isOnList(BackpackAbilities.ITEM_ABILITIES_LIST, stack)) {
                    tooltipComponents.add(Component.translatable("ability.travelersbackpack.item_and_block"));
                } else if (BackpackAbilities.isOnList(BackpackAbilities.BLOCK_ABILITIES_LIST, stack) && !BackpackAbilities.isOnList(BackpackAbilities.ITEM_ABILITIES_LIST, stack)) {
                    tooltipComponents.add(Component.translatable("ability.travelersbackpack.block"));
                } else if (BackpackAbilities.isOnList(BackpackAbilities.ITEM_ABILITIES_LIST, stack) && !BackpackAbilities.isOnList(BackpackAbilities.BLOCK_ABILITIES_LIST, stack)) {
                    tooltipComponents.add(Component.translatable("ability.travelersbackpack.item"));
                }
            } else {
                tooltipComponents.add(Component.translatable("ability.sophtravelerspack.hold_ctrl").withStyle(ChatFormatting.BLUE));
            }
        }
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

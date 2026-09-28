package com.vomiter.sophtravelerspack.client;

import com.vomiter.sophtravelerspack.network.ModNetwork;
import com.vomiter.sophtravelerspack.util.SleepingBagUtils;
import com.tiviacz.travelersbackpack.init.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ScreenEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.Button;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import java.util.Map;
import java.util.List;
import java.util.WeakHashMap;

public final class SleepingBagScreenButton {
    private static final Map<BackpackScreen, Button> BUTTONS = new WeakHashMap<>();
    private static final ButtonDefinition DEFINITION = new ButtonDefinition(Dimension.SQUARE_12,
            GuiHelper.SMALL_BUTTON_BACKGROUND, GuiHelper.SMALL_BUTTON_HOVERED_BACKGROUND, null,
            Component.translatable("gui.sophtravelerspack.sleeping_bag"));

    public static void onInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof BackpackScreen screen)) return;
        Button button = new Button(new Position(0, 0), DEFINITION, mouseButton -> {
            if (mouseButton != 0) return;
            var menu = screen.getMenu();
            if (!menu.isFirstLevelStorage()) return;
            menu.getBlockPosition().ifPresentOrElse(
                    pos -> ModNetwork.CHANNEL.sendToServer(new ModNetwork.InWorldSBPSleepingBagRequest(menu.containerId, pos)),
                    () -> ModNetwork.CHANNEL.sendToServer(new ModNetwork.EuipSBPSleepingBagRequest(menu.containerId))
            );
        }) {
            @Override
            protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                graphics.pose().pushPose();
                graphics.pose().translate(x + 1, y + 1, 0);
                graphics.pose().scale(0.625F, 0.625F, 1);
                graphics.renderItem(new ItemStack(ModItems.RED_SLEEPING_BAG.get()), 0, 0);
                graphics.pose().popPose();
            }
        };
        BUTTONS.put(screen, button);
        event.addListener(button);
        update(screen, button);
    }

    public static void onRenderPre(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof BackpackScreen screen) {
            Button button = BUTTONS.get(screen);
            if (button != null) update(screen, button);
        }
    }

    public static void onRenderPost(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof BackpackScreen screen) {
            Button button = BUTTONS.get(screen);
            if (button != null) button.renderTooltip(screen, event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
        }
    }

    private static void update(BackpackScreen screen, Button button) {
        var menu = screen.getMenu();
        var type = menu.getBackpackContext().getType();
        boolean visible = menu.isFirstLevelStorage()
                && (type == BackpackContext.ContextType.ITEM_BACKPACK || type == BackpackContext.ContextType.BLOCK_BACKPACK) &&
                (SleepingBagUtils.isSleepingBagDeployed(menu.getStorageWrapper().getBackpack())
                        || !SleepingBagUtils.getSleepingBag(menu.getStorageWrapper(), true).isEmpty());
        button.setVisible(visible);
        String action = type == BackpackContext.ContextType.ITEM_BACKPACK ? "use"
                : SleepingBagUtils.isSleepingBagDeployed(menu.getStorageWrapper().getBackpack()) ? "recover" : "deploy";
        button.setTooltip(List.of(Component.translatable("gui.sophtravelerspack.sleeping_bag." + action)));
        screen.getTransferToInventoryButtonPosition().ifPresent(position ->
                button.setPosition(new Position(position.x() - 24, position.y())));
    }
}

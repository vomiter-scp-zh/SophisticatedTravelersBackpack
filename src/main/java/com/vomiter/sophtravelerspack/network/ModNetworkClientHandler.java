package com.vomiter.sophtravelerspack.network;

import net.minecraft.client.Minecraft;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;

public class ModNetworkClientHandler {
    static void handleMBPSyncResponse(ModNetwork.MBPSyncResponse response) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, inventoryName, identifier, slot) -> {
            var wrapper = BackpackWrapper.fromStack(backpack);
            if (wrapper.getContentsUuid().filter(response.uuid()::equals).isPresent()) {
                wrapper.onContentsNbtUpdated();
            }
            return false;
        });
        if (player.containerMenu instanceof BackpackContainer menu
                && menu.getStorageWrapper().getContentsUuid().filter(response.uuid()::equals).isPresent()) {
            menu.getStorageWrapper().onContentsNbtUpdated();
        }
    }
}

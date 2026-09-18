package com.vomiter.sophtravelerspack.network;

import net.minecraft.client.Minecraft;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;

public class ModNetworkClientHandler {
    static void handleMBPSyncResponse(ModNetwork.MBPSyncResponse response){
        assert Minecraft.getInstance().player != null;
        PlayerInventoryProvider.get().runOnBackpacks(
                Minecraft.getInstance().player,
                ((backpack, s, s1, i) -> {
                    backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance()).ifPresent(
                            IStorageWrapper::onContentsNbtUpdated
                    );
                    return true;
        }));
    }

}

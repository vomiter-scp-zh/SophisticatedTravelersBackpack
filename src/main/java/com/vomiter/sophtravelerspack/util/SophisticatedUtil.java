package com.vomiter.sophtravelerspack.util;

import com.vomiter.sophtravelerspack.network.ModNetwork;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;

import java.util.concurrent.atomic.AtomicReference;

public class SophisticatedUtil {
    private SophisticatedUtil(){}

    public static long lastRequestTime = -1;

    public static ItemStack getSophBackpackOnBack(Player player){
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
        return backpackRef.get();
    }

    public static void runOnBackpack(Player player, BackpackRunnable runnable){
        PlayerInventoryProvider.get()
                .runOnBackpacks(
                        player,
                        (backpack,
                         inventoryName,
                         identifier,
                         backpackSlot
                        ) -> {
                            return backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance())
                                    .map(
                                            runnable::run
                                    )
                                    .orElse(false);

                        });
    }

    public static void requestBackpackSync(Player player) {
        if (!player.level().isClientSide) {
            return;
        }

        long now = Util.getMillis();

        if (lastRequestTime >= 0 && now - lastRequestTime < 1000L) {
            return;
        }

        lastRequestTime = now;
        runOnBackpack(player, (backpackWrapper -> {
            backpackWrapper.getContentsUuid().ifPresent(uuid -> ModNetwork.CHANNEL.sendToServer(new ModNetwork.MBPSyncRequest(uuid)));
            return false;
        }));
    }

    @FunctionalInterface
    public interface BackpackRunnable {
        boolean run(IBackpackWrapper backpackWrapper);
    }
}

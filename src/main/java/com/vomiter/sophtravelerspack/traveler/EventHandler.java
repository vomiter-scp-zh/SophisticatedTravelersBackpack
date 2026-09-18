package com.vomiter.sophtravelerspack.traveler;

import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.vomiter.sophtravelerspack.STBackpack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

public class EventHandler {
    public static void init(){
        var bus = MinecraftForge.EVENT_BUS;
        bus.addListener(EventHandler::playerTick);
    }

    public static void playerTick(final TickEvent.PlayerTickEvent event) {
        if(event.phase != TickEvent.Phase.END) return;
        if(!TravelerUtil.getShadow(event.player).isEmpty()) {
            BackpackWrapper.tick(TravelerUtil.getShadow(event.player), event.player, true);
            TravelerUtil.saveShadow(event.player);
        }

    }
}

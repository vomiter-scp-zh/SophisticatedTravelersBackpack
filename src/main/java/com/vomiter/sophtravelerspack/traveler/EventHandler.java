package com.vomiter.sophtravelerspack.traveler;

import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.vomiter.sophtravelerspack.STBackpack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class EventHandler {
    public static void init(){
        var bus = NeoForge.EVENT_BUS;
        bus.addListener(EventHandler::playerTick);
    }

    public static void playerTick(final PlayerTickEvent.Post event) {
        if(!TravelerUtil.getShadow(event.getEntity()).isEmpty()) {
            BackpackWrapper.tick(TravelerUtil.getShadow(event.getEntity()), event.getEntity(), true);
            TravelerUtil.saveShadow(event.getEntity());
        }

    }
}

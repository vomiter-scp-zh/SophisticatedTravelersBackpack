package com.vomiter.sophtravelerspack.common.registry;

import com.vomiter.sophtravelerspack.STBackpack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;

public class ModItems {
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM.key(), STBackpack.MODID);

}

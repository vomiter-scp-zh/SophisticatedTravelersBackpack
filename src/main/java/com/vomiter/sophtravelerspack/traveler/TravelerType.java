package com.vomiter.sophtravelerspack.traveler;

import com.tiviacz.travelersbackpack.TravelersBackpack;
import com.tiviacz.travelersbackpack.init.ModItems;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public enum TravelerType {
    NETHERITE,
    DIAMOND,
    GOLD,
    EMERALD,
    IRON,
    LAPIS,
    REDSTONE,
    COAL,
    QUARTZ,
    BOOKSHELF,
    END,
    NETHER,
    SANDSTONE,
    SNOW,
    SPONGE,
    CAKE,
    CACTUS,
    HAY,
    MELON,
    PUMPKIN,
    CREEPER,
    DRAGON,
    ENDERMAN,
    BLAZE,
    GHAST,
    MAGMA_CUBE,
    SKELETON,
    SPIDER,
    WITHER,
    WARDEN,
    BAT,
    BEE,
    WOLF,
    FOX,
    OCELOT,
    HORSE,
    COW,
    PIG,
    SHEEP,
    CHICKEN,
    SQUID,
    VILLAGER,
    IRON_GOLEM;

    public String getStringRepresentation() {
        return name().toLowerCase(Locale.ROOT);
    }

    public Item getItem() {
        return BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(
                        TravelersBackpack.MODID,
                        getStringRepresentation()
                )
        );
    }

    public ItemStack createItemStack() {
        ItemStack stack = getItem().getDefaultInstance();
        if (this == CACTUS) {
            BackpackWrapper wrapper = BackpackWrapper.fromStack(stack);
            wrapper.upgrades.setStackInSlot(
                    0,
                    ModItems.TANKS_UPGRADE.get().getDefaultInstance()
            );
        }
        return stack;
    }

    public static TravelerTypeInstance getTravelerTypeFromBackpack(ItemStack backpack) {
        return TravelerUtil.get(backpack).orElse(null);
    }
}
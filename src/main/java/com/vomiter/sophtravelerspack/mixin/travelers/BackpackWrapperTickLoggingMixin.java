package com.vomiter.sophtravelerspack.mixin.travelers;

import com.mojang.logging.LogUtils;
import com.tiviacz.travelersbackpack.TravelersBackpack;
import com.tiviacz.travelersbackpack.capability.CapabilityUtils;
import com.tiviacz.travelersbackpack.common.BackpackAbilities;
import com.tiviacz.travelersbackpack.config.TravelersBackpackConfig;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.upgrades.IEnable;
import com.tiviacz.travelersbackpack.inventory.upgrades.ITickableUpgrade;
import com.tiviacz.travelersbackpack.util.NbtHelper;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = BackpackWrapper.class, remap = false)
public abstract class BackpackWrapperTickLoggingMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * @author Vomiter
     * @reason Temporarily trace every branch that prevents BackpackWrapper.tick
     * from reaching ability or upgrade ticking.
     */
    @Overwrite
    public static void tick(ItemStack stack, Player player, boolean integration) {
        long gameTime = player.level().getGameTime();

        LOGGER.info(
                "[BackpackTick] ENTER player={} side={} gameTime={} integration={} stack={}",
                player.getScoreboardName(),
                player.level().isClientSide ? "CLIENT" : "SERVER",
                gameTime,
                integration,
                stack
        );

        boolean integrationEnabled = TravelersBackpack.enableIntegration();

        if (!integration && integrationEnabled) {
            LOGGER.warn(
                    "[BackpackTick] STOP at integration gate: " +
                            "integration=false and enableIntegration=true"
            );
            return;
        }

        if (!player.isAlive()) {
            LOGGER.warn("[BackpackTick] STOP: player is not alive");
            return;
        }

        boolean wearingBackpack = CapabilityUtils.isWearingBackpack(player) || !TravelerUtil.getShadow(player).isEmpty();
        if (!wearingBackpack) {
            LOGGER.warn(
                    "[BackpackTick] STOP: CapabilityUtils.isWearingBackpack(player)=false"
            );
            return;
        }

        int ticks = (int) gameTime;
        ItemStack wornBackpack = CapabilityUtils.getWearingBackpack(player);

        boolean onAbilityList = BackpackAbilities.isOnList(
                BackpackAbilities.ITEM_ABILITIES_LIST,
                wornBackpack
        );

        LOGGER.info(
                "[BackpackTick] wornBackpack={} suppliedStack={} sameItem={} onAbilityList={}",
                wornBackpack,
                stack,
                ItemStack.isSameItemSameTags(wornBackpack, stack),
                onAbilityList
        );

        if (onAbilityList) {
            boolean enabledInConfig =
                    BackpackAbilities.isAbilityEnabledInConfig(stack);

            if (!enabledInConfig) {
                LOGGER.warn(
                        "[BackpackTick] SKIP ability section: " +
                                "isAbilityEnabledInConfig(stack)=false"
                );
            } else {
                boolean forceEnabled =
                        TravelersBackpackConfig.SERVER.backpackAbilities
                                .forceAbilityEnabled.get();

                boolean abilityEnabled = NbtHelper.getOrDefault(
                        stack,
                        "AbilityEnabled",
                        forceEnabled
                );

                int cooldown = NbtHelper.getOrDefault(stack, "Cooldown", 0);

                LOGGER.info(
                        "[BackpackTick] abilityEnabled={} forceAbilityEnabled={} cooldown={}",
                        abilityEnabled,
                        forceEnabled,
                        cooldown
                );

                if (abilityEnabled) {
                    boolean decreaseCooldown =
                            BackpackAbilities.ABILITIES.abilityTick(stack, player);

                    LOGGER.info(
                            "[BackpackTick] abilityTick completed: decreaseCooldown={}",
                            decreaseCooldown
                    );

                    if (cooldown <= 0) {
                        LOGGER.info(
                                "[BackpackTick] SKIP enabled cooldown update: cooldown <= 0"
                        );
                    } else if (ticks % 100 != 0) {
                        LOGGER.info(
                                "[BackpackTick] SKIP enabled cooldown update: ticks % 100 = {}",
                                ticks % 100
                        );
                    } else if (!decreaseCooldown) {
                        LOGGER.info(
                                "[BackpackTick] SKIP enabled cooldown update: " +
                                        "abilityTick returned false"
                        );
                    } else {
                        BackpackWrapper wrapper =
                                CapabilityUtils.getBackpackWrapper(
                                        player,
                                        stack,
                                        CapabilityUtils.NO_ITEMS.get()
                                );

                        int wrapperCooldown = wrapper.getCooldown();

                        if (player.level().isClientSide) {
                            LOGGER.warn(
                                    "[BackpackTick] EARLY RETURN #1: " +
                                            "client side during enabled cooldown update; " +
                                            "wrapperCooldown={}",
                                    wrapperCooldown
                            );
                            return;
                        }

                        int newCooldown = Math.max(0, wrapperCooldown - 100);

                        LOGGER.info(
                                "[BackpackTick] updating enabled cooldown: {} -> {}",
                                wrapperCooldown,
                                newCooldown
                        );

                        wrapper.setCooldown(newCooldown);
                    }
                } else {
                    if (cooldown <= 0) {
                        LOGGER.info(
                                "[BackpackTick] SKIP disabled cooldown update: cooldown <= 0"
                        );
                    } else if (ticks % 100 != 0) {
                        LOGGER.info(
                                "[BackpackTick] SKIP disabled cooldown update: ticks % 100 = {}",
                                ticks % 100
                        );
                    } else {
                        BackpackWrapper wrapper =
                                CapabilityUtils.getBackpackWrapper(
                                        player,
                                        stack,
                                        CapabilityUtils.NO_ITEMS.get()
                                );

                        int wrapperCooldown = wrapper.getCooldown();

                        if (player.level().isClientSide) {
                            LOGGER.warn(
                                    "[BackpackTick] EARLY RETURN #2: " +
                                            "client side during disabled cooldown update; " +
                                            "wrapperCooldown={}",
                                    wrapperCooldown
                            );
                            return;
                        }

                        int newCooldown = Math.max(0, wrapperCooldown - 100);

                        LOGGER.info(
                                "[BackpackTick] updating disabled cooldown: {} -> {}",
                                wrapperCooldown,
                                newCooldown
                        );

                        wrapper.setCooldown(newCooldown);
                    }
                }
            }
        } else {
            boolean abilityEnabled = NbtHelper.getOrDefault(
                    stack,
                    "AbilityEnabled",
                    false
            );

            if (abilityEnabled) {
                LOGGER.warn(
                        "[BackpackTick] backpack not on ability list; " +
                                "setting AbilityEnabled=false"
                );
                NbtHelper.set(stack, "AbilityEnabled", false);
            } else {
                LOGGER.info(
                        "[BackpackTick] backpack not on ability list and ability already disabled"
                );
            }
        }

        if (!NbtHelper.has(stack, "UpgradeTickInterval")) {
            LOGGER.warn(
                    "[BackpackTick] END before upgrade ticking: " +
                            "stack has no UpgradeTickInterval"
            );
            return;
        }

        int upgradeTicks = NbtHelper.get(stack, "UpgradeTickInterval");

        LOGGER.info(
                "[BackpackTick] UpgradeTickInterval={} ticks={} remainder={}",
                upgradeTicks,
                ticks,
                upgradeTicks == 0 ? "undefined" : ticks % upgradeTicks
        );

        if (upgradeTicks == 0) {
            LOGGER.warn(
                    "[BackpackTick] EARLY RETURN #3: UpgradeTickInterval=0"
            );
            return;
        }

        if (ticks % upgradeTicks != 0) {
            LOGGER.info(
                    "[BackpackTick] END without upgrade tick: " +
                            "ticks % UpgradeTickInterval = {}",
                    ticks % upgradeTicks
            );
            return;
        }

        BackpackWrapper wrapper = CapabilityUtils.getBackpackWrapper(
                player,
                stack,
                CapabilityUtils.UPGRADES_ONLY.get()
        );

        LOGGER.info(
                "[BackpackTick] starting upgrade iteration; upgradeCount={}",
                wrapper.getUpgradeManager().upgrades.size()
        );

        wrapper.getUpgradeManager().upgrades.forEach(upgradeBase -> {
            LOGGER.info(
                    "[BackpackTick] inspecting upgrade: class={}",
                    upgradeBase.getClass().getName()
            );

            if (!(upgradeBase instanceof ITickableUpgrade tickable)) {
                LOGGER.info(
                        "[BackpackTick] SKIP upgrade: does not implement ITickableUpgrade"
                );
                return;
            }

            boolean shouldTick = true;

            if (upgradeBase instanceof IEnable enable) {
                shouldTick = enable.isEnabled(upgradeBase);

                LOGGER.info(
                        "[BackpackTick] IEnable result: enabled={}",
                        shouldTick
                );
            }

            if (!shouldTick) {
                LOGGER.warn(
                        "[BackpackTick] SKIP tickable upgrade: upgrade is disabled"
                );
                return;
            }

            LOGGER.info(
                    "[BackpackTick] CALL upgrade tick: class={}",
                    upgradeBase.getClass().getName()
            );

            tickable.tick(
                    player,
                    player.level(),
                    player.blockPosition(),
                    ticks
            );

            LOGGER.info(
                    "[BackpackTick] FINISHED upgrade tick: class={}",
                    upgradeBase.getClass().getName()
            );
        });

        LOGGER.info("[BackpackTick] EXIT normally");
    }
}
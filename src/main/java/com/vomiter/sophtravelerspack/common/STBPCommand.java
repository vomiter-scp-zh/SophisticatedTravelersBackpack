package com.vomiter.sophtravelerspack.common;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.tiviacz.travelersbackpack.TravelersBackpack;
import com.vomiter.sophtravelerspack.common.registry.ModTravelerTypeRegistry;
import com.vomiter.sophtravelerspack.traveler.TravelerType;
import com.vomiter.sophtravelerspack.traveler.TravelerTypeInstance;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;

import java.util.Arrays;
import java.util.Locale;

public final class STBPCommand {
    private static final DynamicCommandExceptionType INVALID_CAMOUFLAGE =
            new DynamicCommandExceptionType(value ->
                    Component.literal("Unknown camouflage type: " + value));

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("stbp")
                        .then(
                                Commands.literal("get")
                                        .executes(context ->
                                                getTravelerType(
                                                        context.getSource()
                                                                .getPlayerOrException()
                                                )
                                        )
                        )
                        .then(
                                Commands.literal("remove")
                                        .executes(context ->
                                                removeTravelerType(
                                                        context.getSource()
                                                                .getPlayerOrException()
                                                )
                                        )
                        )
                        .then(
                                Commands.literal("set")
                                        .requires(source -> source.hasPermission(2))
                                        .then(
                                                Commands.argument("camouflage", StringArgumentType.word())
                                                        .suggests((context, builder) ->
                                                                SharedSuggestionProvider.suggest(
                                                                        Arrays.stream(TravelerType.values())
                                                                                .map(camouflage ->
                                                                                        camouflage.name()
                                                                                                .toLowerCase(Locale.ROOT)
                                                                                ),
                                                                        builder
                                                                )
                                                        )
                                                        .executes(context ->
                                                                setTravelerType(
                                                                        context.getSource().getPlayerOrException(),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "camouflage"
                                                                        )
                                                                )
                                                        )
                                        )
                        )
        );
    }

    private static int setTravelerType(
            ServerPlayer player,
            String camouflageName
    ) throws CommandSyntaxException {
        ItemStack stack = getHeldBackpack(player);

        TravelerType travelerType;

        try {
            travelerType = TravelerType.valueOf(camouflageName.toUpperCase(Locale.ROOT));
            TravelerUtil.setShadow(stack, travelerType.createItemStack());

        } catch (IllegalArgumentException exception) {
            throw INVALID_CAMOUFLAGE.create(camouflageName);
        }

        TravelerUtil.set(stack, ModTravelerTypeRegistry.get(travelerType));
        player.getInventory().setChanged();

        player.sendSystemMessage(
                Component.literal(
                        "TravelerType Type = " + travelerType.name()
                )
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int getTravelerType(ServerPlayer player)
            throws CommandSyntaxException {
        ItemStack stack = getHeldBackpack(player);

        String camouflageName = TravelerUtil.get(stack)
                .map(TravelerTypeInstance::getStringRepresentation)
                .orElse("NONE");

        player.sendSystemMessage(
                Component.literal(
                        "TravelerType Type = " + camouflageName
                )
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int removeTravelerType(ServerPlayer player)
            throws CommandSyntaxException {
        ItemStack stack = getHeldBackpack(player);

        boolean removed = TravelerUtil.clear(stack);
        player.getInventory().setChanged();

        if (removed) {
            player.sendSystemMessage(
                    Component.literal("TravelerType removed")
            );
        } else {
            player.sendSystemMessage(
                    Component.literal("TravelerType Type = NONE")
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static ItemStack getHeldBackpack(ServerPlayer player)
            throws CommandSyntaxException {
        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof BackpackItem)) {
            throw NOT_HOLDING_BACKPACK;
        }

        return stack;
    }

    private static final CommandSyntaxException NOT_HOLDING_BACKPACK =
            new CommandSyntaxException(
                    CommandSyntaxException.BUILT_IN_EXCEPTIONS
                            .dispatcherUnknownArgument(),
                    Component.literal(
                            "You must hold a Sophisticated Backpacks backpack"
                    )
            );
}
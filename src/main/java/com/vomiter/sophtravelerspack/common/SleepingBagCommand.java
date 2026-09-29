package com.vomiter.sophtravelerspack.common;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.vomiter.sophtravelerspack.util.SleepingBagUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class SleepingBagCommand {
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("sbp_sleeping_bag")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("deploy")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> execute(context, true))))
                        .then(Commands.literal("recover")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> execute(context, false))))
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context, boolean deploy)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        boolean success = deploy
                ? SleepingBagUtils.deploySleepingBagFromSBP(source.getLevel(), pos)
                : SleepingBagUtils.recoverSleepingBagIntoSBP(source.getLevel(), pos);

        if (success) {
            source.sendSuccess(() -> Component.literal(
                    (deploy ? "Deployed" : "Recovered") + " sleeping bag at " + pos.toShortString()
            ), true);
            return Command.SINGLE_SUCCESS;
        }

        source.sendFailure(Component.literal(
                "Could not " + (deploy ? "deploy" : "recover")
                        + " sleeping bag at " + pos.toShortString()
        ));
        return 0;
    }
}

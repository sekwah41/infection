package com.sekwah.infection.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.sekwah.infection.InfectionMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static net.minecraft.ChatFormatting.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class InfectionCommand {

    private static final String FUNCTION_TRIGGER = "functionTrigger";
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> infection = literal("infection").requires((sender) -> sender.hasPermission(2))
                .executes(ctx -> {
            ctx.getSource().sendSuccess(Component.literal("Hey, this is the infection command :D"), true);
            return Command.SINGLE_SUCCESS;
        })
        .then(Commands.literal("hardreset").executes(InfectionCommand::hardreset))
        .then(Commands.literal("hardreset").executes(InfectionCommand::hardreset))
        .then(Commands.literal("shownames").executes(InfectionCommand::shownames))
        .then(Commands.literal("hidenames").executes(InfectionCommand::hidenames))
        .then(Commands.literal("infect").executes(InfectionCommand::infect))
        .then(Commands.literal("lock").executes(InfectionCommand::lock))
        .then(Commands.literal("unlock").executes(InfectionCommand::unlock))
        .then(Commands.literal("reload").executes(InfectionCommand::reload))
        .then(Commands.literal("stop").executes(InfectionCommand::stop))
        .then(Commands.literal("setup").executes(InfectionCommand::setup))
        .then(Commands.literal("start").executes(InfectionCommand::start))
        .then(Commands.literal("upgrade").executes(InfectionCommand::upgrade))
        .then(Commands.literal("config")
                .then(Commands.literal("compass").then(Commands.argument("number", IntegerArgumentType.integer(0)).executes(InfectionCommand::configCompass)))
        );

        dispatcher.register(infection);
    }

    private static int configCompass(CommandContext<CommandSourceStack> ctx) {
        var accuracy = IntegerArgumentType.getInteger(ctx, "number");
        sendInfectionMessage(ctx, Component.literal("Setting compass accuracy to: " + accuracy + " blocks").withStyle(GREEN));
        var config = InfectionMod.infectionController.configController;
        config.getConfig().compassAccuracy = accuracy;
        config.saveConfig();
        return Command.SINGLE_SUCCESS;
    }

    public static int hardreset(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Hard resetting game").withStyle(GREEN));
        InfectionMod.infectionController.hardReset();
        return Command.SINGLE_SUCCESS;
    }

    public static int shownames(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Showing speedrunner names").withStyle(GREEN));
        InfectionMod.infectionController.setRunnerNamesVisible(true);
        return Command.SINGLE_SUCCESS;
    }

    public static int reload(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Reloading config").withStyle(GREEN));
        InfectionMod.infectionController.configController.loadConfig();
        return Command.SINGLE_SUCCESS;
    }


    public static int lock(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Locking in the players").withStyle(GREEN));
        InfectionMod.infectionController.lock();
        return Command.SINGLE_SUCCESS;
    }


    public static int unlock(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Unlocking the players").withStyle(RED));
        InfectionMod.infectionController.unlock();
        return Command.SINGLE_SUCCESS;
    }

    public static int hidenames(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Hiding speedrunner names").withStyle(RED));
        InfectionMod.infectionController.setRunnerNamesVisible(false);
        return Command.SINGLE_SUCCESS;
    }

    public static int stop(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Countdown cancelled!").withStyle(RED));
        return Command.SINGLE_SUCCESS;
    }

    public static int start(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Infection countdown started!").withStyle(GREEN));
        InfectionMod.infectionController.startCountdown();
        return Command.SINGLE_SUCCESS;
    }

    public static int setup(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Preparing the event.").withStyle(GREEN));
        InfectionMod.infectionController.setup();
        return Command.SINGLE_SUCCESS;
    }

    public static int infect(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("A player will be infected.").withStyle(GREEN));
        try {
            InfectionMod.infectionController.infectPlayer(ctx.getSource().getPlayerOrException());
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int upgrade(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Upgrading the infection equipment!").withStyle(GREEN));
        InfectionMod.infectionController.inventoryController.upgrade(ctx.getSource().getServer());
        return Command.SINGLE_SUCCESS;
    }


    public static MutableComponent text(String text) {
        return Component.literal(text);
    }

    public static void sendInfectionMessage(CommandContext<CommandSourceStack> ctx, MutableComponent text) {
        ctx.getSource().sendSuccess(text("").append(text("[").withStyle(GOLD))
                        .append(text("Infection").withStyle(AQUA)).append(
                text("]").withStyle(GOLD).append(text(" ")))
                        .append(text)
                , false);
    }
}

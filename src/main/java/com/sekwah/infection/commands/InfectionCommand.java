package com.sekwah.infection.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.sekwah.infection.InfectionMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static net.minecraft.ChatFormatting.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class InfectionCommand {

    private static final String[] SUB_COMMANDS = new String[]{"infect", "setup", "start", "shownames", "hidenames", "reload", "lock", "unlock"};

    private static SuggestionProvider<CommandSourceStack> SUB_COMMAND_SUGGESTIONS = (ctx, builder)
            -> SharedSuggestionProvider.suggest(SUB_COMMANDS, builder);

    private static final String FUNCTION_TRIGGER = "functionTrigger";
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> infection = literal("infection").requires((sender) -> sender.hasPermission(2))
                .executes(ctx -> {
            ctx.getSource().sendSuccess(Component.literal("Hey, this is the infection command :D"), true);
            return Command.SINGLE_SUCCESS;
        }).then(argument(FUNCTION_TRIGGER, StringArgumentType.word()).suggests(SUB_COMMAND_SUGGESTIONS).executes(ctx -> {
                    String subCommand = StringArgumentType.getString(ctx, FUNCTION_TRIGGER);
                    switch (subCommand) {
                        case "hardreset" -> hardreset(ctx);
                        case "shownames" -> shownames(ctx);
                        case "hidenames" -> hidenames(ctx);
                        case "infect" -> infect(ctx);
                        case "lock" -> lock(ctx);
                        case "unlock" -> unlock(ctx);
                        case "reload" -> reload(ctx);
                        case "stop" -> stop(ctx);
                        case "setup" -> setup(ctx);
                        case "start" -> start(ctx);
                        case "upgrade" -> upgrade(ctx);
                        default -> unrecognised(ctx, subCommand);
                    }
                    return Command.SINGLE_SUCCESS;
                })
        );

        dispatcher.register(infection);
    }

    public static void hardreset(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Hard resetting game").withStyle(GREEN));
        InfectionMod.infectionController.hardReset();
    }

    public static void shownames(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Showing speedrunner names").withStyle(GREEN));
        InfectionMod.infectionController.setRunnerNamesVisible(true);
    }

    public static void reload(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Reloading config").withStyle(GREEN));
        InfectionMod.infectionController.configController.loadConfig();
    }


    public static void lock(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Locking in the players").withStyle(GREEN));
        InfectionMod.infectionController.lock();
    }


    public static void unlock(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Unlocking the players").withStyle(RED));
        InfectionMod.infectionController.unlock();
    }

    public static void hidenames(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Hiding speedrunner names").withStyle(RED));
        InfectionMod.infectionController.setRunnerNamesVisible(false);
    }

    public static void stop(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Countdown cancelled!").withStyle(RED));
    }

    public static void start(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Infection countdown started!").withStyle(GREEN));
        InfectionMod.infectionController.startCountdown();
    }

    public static void setup(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Preparing the event.").withStyle(GREEN));
        InfectionMod.infectionController.setup();
    }

    public static void infect(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("A player will be infected.").withStyle(GREEN));
        try {
            InfectionMod.infectionController.infectPlayer(ctx.getSource().getPlayerOrException());
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void upgrade(CommandContext<CommandSourceStack> ctx) {
        sendInfectionMessage(ctx, Component.literal("Upgrading the infection equipment!").withStyle(GREEN));
        InfectionMod.infectionController.inventoryController.upgrade();
    }


    public static MutableComponent text(String text) {
        return Component.literal(text);
    }

    public static void unrecognised(CommandContext<CommandSourceStack> ctx, String subCommand) {
        sendInfectionMessage(ctx, Component.literal("Did not recognise subcommand: " + subCommand).withStyle(RED));
    }

    public static void sendInfectionMessage(CommandContext<CommandSourceStack> ctx, MutableComponent text) {
        ctx.getSource().sendSuccess(text("").append(text("[").withStyle(GOLD))
                        .append(text("Infection").withStyle(AQUA)).append(
                text("]").withStyle(GOLD).append(text(" ")))
                        .append(text)
                , false);
    }
}

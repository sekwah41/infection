package com.sekwah.infection.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;

import java.time.format.TextStyle;

import static net.minecraft.ChatFormatting.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class InfectionCommand {

    private static String[] SUB_COMMANDS = new String[]{"start", "stop", "reset", "infect"};

    private static SuggestionProvider<CommandSourceStack> SUB_COMMAND_SUGGESTIONS = (ctx, builder)
            -> SharedSuggestionProvider.suggest(SUB_COMMANDS, builder);

    private static final String FUNCTION_TRIGGER = "functionTrigger";
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> infection = literal("infection").requires((sender) -> sender.hasPermission(2))
                .executes(ctx -> {
            ctx.getSource().sendSuccess(new TextComponent("Hey, this is the infection command :D"), true);
            return Command.SINGLE_SUCCESS;
        }).then(argument(FUNCTION_TRIGGER, StringArgumentType.word()).suggests(SUB_COMMAND_SUGGESTIONS).executes(ctx -> {
                    String subCommand = StringArgumentType.getString(ctx, FUNCTION_TRIGGER);
                    switch (subCommand) {
                        default -> unrecognised(ctx, subCommand);
                    }
                    return Command.SINGLE_SUCCESS;
                })
        );

        dispatcher.register(infection);
    }

    public static TextComponent text(String text) {
        return new TextComponent(text);
    }

    public static void unrecognised(CommandContext<CommandSourceStack> ctx, String subCommand) {
        sendInfectionMessage(ctx, new TextComponent("Did not recognise subcommand: " + subCommand).withStyle(RED));
    }

    public static void sendInfectionMessage(CommandContext<CommandSourceStack> ctx, MutableComponent text) {
        ctx.getSource().sendSuccess(text("").append(text("[").withStyle(GOLD))
                        .append(text("Infection").withStyle(AQUA)).append(
                text("]").withStyle(GOLD).append(text(" ")))
                        .append(text)
                , true);
    }
}

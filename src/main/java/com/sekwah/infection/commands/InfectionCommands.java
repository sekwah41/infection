package com.sekwah.infection.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class InfectionCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        InfectionCommand.register(dispatcher);
    }
}

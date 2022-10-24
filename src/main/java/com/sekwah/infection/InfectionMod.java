package com.sekwah.infection;

import com.sekwah.infection.commands.InfectionCommands;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

/**
 * This mod should be server side only and designed to work with vanilla clients.
 */
public class InfectionMod implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            InfectionCommands.register(dispatcher);
        }));
    }
}
package com.sekwah.infection;

import com.sekwah.infection.commands.InfectionCommands;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/**
 * This mod should be server side only and designed to work with vanilla clients.
 */
public class InfectionMod implements DedicatedServerModInitializer {

    public static InfectionController infectionController;

    public static String MOD_ID = "infection";

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            InfectionCommands.register(dispatcher);
        }));

        ServerLifecycleEvents.SERVER_STARTED.register((dedicated) -> {
            infectionController = new InfectionController(dedicated);
            infectionController.init();
        });

        ServerTickEvents.START_SERVER_TICK.register((listener) -> {
            infectionController.tick();
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            // Just ensure they are on the right team
            infectionController.infectPlayer(newPlayer);
        });
    }
}
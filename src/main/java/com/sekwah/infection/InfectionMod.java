package com.sekwah.infection;

import com.sekwah.infection.commands.InfectionCommands;
import com.sekwah.infection.config.InfectionConfig;
import com.sekwah.infection.controller.InfectionController;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerConnection;

/**
 * This mod should be server side only and designed to work with vanilla clients.
 */
public class InfectionMod implements DedicatedServerModInitializer {

    public static InfectionController infectionController;

    public static String MOD_ID = "infection";

    @Override
    public void onInitializeServer() {

        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated, selection) -> {
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
            infectionController.inventoryController.handleItems(newPlayer);
        });
    }

    public static InfectionConfig getConfig() {
        return infectionController.configController.getConfig();
    }
}
package com.sekwah.infection.controller;

import com.sekwah.infection.InfectionMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvent;

public class RemainingPlayersBar {
    private final MinecraftServer server;

    ResourceLocation PLAYER_COUNT_BAR = new ResourceLocation(InfectionMod.MOD_ID, "playercount");

    private CustomBossEvent customBar;

    public RemainingPlayersBar(MinecraftServer server) {
        this.server = server;

        var bossEvents = InfectionMod.infectionController.bossevents;
        var title = Component.literal("Players Remaining");
        customBar = bossEvents.get(PLAYER_COUNT_BAR);
        if(customBar == null) {
            customBar = bossEvents.create(PLAYER_COUNT_BAR, title);
        }
        customBar.removeAllPlayers();
        customBar.setName(title);
        customBar.setColor(CustomBossEvent.BossBarColor.RED);
    }

    public void tick() {

    }
}

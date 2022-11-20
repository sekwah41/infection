package com.sekwah.infection.controller;

import com.sekwah.infection.InfectionMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.world.BossEvent;

public class RemainingPlayersBar {
    private final MinecraftServer server;

    ResourceLocation PLAYER_COUNT_BAR = new ResourceLocation(InfectionMod.MOD_ID, "playercount");

    private CustomBossEvent customBar;

    boolean isVisible = false;

    public RemainingPlayersBar(MinecraftServer server) {
        this.server = server;

        var bossEvents = InfectionMod.infectionController.bossevents;
        var title = Component.literal("Speed-runners Remaining").withStyle(ChatFormatting.GREEN);
        customBar = bossEvents.get(PLAYER_COUNT_BAR);
        if(customBar == null) {
            customBar = bossEvents.create(PLAYER_COUNT_BAR, title);
        }
        customBar.removeAllPlayers();
        customBar.setName(title);
        customBar.setColor(CustomBossEvent.BossBarColor.GREEN);
    }

    public void tick() {
        if(!isVisible) {
            return;
        }
        server.getPlayerList().getPlayers().forEach(player -> {
            customBar.addPlayer(player);
        });

        int speedrunners = 0;
        int infected = 0;

        for (var player: server.getPlayerList().getPlayers()) {
            if(player.getTeam() == InfectionMod.infectionController.speedRunnerTeam) {
                speedrunners++;
            }
            else if(player.getTeam() == InfectionMod.infectionController.infectedTeam) {
                infected++;
            }
        }

        var total = speedrunners + infected;
        var percentage = (float) speedrunners / total;

        ChatFormatting colour;

        if(percentage > 0.5) {
            customBar.setColor(CustomBossEvent.BossBarColor.GREEN);
            colour = ChatFormatting.GREEN;
        }
        else if(percentage > 0.45) {
            customBar.setColor(CustomBossEvent.BossBarColor.YELLOW);
            colour = ChatFormatting.YELLOW;
        }
        else {
            customBar.setColor(CustomBossEvent.BossBarColor.RED);
            colour = ChatFormatting.GOLD;
        }

        customBar.setMax(total);
        customBar.setValue(speedrunners);

        customBar.setName(Component.literal("Speed-runners Remaining").withStyle(colour));

        if(speedrunners == 0) {
            InfectionMod.infectionController.endGame(false);
        }

        // TODO add code for when the last survior dies (use wither death)
        // TODO add a sound for when the infection gets stronger
    }

    public void hide() {
        customBar.removeAllPlayers();
        isVisible = false;
    }

    public void show() {
        isVisible = true;
    }
}

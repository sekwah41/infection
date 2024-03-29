package com.sekwah.infection.controller;

import com.sekwah.infection.InfectionMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Can also trigger code when the countdown reaches 0
 */
public class CountdownBar {

    public int maxTime;
    public int currentTime = 1;

    private CustomBossEvent customBar;
    private MutableComponent name;
    private MinecraftServer server;

    public CountdownBar(MutableComponent name, ResourceLocation barName, MinecraftServer server) {
        this.name = name;
        this.server = server;

        var bossEvents = InfectionMod.infectionController.bossevents;
        customBar = bossEvents.get(barName);
        if(customBar == null) {
            customBar = bossEvents.create(barName, name);
        }
        customBar.removeAllPlayers();
        customBar.setName(name);
        customBar.setColor(CustomBossEvent.BossBarColor.RED);
    }

    public void startCountdown(int seconds) {
        customBar.removeAllPlayers();
        this.maxTime = seconds;
        this.currentTime = 0;
    }

    /**
     * Due to the task scheduler it'll tick every second.
     */
    public void tick() {
        if(maxTime < currentTime) {
            return;
        }
        // Probably could be more efficient, but it's stupidly fast to do anyway
        server.getPlayerList().getPlayers().forEach(player -> {
            customBar.addPlayer(player);
        });

        int seconds = (maxTime - currentTime);
        int mins = seconds / 60;
        int secs = seconds % 60;
        customBar.setName(name.copy().append(Component.literal(mins + ":" + (secs > 9 ? secs : "0" + secs)).withStyle(ChatFormatting.YELLOW)));
        customBar.setMax(maxTime);
        customBar.setValue(maxTime - currentTime);
        server.getPlayerList().getPlayers().forEach(player -> {
            if(seconds <= 5 && seconds > 0) {
                player.playNotifySound(SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.MASTER, 1.0f, 1f);
            }
        });

        if(currentTime == maxTime) {
            customBar.setVisible(false);
            customBar.removeAllPlayers();
            InfectionMod.infectionController.startInfection();
        }
        else {
            customBar.setVisible(true);
        }

        currentTime++;
    }

    public void remove() {
        server.getPlayerList().getPlayers().forEach(player -> {
            customBar.removePlayer(player);
        });
    }
}

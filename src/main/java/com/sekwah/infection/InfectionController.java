package com.sekwah.infection;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

/**
 * Will handle the boss bars as well as any countdowns until more players getting infected
 */
public class InfectionController {

    private final CustomBossEvents bossevents;
    PlayerTeam speedRunnerTeam;
    PlayerTeam infectedTeam;

    PlayerTeam adminTeam;

    String INFECTED = "infected";
    String SPEEDRUNNER = "speedrunner";
    String ADMIN = "admin";

    final MinecraftServer server;
    final Scoreboard scoreboard;

    ResourceLocation INFECTION_COUNTDOWN_BAR = new ResourceLocation(InfectionMod.MOD_ID, "countdown");
    ResourceLocation PLAYER_COUNT_BAR = new ResourceLocation(InfectionMod.MOD_ID, "playercount");
    private CustomBossEvent infectionCountdownBar;
    private CustomBossEvent playerCount;

    public InfectionController(MinecraftServer server) {
        this.server = server;
        this.scoreboard = server.getScoreboard();
        this.bossevents = server.getCustomBossEvents();
    }

    public void init() {
        var scoreboard = server.getScoreboard();

        infectedTeam = scoreboard.getPlayerTeam(INFECTED);
        speedRunnerTeam = scoreboard.getPlayerTeam(SPEEDRUNNER);
        adminTeam = scoreboard.getPlayerTeam(ADMIN);

        infectionCountdownBar = bossevents.get(INFECTION_COUNTDOWN_BAR);
        playerCount = bossevents.get(PLAYER_COUNT_BAR);

        if(infectedTeam == null) {
            infectedTeam = scoreboard.addPlayerTeam(INFECTED);
        }
        if(speedRunnerTeam == null) {
            speedRunnerTeam = scoreboard.addPlayerTeam(SPEEDRUNNER);
        }
        if(adminTeam == null) {
            adminTeam = scoreboard.addPlayerTeam(ADMIN);
        }
        if(infectionCountdownBar == null) {
            infectionCountdownBar = bossevents.create(INFECTION_COUNTDOWN_BAR, new TextComponent("Infection Countdown"));
        }
        if(playerCount == null) {
            playerCount = bossevents.create(PLAYER_COUNT_BAR, new TextComponent("Player Count"));
        }

        configureInfectedTeam();
        configureSpeedrunnerTeam();
    }

    /**
     * Handle logic and timers that are needed and not event based
     */
    public void tick() {

    }

    private void configureSpeedrunnerTeam() {
        this.configureTeam(this.speedRunnerTeam, new TextComponent("Speed-runners"), new TextComponent("Alive"), ChatFormatting.GREEN, true);
    }

    private void configureInfectedTeam() {
        this.configureTeam(this.infectedTeam, new TextComponent("Infected"), new TextComponent("Inf"), ChatFormatting.RED, false);
    }

    private void configureTeam(PlayerTeam team, TextComponent name, TextComponent prefix, ChatFormatting color, boolean hideName) {
        team.setAllowFriendlyFire(false);
        team.setSeeFriendlyInvisibles(true);
        team.setColor(color);
        team.setPlayerSuffix(new TextComponent("").withStyle(ChatFormatting.RESET));
        team.setCollisionRule(PlayerTeam.CollisionRule.NEVER);
        if(hideName) {
            team.setNameTagVisibility(Team.Visibility.HIDE_FOR_OTHER_TEAMS);
        }
    }

    /**
     * If a player is killed
     * @param infected
     */
    public void infectPlayer(ServerPlayer infected) {
        if(infected.getTeam() != null && !infected.getTeam().equals(adminTeam)){
            scoreboard.addPlayerToTeam(infected.getName().getString(), infectedTeam);
        }
    }

    public void hardReset() {
        this.scoreboard.removePlayerTeam(infectedTeam);
        this.scoreboard.removePlayerTeam(speedRunnerTeam);
        this.init();
    }

    public void setRunnerNamesVisible(boolean visible) {
        if(visible) {
            this.speedRunnerTeam.setNameTagVisibility(Team.Visibility.ALWAYS);
        }
        else {
            this.speedRunnerTeam.setNameTagVisibility(Team.Visibility.HIDE_FOR_OTHER_TEAMS);
        }
    }
}

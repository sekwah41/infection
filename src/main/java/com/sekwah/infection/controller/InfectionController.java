package com.sekwah.infection.controller;

import com.sekwah.infection.InfectionMod;
import com.sekwah.infection.mixin.FoodDataMixin;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

import java.util.Random;
import java.util.stream.Collectors;

import static com.sekwah.infection.commands.InfectionCommand.text;
import static net.minecraft.ChatFormatting.AQUA;
import static net.minecraft.ChatFormatting.GOLD;

/**
 * Will handle the boss bars as well as any countdowns until more players getting infected
 */
public class InfectionController {

    public final CustomBossEvents bossevents;
    PlayerTeam speedRunnerTeam;
    PlayerTeam infectedTeam;

    PlayerTeam adminTeam;

    String INFECTED = "infected";
    String SPEEDRUNNER = "speedrunner";
    String ADMIN = "admin";

    public final MinecraftServer server;
    final Scoreboard scoreboard;

    ResourceLocation INFECTION_COUNTDOWN_BAR = new ResourceLocation(InfectionMod.MOD_ID, "countdown");
    ResourceLocation PLAYER_COUNT_BAR = new ResourceLocation(InfectionMod.MOD_ID, "playercount");
    private CustomBossEvent playerCount;

    private CountdownBar countdownBar;

    public InfectionController(MinecraftServer server) {
        this.server = server;
        this.scoreboard = server.getScoreboard();
        this.bossevents = server.getCustomBossEvents();
    }

    public void init() {
        this.countdownBar = new CountdownBar(new TextComponent("Infection Begins in "), INFECTION_COUNTDOWN_BAR, server);
        var scoreboard = server.getScoreboard();

        infectedTeam = scoreboard.getPlayerTeam(INFECTED);
        speedRunnerTeam = scoreboard.getPlayerTeam(SPEEDRUNNER);
        adminTeam = scoreboard.getPlayerTeam(ADMIN);

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
        countdownBar.tick();
    }

    /**
     * Lock in the current players.
     */
    public void lock() {
        var whitelist = server.getPlayerList().getWhiteList();
        for(ServerPlayer player : server.getPlayerList().getPlayers()) {
            whitelist.add(new UserWhiteListEntry(player.getGameProfile()));
            if(player.getTeam() != adminTeam) {
                scoreboard.addPlayerToTeam(player.getGameProfile().getName(), speedRunnerTeam);
            }
        }
        server.setEnforceWhitelist(true);
    }
    public void unlock() {
        var whitelist = server.getPlayerList().getWhiteList();
        whitelist.getEntries().clear();
        server.setEnforceWhitelist(false);
    }

    private void configureSpeedrunnerTeam() {
        this.configureTeam(this.speedRunnerTeam, ChatFormatting.GREEN, true);
    }

    private void configureInfectedTeam() {
        this.configureTeam(this.infectedTeam, ChatFormatting.RED, false);
    }

    private void configureTeam(PlayerTeam team, ChatFormatting color, boolean hideName) {
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
        if(infected.getTeam() != adminTeam){
            if(infected.getTeam().equals(speedRunnerTeam)) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(infected.level);
                bolt.setPos(infected.getX(), infected.getY(), infected.getZ());
                bolt.setVisualOnly(true);
                infected.level.addFreshEntity(bolt);
            }
            scoreboard.addPlayerToTeam(infected.getGameProfile().getName(), infectedTeam);
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

    public void start() {
        setupPlayers();
        this.countdownBar.startCountdown(20 /** 60*/ * 15);

        // Example, try sending vanilla packets where you need to e.g. updating GameProfile
        /*server.getPlayerList().getPlayers().forEach(player -> {
            player.connection.send(new ClientboundSetTitlesPacket(20, 20 * 7, 20));
            player.connection.send(new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.TITLE,
                    new TextComponent("Infection").withStyle(ChatFormatting.RED)
                            .append(new TextComponent(" vs ").withStyle(ChatFormatting.WHITE))
                            .append(new TextComponent("Speedrunners").withStyle(ChatFormatting.GREEN))));
        });*/
    }



    public void broadcastMessage(MutableComponent component) {
        server.sendMessage(text("").append(text("[").withStyle(GOLD))
                .append(text("Infection").withStyle(AQUA)).append(
                        text("]").withStyle(GOLD).append(text(" ")))
                .append(component), null);
    }

    private void setupPlayers() {
        for(ServerPlayer player : server.getPlayerList().getPlayers()) {
            if(player.getTeam() != adminTeam) {
                scoreboard.addPlayerToTeam(player.getGameProfile().getName(), speedRunnerTeam);
                player.clearFire();
                player.inventory.clearContent();
                player.heal(100);
                var foodData = player.getFoodData();
                FoodDataMixin foodDataAccessor = (FoodDataMixin) player.getFoodData();
                foodData.setFoodLevel(20);
                foodDataAccessor.setSaturationLevel(5);
                foodDataAccessor.setExhaustionLevel(0);
                foodDataAccessor.setLastFoodLevel(20);
                foodDataAccessor.setTickTimer(0);
            }
        }
    }

    /** infect a random player
     *
     */
    public void infectPlayer() {
        var players = server.getPlayerList().getPlayers().stream().filter(player -> player.getTeam() == speedRunnerTeam).toList();
        if(players.size() == 0) {
            return;
        }
        var random = new Random();
        var player = players.get(random.nextInt(players.size()));
        if(player.getTeam() != adminTeam) {
            this.infectPlayer(player);
        }
    }
}

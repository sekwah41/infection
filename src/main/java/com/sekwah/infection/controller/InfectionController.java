package com.sekwah.infection.controller;

import com.mojang.authlib.properties.Property;
import com.sekwah.infection.InfectionMod;
import com.sekwah.infection.mixin.FoodDataMixin;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

import java.util.*;

import static com.sekwah.infection.commands.InfectionCommand.text;
import static net.minecraft.ChatFormatting.AQUA;
import static net.minecraft.ChatFormatting.GOLD;

/**
 * Will handle the boss bars as well as any countdowns until more players getting infected
 */
public class InfectionController {

    public final CustomBossEvents bossevents;
    public PlayerTeam speedRunnerTeam;
    public PlayerTeam infectedTeam;

    public PlayerTeam adminTeam;

    String INFECTED = "infected";
    String SPEEDRUNNER = "speedrunner";
    String ADMIN = "admin";

    public final MinecraftServer server;
    final Scoreboard scoreboard;

    ResourceLocation INFECTION_COUNTDOWN_BAR = new ResourceLocation(InfectionMod.MOD_ID, "countdown");

    private CountdownBar countdownBar;
    private RemainingPlayersBar remainingPlayersBar;

    private boolean started = false;

    public InfectionController(MinecraftServer server) {
        this.server = server;
        this.scoreboard = server.getScoreboard();
        this.bossevents = server.getCustomBossEvents();
    }

    public void init() {
        this.countdownBar = new CountdownBar(Component.literal("Infection Begins in "), INFECTION_COUNTDOWN_BAR, server);
        this.remainingPlayersBar = new RemainingPlayersBar(server);
        var scoreboard = server.getScoreboard();

        infectedTeam = scoreboard.getPlayerTeam(INFECTED);
        speedRunnerTeam = scoreboard.getPlayerTeam(SPEEDRUNNER);
        adminTeam = scoreboard.getPlayerTeam(ADMIN);

        if(infectedTeam == null) {
            infectedTeam = scoreboard.addPlayerTeam(INFECTED);
        }
        if(speedRunnerTeam == null) {
            speedRunnerTeam = scoreboard.addPlayerTeam(SPEEDRUNNER);
        }
        if(adminTeam == null) {
            adminTeam = scoreboard.addPlayerTeam(ADMIN);
        }

        configureInfectedTeam();
        configureSpeedrunnerTeam();
    }

    /**
     * Handle logic and timers that are needed and not event based
     */
    public void tick() {
        countdownBar.tick();
        remainingPlayersBar.tick();
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
                revertSkin(player);
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
        team.setPlayerSuffix(Component.literal("").withStyle(ChatFormatting.RESET));
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
            switchSkin(infected);
        }
    }

    private void emitSkinChange(ServerPlayer infected) {
        server.getPlayerList().broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, infected));
        server.getPlayerList().broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, infected));
        var level = infected.getLevel();

        infected.connection.send(new ClientboundRespawnPacket(level.dimensionTypeId(),
                level.dimension(),
                BiomeManager.obfuscateSeed(level.getSeed()),
                infected.gameMode.getGameModeForPlayer(),
                infected.gameMode.getPreviousGameModeForPlayer(),
                level.isDebug(),
                level.isFlat(),
                true,
                infected.getLastDeathLocation()));

        broadcastToAllBut(infected, new ClientboundRemoveEntitiesPacket(infected.getId()));
        broadcastToAllBut(infected, new ClientboundAddPlayerPacket(infected));


        infected.connection.send(new ClientboundPlayerPositionPacket(infected.getX(), infected.getY(), infected.getZ(), infected.getYRot(), infected.getXRot(), Collections.emptySet(), 0, false));
        infected.connection.send(new ClientboundSetCarriedItemPacket(infected.getInventory().selected));
        infected.inventoryMenu.sendAllDataToRemote();
    }

    private void broadcastToAllBut(ServerPlayer player, Packet packet) {
        for(ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
            if(serverPlayer != player) {
                serverPlayer.connection.send(packet);
            }
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

    public void startCountdown() {
        started = false;
        setupPlayers();
        this.countdownBar.startCountdown(20 /** 60*/ * 15);
        this.remainingPlayersBar.hide();

        // Example, try sending vanilla packets where you need to e.g. updating GameProfile
        /*
        server.getPlayerList().broadcastAll();
        server.getPlayerList().getPlayers().forEach(player -> {
            player.connection.send(new ClientboundSetTitlesPacket(20, 20 * 7, 20));
            player.connection.send(new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.TITLE,
                    Component.literal("Infection").withStyle(ChatFormatting.RED)
                            .append(Component.literal(" vs ").withStyle(ChatFormatting.WHITE))
                            .append(Component.literal("Speedrunners").withStyle(ChatFormatting.GREEN))));
        });*/
    }



    public void broadcastMessage(MutableComponent component) {
        server.sendSystemMessage(text("").append(text("[").withStyle(GOLD))
                .append(text("Infection").withStyle(AQUA)).append(
                        text("]").withStyle(GOLD).append(text(" ")))
                .append(component));
    }

    private void setupPlayers() {
        for(ServerPlayer player : server.getPlayerList().getPlayers()) {
            if(player.getTeam() != adminTeam) {
                scoreboard.addPlayerToTeam(player.getGameProfile().getName(), speedRunnerTeam);
                revertSkin(player);
                player.clearFire();
                //player.inventory.clearContent();
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
        if(!started) {
            return;
        }
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

    public Map<UUID, Property> originalSkins = new HashMap<>();

    public final Property ZOMBIE_SKIN = new Property("textures",
            "ewogICJ0aW1lc3RhbXAiIDogMTY2ODg2MDk2MDUyMCwKICAicHJvZmlsZUlkIiA6ICJjMjZjNjRkZTM5MGE0YTcyODQ1MjUwYzQwZDRhYWE4NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJzZWt3YWg0MSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zZTE5NWEzODIxZTE2Nzg0OGIwNjRhMWRkNzMwMDMzNjRlYjI1OGVlNjVlMDNlM2Q4OGZjNTU3N2Y1ZDM5ZDEzIgogICAgfQogIH0KfQ==",
            "NnC2HzeNMFbAzZspGRozTmexIEy5ZY0JffFffIu7KIzMDQUUj11MAAEDlxXj4BxIqiPRcGRFIq6jwjFxe8xHnHVduqK6YVp7PFCqa6hJlEY1LHSYP66RDX27+kh+Q16hfxMWjitQKaaMau6bg/Gj60jhW7OsKfHGXj5E5CxnW73PgFlhc7A6XzTKc+WMXRqAiVA9SQlmnPAe5qDKISYtcCm7N85oNXho57E1Zyz5t+ToUY5gRHkmoB5KxCg/x9ikOaysHjDFReEpZjAcDBEw8oLRLOZTvjEbYWAqEdYuM0eOHWF8hf8jz+qpPET4LtT2kx3wxyx0TXvIxVVC02NzbY2bomphPTY6SDXHIuUJAuOph9ZfaXo1NZRKu7Ge85O7uu5CYOz410VBwr/iuZ2kow5+TgTpkGzLUyoHnZg/20lpRPrwcN3jWumK7IKe+urQ+IV7LsxOFBIm/YecVMC5113fkMR86TLMIunihhox9/JLy0k7wLX8RQDgpGKnFCcDV099YVhMd+utkGRkTqvyf6ol/T8HTf32HyY+gj4NWjuYrwdtYxbcFjlpgP2oSj5+JPS4OO+NqfxkARzRipS/gMkd15EwcQUhcUl5Mx9oGjFfX9r+QqTEgKqWB/yNirZmPETg+V7fZQgVIS32dOCZ5FNe/1IeV1Xd/NcM1iFbszM="
    );

    public void switchSkin(ServerPlayer player) {
        var profile = player.getGameProfile();
        var properties = profile.getProperties();
        var textures = properties.get("textures");

        var originalSkin = textures.iterator().next();
        if(originalSkin != ZOMBIE_SKIN) {
            originalSkins.put(player.getUUID(), originalSkin);

            textures.clear();
            textures.add(ZOMBIE_SKIN);
            emitSkinChange(player);
        }
    }

    public void revertSkin(ServerPlayer player) {
        var profile = player.getGameProfile();
        var properties = profile.getProperties();
        var textures = properties.get("textures");

        if(ZOMBIE_SKIN == textures.iterator().next()) {
            if(originalSkins.containsKey(player.getUUID())) {
                textures.clear();
                textures.add(originalSkins.get(player.getUUID()));
                emitSkinChange(player);
            }
        }
    }

    public void startInfection() {
        started = true;
        infectPlayer();
        this.remainingPlayersBar.show();
    }
}

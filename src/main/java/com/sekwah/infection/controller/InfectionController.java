package com.sekwah.infection.controller;

import com.mojang.authlib.properties.Property;
import com.sekwah.infection.InfectionMod;
import com.sekwah.infection.mixin.FoodDataAccessor;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.*;

import static com.sekwah.infection.commands.InfectionCommand.text;
import static net.minecraft.ChatFormatting.*;

/**
 * Will handle the boss bars as well as any countdowns until more players getting infected
 */
public class InfectionController {

    public final CustomBossEvents bossevents;
    public final InfectionConfigController configController;
    public final InfectedInventoryController inventoryController;
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

    public UUID firstInfected = null;

    private boolean started = false;
    private boolean gameOver = false;

    public InfectionController(MinecraftServer server) {
        this.server = server;
        this.scoreboard = server.getScoreboard();
        this.bossevents = server.getCustomBossEvents();

        this.configController = new InfectionConfigController();
        this.inventoryController = new InfectedInventoryController();
    }

    public boolean hasStarted() {
        return started;
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
        if(gameOver) {
            return;
        }
        countdownBar.tick();
        remainingPlayersBar.tick();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if(player.getTeam() == infectedTeam) {
                if(!player.hasEffect(MobEffects.HUNGER)) {
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, Integer.MAX_VALUE, 0, false, false ));
                }
            }
        }
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
     * @param player the player to infect
     */
    public void infectPlayer(ServerPlayer player) {
        if(player.getTeam() != adminTeam && player.getTeam() != infectedTeam) {
            this.inventoryController.handleItems(player);
            if(Objects.equals(player.getTeam(), speedRunnerTeam)) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(player.level);
                bolt.setPos(player.getX(), player.getY(), player.getZ());
                bolt.setVisualOnly(true);
                player.level.addFreshEntity(bolt);
            }
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 20, 10));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("Now, kill the others!")));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Infected").withStyle(RED)));
            scoreboard.addPlayerToTeam(player.getGameProfile().getName(), infectedTeam);
            switchSkin(player);
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
        gameOver = false;
        setupPlayers();

        var countdown = configController.getConfig().countdown;

        this.countdownBar.startCountdown(20 * countdown);
        this.remainingPlayersBar.hide();

        var playerList = server.getPlayerList();

        // Example, try sending vanilla packets where you need to e.g. updating GameProfile
        playerList.broadcastAll(new ClientboundSetTitlesAnimationPacket(20, 20 * 4, 20));
        playerList.broadcastAll(new ClientboundSetSubtitleTextPacket(Component.literal("You have " + countdown + " seconds, start running!")));
        playerList.broadcastAll(new ClientboundSetTitleTextPacket(Component.literal("Infection").withStyle(GREEN)));
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
                player.removeAllEffects();
                player.clearFire();
                player.getInventory().clearContent();
                //player.inventory.clearContent();
                player.heal(100);
                resetHunger(player);
            }
        }
    }

    public void resetHunger(ServerPlayer player) {
        var foodData = player.getFoodData();
        FoodDataAccessor foodDataAccessor = (FoodDataAccessor) foodData;
        foodData.setFoodLevel(20);
        foodData.setSaturation(5);
        foodData.setExhaustion(0);
        foodDataAccessor.setLastFoodLevel(20);
        foodDataAccessor.setTickTimer(0);
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
        InfectionMod.infectionController.firstInfected = player.getUUID();
        this.infectPlayer(player);
        resetHunger(player);
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

        var players = server.getPlayerList().getPlayers().stream().filter(player -> player.getTeam() == speedRunnerTeam).toList();
        // TODO remove this, it shouldn't cause a problem in production though it'll help for testing win conditions alone.
        if(players.size() != 1) {
            infectPlayer();
        }
        this.remainingPlayersBar.show();
    }


    public void endGame(boolean speedRunnersWin) {
        // Stops the win screen being shown if the game already was won!
        if(gameOver) {
            System.out.println("Game already over: speedRunnersWin" + speedRunnersWin);
            return;
        }
        gameOver = true;

        this.remainingPlayersBar.hide();

        var playerList = server.getPlayerList();
        if(speedRunnersWin) {
            playerList.broadcastAll(new ClientboundSetTitlesAnimationPacket(20, 20 * 4, 20));
            playerList.broadcastAll(new ClientboundSetSubtitleTextPacket(Component.literal("The dragon is dead!")));
            playerList.broadcastAll(new ClientboundSetTitleTextPacket(Component.literal("Speed-runners Win!").withStyle(GREEN)));
        } else {
            playerList.broadcastAll(new ClientboundSetTitlesAnimationPacket(20, 20 * 4, 20));
            playerList.broadcastAll(new ClientboundSetSubtitleTextPacket(Component.literal("All players have been infected!")));
            playerList.broadcastAll(new ClientboundSetTitleTextPacket(Component.literal("Infection Wins!").withStyle(RED)));
            for(ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.playNotifySound(SoundEvents.WITHER_DEATH, SoundSource.MASTER, Float.MAX_VALUE, 1.0f);
            }
        }

    }
}

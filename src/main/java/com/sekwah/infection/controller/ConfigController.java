package com.sekwah.infection.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sekwah.infection.InfectionMod;
import com.sekwah.infection.config.InfectionConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;

public class ConfigController {

    private final Gson gson;
    private File file;
    private InfectionConfig config;

    public ConfigController() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        loadConfig();
    }

    public InfectionConfig getConfig() {
        return config;
    }

    public void loadConfig() {
        if (file == null) {
            file = new File(FabricLoader.getInstance().getConfigDir().toFile(), InfectionMod.MOD_ID + ".json");
        }
        if (!file.exists()) {
            config = new InfectionConfig();
            saveConfig();
            return;
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            config = gson.fromJson(bufferedReader, InfectionConfig.class);
        } catch (FileNotFoundException e) {
            System.err.println("Failed to load config");
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        if (file == null) {
            file = new File(FabricLoader.getInstance().getConfigDir().toFile(), InfectionMod.MOD_ID + ".json");
        }

        String jsonString = gson.toJson(config);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jsonString);
        } catch (IOException e) {
            System.err.println("Failed to save Better Creativity config");
            e.printStackTrace();
        }
    }

}

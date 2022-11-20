package com.sekwah.infection.config;


import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name="infection")
public class InfectionConfig implements ConfigData {

    public int compassAccuracy = 64;
    public int countdown = 15;
}

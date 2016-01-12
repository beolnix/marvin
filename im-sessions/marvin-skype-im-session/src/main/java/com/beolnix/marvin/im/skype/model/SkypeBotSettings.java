package com.beolnix.marvin.im.skype.model;

import com.beolnix.marvin.config.api.BotSettings;
import com.beolnix.marvin.config.api.model.Property;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by beolnix on 11/01/16.
 */
public class SkypeBotSettings implements BotSettings {

    // dependencies
    private final BotSettings botSettings;
    private final Map<String, String> propertiesMap;

    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";

    public SkypeBotSettings(BotSettings botSettings) {
        this.botSettings = botSettings;

        propertiesMap = botSettings.getProperties().stream().collect(Collectors.toMap(
                Property::getName,
                Property::getValue
        ));

    }

    public String getLogin() {
        return propertiesMap.get(LOGIN);
    }

    public String getPassword() {
        return propertiesMap.get(PASSWORD);
    }

    @Override
    public String getProtocol() {
        return botSettings.getProtocol();
    }

    @Override
    public String getName() {
        return botSettings.getName();
    }

    @Override
    public List<Property> getProperties() {
        return botSettings.getProperties();
    }
}

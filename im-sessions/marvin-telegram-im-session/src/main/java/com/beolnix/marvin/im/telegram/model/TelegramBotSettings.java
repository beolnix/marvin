package com.beolnix.marvin.im.telegram.model;

import com.beolnix.marvin.config.api.BotSettings;
import com.beolnix.marvin.config.api.model.Property;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TelegramBotSettings implements BotSettings {

    // dependencies
    private final BotSettings botSettings;
    private final Map<String, String> propertiesMap;

    public static final String TOKEN = "token";

    public TelegramBotSettings(BotSettings botSettings) {
        this.botSettings = botSettings;

        propertiesMap = botSettings.getProperties().stream().collect(Collectors.toMap(
                Property::getName,
                Property::getValue
        ));

    }

    public String getToken() {
        return propertiesMap.get(TOKEN);
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

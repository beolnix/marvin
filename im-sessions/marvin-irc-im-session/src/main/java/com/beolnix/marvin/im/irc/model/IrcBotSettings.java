package com.beolnix.marvin.im.irc.model;

import com.beolnix.marvin.config.api.BotSettings;
import com.beolnix.marvin.config.api.model.Property;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by beolnix on 01/11/15.
 */
public class IrcBotSettings implements BotSettings {

    // dependencies
    private final BotSettings botSettings;
    private final Map<String, String> propertiesMap;

    // constants
    public static final String NICKNAME = "nickname";
    public static final String PASSWORD = "password";
    public static final String CHANNEL_NAME = "channelName";
    public static final String CHANNEL_PASSWORD = "channelPassword";
    public static final String SERVER_NAME = "serverName";
    public static final String PORT_NUMBER = "portNumber";
    public static final String CHARSET = "charset";


    public IrcBotSettings(BotSettings botSettings) {
        this.botSettings = botSettings;

        propertiesMap = botSettings.getProperties().stream().collect(Collectors.toMap(
                Property::getName,
                Property::getValue
        ));

    }

    public String getNickname() {
        return propertiesMap.get(NICKNAME);
    }

    public String getPassword() {
        return propertiesMap.get(PASSWORD);
    }

    public String getChannelName() {
        return propertiesMap.get(CHANNEL_NAME);
    }

    public String getChannelPassword() {
        return propertiesMap.get(CHANNEL_PASSWORD);
    }

    public String getServerName() {
        return propertiesMap.get(SERVER_NAME);
    }

    public Integer getPortNumber() {

        try {
            return Integer.parseInt(propertiesMap.get(PORT_NUMBER));
        } catch (NumberFormatException e) {
            return null;
        }

    }

    public String getCharset() {
        return propertiesMap.get(CHARSET);
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

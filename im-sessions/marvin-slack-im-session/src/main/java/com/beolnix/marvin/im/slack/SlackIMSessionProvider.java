package com.beolnix.marvin.im.slack;

import com.beolnix.marvin.config.api.BotSettings;
import com.beolnix.marvin.im.IMSessionUtils;
import com.beolnix.marvin.im.api.IMSession;
import com.beolnix.marvin.im.api.IMSessionManager;
import com.beolnix.marvin.im.api.IMSessionProvider;
import com.beolnix.marvin.im.slack.model.SlackBotSettings;
import com.beolnix.marvin.plugins.api.PluginsManager;

public class SlackIMSessionProvider implements IMSessionProvider {

    private final static SlackIMSessionProvider instance = new SlackIMSessionProvider();
    private IMSessionUtils imSessionUtils = new IMSessionUtils();

    public static IMSessionProvider getInstance(IMSessionManager imSessionManager) {
        imSessionManager.registerIMSessionProvider(instance);
        return instance;
    }

    @Override
    public IMSession createNewSession(BotSettings botSettings, PluginsManager pluginsManager) {
        SlackBotSettings telegramBotSettings = new SlackBotSettings(botSettings);
        SlackIMSession telegramIMSession = new SlackIMSession(telegramBotSettings, pluginsManager, imSessionUtils);
        return telegramIMSession;
    }

    @Override
    public String getProtocol() {
        return SlackIMSession.PROTOCOL;
    }
}

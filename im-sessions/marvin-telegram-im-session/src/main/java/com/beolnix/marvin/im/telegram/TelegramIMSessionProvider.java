package com.beolnix.marvin.im.telegram;

import com.beolnix.marvin.config.api.BotSettings;
import com.beolnix.marvin.im.IMSessionUtils;
import com.beolnix.marvin.im.api.IMSession;
import com.beolnix.marvin.im.api.IMSessionManager;
import com.beolnix.marvin.im.api.IMSessionProvider;
import com.beolnix.marvin.im.telegram.model.TelegramBotSettings;
import com.beolnix.marvin.plugins.api.PluginsManager;

public class TelegramIMSessionProvider implements IMSessionProvider {

    private final static TelegramIMSessionProvider instance = new TelegramIMSessionProvider();
    private IMSessionUtils imSessionUtils = new IMSessionUtils();

    public static IMSessionProvider getInstance(IMSessionManager imSessionManager) {
        imSessionManager.registerIMSessionProvider(instance);
        return instance;
    }

    @Override
    public IMSession createNewSession(BotSettings botSettings, PluginsManager pluginsManager) {
        TelegramBotSettings telegramBotSettings = new TelegramBotSettings(botSettings);
        TelegramIMSession telegramIMSession = new TelegramIMSession(telegramBotSettings, pluginsManager, imSessionUtils);
        return telegramIMSession;
    }

    @Override
    public String getProtocol() {
        return TelegramIMSession.PROTOCOL;
    }
}

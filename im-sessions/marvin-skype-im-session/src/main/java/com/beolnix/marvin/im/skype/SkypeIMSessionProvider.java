package com.beolnix.marvin.im.skype;

import com.beolnix.marvin.config.api.BotSettings;
import com.beolnix.marvin.im.IMSessionUtils;
import com.beolnix.marvin.im.api.IMSession;
import com.beolnix.marvin.im.api.IMSessionManager;
import com.beolnix.marvin.im.api.IMSessionProvider;
import com.beolnix.marvin.im.skype.model.SkypeBotSettings;
import com.beolnix.marvin.plugins.api.PluginsManager;

/**
 * Created by beolnix on 12/01/16.
 */
public class SkypeIMSessionProvider implements IMSessionProvider {

    private final static SkypeIMSessionProvider instance = new SkypeIMSessionProvider();
    private IMSessionUtils imSessionUtils = new IMSessionUtils();

    public static IMSessionProvider getInstance(IMSessionManager imSessionManager) {
        imSessionManager.registerIMSessionProvider(instance);
        return instance;
    }

    @Override
    public IMSession createNewSession(BotSettings botSettings, PluginsManager pluginsManager) {
        SkypeBotSettings skypeBotSettings = new SkypeBotSettings(botSettings);
        SkypeIMSession skypeIMSession = new SkypeIMSession(skypeBotSettings, pluginsManager, imSessionUtils);
        return skypeIMSession;
    }

    @Override
    public String getProtocol() {
        return SkypeIMSession.PROTOCOL;
    }
}

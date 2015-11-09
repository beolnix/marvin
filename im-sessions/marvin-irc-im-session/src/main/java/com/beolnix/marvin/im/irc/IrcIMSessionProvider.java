package com.beolnix.marvin.im.irc;

import com.beolnix.marvin.config.api.BotSettings;
import com.beolnix.marvin.im.api.IMSession;
import com.beolnix.marvin.im.api.IMSessionManager;
import com.beolnix.marvin.im.api.IMSessionProvider;
import com.beolnix.marvin.im.irc.impl.IrcMessageListenerImpl;
import com.beolnix.marvin.im.irc.model.IrcBotSettings;
import com.beolnix.marvin.plugins.api.PluginsManager;

/**
 * Created by DAtmakin on 11/2/2015.
 */
public class IrcIMSessionProvider implements IMSessionProvider {

    private final static IrcIMSessionProvider instance = new IrcIMSessionProvider();

    public static IMSessionProvider getInstance(IMSessionManager imSessionManager) {
        imSessionManager.registerIMSessionProvider(instance);
        return instance;
    }

    @Override
    public IMSession createNewSession(BotSettings botSettings, PluginsManager pluginsManager) {
        IrcBotSettings ircBotSettings = new IrcBotSettings(botSettings);
        IrcMessageListenerImpl ircMessageListenerImpl = new IrcMessageListenerImpl(ircBotSettings.getName(), pluginsManager);
        return IrcIMSession.createNewInstance(ircBotSettings, ircMessageListenerImpl);
    }

    @Override
    public String getProtocol() {
        return IrcIMSession.PROTOCOL;
    }
}

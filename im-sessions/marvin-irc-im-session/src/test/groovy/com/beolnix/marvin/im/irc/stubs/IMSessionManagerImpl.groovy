package com.beolnix.marvin.im.irc.stubs

import com.beolnix.marvin.config.api.error.ConfigurationException
import com.beolnix.marvin.im.api.IMSession
import com.beolnix.marvin.im.api.IMSessionManager
import com.beolnix.marvin.im.api.IMSessionProvider
import com.beolnix.marvin.im.api.model.IMOutgoingMessage
import com.beolnix.marvin.plugins.api.PluginsManager

/**
 * Created by beolnix on 09/11/15.
 */
class IMSessionManagerImpl implements IMSessionManager {
    @Override
    void sendMessage(IMOutgoingMessage... outMsg) {

    }

    @Override
    void registerIMSessionProvider(IMSessionProvider imSessionProvider) {

    }

    @Override
    void createSessions(PluginsManager pluginManager) throws ConfigurationException {

    }

    @Override
    void createSessionFor(String botName, PluginsManager pluginManager) throws ConfigurationException {

    }

    @Override
    Map<String, IMSession> getIMSessions() {
        return null
    }

    @Override
    IMSession getIMSession(String botName) {
        return null
    }
}

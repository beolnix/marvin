package com.beolnix.marvin.im;

import com.beolnix.marvin.config.api.BotSettings;
import com.beolnix.marvin.config.api.ConfigurationProvider;
import com.beolnix.marvin.config.api.error.ConfigurationException;
import com.beolnix.marvin.im.api.*;
import com.beolnix.marvin.im.api.model.*;
import com.beolnix.marvin.im.api.IMSessionState;
import com.beolnix.marvin.plugins.api.PluginsManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Created by beolnix on 31/10/15.
 */
public class IMSessionManagerImpl implements IMSessionManager {

    // dependencies
    private final ConfigurationProvider configProvider;
    private final Executor senderExecutor;

    // state
    private Map<String, IMSession> sessionsMap = new ConcurrentHashMap<>();
    private Map<String, IMSessionProvider> sessionProvidersMap = new ConcurrentHashMap<>();

    // constants
    private static final Logger logger = Logger.getLogger(IMSessionManagerImpl.class);

    public IMSessionManagerImpl(ConfigurationProvider configProvider, Executor senderExecutor) {
        this.configProvider = configProvider;
        this.senderExecutor = senderExecutor;
    }

    @Override
    public void registerIMSessionProvider(IMSessionProvider imSessionProvider) {
        sessionProvidersMap.putIfAbsent(imSessionProvider.getProtocol(), imSessionProvider);
        logger.info("New IM Session provider has been registered for protocol: " + imSessionProvider.getProtocol());
    }

    @Override
    public void createSessions(PluginsManager pluginsManager) throws ConfigurationException {
        for (String botName : configProvider.getBotSettings().keySet()) {
            try {
                createSessionFor(botName, pluginsManager);
            } catch (ConfigurationException e) {
                logger.error("Session configuration error: " + e, e);
            } catch (Exception e) {
                logger.error("Session creation error: " + e, e);
            }
        }
    }

    @Override
    public void createSessionFor(String botName, PluginsManager pluginsManager) throws ConfigurationException {
        if (sessionsMap.get(botName) != null) {
            throw new ConfigurationException("Session for bot " + botName + " has been already created");
        }

        BotSettings botSettings = configProvider.getBotSettings().get(botName);

        IMSessionProvider imSessionProvider = sessionProvidersMap.get(botSettings.getProtocol());

        if (imSessionProvider != null) {
            IMSession imSession = imSessionProvider.createNewSession(botSettings, pluginsManager);
            imSession.connect();
            sessionsMap.put(botName, imSession);
            logger.info("New bot created: " + botName + "; protocol: " + imSession.getProtocol());
        } else {
            throw new ConfigurationException("Can't find IM Session Provider for the protocol: " + botSettings.getProtocol());
        }
    }

    @Override
    public void sendMessage(final IMOutgoingMessage... outMessages) {
        if (outMessages.length < 1) {
            return;
        }
        logger.trace("New message scheduled to be sent from: " + outMessages[0].getBotName() +
                "; protocol: " + outMessages[0].getProtocol());

        this.senderExecutor.execute(() -> send(outMessages));
    }

    private void send(IMOutgoingMessage... outMessages) {

        IMSession imSession = sessionsMap.get(outMessages[0].getBotName());
        if (imSession == null) {
            logger.error("session " + outMessages[0].getBotName() + " not found.");
            return;
        }

        if (imSession.getState() != IMSessionState.CONNECTED) {
            logger.error("Bot '" + outMessages[0].getBotName() + "' can't send the message because it isn't connected");
            return;
        }
        for (IMOutgoingMessage msg : outMessages) {
            imSession.sendMessage(msg);
            logger.trace("Message passed to imSession from bot name: " + msg.getBotName() +
                    "; protocol: " + msg.getProtocol() +
                    "; msgBody: " + msg.getRawMessageBody());
        }

    }

    @Override
    public Map<String, IMSession> getIMSessions() {
        return new HashMap<>(sessionsMap);
    }

    @Override
    public IMSession getIMSession(String botName) {
        return sessionsMap.get(botName);
    }

}

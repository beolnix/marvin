package com.beolnix.marvin.im

import com.beolnix.marvin.config.api.BotSettings
import com.beolnix.marvin.config.api.ConfigurationProvider
import com.beolnix.marvin.config.api.error.ConfigurationException
import com.beolnix.marvin.im.api.IMSession
import com.beolnix.marvin.im.api.IMSessionProvider
import com.beolnix.marvin.im.api.IMSessionState
import com.beolnix.marvin.im.api.model.IMOutgoingMessage
import com.beolnix.marvin.im.api.model.IMOutgoingMessageBuilder
import com.beolnix.marvin.plugins.api.PluginsManager
import org.junit.Test

import java.util.concurrent.Executor

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

/**
 * Created by DAtmakin on 11/5/2015.
 */
class IMSessionManagerImplTestCase {

    public static final String PROTOCOL = "test"

    @Test
    public void testRegisterIMSessionProvider() {
        def imSessionProvider = [
                getProtocol: {
                    PROTOCOL
                }
        ] as IMSessionProvider

        def imSessionManager = new IMSessionManagerImpl(null, null)
        imSessionManager.registerIMSessionProvider(imSessionProvider)

        assertTrue(imSessionManager.sessionProvidersMap.containsKey(PROTOCOL))
        assertNotNull(imSessionManager.sessionProvidersMap.get(PROTOCOL))
    }


    @Test
    public void testCreateSessionsPositive() {
        def isConnectExecuted = false

        // Prepare mocks for the test
        def botSettings = [
                getProtocol: {
                    PROTOCOL
                }
        ] as BotSettings
        def configProvider = [
                getBotSettings: {
                    ["testBot" : botSettings]
                }
        ] as ConfigurationProvider
        def pluginManager = [] as PluginsManager
        def imSession = [
                connect: {
                    isConnectExecuted = true
                }
        ] as IMSession
        def imSessionProvider = [
                getProtocol: {
                    PROTOCOL
                },
                createNewSession: { botConfig, pluginsManager ->
                    imSession
                }
        ] as IMSessionProvider

        // doing the test:
        // 1. registered provider must be used to create the session
        // 2. connect method of newly created session must be executed
        // 3. session must be placed to the sessions map
        def imSessionManager = new IMSessionManagerImpl(configProvider, null)
        imSessionManager.registerIMSessionProvider(imSessionProvider)
        imSessionManager.createSessions(pluginManager)

        assertTrue(imSessionManager.sessionsMap.containsKey("testBot"))
        assertTrue(isConnectExecuted)
    }

    /**
     * Session must not be created because session provider for the protocol specified in botSettings has not been provided
     */
    @Test
    public void testCreateSessionsNegative() {

        def pluginManager = [] as PluginsManager
        def botSettings = [
                getProtocol: {
                    PROTOCOL
                }
        ] as BotSettings
        def configProvider = [
                getBotSettings: {
                    ["testBot" : botSettings]
                }
        ] as ConfigurationProvider

        def imSessionManager = new IMSessionManagerImpl(configProvider, null)
        imSessionManager.createSessions(pluginManager)

        assertFalse(imSessionManager.sessionsMap.containsKey("testBot"))
    }

    /**
     * Message should be passed to IMSession
     */
    @Test
    public void testSendMessagePositive() {
        def isSendMessageExecuted = false

        def executor = [
                execute: { runnable ->
                    runnable.run()
                }
        ] as Executor
        def imSessionManager = new IMSessionManagerImpl(null, executor)
        def imSession = [
                getState : {
                    IMSessionState.CONNECTED
                },
                sendMessage : {
                    isSendMessageExecuted = true
                }
        ] as IMSession

        imSessionManager.sessionsMap["test bot"] = imSession
        def msg = new IMOutgoingMessageBuilder()
            .withBotName("test bot")
            .withProtocol(PROTOCOL)
            .withRawMessageBody("msg text")
            .build()

        imSessionManager.sendMessage(msg)
        assertTrue(isSendMessageExecuted)
    }

    /**
     * Message should be passed to IMSession if IMSession is not in CONNECTED state
     */
    @Test
    public void testSendMessageNegative1() {
        def isSendMessageExecuted = false

        def executor = [
                execute: { runnable ->
                    runnable.run()
                }
        ] as Executor
        def imSessionManager = new IMSessionManagerImpl(null, executor)
        def imSession = [
                getState : {
                    IMSessionState.DISCONNECTED
                },
                sendMessage : {
                    isSendMessageExecuted = true
                }
        ] as IMSession

        imSessionManager.sessionsMap["test bot"] = imSession
        def msg = new IMOutgoingMessageBuilder()
                .withBotName("test bot")
                .withProtocol(PROTOCOL)
                .withRawMessageBody("msg text")
                .build()

        imSessionManager.sendMessage(msg)
        assertFalse(isSendMessageExecuted)
    }
}

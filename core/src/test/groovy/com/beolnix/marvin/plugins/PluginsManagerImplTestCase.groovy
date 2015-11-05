package com.beolnix.marvin.plugins

import com.beolnix.marvin.im.api.IMIncomingMessage
import com.beolnix.marvin.im.api.IMIncomingMessageBuilder
import com.beolnix.marvin.plugins.api.IMPlugin
import com.beolnix.marvin.plugins.api.IMPluginState
import com.beolnix.marvin.plugins.api.PluginsProvider
import org.junit.Test

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

import java.util.concurrent.Executor

/**
 * Created by DAtmakin on 11/5/2015.
 */
class PluginsManagerImplTestCase {

    def executor = [
            execute: { runnable ->
                runnable.run()
            }
    ] as Executor
    def incomingMessage = new IMIncomingMessageBuilder()
        .withProtocol("test")
        .withCommandName("test")
        .build()

    /**
     * msg should be processed because plugin is in INITIALIZED state
     */
    @Test
    public void testProcessPositive1() {
        def isProcessCalled = false

        def plugin = [
                process: { msg ->
                    isProcessCalled = true
                },
                getPluginState: {
                    IMPluginState.INITIALIZED
                },
                getCommandsList: {
                    "test"
                },
                isProcessAll: {
                    true
                },
                isAllProtocolsSupported: {
                    true
                },
                setIMSessionManager: {
                    //nop
                },
                getPluginName: {
                    "test plugin"
                }
        ] as IMPlugin
        def pluginsManager = new PluginsManagerImpl(null, executor)
        pluginsManager.deployPlugin(plugin)
        pluginsManager.process(incomingMessage)
        assertTrue(isProcessCalled)
    }

    /**
     * msg should be processed because protocol is supported by plugin
     */
    @Test
    public void testProcessPositive2() {
        def isProcessCalled = false

        def plugin = [
                process: { msg ->
                    isProcessCalled = true
                },
                getPluginState: {
                    IMPluginState.INITIALIZED
                },
                getCommandsList: {
                    "test"
                },
                isProcessAll: {
                    true
                },
                isAllProtocolsSupported: {
                    false
                },
                isProtocolSupported : { protocol ->
                    "test".equals(protocol)
                },
                setIMSessionManager: {
                    //nop
                },
                getPluginName: {
                    "test plugin"
                }
        ] as IMPlugin
        def pluginsManager = new PluginsManagerImpl(null, executor)
        pluginsManager.deployPlugin(plugin)
        pluginsManager.process(incomingMessage)
        assertTrue(isProcessCalled)
    }

    /**
     * msg should be processed because command is supported by plugin
     */
    @Test
    public void testProcessPositive3() {
        def isProcessCalled = false

        def plugin = [
                process: { msg ->
                    isProcessCalled = true
                },
                getPluginState: {
                    IMPluginState.INITIALIZED
                },
                isCommandSupported: { command ->
                    "test".equals(command)
                },
                isProcessAll: {
                    false
                },
                isAllProtocolsSupported: {
                    false
                },
                isProtocolSupported : { protocol ->
                    "test".equals(protocol)
                },
                setIMSessionManager: {
                    //nop
                },
                getPluginName: {
                    "test plugin"
                }
        ] as IMPlugin
        def pluginsManager = new PluginsManagerImpl(null, executor)
        pluginsManager.deployPlugin(plugin)
        pluginsManager.process(incomingMessage)
        assertTrue(isProcessCalled)
    }

    /**
     * msg should not be processed because of unsupported command
     */
    @Test
    public void testProcessNegative1() {
        def isProcessCalled = false

        def plugin = [
                process: { msg ->
                    isProcessCalled = true
                },
                getPluginState: {
                    IMPluginState.INITIALIZED
                },
                isCommandSupported: { command ->
                    "another_command".equals(command)
                },
                isProcessAll: {
                    false
                },
                isAllProtocolsSupported: {
                    false
                },
                isProtocolSupported : { protocol ->
                    "test".equals(protocol)
                },
                setIMSessionManager: {
                    //nop
                },
                getPluginName: {
                    "test plugin"
                }
        ] as IMPlugin
        def pluginsManager = new PluginsManagerImpl(null, executor)
        pluginsManager.deployPlugin(plugin)
        pluginsManager.process(incomingMessage)
        assertFalse(isProcessCalled)
    }

    /**
     * msg should not be processed because of unsupported protocol
     */
    @Test
    public void testProcessNegative2() {
        def isProcessCalled = false

        def plugin = [
                process: { msg ->
                    isProcessCalled = true
                },
                getPluginState: {
                    IMPluginState.INITIALIZED
                },
                isCommandSupported: { command ->
                    "test".equals(command)
                },
                isProcessAll: {
                    false
                },
                isAllProtocolsSupported: {
                    false
                },
                isProtocolSupported : { protocol ->
                    "another_protocol".equals(protocol)
                },
                setIMSessionManager: {
                    //nop
                },
                getPluginName: {
                    "test plugin"
                }
        ] as IMPlugin
        def pluginsManager = new PluginsManagerImpl(null, executor)
        pluginsManager.deployPlugin(plugin)
        pluginsManager.process(incomingMessage)
        assertFalse(isProcessCalled)
    }

    /**
     * msg should not be processed because plugin is not in INITIALIZED state
     */
    @Test
    public void testProcessNegative4() {
        def isProcessCalled = false

        def plugin = [
                process: { msg ->
                    isProcessCalled = true
                },
                getPluginState: {
                    IMPluginState.NOT_INITIALIZED
                },
                getCommandsList: {
                    "test"
                },
                isProcessAll: {
                    true
                },
                isAllProtocolsSupported: {
                    true
                },
                setIMSessionManager: {
                    //nop
                },
                getPluginName: {
                    "test plugin"
                }
        ] as IMPlugin
        def pluginsManager = new PluginsManagerImpl(null, executor)
        pluginsManager.deployPlugin(plugin)
        pluginsManager.process(incomingMessage)
        assertFalse(isProcessCalled)
    }

    @Test
    public void testUndeployPlugin() {
        def isProcessCalled = false

        def plugin = [
                setIMSessionManager: {
                    //nop
                },
                getPluginName: {
                    "test plugin"
                }
        ] as IMPlugin
        def pluginsManager = new PluginsManagerImpl(null, executor)
        pluginsManager.deployPlugin(plugin)
        assertTrue(pluginsManager.pluginsMap.containsKey("test plugin"))
        pluginsManager.undeployPlugin(plugin)
        assertFalse(pluginsManager.pluginsMap.containsKey("test plugin"))
    }

    @Test
    public void testRegisterPluginsProvider() {
        def isRegisterPluginsListenerExecuted = false
        def pluginsProvider = [
                registerPluginsListener : {
                    isRegisterPluginsListenerExecuted = true
                }
        ] as PluginsProvider
        def pluginsManager = new PluginsManagerImpl(null, executor)
        pluginsManager.registerPluginsProvider(pluginsProvider)
        assertTrue(pluginsManager.pluginsProvidersList.contains(pluginsProvider))
        assertTrue(isRegisterPluginsListenerExecuted)
    }


}

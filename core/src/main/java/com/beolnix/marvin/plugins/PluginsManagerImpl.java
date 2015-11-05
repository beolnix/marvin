package com.beolnix.marvin.plugins;

import com.beolnix.marvin.im.api.model.IMIncomingMessage;
import com.beolnix.marvin.im.api.IMSessionManager;
import com.beolnix.marvin.plugins.api.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Created by beolnix on 31/10/15.
 */
public class PluginsManagerImpl implements PluginsManager, PluginsListener {

    // dependencies
    private final IMSessionManager imSessionManager;
    private final Executor executor;

    // state
    private final Map<String, IMPlugin> pluginsMap = new ConcurrentHashMap<>();
    private List<PluginsProvider> pluginsProvidersList = new ArrayList<>();

    // constants
    private static final Logger logger = Logger.getLogger(PluginsManagerImpl.class);

    public PluginsManagerImpl(IMSessionManager imSessionManager, Executor executor) {
        this.imSessionManager = imSessionManager;
        this.executor = executor;
    }

    public void registerPluginsProvider(PluginsProvider pluginsProvider) {
        pluginsProvider.registerPluginsListener(this);
        pluginsProvidersList.add(pluginsProvider);
        logger.info("New plugins provider registered: " + pluginsProvider.getClass().getName());
    }

    public void process(IMIncomingMessage msg) {
        logger.trace("Processing msg: '" + msg.getRawMessageBody() + "'; from: " + msg.getBotName());
        pluginsMap.values().stream()
                // pass message to Initialized plugins only
                .filter(plugin -> IMPluginState.INITIALIZED == plugin.getPluginState())
                // which supports the protocol
                .filter(plugin -> plugin.isAllProtocolsSupported() || plugin.isProtocolSupported(msg.getProtocol()))
                // and can process requested command or should intercept all messages
                .filter(plugin -> plugin.isProcessAll() || plugin.isCommandSupported(msg.getCommandName()))

                .forEach(plugin -> executor.execute(() -> plugin.process(msg)));
    }

    @Override
    public void deployPlugin(IMPlugin imPlugin) {
        imPlugin.setIMSessionManager(imSessionManager);
        pluginsMap.put(imPlugin.getPluginName(), imPlugin);
        logger.info("New plugin has been deployed: " + imPlugin.getPluginName());
    }

    @Override
    public void undeployPlugin(IMPlugin imPlugin) {
        pluginsMap.remove(imPlugin.getPluginName());
        logger.info("Plugin has been undeployed: " + imPlugin.getPluginName());
    }

}

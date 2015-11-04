package com.beolnix.marvin.plugins;

import com.beolnix.marvin.im.api.IMIncomingMessage;
import com.beolnix.marvin.im.api.IMSessionManager;
import com.beolnix.marvin.plugins.api.PluginsManager;
import com.beolnix.marvin.plugins.api.PluginsListener;
import com.beolnix.marvin.plugins.api.PluginsProvider;
import com.beolnix.marvin.plugins.api.IMPlugin;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

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
        for (IMPlugin imPlugin : pluginsMap.values()) {
            if (imPlugin.getCommandsList().contains(msg.getCommandName())) {
                executor.execute(() -> imPlugin.process(msg));
            } else if (imPlugin.isProcessAll()) {
                executor.execute(() -> imPlugin.process(msg));
            }
        }
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

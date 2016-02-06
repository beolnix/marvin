package com.beolnix.marvin.plugins;

import com.beolnix.marvin.config.api.ConfigurationProvider;
import com.beolnix.marvin.config.api.model.PluginProperties;
import com.beolnix.marvin.config.api.model.PluginsSettings;
import com.beolnix.marvin.config.api.model.Property;
import com.beolnix.marvin.im.api.model.IMIncomingMessage;
import com.beolnix.marvin.im.api.IMSessionManager;
import com.beolnix.marvin.plugins.api.*;
import com.beolnix.marvin.plugins.utils.PluginUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Created by beolnix on 31/10/15.
 */
public class PluginsManagerImpl implements PluginsManager, PluginsListener {

    // dependencies
    private final IMSessionManager imSessionManager;
    private final Executor executor;
    private final ConfigurationProvider configProvider;
    private final Map<String, PluginProperties> pluginConfigMap = new HashMap<>();

    // state
    private final Map<String, IMPlugin> pluginsMap = new ConcurrentHashMap<>();
    private List<PluginsProvider> pluginsProvidersList = new ArrayList<>();
    private String pluginsLogsDirPath;
    private String pluginsHomeDir;

    // constants
    private static final Logger logger = Logger.getLogger(PluginsManagerImpl.class);
    private final PluginUtils pluginUtils = new PluginUtils();

    public PluginsManagerImpl(IMSessionManager imSessionManager, Executor executor, ConfigurationProvider configProvider) {
        this.configProvider = configProvider;
        this.imSessionManager = imSessionManager;
        this.executor = executor;

        initPluginConfigMap(configProvider);
        initPluginSettings(configProvider);
    }

    private void initPluginSettings(ConfigurationProvider configProvider) {
        try {
            PluginsSettings pluginSettings = configProvider.getPluginSettings();
            pluginsHomeDir = pluginSettings.getDirPath();
            pluginsLogsDirPath = pluginSettings.getLogsPath();
        } catch (Exception e) {
            throw new RuntimeException("Illegal plugins configuration.", e);
        }
    }

    private void initPluginConfigMap(ConfigurationProvider configProvider) {
        try {
            PluginsSettings pluginSettings = configProvider.getPluginSettings();
            pluginSettings.getPluginProperties().stream()
                    .forEach(pluginProperties -> {
                        pluginConfigMap.put(pluginProperties.getName(), pluginProperties);
                    });
        } catch (Exception e) {
            logger.error("Can't fetch plugins specific configs because of: " + e.getMessage(), e);
        }
    }

    @Override
    public void registerPluginsProvider(PluginsProvider pluginsProvider) {
        pluginsProvider.registerPluginsListener(this);
        pluginsProvidersList.add(pluginsProvider);
        logger.info("New plugins provider registered: " + pluginsProvider.getClass().getName());
    }

    @Override
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
    public void onError(String s, Throwable throwable) {
        logger.warn("Plugin: " + s + " can't be deployed because of: " + throwable.getMessage());
    }

    @Override
    public void deployPlugin(IMPlugin imPlugin) {
        imPlugin.init(getPluginConfig(imPlugin.getPluginName()), imSessionManager);

        pluginsMap.put(imPlugin.getPluginName(), imPlugin);
        logger.info("New plugin has been deployed: " + imPlugin.getPluginName());
    }

    public PluginConfig getPluginConfig(String pluginName) {
        PluginProperties pluginProperties = pluginConfigMap.get(pluginName);
        List<Property> properties = pluginProperties == null ? Collections.EMPTY_LIST : pluginProperties.getProperties();

        Logger logger = pluginUtils.getLogger(pluginsLogsDirPath, pluginName);
        File file = pluginUtils.getPluginHomeDir(pluginsHomeDir, pluginName);
        return new PluginConfig(logger, file, properties);
    }

    @Override
    public void undeployPlugin(IMPlugin imPlugin) {
        pluginsMap.remove(imPlugin.getPluginName());
        logger.info("Plugin has been undeployed: " + imPlugin.getPluginName());
    }

}

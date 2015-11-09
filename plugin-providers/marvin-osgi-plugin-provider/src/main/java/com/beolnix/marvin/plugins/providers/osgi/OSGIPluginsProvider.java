package com.beolnix.marvin.plugins.providers.osgi;


import com.beolnix.marvin.config.api.ConfigurationProvider;

import com.beolnix.marvin.plugins.api.PluginsListener;
import com.beolnix.marvin.plugins.api.PluginsManager;
import com.beolnix.marvin.plugins.api.PluginsProvider;
import com.beolnix.marvin.plugins.api.error.PluginsProviderConfigurationException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by beolnix on 31/10/15.
 */
public class OSGIPluginsProvider implements PluginsProvider {

    // dependencies
    private final ConfigurationProvider configurationProvider;
    private final FelixOSGIContainer felixOSGIContainer;

    // constants
    private final static Logger logger = Logger.getLogger(OSGIPluginsProvider.class);


    private OSGIPluginsProvider(ConfigurationProvider configurationProvider,
                                FelixOSGIContainer felixOSGIContainer) {
        this.configurationProvider = configurationProvider;
        this.felixOSGIContainer = felixOSGIContainer;
    }

    public static PluginsProvider createNewInstance(ConfigurationProvider configurationProvider,
                                                    FelixOSGIContainer felixOSGIContainer,
                                                    PluginsManager pluginsManager) throws PluginsProviderConfigurationException {

        OSGIPluginsProvider pluginsProvider = new OSGIPluginsProvider(configurationProvider, felixOSGIContainer);
        pluginsManager.registerPluginsProvider(pluginsProvider);

        return pluginsProvider;
    }

    @Override
    public void registerPluginsListener(PluginsListener pluginsListener) {
        this.felixOSGIContainer.registerPluginsListener(pluginsListener);
    }

    @Override
    public void unregisterPluginsListener(PluginsListener pluginsListener) {
        this.felixOSGIContainer.unregisterPluginsListener(pluginsListener);
    }




}

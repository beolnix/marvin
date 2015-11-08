package com.beolnix.marvin.plugins.providers.osgi;


import com.beolnix.marvin.config.api.ConfigurationProvider;

import com.beolnix.marvin.config.api.error.ConfigurationException;
import com.beolnix.marvin.config.api.model.Configuration;
import com.beolnix.marvin.plugins.api.PluginsListener;
import com.beolnix.marvin.plugins.api.PluginsManager;
import com.beolnix.marvin.plugins.api.PluginsProvider;
import com.beolnix.marvin.plugins.api.error.PluginsProviderConfigurationException;
import org.apache.commons.io.FileUtils;
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
        pluginsProvider.copySystemBundles();
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

    private void copySystemBundles() throws PluginsProviderConfigurationException {

        try {
            Configuration configuration = configurationProvider.getConfiguration();
            FileUtils.copyFile(new File(configuration.getPluginSettings().getLibsPath() + "/org.apache.felix.fileinstall-3.1.10.jar"),
                new File(configuration.getPluginSettings().getSystemDeployPath() + "/org.apache.felix.fileinstall-3.1.10.jar"),
                true);
        } catch (IOException | ConfigurationException e) {
            throw new PluginsProviderConfigurationException(e);
        }
    }




}

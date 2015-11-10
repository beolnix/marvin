package com.beolnix.marvin.plugin.stubs

import com.beolnix.marvin.config.api.BotSettings
import com.beolnix.marvin.config.api.ConfigurationProvider
import com.beolnix.marvin.config.api.error.ConfigurationException
import com.beolnix.marvin.config.api.model.Configuration
import com.beolnix.marvin.config.api.model.PluginsSettings

/**
 * Created by beolnix on 09/11/15.
 */
class ConfigProviderImpl implements ConfigurationProvider {
    @Override
    Configuration getConfiguration() throws ConfigurationException {
        return null
    }

    @Override
    Map<String, BotSettings> getBotSettings() throws ConfigurationException {
        return null
    }

    @Override
    PluginsSettings getPluginSettings() throws ConfigurationException {
        PluginsSettings ps = new PluginsSettings()
        ps.cachePath = "build/cache"
        ps.dirPath = "build/dir"
        ps.libsPath = "lib"
        ps.logsPath = "build/logs"
        ps.managerPassword = "test"
        ps.pluginsDeployPath = "build/plugins"
        ps.systemDeployPath = "build/system"
        ps.tmpPath = "build/tmp"
        ps.pollPeriod = 200
        return ps
    }

    @Override
    void updateConfiguration(Configuration configuration) throws ConfigurationException {

    }
}

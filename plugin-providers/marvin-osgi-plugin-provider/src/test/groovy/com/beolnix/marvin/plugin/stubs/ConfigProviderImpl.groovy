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
        ps.setCachePath("build/cache")
        ps.setDirPath("build/dir")
        ps.setLibsPath("lib")
        ps.setLogsPath("build/logs")
        ps.setManagerPassword("test")
        ps.setPluginsDeployPath("build/plugins")
        ps.setSystemDeployPath("build/system")
        ps.setTmpPath("build/tmp")
        return ps
    }

    @Override
    void updateConfiguration(Configuration configuration) throws ConfigurationException {

    }
}

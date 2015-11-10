package com.beolnix.marvin.plugin

import com.beolnix.marvin.config.api.ConfigurationProvider
import com.beolnix.marvin.config.api.model.PluginsSettings
import com.beolnix.marvin.plugins.providers.osgi.FelixOSGIContainer
import org.junit.Test
import org.osgi.framework.BundleContext
import org.osgi.framework.launch.Framework
import org.osgi.framework.launch.FrameworkFactory
import org.osgi.service.startlevel.StartLevel

import static org.junit.Assert.assertNotNull;

/**
 * Created by DAtmakin on 11/10/2015.
 */
class FelixOSGIContainerTestCase {

    @Test
    public void testCreateNewInstance() {
        def configProvider = [
                getPluginSettings: { getPluginSettings() }
        ] as ConfigurationProvider

        FelixOSGIContainer container = FelixOSGIContainer.createNewInstance(configProvider, new org.apache.felix.framework.FrameworkFactory())
        assertNotNull(container)
    }

    private PluginsSettings getPluginSettings() {
        PluginsSettings ps = new PluginsSettings();
        ps.cachePath = "build/cache"
        ps.dirPath = "build/dir"
        ps.libsPath = "lib"
        ps.logsPath = "build/logs"
        ps.managerPassword = "test"
        ps.pluginsDeployPath = "build/deploy"
        ps.systemDeployPath  = "build/system"
        ps.tmpPath = "build/tmp"
        ps.pollPeriod = 200

        ps
    }
}

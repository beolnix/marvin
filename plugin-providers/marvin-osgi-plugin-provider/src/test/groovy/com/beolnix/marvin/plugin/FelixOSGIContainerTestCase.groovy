package com.beolnix.marvin.plugin

import com.beolnix.marvin.config.api.ConfigurationProvider
import com.beolnix.marvin.config.api.model.PluginsSettings
import com.beolnix.marvin.plugins.api.IMPlugin
import com.beolnix.marvin.plugins.api.PluginsListener
import com.beolnix.marvin.plugins.providers.osgi.FelixOSGIContainer
import org.junit.Test
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceEvent
import org.osgi.framework.ServiceReference
import org.osgi.framework.launch.Framework
import org.osgi.framework.launch.FrameworkFactory
import org.osgi.service.startlevel.StartLevel

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testServiceChanged() {
        def isDeployPluginExecuted = false
        def listener = [
                deployPlugin: {
                    isDeployPluginExecuted = true
                }
        ] as PluginsListener
        def configProvider = [
                getPluginSettings: { getPluginSettings() }
        ] as ConfigurationProvider

        FelixOSGIContainer container = FelixOSGIContainer.createNewInstance(configProvider, new org.apache.felix.framework.FrameworkFactory())
        container.registerPluginsListener(listener)

        def plugin = [
                getPluginName: {
                    "testPlugin"
                }
        ] as IMPlugin
        def bundleContext = [
                getService : {
                    plugin
                }
        ] as BundleContext

        def serviceRef = [] as ServiceReference
        def event = new ServiceEvent(1, serviceRef)
        container.bundleContext = bundleContext
        container.serviceChanged(event)

        assertTrue(isDeployPluginExecuted)
    }

    private PluginsSettings getPluginSettings() {
        Random rand = new Random();

        PluginsSettings ps = new PluginsSettings();
        ps.cachePath = "build/cache${rand.nextInt()}"
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

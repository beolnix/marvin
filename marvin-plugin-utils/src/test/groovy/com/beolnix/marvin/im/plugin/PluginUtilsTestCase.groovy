package com.beolnix.marvin.im.plugin

import org.junit.Test
import org.osgi.framework.BundleContext

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue;

/**
 * Created by DAtmakin on 11/10/2015.
 */
class PluginUtilsTestCase {

    @Test
    public void testGetLogger() {
        def pluginUtils = new PluginUtils()
        def logger = pluginUtils.getLogger('build/logs', "testPlugin")

        assertNotNull(logger)
    }

    @Test
    public void testGetPluginHomeDir() {
        def pluginUtils = new PluginUtils()
        File homeDir = pluginUtils.getPluginHomeDir('build/home', "testPlugin")

        assertNotNull(homeDir)
        assertTrue(homeDir.exists())
    }

    @Test
    public void testIsBlank1() {
        assertTrue(new PluginUtils().isBlank(" "))
    }

    @Test
    public void testIsBlank2() {
        assertTrue(new PluginUtils().isBlank(null))
    }

    @Test
    public void testIsBlank3() {
        assertFalse(new PluginUtils().isBlank("sdf asf "))
    }

    @Test
    public void testIsBlank4() {
        assertTrue(new PluginUtils().isBlank(null))
    }
}

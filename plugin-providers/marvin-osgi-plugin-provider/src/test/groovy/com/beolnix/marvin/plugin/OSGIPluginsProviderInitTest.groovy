package com.beolnix.marvin.plugin

import com.beolnix.marvin.plugins.api.PluginsProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import static org.junit.Assert.assertNotNull;

/**
 * Created by beolnix on 09/11/15.
 */
@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations = ["/spring/osgi-plugin-provider-context.xml", "/spring/stubs-context.xml"])
public class OSGIPluginsProviderInitTest {

    @Autowired
    PluginsProvider pluginsProvider

    @Test
    public void initTest() {
        assertNotNull(pluginsProvider)
    }
}

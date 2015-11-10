package com.beolnix.marvin.config

import com.beolnix.marvin.config.api.ConfigurationProvider
import com.beolnix.marvin.config.api.model.Bot
import com.beolnix.marvin.config.api.model.Configuration
import com.beolnix.marvin.config.api.model.PluginsSettings
import com.beolnix.marvin.config.api.model.Property
import com.beolnix.marvin.im.irc.model.IrcBotSettings
import org.junit.Test
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.oxm.jaxb.Jaxb2Marshaller

import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.ContextConfiguration

import org.springframework.beans.factory.annotation.Autowired

/**
 * Author: Danila Atmakin
 * Email: atmakin.dv@gmail.com
 * Date: 20.04.12
 * Time: 17:49
 */

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations = ["/app-context/config-context.xml"])
class XMLConfigurationProviderTestCase {

    @Autowired
    ConfigurationProvider xmlConfigurationProvider

    @Autowired
    @Qualifier("marshaller")
    Jaxb2Marshaller marshaller

    /**
     * validating unmarshalled values
     */
    @Test
    public void unmarshalTest() {
        System.getProperties()[XmlConfigurationProvider.CONFIG_PROPERTY_NAME] = "src/test/resources/config/data/sample-config.xml"
        Configuration configuration = xmlConfigurationProvider.getConfiguration()
        assertEquals(getExpectedConfiguration().toString(), configuration.toString())
    }

    def getExpectedConfiguration() {
        def config = new Configuration()

        def bot = new Bot()
        bot.name = 'ircBot1'
        bot.protocol = 'IRC'
        bot.properties += new Property(IrcBotSettings.NICKNAME, 'marvin|irc')
        bot.properties += new Property(IrcBotSettings.CHANNEL_NAME, '#dev')
        bot.properties += new Property(IrcBotSettings.CHANNEL_PASSWORD, 'devdev')
        bot.properties += new Property(IrcBotSettings.SERVER_NAME, 'irc.dalnet.ru')
        bot.properties += new Property(IrcBotSettings.PORT_NUMBER, '6665')
        bot.properties += new Property(IrcBotSettings.CHARSET, 'KOI8-R')
        config.bots.add(bot)

        def pluginSettings = new PluginsSettings()
        pluginSettings.managerPassword = 'test'
        pluginSettings.libsPath = 'lib'
        pluginSettings.systemDeployPath = 'system/systemBundles'
        pluginSettings.pluginsDeployPath = 'plugins'
        pluginSettings.cachePath = 'system/bundlesCache'
        pluginSettings.tmpPath = 'system/bundlesTmp'
        pluginSettings.logsPath = 'logs'
        pluginSettings.dirPath = 'system/plugins-home'
        pluginSettings.pollPeriod = 2000
        config.pluginSettings = pluginSettings

        config
    }

}

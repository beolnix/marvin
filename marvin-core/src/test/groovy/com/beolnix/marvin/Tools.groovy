package com.beolnix.marvin

import com.beolnix.marvin.config.api.model.Bot
import com.beolnix.marvin.config.api.model.Configuration
import com.beolnix.marvin.config.api.ConfigurationProvider
import com.beolnix.marvin.config.api.model.Property
import com.beolnix.marvin.config.api.model.Configuration
import com.beolnix.marvin.config.api.model.Property
import com.beolnix.marvin.im.irc.model.IrcBotSettings
import org.junit.Ignore
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.oxm.jaxb.Jaxb2Marshaller

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.SchemaOutputResolver
import javax.xml.transform.Result
import javax.xml.transform.stream.StreamResult

/**
 * Created by DAtmakin on 11/3/2015.
 */

class Tools {

    @Autowired
    ConfigurationProvider xmlConfigurationProvider

    @Autowired
    @Qualifier("marshaller")
    Jaxb2Marshaller marshaller

    @Test
    @Ignore
    public void marshalTest() {
        Configuration configuration = new Configuration()
        Bot bot = new Bot()
        bot.name = "ircBot1"
        bot.protocol = "IRC"
        bot.properties.add(new Property(IrcBotSettings.NICKNAME, "marvin|irc"))
        bot.properties.add(new Property(IrcBotSettings.CHANNEL_NAME, "#dev"))
        bot.properties.add(new Property(IrcBotSettings.CHANNEL_PASSWORD, "devdev"))
        bot.properties.add(new Property(IrcBotSettings.SERVER_NAME, "irc.dalnet.ru"))
        bot.properties.add(new Property(IrcBotSettings.PORT_NUMBER, "6665"))
        bot.properties.add(new Property(IrcBotSettings.CHARSET, "KOI8-R"))


        configuration.bots.add(bot)
        println("\n\n")
        ((Jaxb2Marshaller)marshaller).properties[Marshaller.JAXB_FORMATTED_OUTPUT] = Boolean.TRUE
        marshaller.marshal(configuration, new StreamResult(System.out))
        println("\n\n")

    }

    @Test
    @Ignore
    public void schemaGeneration() {
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
        SchemaOutputResolver sor = new MySchemaOutputResolver();
        jaxbContext.generateSchema(sor);
    }

    public class MySchemaOutputResolver extends SchemaOutputResolver {

        @Override
        Result createOutput(java.lang.String namespaceUri, java.lang.String suggestedFileName) throws IOException {
            StreamResult result = new StreamResult(System.out);
            result.setSystemId("out");
            return result;
        }

    }

}

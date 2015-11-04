package config

import com.beolnix.marvin.config.XmlConfigurationProvider
import com.beolnix.marvin.config.api.ConfigurationProvider
import com.beolnix.marvin.config.api.Configuration
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
        String expected = new File("src/test/resources/config/data/expected_output.txt").text
        assertEquals(expected, configuration.toString())
    }

}

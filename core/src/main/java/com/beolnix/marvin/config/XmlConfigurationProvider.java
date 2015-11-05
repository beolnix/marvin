package com.beolnix.marvin.config;

import com.beolnix.marvin.config.api.*;
import com.beolnix.marvin.config.api.error.ConfigurationException;
import com.beolnix.marvin.config.api.model.Bot;
import com.beolnix.marvin.config.api.model.Configuration;
import com.beolnix.marvin.config.api.model.PluginsSettings;
import org.apache.log4j.Logger;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Created by beolnix on 31/10/15.
 */

public class XmlConfigurationProvider implements ConfigurationProvider {

    // dependencies
    private final Marshaller marshaller;        // if there will be a plugin in future which changes the config..
    private final Unmarshaller unmarshaller;

    // state
    private Configuration configuration;
    final private Lock lock = new ReentrantLock();

    // constants
    public static final String ENV_PARAM_NAME = "MARVIN_XML_CONFIG_PATH";
    public static final String CONFIG_PROPERTY_NAME = "marvin.config.xml.path";
    public static final String DEFAULT_CONFIG_PATH = "config.xml";
    private static final Logger logger = Logger.getLogger(XmlConfigurationProvider.class);

    public XmlConfigurationProvider(Marshaller marshaller, Unmarshaller unmarshaller) {
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
    }

    public Configuration getConfiguration() throws ConfigurationException {
        if (configuration == null) {
            lock.lock();
            if (configuration == null) {
                this.configuration = readConfig();
            }
            lock.unlock();
        }
        return configuration;
    }

    @Override
    public Map<String, BotSettings> getBotSettings() throws ConfigurationException {
        return getConfiguration().getBots().stream().collect(Collectors.toMap(
                Bot::getName,
                e -> (BotSettings) e
        ));
    }

    @Override
    public PluginsSettings getPluginSettings() throws ConfigurationException {
        return getConfiguration().getPluginSettings();
    }

    private Configuration readConfig() throws ConfigurationException {
        String configPath = getConfigPath();
        String absoluteConfigPath = checkFilePermissions(configPath);
        return unmarshalConfigFile(absoluteConfigPath);
    }

    private String getConfigPath() {
        String configPath = System.getProperty(CONFIG_PROPERTY_NAME);
        if (configPath == null) {
            configPath = System.getenv(ENV_PARAM_NAME);
        }
        if (configPath == null) {
            configPath = DEFAULT_CONFIG_PATH;
        }
        return configPath;
    }


    private String checkFilePermissions(String configPath) throws ConfigurationException {
        File file = new File(configPath);
        if (!file.exists() || !file.isFile()) {
            throw new ConfigurationException("file doesn't exist: " + file.getAbsolutePath());
        }

        if (!file.canRead()) {
            throw new ConfigurationException("can't read config file, permissions error.");
        }
        return file.getAbsolutePath();
    }

    private Configuration unmarshalConfigFile(String configPath) throws ConfigurationException {
        logger.debug("try to unmarshal ConfigFile with config path: " + configPath);

        try {
            InputStream is = new FileInputStream(configPath);
            Configuration configuration = (Configuration) unmarshaller.unmarshal(new StreamSource(is));
            logger.debug("Config file unmarshalled successfully.");
            return configuration;
        } catch (Exception e) {
            logger.error("can't parse configuration file: " + e.getMessage());
            throw new ConfigurationException(e);
        }
    }

    @Override
    public void updateConfiguration(Configuration configuration) throws ConfigurationException {

    }
}

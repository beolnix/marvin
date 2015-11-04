package com.beolnix.marvin.plugins.providers.osgi;

import com.beolnix.marvin.config.api.Configuration;
import com.beolnix.marvin.config.api.ConfigurationProvider;
import com.beolnix.marvin.config.api.PluginsSettings;
import com.beolnix.marvin.config.api.error.ConfigurationException;
import com.beolnix.marvin.plugins.api.PluginsListener;
import com.beolnix.marvin.plugins.api.IMPlugin;
import com.beolnix.marvin.plugins.api.error.PluginsProviderConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.fileinstall.internal.DirectoryWatcher;
import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.main.AutoProcessor;
import org.apache.log4j.Logger;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by beolnix on 31/10/15.
 */
public class FelixOSGiContainer implements ServiceListener {

    // dependencies
    private final ConfigurationProvider configurationProvider;

    // state
    private Framework framework = null;
    private BundleContext bundleContext = null;
    private List<PluginsListener> pluginsListenerList = new ArrayList<>();
    final private List<IMPlugin> pluginList = new ArrayList<>();

    // constants
    private final static Logger logger = Logger.getLogger(FelixOSGiContainer.class);

    private FelixOSGiContainer(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    public static FelixOSGiContainer createNewInstance(ConfigurationProvider configurationProvider) throws PluginsProviderConfigurationException, ConfigurationException {
        FelixOSGiContainer felixOSGiContainer = new FelixOSGiContainer(configurationProvider);
        felixOSGiContainer.checkConfiguration();
        felixOSGiContainer.initFramework();
        return felixOSGiContainer;
    }

    private void checkConfiguration() throws PluginsProviderConfigurationException {
        try {
            Configuration configuration = configurationProvider.getConfiguration();
            checkForWritable(configuration.getPluginSettings().getCachePath(), "cachePath");
            checkForWritable(configuration.getPluginSettings().getSystemDeployPath(), "systemDeployPath");
            checkForWritable(configuration.getPluginSettings().getPluginsDeployPath(), "pluginsDeployPath");
            checkForWritable(configuration.getPluginSettings().getLogsPath(), "logsPath");
            checkForWritable(configuration.getPluginSettings().getDirPath(), "dirPath");
        } catch (ConfigurationException e) {
            throw new PluginsProviderConfigurationException(e);
        }
    }

    private void checkForWritable(String dirPath, String dirName) throws PluginsProviderConfigurationException {
        if (StringUtils.isBlank(dirPath))
            throw new PluginsProviderConfigurationException(dirName + " must be provided");

        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        if(!file.isDirectory() || !file.canWrite())
            throw new PluginsProviderConfigurationException("can't write to dir with path: " + file.getAbsolutePath());
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.REGISTERED) {
            ServiceReference ref = event.getServiceReference();
            Object testObject = bundleContext.getService(ref);
            if (testObject instanceof IMPlugin) {
                IMPlugin newIMPlugin = (IMPlugin) bundleContext.getService(ref);
                synchronized (pluginList) {
                    pluginList.add(newIMPlugin);
                }
                for (PluginsListener pluginsListener : pluginsListenerList) {
                    pluginsListener.deployPlugin(newIMPlugin);
                }

                logger.info("registered new plugin: " + newIMPlugin.getPluginName());
            }
        } else if (event.getType() == ServiceEvent.UNREGISTERING) {
            ServiceReference ref = event.getServiceReference();
            Object testObject = bundleContext.getService(ref);
            if (testObject instanceof IMPlugin) {
                IMPlugin imPlugin = (IMPlugin) bundleContext.getService(ref);
                synchronized (pluginList) {
                    pluginList.remove(imPlugin);
                }
                for (PluginsListener pluginsListener : pluginsListenerList) {
                    pluginsListener.undeployPlugin(imPlugin);
                }

                logger.info("unregistered IMPlugin: " + imPlugin.getPluginName());
            }
        }
    }

    private Properties getConfig() throws PluginsProviderConfigurationException {
        PluginsSettings ps = null;
        try {
            ps = configurationProvider.getPluginSettings();
        } catch (ConfigurationException e) {
            throw new PluginsProviderConfigurationException(e);
        }

        if (ps == null) {
            throw new PluginsProviderConfigurationException("PluginSettings are not provided");
        }

        Properties configProps = new Properties();

        configProps.setProperty(AutoProcessor.AUTO_DEPLOY_DIR_PROPERY, ps.getSystemDeployPath());
        configProps.setProperty(Constants.FRAMEWORK_STORAGE, ps.getCachePath());
        configProps.setProperty(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                "com.beolnix.marvin.im.api; version=1.0.0," +
                        "com.beolnix.marvin.im.api.error; version=1.0.0," +
                        "com.beolnix.marvin.plugins.api; version=1.0.0");
        configProps.setProperty(AutoProcessor.AUTO_DEPLOY_ACTION_PROPERY,
                AutoProcessor.AUTO_DEPLOY_INSTALL_VALUE + ", " + AutoProcessor.AUTO_DEPLOY_START_VALUE);
        configProps.setProperty(DirectoryWatcher.DIR, new File(ps.getPluginsDeployPath()).getAbsolutePath());
        configProps.setProperty(DirectoryWatcher.TMPDIR, new File(ps.getTmpPath()).getAbsolutePath());
        configProps.setProperty(DirectoryWatcher.POLL, "2000");
        configProps.setProperty(IMPlugin.LOGS_PATH_PARAM_NAME, ps.getLogsPath());
        configProps.setProperty(IMPlugin.DIRECTORY_PARAM_NAME, ps.getDirPath());

        logger.info("configuring autodeploy for dir " + new File(ps.getPluginsDeployPath()).getAbsolutePath());

        return configProps;

    }


    private void initFramework() throws PluginsProviderConfigurationException {
        Properties configProps = getConfig();
        try {
            logger.info("starting osgi container");
            FrameworkFactory factory = new FrameworkFactory();
            framework = factory.newFramework(configProps);
            framework.init();
            AutoProcessor.process(configProps, framework.getBundleContext());
            framework.getBundleContext().addServiceListener(this);
            bundleContext = framework.getBundleContext();
            framework.start();

            logger.info("osgi container started successfully");

        } catch (Exception ex) {
            logger.error("Could not create osgi framework: " + ex.getMessage());
            throw new PluginsProviderConfigurationException(ex);
        }
    }

    public void registerPluginsListener(PluginsListener pluginsListener) {
        synchronized (pluginList) {
            pluginList.forEach(pluginsListener::deployPlugin);
        }
        pluginsListenerList.add(pluginsListener);
    }

    public void unregisterPluginsListener(PluginsListener pluginsListener) {
        pluginsListenerList.remove(pluginsListener);
    }
}

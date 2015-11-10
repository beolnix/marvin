package com.beolnix.marvin.plugins.providers.osgi;

import com.beolnix.marvin.config.api.ConfigurationProvider;
import com.beolnix.marvin.config.api.error.ConfigurationException;
import com.beolnix.marvin.config.api.model.PluginsSettings;
import com.beolnix.marvin.plugins.api.PluginsListener;
import com.beolnix.marvin.plugins.api.IMPlugin;
import com.beolnix.marvin.plugins.api.error.PluginsProviderConfigurationException;
import org.apache.felix.fileinstall.internal.DirectoryWatcher;
import org.osgi.framework.launch.FrameworkFactory;
import org.apache.felix.main.AutoProcessor;
import org.apache.log4j.*;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;

import static org.osgi.framework.FrameworkEvent.ERROR;
import static org.osgi.framework.FrameworkEvent.WARNING;

import java.util.*;

/**
 * Created by beolnix on 31/10/15.
 */
public class FelixOSGIContainer implements ServiceListener, FrameworkListener {

    // dependencies
    private final Framework framework;


    // state
    private List<PluginsListener> pluginsListenerList = new ArrayList<>();
    final private List<IMPlugin> pluginList = new ArrayList<>();
    private BundleContext bundleContext;

    // constants
    private final static Utils utils = new Utils();
    private final static Logger logger = Logger.getLogger(FelixOSGIContainer.class);
    public static final String FELIX_FILEINSTALL_ARTIFACT = "org.apache.felix.fileinstall-3.1.10.jar";

    private FelixOSGIContainer(Framework framework) {
        this.framework = framework;
        framework.getBundleContext().addServiceListener(this);
        framework.getBundleContext().addFrameworkListener(this);
        this.bundleContext = framework.getBundleContext();
    }

    public static FelixOSGIContainer createNewInstance(ConfigurationProvider configurationProvider, FrameworkFactory frameworkFactory) throws PluginsProviderConfigurationException {
        utils.checkConfiguration(configurationProvider);
        logger.info("starting osgi container");
        try {
            Map<String, String> configProps = utils.getConfig(configurationProvider);
            utils.copySystemBundles(configurationProvider);

            Framework framework = frameworkFactory.newFramework(configProps);
            framework.init();
            AutoProcessor.process(configProps, framework.getBundleContext());

            FelixOSGIContainer felixOSGIContainer = new FelixOSGIContainer(framework);
            framework.start();

            logger.info("osgi container started successfully");
            return felixOSGIContainer;
        } catch (PluginsProviderConfigurationException | BundleException e) {
            logger.error("Could not create osgi framework: " + e.getMessage());
            throw new PluginsProviderConfigurationException(e);
        }
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

    @Override
    public void frameworkEvent(FrameworkEvent event) {
        switch (event.getType()) {
            case ERROR:
                if (event.getThrowable() != null) {
                    logger.error(event.getThrowable().getMessage(), event.getThrowable());
                    pluginsListenerList.forEach( e -> {
                        if (event.getBundle() != null) {
                            e.onError(event.getBundle().getSymbolicName(), event.getThrowable());
                        } else {
                            e.onError("Undefined bundle.", event.getThrowable());
                        }
                    });
                } else if (event.getBundle() != null) {
                    logger.error("Boundle '" + event.getBundle().getSymbolicName() +
                            "' has been stopped because of the error.");
                }
                break;
            case WARNING:
                if (event.getThrowable() != null) {
                    logger.error(event.getThrowable().getMessage(), event.getThrowable());
                } else if (event.getBundle() != null) {
                    logger.error("Boundle '" + event.getBundle().getSymbolicName() +
                            "' has been stopped because of the error.");
                }
                break;
            default:
                if (event.getThrowable() != null) {
                    logger.error(event.getThrowable().getMessage(), event.getThrowable());
                }
                break;
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

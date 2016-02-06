package com.beolnix.marvin.plugins.providers.osgi;

import com.beolnix.marvin.config.api.ConfigurationProvider;
import com.beolnix.marvin.config.api.error.ConfigurationException;
import com.beolnix.marvin.config.api.model.PluginsSettings;
import com.beolnix.marvin.plugins.api.IMPlugin;
import com.beolnix.marvin.plugins.api.error.PluginsProviderConfigurationException;
import org.apache.felix.fileinstall.internal.DirectoryWatcher;
import org.apache.felix.main.AutoProcessor;
import org.apache.log4j.Logger;
import org.osgi.framework.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.beolnix.marvin.plugins.providers.osgi.FelixOSGIContainer.FELIX_FILEINSTALL_ARTIFACT;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by DAtmakin on 11/10/2015.
 */
public class Utils {
    private final static Logger logger = Logger.getLogger(Utils.class);

    public void checkConfiguration(ConfigurationProvider configurationProvider) throws PluginsProviderConfigurationException {
        try {
            PluginsSettings ps = configurationProvider.getPluginSettings();
            checkForWritable(ps.getCachePath(), "cachePath");
            checkForWritable(ps.getSystemDeployPath(), "systemDeployPath");
            checkForWritable(ps.getPluginsDeployPath(), "pluginsDeployPath");
            checkForWritable(ps.getLogsPath(), "logsPath");
            checkForWritable(ps.getDirPath(), "dirPath");
            checkNumber(ps.getPollPeriod());
        } catch (ConfigurationException e) {
            throw new PluginsProviderConfigurationException(e);
        }
    }

    public void checkNumber(Integer value) throws PluginsProviderConfigurationException {
        if (value == null) {
            throw new PluginsProviderConfigurationException("pollPeriod must be provided");
        }

        if (value < 1) {
            throw new PluginsProviderConfigurationException("pollPeriod must be positive");
        }
    }

    public void checkForWritable(String dirPath, String dirName) throws PluginsProviderConfigurationException {
        if (isBlank(dirPath))
            throw new PluginsProviderConfigurationException(dirName + " must be provided");

        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        if (!file.isDirectory() || !file.canWrite())
            throw new PluginsProviderConfigurationException("can't write to dir with path: " + file.getAbsolutePath());
    }

    public boolean isBlank(String value) {
        if (value == null) {
            return true;
        }

        return value.replace(" ", "").length() == 0;
    }

    public Map<String, String> getConfig(ConfigurationProvider configurationProvider) throws PluginsProviderConfigurationException {
        PluginsSettings ps = null;
        try {
            ps = configurationProvider.getPluginSettings();
        } catch (ConfigurationException e) {
            throw new PluginsProviderConfigurationException(e);
        }

        if (ps == null) {
            throw new PluginsProviderConfigurationException("PluginSettings are not provided");
        }

        Map<String, String> configProps = new HashMap<>();

        configProps.put(AutoProcessor.AUTO_DEPLOY_DIR_PROPERY, ps.getSystemDeployPath());
        configProps.put(Constants.FRAMEWORK_STORAGE, ps.getCachePath());
        configProps.put(
                Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                "com.beolnix.marvin.im.api; version=1.0.0," +
                        "com.beolnix.marvin.im.api.model; version=1.0.0," +
                        "com.beolnix.marvin.im.api.error; version=1.0.0," +
                        "com.beolnix.marvin.config.api; version=1.0.0," +
                        "com.beolnix.marvin.config.api.model; version=1.0.0," +
                        "com.beolnix.marvin.config.api.error; version=1.0.0," +
                        "com.beolnix.marvin.plugins.api.error; version=1.0.0," +
                        "com.beolnix.marvin.plugins.api; version=1.0.0"
        );
        configProps.put(Constants.FRAMEWORK_BOOTDELEGATION, "*");
        configProps.put(AutoProcessor.AUTO_DEPLOY_ACTION_PROPERY,
                AutoProcessor.AUTO_DEPLOY_INSTALL_VALUE + ", " + AutoProcessor.AUTO_DEPLOY_START_VALUE);
        configProps.put(DirectoryWatcher.DIR, new File(ps.getPluginsDeployPath()).getAbsolutePath());
        configProps.put(DirectoryWatcher.TMPDIR, new File(ps.getTmpPath()).getAbsolutePath());
        configProps.put(DirectoryWatcher.POLL, ps.getPollPeriod().toString());

        logger.info("auto deploy is configured for dir: " + ps.getPluginsDeployPath());
        logger.info("auto deploy dir is triggered every: " + ps.getPollPeriod());

        return configProps;
    }

    public void copySystemBundles(ConfigurationProvider configurationProvider) throws PluginsProviderConfigurationException {

        try {
            PluginsSettings ps = configurationProvider.getPluginSettings();
            Files.copy(Paths.get(ps.getLibsPath() + "/" + FELIX_FILEINSTALL_ARTIFACT),
                    Paths.get(ps.getSystemDeployPath() + "/" + FELIX_FILEINSTALL_ARTIFACT), REPLACE_EXISTING);
        } catch (IOException | ConfigurationException e) {
            throw new PluginsProviderConfigurationException(e);
        }
    }
}

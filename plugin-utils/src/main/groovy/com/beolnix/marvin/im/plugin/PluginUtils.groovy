package com.beolnix.marvin.im.plugin

import com.beolnix.marvin.plugins.api.IMPlugin
import org.osgi.framework.BundleContext
import org.apache.log4j.*

/**
 * Author: Atmakin Danila 
 * Email: atmakin.dv@gmail.com
 * Date: 29.11.11
 * Time: 12:46
 */
class PluginUtils {
    public Logger getLogger(BundleContext bundleContext, String pluginName) {
        String logsPath = ''
        if (bundleContext)
             logsPath = bundleContext.getProperty(IMPlugin.LOGS_PATH_PARAM_NAME)
        if (org.apache.commons.lang.StringUtils.isEmpty(logsPath))
            logsPath = 'logs'

        Layout layout = new PatternLayout('%d{HH:mm:ss} %-5p: %c{2}.%M() - %m%n')

        Appender appender = new DailyRollingFileAppender(layout,
                                                         "${logsPath}/${pluginName}.log",
                                                         "'.'yyyy-MM-dd'.log'")
        Logger rootLogger = Logger.getRootLogger()
        rootLogger.setLevel(Level.DEBUG)
        rootLogger.addAppender(appender)
        return rootLogger
    }

    public File getPluginHomeDir(BundleContext bundleContext, String pluginName) {
        String dirPath = bundleContext.getProperty(IMPlugin.LOGS_PATH_PARAM_NAME)
        if (org.apache.commons.lang.StringUtils.isEmpty(dirPath))
            dirPath = 'plugins-home'

        File dir = new File("${dirPath}/${pluginName}")
        dir.mkdirs()

        return dir
    }
}

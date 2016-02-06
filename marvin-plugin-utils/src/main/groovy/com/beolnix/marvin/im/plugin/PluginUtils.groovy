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
    public Logger getLogger(String logsPath, String pluginName) {
        Layout layout = new PatternLayout('%d{HH:mm:ss} %-5p: %c{2}.%M() - %m%n')

        Appender appender = new DailyRollingFileAppender(layout,
                                                         "${logsPath}/${pluginName}.log",
                                                         "'.'yyyy-MM-dd'.log'")
        Logger rootLogger = Logger.getRootLogger()
        rootLogger.setLevel(Level.DEBUG)
        rootLogger.addAppender(appender)
        return rootLogger
    }

    public File getPluginHomeDir(String dirPath, String pluginName) {
        File dir = new File("${dirPath}/${pluginName}")
        dir.mkdirs()

        return dir
    }

    boolean isBlank(String value) {
        if (value == null) {
            return true
        }

        return value.replace(" ", "").length() == 0
    }
}

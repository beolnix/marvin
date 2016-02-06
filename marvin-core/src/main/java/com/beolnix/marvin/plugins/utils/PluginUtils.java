package com.beolnix.marvin.plugins.utils;

import org.apache.log4j.*;

import java.io.File;
import java.io.IOException;

/**
 * Created by beolnix on 06/02/16.
 */
public class PluginUtils {

    public Logger getLogger(String logsPath, String pluginName) {
        Layout layout = new PatternLayout("%d{HH:mm:ss} %-5p: %c{2}.%M() - %m%n");

        try {
            Appender appender = new DailyRollingFileAppender(layout,
                    logsPath + "/" + pluginName + ".log",
                    "'.'yyyy-MM-dd'.log'");
            Logger rootLogger = Logger.getRootLogger();
            rootLogger.setLevel(Level.DEBUG);
            rootLogger.addAppender(appender);
            return rootLogger;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getPluginHomeDir(String dirPath, String pluginName) {
        File dir = new File(dirPath + "/" + pluginName);
        dir.mkdirs();

        return dir;
    }

    boolean isBlank(String value) {
        if (value == null) {
            return true;
        }

        return value.replace(" ", "").length() == 0;
    }
}

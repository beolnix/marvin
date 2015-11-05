package com.beolnix.marvin;


import com.beolnix.marvin.config.XmlConfigurationProvider;
import com.beolnix.marvin.config.api.error.ConfigurationException;
import com.beolnix.marvin.im.api.IMSessionManager;
import com.beolnix.marvin.plugins.api.PluginsManager;
import com.beolnix.trpg.cmdargs.ArgumentsParser;

import com.beolnix.trpg.cmdargs.error.UnknownFlag;
import com.beolnix.trpg.cmdargs.impl.DefaultArgumentsParser;
import com.beolnix.trpg.cmdargs.model.CommandLineArgument;
import com.beolnix.trpg.cmdargs.utils.CommandLineArgumentBuilder;
import com.beolnix.trpg.cmdargs.utils.DefaultArguments;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Author: beolnix
 * Email: beolnix@gmail.com
 * Date: 30.10.11
 * Time: 17:49
 */
class App {

    private static final Logger logger = Logger.getLogger(App.class);

    private static final String SPRING_CONTEXT_LOCATION = "classpath:/app-context/application-context.xml";

    private App() {} // use static main

    public static void main(String[] args) {
        processCommandLineArgs(args);
        initApp();
        keepAliveMainThread();
    }

    private static void keepAliveMainThread() {
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("I've got an interrupted exception. bye-bye.", e);
            }
        }
    }

    private static void initApp() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(SPRING_CONTEXT_LOCATION);
        IMSessionManager imSessionManager = ctx.getBean(IMSessionManager.class);
        PluginsManager pluginsManager = ctx.getBean(PluginsManager.class);

        try {
            imSessionManager.createSessions(pluginsManager);
        } catch (ConfigurationException e) {
            logger.error("Seems app isn't configured right.", e);
        }

    }

    private static final CommandLineArgument configFlag = new CommandLineArgumentBuilder()
            .withDescription("is used to provide a path to the config.")
            .withExample("-c ./config.xml")
            .withFlags("-c", "--config")
            .build();

    private static final CommandLineArgument versionFlag = new CommandLineArgumentBuilder()
            .withDescription("displayes version of the app.")
            .withExample("-v")
            .withFlags("-v", "--version")
            .build();

    private static final ArgumentsParser argumentsParser = new DefaultArgumentsParser(configFlag, versionFlag);

    private static void processCommandLineArgs(String[] args) {
        try {
            Map<CommandLineArgument, String> passedArgs = argumentsParser.parse(args);
            if (passedArgs.containsKey(configFlag) && passedArgs.get(configFlag) != null) {
                System.getProperties().setProperty(XmlConfigurationProvider.CONFIG_PROPERTY_NAME, passedArgs.get(configFlag));
            }

            if (passedArgs.containsKey(DefaultArguments.helpCommandLineArgument)) {
                System.out.println(argumentsParser.getHelpMessage());
                System.exit(0);
            }

            if (passedArgs.containsKey(versionFlag)) {
                System.out.println("Version of the app is: " + VersionHelper.getVersion());
                System.exit(0);
            }

        } catch (UnknownFlag e) {
            System.out.println("Argument '" + e.getUnknownFlag() + "' isn't supported. " +
                    "Please use -h to see the list of supported arguments.");
        }
    }

}

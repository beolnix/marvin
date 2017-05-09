## Project description

Marvin is a bot with dynamic osgi based plugin system. Right now it supports IRC, Skype, Telegram and Slack protocols only but it was designed with support of other protocols in mind.

## Project details
Version | State | Code link |
--- | --- | --- | 
0.2 | Stable | [0.2-release](https://github.com/beolnix/marvin/releases/tag/0.2-release) |
0.3-SNAPSHOT | In dev | [master](https://github.com/beolnix/marvin/) | 

## Requirements
It was tested on Mac OS X and Ubuntu 14.04. 
I guess it should also work fine on Windows if you run it as `java -jar core.jar`.

#### To run
* JDK 8 only

#### To build
* JDK 8
* Gradle 2.8
* Groovy 2.4.4

## Build from source
Just execute the following command and may the force be with you:
```
gradle clean packageMarvin
```

If everything was fine, the distr will be in **build/distr**. 

## Launch
Edit config.xml placed in a root dir of the distr and launch **marvin.sh**
You may also provide some specific path to the config using -c command line argument.
Please use -h to see the help.

## Distr layout
* marvin.sh - run script
* marvin.bat - run script for windows
* core.jar - bot core with Main class. It is an entry point and used to execute the bot from run scripts
* config.xml - default bot config with basic configuration
* **lib** - directory with all required libs
* **logs** - directory with logs
* **plugins** - directory scanned by bot for plagins to deploy.
* **system** - directory used by bot for his purposes

## Plugins deployment
To deploy plugin simply copy plugin jar to the plugins directory. If jar is correct marvin bot will pick it up on the fly. No restart is required.
To undeploy plugin simply remove plugin's jar from the plugins directory. Marvin bot will undeploy it on the fly. No restart is required.



## Plugins development
Basically you just need to implement:
- The `Plugin` interface
- The `Activator` to register your plugin in the system on deploy

You also need to pack it as a correct osgi boundle. Please use [echo-plugin](https://github.com/beolnix/marvin-echo-plugin) and [newyear-plugin](https://github.com/beolnix/marvin-newyear-plugin) as  examples.

## Plugins Integration testing
[marvin-osgi-plugins-int-test](https://github.com/beolnix/marvin-osgi-plugins-int-test) - Example of plugin integration test






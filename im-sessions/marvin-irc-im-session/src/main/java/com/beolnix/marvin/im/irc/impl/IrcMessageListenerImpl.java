package com.beolnix.marvin.im.irc.impl;

import com.beolnix.marvin.im.IMSessionUtils;
import com.beolnix.marvin.im.api.model.IMIncomingMessage;
import com.beolnix.marvin.im.api.model.IMIncomingMessageBuilder;
import com.beolnix.marvin.im.irc.ConnectionListener;
import com.beolnix.marvin.im.irc.ErrorListener;
import com.beolnix.marvin.im.irc.IrcIMSession;
import com.beolnix.marvin.im.irc.IrcMessageListener;
import com.beolnix.marvin.plugins.api.PluginsManager;
import jerklib.events.ErrorEvent;
import jerklib.events.IRCEvent;
import jerklib.events.MessageEvent;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Created by beolnix on 31/10/15.
 */
public class IrcMessageListenerImpl implements IrcMessageListener {

    // dependencies
    private final String botName;
    private final PluginsManager pluginsManager;

    // state
    private List<ConnectionListener> connectionListenerList = new ArrayList<>();
    private List<ErrorListener> errorListenersList = new ArrayList<>();
    private IMSessionUtils imSessionUtils = new IMSessionUtils();

    // constants
    private final static Logger logger = Logger.getLogger(IrcMessageListenerImpl.class);

    public IrcMessageListenerImpl(String botName, PluginsManager pluginsManager) {
        this.botName = botName;
        this.pluginsManager = pluginsManager;
    }

    @Override
    public void registerConnectionListener(ConnectionListener connectionListener) {
        connectionListenerList.add(connectionListener);
    }

    @Override
    public void registerErrorListener(ErrorListener errorListener) {
        errorListenersList.add(errorListener);
    }

    @Override
    public void receiveEvent(IRCEvent ircEvent) {
        if (ircEvent.getRawEventData().contains("PRIVMSG #")) {
            MessageEvent me = (MessageEvent) ircEvent;
            IMIncomingMessage incomingMessage = convertMsg(me, botName, true, getConferenceName(ircEvent));
            logger.trace("Bot with name '" + botName + "' got a message from conference room. msg body: " +
                    incomingMessage.getRawMessageBody());
            pluginsManager.process(incomingMessage);
        } else if (ircEvent.getType() == IRCEvent.Type.PRIVATE_MESSAGE) {
            MessageEvent me = (MessageEvent) ircEvent;
            IMIncomingMessage incomingMessage = convertMsg(me, botName, false, null);
            logger.trace("Bot with name '" + botName + "' got a private message. msg body: " +
                    incomingMessage.getRawMessageBody());
            pluginsManager.process(incomingMessage);
        } else if (ircEvent.getType() == IRCEvent.Type.CONNECT_COMPLETE) {
            for (ConnectionListener connectionListener : connectionListenerList) {
                connectionListener.onConnect();
            }
        } else if (ircEvent.getType() == IRCEvent.Type.ERROR) {
            ErrorEvent ee = (ErrorEvent) ircEvent;
            logger.error(ee.getRawEventData());
            for (ErrorListener errorListener : errorListenersList) {
                errorListener.onError(ee.getRawEventData());
            }
        } else {
            logger.trace(ircEvent.getRawEventData());
        }
    }

    private String getConferenceName(IRCEvent ircEvent) {
        String[] rawMsg = ircEvent.getRawEventData().split(" ");
        String conferenceName = "";
        for (String rawMsgPart : rawMsg) {
            if (rawMsgPart.startsWith("#")) {
                conferenceName = rawMsgPart;
            }
        }

        return conferenceName;
    }

    private IMIncomingMessage convertMsg(MessageEvent me, String botName, boolean conference, String conferenceName) {
        String msg = me.getMessage();
        boolean command = imSessionUtils.isCommand(msg, IrcIMSession.COMMAND_SYMBOL);

        IMIncomingMessageBuilder builder = new IMIncomingMessageBuilder()
                .withBotName(botName)
                .withRawMessageBody(me.getMessage())
                .withAutor(me.getNick())
                .withCommand(command)
                .withConference(conference)
                .withProtocol(IrcIMSession.PROTOCOL)
                .withCommandSymbol(IrcIMSession.COMMAND_SYMBOL)
                .withTimestamp(Calendar.getInstance());

        if (command) {
            Optional<String> commandName = imSessionUtils.parseCommand(msg, IrcIMSession.COMMAND_SYMBOL);
            if (commandName.isPresent()) {
                Optional<String> commandAttributes = imSessionUtils.parseCommandAttributes(msg, commandName.get(), IrcIMSession.COMMAND_SYMBOL);
                builder.withCommandName(commandName.get());
                if (commandAttributes.isPresent()) {
                    builder.withCommandAttributes(commandAttributes.get());
                }
            }
        }

        if (conference) {
            builder.withConferenceName(conferenceName);
        }

        return builder.build();
    }
}

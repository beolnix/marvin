package com.beolnix.marvin.im.skype;

import com.beolnix.marvin.config.api.BotSettings;
import com.beolnix.marvin.im.IMSessionUtils;
import com.beolnix.marvin.im.api.IMSession;
import com.beolnix.marvin.im.api.IMSessionState;
import com.beolnix.marvin.im.api.model.IMIncomingMessage;
import com.beolnix.marvin.im.api.model.IMIncomingMessageBuilder;
import com.beolnix.marvin.im.api.model.IMOutgoingMessage;
import com.beolnix.marvin.im.skype.model.SkypeBotSettings;
import com.beolnix.marvin.plugins.api.PluginsManager;
import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.SkypeBuilder;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.messages.ReceivedMessage;
import com.samczsun.skype4j.events.EventHandler;
import com.samczsun.skype4j.events.Listener;
import com.samczsun.skype4j.events.chat.message.MessageReceivedEvent;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.user.Contact;

import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.Optional;

/**
 * Created by beolnix on 11/01/16.
 */
public class SkypeIMSession implements IMSession {

    // constants
    private Logger logger = Logger.getLogger(getClass().getName());
    public static final String PROTOCOL = "SKYPE";
    public static final String COMMAND_SYMBOL = "!";
    public static final int CONNECTION_TIMEOUT = 60 * 1000;

    // dependencies
    private final SkypeBotSettings botSettings;
    private final PluginsManager pluginManager;
    private final IMSessionUtils imSessionUtils;

    // internal state
    private Skype skype = null;
    private IMSessionState state = IMSessionState.NOT_INITIALIZED;
    private String errorMsg = null;

    public SkypeIMSession(SkypeBotSettings botSettings, PluginsManager pluginManager, IMSessionUtils imSessionUtils) {
        this.botSettings = botSettings;
        this.pluginManager = pluginManager;
        this.imSessionUtils = imSessionUtils;
        state = IMSessionState.INITIALIZED;
    }

    @Override
    public String getBotName() {
        return botSettings.getName();
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void sendMessage(IMOutgoingMessage imOutgoingMessage) {
        if (imOutgoingMessage.isConference()) {
            sendToConference(imOutgoingMessage);
        } else {
            sendToContact(imOutgoingMessage);
        }
    }

    private void sendToConference(IMOutgoingMessage imOutgoingMessage) {
        String chatName = imOutgoingMessage.getConferenceName();
        Chat chat = skype.getChat(chatName);
        if (chat == null) {
            logger.error("Can't sent message because chat: " + chatName + " does not exist.");
            return;
        }
        try {
            chat.sendMessage(imOutgoingMessage.getRawMessageBody());
            logger.trace("Message " + imOutgoingMessage.getConferenceName() + " to '" +
                    imOutgoingMessage.getRawMessageBody() + "' over skype has been sent successfully");
        } catch (Exception e) {
            logger.error("Can't sent message because of: " + e.getMessage(), e);
        }
    }

    private void sendToContact(IMOutgoingMessage imOutgoingMessage) {
        String contactName = imOutgoingMessage.getRecepient();
        Contact contact = skype.getContact(contactName);
        if (contact == null) {
            logger.error("Can't sent message because contact: " + contactName + " does not exist.");
            return;
        }
        try {
            contact.sendRequest(imOutgoingMessage.getRawMessageBody());
            logger.trace("Message " + imOutgoingMessage.getConferenceName() + " to '" +
                    imOutgoingMessage.getRawMessageBody() + "' over skype has been sent successfully");
        } catch (Exception e) {
            logger.error("Can't sent message because of: " + e.getMessage(), e);
        }
    }

    @Override
    public void connect() {
        if (skype != null) {
            disconnect();
        }

        logger.info("Trying to login using " + botSettings.getLogin() + " and " + botSettings.getPassword());
        skype = new SkypeBuilder(botSettings.getLogin(), botSettings.getPassword())
                .withAllResources()
                .build();
        try {
            skype.login();
            registerListener();
            state = IMSessionState.CONNECTED;
            logger.info("skype bot " + botSettings.getName() + " has been connected successfully.");
        } catch (Exception e) {
            state = IMSessionState.DISCONNECTED;
            this.errorMsg = e.getMessage();
            logger.error("Skype bot " + botSettings.getName() + " can't connect because of: " + e.getMessage(), e);
        }
    }

    private void registerListener() {
        skype.getEventDispatcher().registerListener(new Listener() {
            @EventHandler
            public void onMessage(MessageReceivedEvent e) {
                logger.info("Skype bot " + botSettings.getName() + " got msg: " + e.getMessage().getContent().asPlaintext());
                IMIncomingMessage msg = convertMessage(e);
                pluginManager.process(msg);
            }
        });
        try {
            skype.subscribe();
        } catch (Exception e) {
            logger.error("Can't subscribe for messages because of: " + e.getMessage(), e);
            state = IMSessionState.ERROR;
            errorMsg = e.getMessage();
        }
    }

    private IMIncomingMessage convertMessage(MessageReceivedEvent e) {
        ReceivedMessage skypeMessage = e.getMessage();
        String msg = skypeMessage.getContent().asPlaintext();
        boolean conference = true;
        boolean command = imSessionUtils.isCommand(msg, SkypeIMSession.COMMAND_SYMBOL);

        IMIncomingMessageBuilder builder = new IMIncomingMessageBuilder()
                .withBotName(botSettings.getName())
                .withRawMessageBody(msg)
                .withAutor(skypeMessage.getSender().getUsername())
                .withCommand(command)
                .withConference(conference)
                .withProtocol(SkypeIMSession.PROTOCOL)
                .withCommandSymbol(SkypeIMSession.COMMAND_SYMBOL)
                .withTimestamp(Calendar.getInstance());

        if (command) {
            Optional<String> commandName = imSessionUtils.parseCommand(msg, SkypeIMSession.COMMAND_SYMBOL);
            if (commandName.isPresent()) {
                Optional<String> commandAttributes = imSessionUtils.parseCommandAttributes(msg, commandName.get(), SkypeIMSession.COMMAND_SYMBOL);
                builder.withCommandName(commandName.get());
                if (commandAttributes.isPresent()) {
                    builder.withCommandAttributes(commandAttributes.get());
                }
            }
        }

        if (conference) {
            builder.withConferenceName(skypeMessage.getChat().getIdentity());
        }

        return builder.build();
    }

    @Override
    public void disconnect() {
        try {
            skype.logout();
            state = IMSessionState.DISCONNECTED;
        } catch (Exception e) {
            state = IMSessionState.ERROR;
            this.errorMsg = e.getMessage();
        }
    }

    @Override
    public IMSessionState getState() {
        return state;
    }

    @Override
    public String getErrorMessage() {
        return errorMsg;
    }
}

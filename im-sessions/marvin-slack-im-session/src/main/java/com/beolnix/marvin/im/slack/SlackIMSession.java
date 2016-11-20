package com.beolnix.marvin.im.slack;

import com.beolnix.marvin.im.IMSessionUtils;
import com.beolnix.marvin.im.api.IMSession;
import com.beolnix.marvin.im.api.IMSessionState;
import com.beolnix.marvin.im.api.model.IMOutgoingMessage;
import com.beolnix.marvin.im.slack.model.SlackBotSettings;
import com.beolnix.marvin.im.slack.slack.SlackMessageListener;
import com.beolnix.marvin.plugins.api.PluginsManager;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.apache.log4j.Logger;

import java.io.IOException;

public class SlackIMSession implements IMSession {

    // constants
    private Logger logger = Logger.getLogger(getClass().getName());
    public static final String PROTOCOL = "SLACK";
    public static final String COMMAND_SYMBOL = "!";

    // dependencies
    private final SlackBotSettings botSettings;
    private final PluginsManager pluginManager;
    private final IMSessionUtils imSessionUtils;

    // state
    private IMSessionState state = IMSessionState.NOT_INITIALIZED;
    private String errorMsg = null;
    private SlackSession session = null;
    private SlackMessageListener slackMessageListener = null;

    public SlackIMSession(SlackBotSettings botSettings, PluginsManager pluginManager, IMSessionUtils imSessionUtils) {
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
        if (session == null || !IMSessionState.CONNECTED.equals(getState())) {
            logger.error("Slack bot "+ botSettings.getName()+" hasn't started yet");
            return;
        }

        if (imOutgoingMessage.isConference()) {
            logger.info("Slack bot " + botSettings.getName() + " sends channel message: " + imOutgoingMessage.toString());

            SlackChannel channel = session.findChannelByName(imOutgoingMessage.getConferenceName());
            if (channel == null) {
                logger.error("Slack bot can't send message to the " + imOutgoingMessage.getConferenceName() +
                " channel as it doesn't exist.");
                return;
            }

            session.sendMessage(channel, imOutgoingMessage.getRawMessageBody());
        } else {
            logger.info("Slack bot " + botSettings.getName() + " sends private: " + imOutgoingMessage.toString());

            SlackUser user = session.findUserByUserName(imOutgoingMessage.getRecepient());
            if (user == null) {
                logger.error("Slack bot can't send message to the " + imOutgoingMessage.getConferenceName() +
                        " user as it doesn't exist.");
                return;
            }

            session.sendMessageToUser(user, imOutgoingMessage.getRawMessageBody(), null);
        }
    }

    @Override
    public void connect() {
        if (session != null) {
            state = IMSessionState.RECONNECTING;
            disconnect();
        } else {
            state = IMSessionState.CONNECTING;
        }

        try {
            session = SlackSessionFactory.createWebSocketSlackSession(botSettings.getToken());
            session.connect();
            slackMessageListener = new SlackMessageListener(botSettings, pluginManager, imSessionUtils);
            session.addMessagePostedListener(slackMessageListener);
            state = IMSessionState.CONNECTED;
            logger.info("Slack bot " + botSettings.getName() + " connected successfully.");
        } catch (IOException e) {
            state = IMSessionState.DISCONNECTED;
            this.errorMsg = e.getMessage();
            logger.error("Slack bot " + botSettings.getName() + " can't connect because of: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        if (slackMessageListener != null && session != null) {
            session.removeMessagePostedListener(slackMessageListener);
        }

        if (slackMessageListener != null) {
            slackMessageListener = null;
        }

        if (session != null) {
            try {
                session.disconnect();
            } catch (IOException e) {
                logger.error("Slack bot " + getBotName() + " got error during a disconnect: " + e.getMessage(), e);
            }

            session = null;
        }

        if (state != IMSessionState.RECONNECTING) {
            state = IMSessionState.DISCONNECTED;
        }
    }

    @Override
    public void reconnect() {
        state = IMSessionState.RECONNECTING;
        disconnect();
        connect();
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

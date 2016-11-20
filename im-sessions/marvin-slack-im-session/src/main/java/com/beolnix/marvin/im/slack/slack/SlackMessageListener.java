package com.beolnix.marvin.im.slack.slack;

import com.beolnix.marvin.im.IMSessionUtils;
import com.beolnix.marvin.im.api.model.IMIncomingMessage;
import com.beolnix.marvin.im.api.model.IMIncomingMessageBuilder;
import com.beolnix.marvin.im.slack.SlackIMSession;
import com.beolnix.marvin.im.slack.model.SlackBotSettings;
import com.beolnix.marvin.plugins.api.PluginsManager;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.apache.log4j.Logger;

import java.util.Calendar;

public class SlackMessageListener implements SlackMessagePostedListener {

    private Logger logger = Logger.getLogger(getClass().getName());

    private final SlackBotSettings botSettings;
    private final PluginsManager pluginManager;
    private final IMSessionUtils imSessionUtils;

    public SlackMessageListener(SlackBotSettings botSettings, PluginsManager pluginManager, IMSessionUtils imSessionUtils) {
        this.botSettings = botSettings;
        this.pluginManager = pluginManager;
        this.imSessionUtils = imSessionUtils;
    }

    @Override
    public void onEvent(SlackMessagePosted event, SlackSession session) {
        logger.debug("update received: " + event.toString());
        pluginManager.process(convert(event));
    }

    private IMIncomingMessage convert(SlackMessagePosted event) {
        SlackUser sender = event.getSender();
        SlackChannel channel = event.getChannel();

        boolean isConference = channel != null && !channel.isDirect();

        IMIncomingMessageBuilder builder = new IMIncomingMessageBuilder()
                .withBotName(botSettings.getName())
                .withAutor(sender.getUserName())
                .withCommandSymbol(SlackIMSession.COMMAND_SYMBOL)
                .withRawMessageBody(event.getMessageContent())
                .withProtocol(SlackIMSession.PROTOCOL)
                .withTimestamp(Calendar.getInstance())
                .withCommand(false);

        if (isConference) {
            builder.withConference(true)
                    .withConferenceName(channel.getName());
        }

        imSessionUtils.parseCommand(event.getMessageContent(), SlackIMSession.COMMAND_SYMBOL)
                .ifPresent(command -> {
                    builder.withCommandName(command)
                            .withCommand(true);
                });

        return builder.build();
    }
}

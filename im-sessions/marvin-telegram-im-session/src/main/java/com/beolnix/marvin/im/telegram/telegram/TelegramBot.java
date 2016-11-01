package com.beolnix.marvin.im.telegram.telegram;

import com.beolnix.marvin.im.IMSessionUtils;
import com.beolnix.marvin.im.api.model.IMIncomingMessage;
import com.beolnix.marvin.im.api.model.IMIncomingMessageBuilder;
import com.beolnix.marvin.im.telegram.TelegramIMSession;
import com.beolnix.marvin.im.telegram.model.TelegramBotSettings;
import com.beolnix.marvin.plugins.api.PluginsManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.Calendar;

public class TelegramBot extends TelegramLongPollingBot {

    private Logger logger = Logger.getLogger(getClass().getName());

    private final TelegramBotSettings botSettings;
    private final PluginsManager pluginManager;
    private final IMSessionUtils imSessionUtils;

    public TelegramBot(TelegramBotSettings botSettings, PluginsManager pluginManager, IMSessionUtils imSessionUtils) {
        this.botSettings = botSettings;
        this.pluginManager = pluginManager;
        this.imSessionUtils = imSessionUtils;
    }

    @Override
    public String getBotToken() {
        return botSettings.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        logger.debug("update received: " + update.toString());
        pluginManager.process(convert(message));
    }

    private IMIncomingMessage convert(Message message) {
        IMIncomingMessageBuilder imBuilder = new IMIncomingMessageBuilder()
                .withBotName(botSettings.getName())
                .withAutor(message.getMessageId().toString())
                .withRawMessageBody(message.getText())
                .withCommand(message.isCommand())
                .withConference(message.isGroupMessage())
                .withProtocol(TelegramIMSession.PROTOCOL)
                .withCommandSymbol(TelegramIMSession.COMMAND_SYMBOL)
                .withTimestamp(Calendar.getInstance());

        if (message.isGroupMessage()) {
            imBuilder.withConferenceName(message.getChatId().toString());
        }

        return imBuilder.build();
    }

    @Override
    public String getBotUsername() {
        return botSettings.getName();
    }
}

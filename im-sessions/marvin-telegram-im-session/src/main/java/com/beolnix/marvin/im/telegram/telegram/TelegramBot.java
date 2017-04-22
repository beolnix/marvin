package com.beolnix.marvin.im.telegram.telegram;

import com.beolnix.marvin.im.IMSessionUtils;
import com.beolnix.marvin.im.api.model.IMIncomingMessage;
import com.beolnix.marvin.im.api.model.IMIncomingMessageBuilder;
import com.beolnix.marvin.im.telegram.TelegramIMSession;
import com.beolnix.marvin.im.telegram.model.TelegramBotSettings;
import com.beolnix.marvin.plugins.api.PluginsManager;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.Calendar;

public class TelegramBot extends TelegramLongPollingBot {

    private Logger logger = Logger.getLogger(getClass().getName());

    private final TelegramBotSettings botSettings;
    private final PluginsManager pluginManager;
    private final IMSessionUtils imSessionUtils;

    private final String botNameSuffix;

    public TelegramBot(TelegramBotSettings botSettings, PluginsManager pluginManager, IMSessionUtils imSessionUtils) {
        this.botSettings = botSettings;
        this.pluginManager = pluginManager;
        this.imSessionUtils = imSessionUtils;
        this.botNameSuffix = "@" + botSettings.getName();
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

        // it is needed to let plugins created for non-telegram protocols correctly process the command
        String msgText = message.getText();
        if (msgText.endsWith(botNameSuffix)) {
            msgText = msgText.replace(botNameSuffix, "");
            logger.info("Removed bot name suffix " + botNameSuffix + " from the original message.");
        }

        IMIncomingMessageBuilder imBuilder = new IMIncomingMessageBuilder()
                .withBotName(botSettings.getName())
                .withAutor(parseAutor(message))
                .withRawMessageBody(msgText)
                .withCommand(message.isCommand())
                .withConference(true)
                .withProtocol(TelegramIMSession.PROTOCOL)
                .withCommandSymbol(TelegramIMSession.COMMAND_SYMBOL)
                .withTimestamp(Calendar.getInstance())
                .withConferenceName(message.getChatId().toString());

        imSessionUtils.parseCommand(msgText, TelegramIMSession.COMMAND_SYMBOL)
                .ifPresent(imBuilder::withCommandName);

        return imBuilder.build();
    }

    private String parseAutor(Message message) {
        String userName = message.getFrom().getUserName();
        if (!StringUtils.isEmpty(userName)) {
            return userName;
        }

        return message.getFrom().getFirstName() + " " + message.getFrom().getLastName();
    }

    @Override
    public String getBotUsername() {
        return botSettings.getName();
    }
}

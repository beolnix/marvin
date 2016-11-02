package com.beolnix.marvin.im.telegram;

import com.beolnix.marvin.im.IMSessionUtils;
import com.beolnix.marvin.im.api.IMSession;
import com.beolnix.marvin.im.api.IMSessionState;
import com.beolnix.marvin.im.api.model.IMOutgoingMessage;
import com.beolnix.marvin.im.telegram.model.TelegramBotSettings;
import com.beolnix.marvin.im.telegram.telegram.TelegramBot;
import com.beolnix.marvin.plugins.api.PluginsManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

public class TelegramIMSession implements IMSession {

    // constants
    private Logger logger = Logger.getLogger(getClass().getName());
    public static final String PROTOCOL = "TELEGRAM";
    public static final String COMMAND_SYMBOL = "/";

    // dependencies
    private final TelegramBotSettings botSettings;
    private final PluginsManager pluginManager;
    private final IMSessionUtils imSessionUtils;

    // state
    private IMSessionState state = IMSessionState.NOT_INITIALIZED;
    private String errorMsg = null;
    private TelegramBot telegramBot;
    private TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

    public TelegramIMSession(TelegramBotSettings botSettings, PluginsManager pluginManager, IMSessionUtils imSessionUtils) {
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
        if (telegramBot == null) {
            logger.error("Bot "+ botSettings.getName()+" hasn't started yet");
        }
        logger.info("Bot " + botSettings.getName() + " sends message: " + imOutgoingMessage.toString());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(imOutgoingMessage.getRawMessageBody());
        sendMessage.setChatId(imOutgoingMessage.getConferenceName());

        try {
            telegramBot.sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Can't send message " + sendMessage.toString() + " because of: " + e.getMessage(), e);
        }
    }

    @Override
    public void connect() {
        if (telegramBot != null) {
            state = IMSessionState.RECONNECTING;
            disconnect();
        } else {
            state = IMSessionState.CONNECTING;
        }

        try {
            telegramBot = new TelegramBot(botSettings, pluginManager, imSessionUtils);
            telegramBotsApi.registerBot(telegramBot);
            state = IMSessionState.CONNECTED;
            logger.info("Telegram bot " + botSettings.getName() + " connected successfully.");
        } catch (TelegramApiRequestException e) {
            state = IMSessionState.DISCONNECTED;
            this.errorMsg = e.getMessage();
            logger.error("Telegram bot " + botSettings.getName() + " can't connect because of: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        if (telegramBot != null) {
            telegramBot = null;
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

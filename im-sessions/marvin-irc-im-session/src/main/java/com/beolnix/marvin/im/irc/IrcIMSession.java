package com.beolnix.marvin.im.irc;

import com.beolnix.marvin.im.api.IMSession;
import com.beolnix.marvin.im.api.IMSessionState;
import com.beolnix.marvin.im.api.model.IMOutgoingMessage;
import com.beolnix.marvin.im.irc.model.IrcBotSettings;
import jerklib.Channel;
import jerklib.ConnectionManager;
import jerklib.Profile;
import jerklib.Session;
import org.apache.log4j.Logger;

/**
 * Created by beolnix on 31/10/15.
 */
public class IrcIMSession implements IMSession, ConnectionListener, ErrorListener {

    // dependencies
    private final IrcBotSettings ircSettings;
    private final IrcMessageListener ircMessageListener;

    // state
    private final ConnectionManager manager;
    private IMSessionState state = IMSessionState.NOT_INITIALIZED;
    private Session session = null;
    private String errorMsg = null;

    // constants
    private Logger logger = Logger.getLogger(getClass().getName());
    public static final String PROTOCOL = "IRC";
    public static final String COMMAND_SYMBOL = "!";
    public static final int CONNECTION_TIMEOUT = 60 * 1000;

    private IrcIMSession(IrcBotSettings ircSettings, IrcMessageListener ircMessageListener) {
        this.ircSettings = ircSettings;
        this.ircMessageListener = ircMessageListener;
        this.manager = new ConnectionManager(new Profile(ircSettings.getNickname()), ircSettings.getCharset());
    }

    public static IMSession createNewInstance(IrcBotSettings ircSettings, IrcMessageListener ircMessageListener) {
        IrcIMSession imSession = new IrcIMSession(ircSettings, ircMessageListener);
        ircMessageListener.registerConnectionListener(imSession);
        ircMessageListener.registerErrorListener(imSession);
        imSession.state = IMSessionState.INITIALIZED;
        return imSession;
    }

    @Override
    public void onConnect() {
        state = IMSessionState.CONNECTED;
        logger.info(getBotName() + " connected successfully.");
        session.setRejoinOnKick(true);
        session.join(ircSettings.getChannelName(), ircSettings.getChannelPassword());
    }

    @Override
    public void onError(String errorMsg) {
        state = IMSessionState.ERROR;
        this.errorMsg = errorMsg;
        logger.error("Bot with name '" + getBotName() + "' has just got error: " + errorMsg);
    }

    @Override
    public String getBotName() {
        return ircSettings.getName();
    }

    @Override
    public void sendMessage(IMOutgoingMessage outMsg) {
        logger.trace("Bot with name '" + getBotName() + "' got message to send. Protocol: " + getProtocol() +
                "; msgBody: " + outMsg.getRawMessageBody() +
                "; isConference?: " + outMsg.isConference());
        if (session == null || state != IMSessionState.CONNECTED) {
            logger.error("Can't send outgoing message yet, session isn't ready.");
            return;
        }

        if (outMsg == null) {
            logger.error("somebody asked to send null msg");
            return;
        }

        if (isBlank(outMsg.getRawMessageBody())) {
            logger.error(outMsg.getFromPlugin() + " plugin asked to send msg without msg body");
            return;
        }
        try {
            if (outMsg.isConference()) {
                sendConferenceMessage(outMsg);
            } else {
                sendPrivateMessage(outMsg);
            }

            logger.trace("Bot with name '" + getBotName() + "' has sent the message successfully.");
        } catch (Exception e) {
            logger.error("Bot with name '" + getBotName() + "' got error while was sending message: " + e.getMessage());
            logger.error(e);
        }
    }

    private void sendConferenceMessage(IMOutgoingMessage outMsg) {
        if (isBlank(outMsg.getConferenceName())) {
            logger.error(outMsg.getFromPlugin() + " plugin asked to send conference name with empty conferenceName");
            return;
        }

        Channel channel = this.session.getChannel(outMsg.getConferenceName());
        if (channel != null) {
            channel.say(outMsg.getRawMessageBody());
        }
    }

    private void sendPrivateMessage(IMOutgoingMessage outMsg) {
        if (isBlank(outMsg.getRecepient())) {
            logger.error(outMsg.getFromPlugin() + " plugin asked to send private msg without recipient.");
            return;
        }

        session.sayPrivate(outMsg.getRecepient(), outMsg.getRawMessageBody());
    }

    private boolean isBlank(String value) {
        return value == null || value.replace(" ", "").length() == 0;
    }

    @Override
    public void disconnect() {
        if (session != null) {
            session.close("buy");
            session = null;
            if (state != IMSessionState.RECONNECTING) {
                this.state = IMSessionState.DISCONNECTED;
            }
            logger.info("Bot with name '" + getBotName() + "' has been successfully disconnected.");
        }
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public IMSessionState getState() {
        return this.state;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMsg;
    }

    @Override
    public void connect() {
        state = IMSessionState.CONNECTING;
        session = manager.requestConnection(ircSettings.getServerName(), ircSettings.getPortNumber());
        session.addIRCEventListener(ircMessageListener);
        try {
            // give jerklib a chance to establish a connection
            Thread.sleep(CONNECTION_TIMEOUT);
        } catch (InterruptedException e) {}
        if (!session.isConnected()) {
            state = IMSessionState.DISCONNECTED;
        }
    }

    @Override
    public void reconnect() {
        state = IMSessionState.RECONNECTING;
        disconnect();
        connect();
    }
}

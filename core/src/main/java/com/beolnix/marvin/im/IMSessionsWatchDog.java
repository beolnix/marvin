package com.beolnix.marvin.im;

import com.beolnix.marvin.im.api.IMSession;
import com.beolnix.marvin.im.api.IMSessionManager;
import com.beolnix.marvin.im.api.IMSessionState;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Created by beolnix on 04/11/15.
 */
public class IMSessionsWatchDog {

    // dependencies
    private final IMSessionManager sessionManager;

    // constants
    private static final Logger logger = Logger.getLogger(IMSessionManagerImpl.class);

    public IMSessionsWatchDog(IMSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void checkSessions() {
        sessionManager.getIMSessions().values().stream()
                .filter(this::isStateGood)
                .forEach( imSession -> {
                    logger.warn("The  bot '" + imSession.getBotName()
                            + "' is in '" + imSession.getState() + "' state. Try to connect it once again.");
                    imSession.connect();
                });
    }

    private boolean isStateGood(IMSession imSession) {
        IMSessionState state = imSession.getState();
        if (state == null) {
            return false;
        }

        return state == IMSessionState.CONNECTED ||
                state == IMSessionState.CONNECTING;
    }
}

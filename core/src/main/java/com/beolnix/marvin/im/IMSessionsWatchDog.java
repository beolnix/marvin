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
        Map<String, IMSession> sessionMap = sessionManager.getIMSessions();
        for (IMSession imSession : sessionMap.values()) {
            if (isStateBad(imSession.getState())) {
                logger.warn("The state of bot '" + imSession.getBotName()
                        + "' is: " + imSession.getState() + ". Try to connect it once again.");
                imSession.connect();
            }
        }
    }

    private boolean isStateBad(IMSessionState state) {
        if (state == null) {
            return true;
        }

        return state != IMSessionState.CONNECTED &&
                state != IMSessionState.CONNECTING;
    }
}

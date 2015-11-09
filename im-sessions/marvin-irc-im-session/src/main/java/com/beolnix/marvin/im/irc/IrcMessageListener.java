package com.beolnix.marvin.im.irc;

import jerklib.listeners.IRCEventListener;

/**
 * Created by DAtmakin on 11/9/2015.
 */
public interface IrcMessageListener extends IRCEventListener {
    void registerConnectionListener(ConnectionListener connectionListener);
    void registerErrorListener(ErrorListener errorListener);
}

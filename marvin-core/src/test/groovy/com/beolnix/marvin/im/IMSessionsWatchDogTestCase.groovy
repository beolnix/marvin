package com.beolnix.marvin.im

import com.beolnix.marvin.im.api.IMSession
import com.beolnix.marvin.im.api.IMSessionManager
import com.beolnix.marvin.im.api.IMSessionState
import org.junit.Test

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

/**
 * Created by DAtmakin on 11/5/2015.
 */
class IMSessionsWatchDogTestCase {

    @Test
    public void isConnectCalled() {
        def isConnectCalled = false
        def isDisconnectCalled = false
        def imSession = [
                getState: {
                    IMSessionState.ERROR
                },
                disconnect : {
                    isDisconnectCalled = true
                },
                connect : {
                    isConnectCalled = true
                },
                reconnect : {
                    isConnectCalled = true
                },
                getBotName: {
                    "testBot"
                }] as IMSession
        def imSessionManager = [
                getIMSessions: {
                    ["testSession" : imSession]
                }
        ] as IMSessionManager

        def watchDog = new IMSessionsWatchDog(imSessionManager)
        watchDog.checkSessions()
        assertTrue(isConnectCalled)
    }

    @Test
    public void testIsStateBadPositive1() {
        def watchDog = new IMSessionsWatchDog(null)
        assertTrue(
                watchDog.isStateBad(getSessionInState(IMSessionState.ERROR)
                )
        )
    }

    @Test
    public void testIsStateBadPositive2() {
        def watchDog = new IMSessionsWatchDog(null)
        assertTrue(
                watchDog.isStateBad(
                        getSessionInState(IMSessionState.DISCONNECTED)
                )
        )
    }

    @Test
    public void testIsStateBadPositive3() {
        def watchDog = new IMSessionsWatchDog(null)
        assertTrue(
                watchDog.isStateBad(
                        getSessionInState(IMSessionState.INITIALIZED)
                )
        )
    }

    @Test
    public void testIsStateBadPositive4() {
        def watchDog = new IMSessionsWatchDog(null)
        assertTrue(
                watchDog.isStateBad(
                        getSessionInState(IMSessionState.NOT_INITIALIZED)
                )
        )
    }

    @Test
    public void testIsStateBadPositive5() {
        def watchDog = new IMSessionsWatchDog(null)
        assertTrue(
                watchDog.isStateBad(
                        getSessionInState(null)
                )
        )
    }

    @Test
    public void testIsStateBadNegative1() {
        def watchDog = new IMSessionsWatchDog(null)
        assertFalse(
                watchDog.isStateBad(
                        getSessionInState(IMSessionState.CONNECTED)
                )
        )
    }

    @Test
    public void testIsStateBadNegative2() {
        def watchDog = new IMSessionsWatchDog(null)
        assertFalse(
                watchDog.isStateBad(
                        getSessionInState(IMSessionState.CONNECTING)
                )
        )
    }

    def getSessionInState(IMSessionState state) {
        [getState: {
            state
        }] as IMSession
    }
}

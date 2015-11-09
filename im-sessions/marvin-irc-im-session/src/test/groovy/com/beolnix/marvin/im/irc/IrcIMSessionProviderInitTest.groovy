package com.beolnix.marvin.im.irc

import com.beolnix.marvin.im.api.IMSession
import com.beolnix.marvin.im.api.IMSessionProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import static org.junit.Assert.assertNotNull

/**
 * Created by beolnix on 09/11/15.
 */
@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations = ["/spring/irc-im-session-context.xml", "/spring/stubs-context.xml"])
class IrcIMSessionProviderInitTest {

    @Autowired
    IMSessionProvider imSessionProvider

    @Test
    public void initTest() {
        assertNotNull(imSessionProvider)
    }
}

package com.beolnix.marvin.im.skype;


import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.SkypeBuilder;
import com.samczsun.skype4j.events.EventHandler;
import com.samczsun.skype4j.events.Listener;
import com.samczsun.skype4j.events.chat.message.MessageReceivedEvent;
import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;
import org.junit.Test;

/**
 * Created by beolnix on 11/01/16.
 */
public class Prototype {

    @Test
    public void test() throws Exception {
        Skype skype = new SkypeBuilder("marvin.the_bot", "secret").withAllResources().build();
        skype.login();
        skype.getEventDispatcher().registerListener(new Listener() {
            @EventHandler
            public void onMessage(MessageReceivedEvent e) {
                try {
                    e.getChat().sendMessage(e.getMessage().getContent().asPlaintext());
                } catch (Exception ed) {
                    ed.printStackTrace();
                }

            }
        });
        skype.subscribe();
        while(true) {
            Thread.sleep(1000);
        }
//        skype.logout();

    }


}

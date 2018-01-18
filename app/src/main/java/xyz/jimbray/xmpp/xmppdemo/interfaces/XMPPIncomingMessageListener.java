package xyz.jimbray.xmpp.xmppdemo.interfaces;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

/**
 * Created by Jimbray on 2018/1/11.
 */

public interface XMPPIncomingMessageListener {

    void newIncomingMessage(EntityBareJid from, Message message, Chat chat);

}

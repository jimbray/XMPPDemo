package xyz.jimbray.xmpp.xmppdemo.api

import android.graphics.drawable.Drawable
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.chat2.IncomingChatMessageListener
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.roster.RosterEntry
import org.jivesoftware.smack.roster.RosterGroup
import org.jivesoftware.smackx.muc.HostedRoom
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jivesoftware.smackx.vcardtemp.packet.VCard
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.impl.JidCreate
import xyz.jimbray.xmpp.xmppdemo.log_d
import java.io.File

/**
 * Created by Jimbray on 2017/12/30.
 */
class XMPPApiManager private constructor() : IncomingChatMessageListener, OutgoingChatMessageListener, XMPPConnectionListener {

    private var xmpp: XMPPApi = XMPPApi(this, this, this)

    private val mConnectionListeners = ArrayList<XMPPConnectionListener>()
    private val mIncomingChatMessageListeners = ArrayList<IncomingChatMessageListener>()
    private val mOutGoingChatMessageListeners = ArrayList<OutgoingChatMessageListener>()


    private object XMPPApiManagerHolder {
        val INSTANCE = XMPPApiManager()
    }

    companion object {

        private val TAG = XMPPApiManager::class.java.simpleName

        val instance: XMPPApiManager
            get() = XMPPApiManagerHolder.INSTANCE

    }

    fun connect(): Boolean {
        return xmpp.connect()
    }

    fun registerUser(user_name: String, passwd: String): Boolean {
        return xmpp.registerUser(user_name, passwd)
    }

    fun login(user_name: String, passwd: String): Boolean {
        return xmpp.login(user_name, passwd)
    }

    fun getAuthenticatedUser(): String? {
        return xmpp.getAuthenticatedUser()
    }

    fun isAuthenticated(): Boolean {
        return xmpp.isAuthenticated()
    }

    fun isConnected(): Boolean {
        return xmpp.isConnected()
    }

    fun setStatus(code: Int) {
        xmpp.setStatus(code)
    }

    fun disconnect() {
        xmpp.disconnect()
    }

    fun getGroups(): List<RosterGroup>? {
        return xmpp.getGroups()
    }

    fun getFriendsInGroup(group_name: String): List<RosterEntry>? {
        return xmpp.getFriendsInGroup(group_name)
    }

    fun getAllFriends(): List<RosterEntry>? {
        return xmpp.getAllFriends()
    }

    fun getUserVCard(user_name: String): VCard? {
        return xmpp.getUserVCard(user_name)
    }

    fun getUserDrawable(user_name: String): Drawable? {
        return xmpp.getUserDrawable(user_name)
    }

    fun addGroup(group_name: String): Boolean? {
        return xmpp.addGroup(group_name)
    }

    fun removeGroup(group_name: String): Boolean {
        return xmpp.removeGroup(group_name)
    }

    fun addFriend(user_name: String, nick_name: String): Boolean {
        return xmpp.addFriend(user_name, nick_name)
    }

    fun addFriendToGroup(user_name: String, nick_name: String, group_name: String): Boolean {
        return xmpp.addFriendToGroup(user_name, nick_name, group_name)
    }

    fun removeFriend(user_name: String): Boolean {
        return xmpp.removeFriend(user_name)
    }

    fun searchUsers(user_name: String): List<HashMap<String, String>>? {
        return xmpp.searchUsers(user_name)
    }

    fun changeStateMessage(status_msg: String) {
        xmpp.changeStateMessage(status_msg)
    }

    fun changeAvater(file: File): Boolean {
        return xmpp.changeAvater(file)
    }

    fun deleteAccount(): Boolean? {
        return xmpp.deleteAccount()
    }

    fun changePasswd(new_passwd: String): Boolean {
        return xmpp.changePasswd(new_passwd)
    }

    fun getHostRooms(): List<HostedRoom>? {
        return xmpp.getHostRooms()
    }

    fun createChatRoom(room_name: String, passwd: String): MultiUserChat? {
        return xmpp.createChatRoom(room_name, passwd)
    }

    fun joinChatRoom(user_name: String, room_name: String): MultiUserChat? {
        return xmpp.joinChatRoom(user_name, room_name)
    }

    fun sendGroupMessage(multiUserChat: MultiUserChat, message: String) {
        xmpp.sendGroupMessage(multiUserChat, message)
    }

    fun findChatRoomUser(multiUserChat: MultiUserChat): List<String>? {
        return xmpp.findChatRoomUser(multiUserChat)
    }

    fun createSingleChat(target_user_name: String): Chat? {
        return xmpp.createSingleChat(target_user_name)
    }

    fun sendSingleMessage(chat: Chat, message: String) {
        xmpp.sendSingleMessage(chat, message)
    }

    fun sendFile(user_name: String, filePath: String, message: String) {
        xmpp.sendFile(user_name, filePath, message)
    }

    fun getOfflieMessage(): Map<String, List<HashMap<String, String>>>? {
        return xmpp.getOfflieMessage()
    }

    /**
     * ------------连接状态监听---------------------
     */

    fun addConnectionListener(listener: XMPPConnectionListener) {
        if (!mConnectionListeners.contains(listener)) {
            mConnectionListeners.add(listener)
        }
    }

    fun removeConnectionListener(listener: XMPPConnectionListener) {
        if (mConnectionListeners.contains(listener)) {
            mConnectionListeners.remove(listener)
        }
    }

    fun addIncomingChatMessageListener(listener: IncomingChatMessageListener) {
        if (!mIncomingChatMessageListeners.contains(listener)) {
            mIncomingChatMessageListeners.add(listener)
        }
    }

    fun removeIncomingChatMessageListener(listener: IncomingChatMessageListener) {
        if (mIncomingChatMessageListeners.contains(listener)) {
            mIncomingChatMessageListeners.remove(listener)
        }
    }

    fun addOutgoingChatMessageListener(listener: OutgoingChatMessageListener) {
        if (!mOutGoingChatMessageListeners.contains(listener)) {
            mOutGoingChatMessageListeners.add(listener)
        }
    }

    fun removeOutgoingChatMessageListener(listener: OutgoingChatMessageListener) {
        if (mOutGoingChatMessageListeners.contains(listener)) {
            mOutGoingChatMessageListeners.remove(listener)
        }
    }

    override fun onConnected() {
        log_d(TAG, "connection connected")
        for (listener in mConnectionListeners) {
            listener?.onConnected()
        }
    }

    override fun onConnectionClosed() {
        log_d(TAG, "connection closed")
        for (listener in mConnectionListeners) {
            listener?.onConnectionClosed()
        }
    }

    override fun onConnectionClosedError() {
        log_d(TAG, "connection close error")
        for (listener in mConnectionListeners) {
            listener?.onConnectionClosedError()
        }
    }

    override fun onReconnectionSuccessful() {
        log_d(TAG, "connection successful")
        for (listener in mConnectionListeners) {
            listener?.onReconnectionSuccessful()
        }
    }

    override fun onAuthenticated() {
        log_d(TAG, "authenticated")
        for (listener in mConnectionListeners) {
            listener?.onAuthenticated()
        }
    }

    override fun onReconnectionFailed() {
        log_d(TAG, "reconnection failed")
        for (listener in mConnectionListeners) {
            listener.onReconnectionFailed()
        }
    }

    override fun onReconnectingIn() {
        log_d(TAG, "reconnecting")
        for (listener in mConnectionListeners) {
            listener.onReconnectingIn()
        }
    }


    override fun newIncomingMessage(from: EntityBareJid?, message: Message?, chat: Chat?) {
        for (listener in mIncomingChatMessageListeners) {
            listener.newIncomingMessage(from, message, chat)
        }
    }

    override fun newOutgoingMessage(to: EntityBareJid?, message: Message?, chat: Chat?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun sendMessage(user_name: String, message: String) {
        if (xmpp.getConnection() != null) {
            val chat = ChatManager.getInstanceFor(xmpp.getConnection()).chatWith(JidCreate.entityBareFrom(xmpp.generateJID(user_name)))
            sendSingleMessage(chat, message)
        } else {
            xmpp.connect()
        }

    }

}
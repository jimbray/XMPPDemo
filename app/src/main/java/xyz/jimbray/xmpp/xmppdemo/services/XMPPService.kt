package xyz.jimbray.xmpp.xmppdemo

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Binder
import android.os.IBinder
import android.text.TextUtils
import io.reactivex.functions.Consumer
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.IncomingChatMessageListener
import org.jivesoftware.smack.packet.Message
import org.jxmpp.jid.EntityBareJid
import xyz.jimbray.xmpp.xmppdemo.api.XMPPApiManager
import xyz.jimbray.xmpp.xmppdemo.api.XMPPConnectionListener
import xyz.jimbray.xmpp.xmppdemo.common.*
import xyz.jimbray.xmpp.xmppdemo.interfaces.XMPPIncomingMessageListener
import java.util.*

/**
 * Created by Jimbray on 2018/1/12.
 */
class XMPPService : Service(), XMPPConnectionListener, IncomingChatMessageListener {


    private val binder = XMPPBinder()

    protected var mXmppApiManager = XMPPApiManager.instance

    private var networkChangeReceiver: NetworkChangeReceiver? = null

    private val xmppIncomingMessageListeners = ArrayList<XMPPIncomingMessageListener>()

    private val TAG = XMPPService::class.java.simpleName

    private var mCurLoginedName: String? = null // 做个记录，用于自动重连
    private var mCurLoginPasswd : String? = null // 做个记录，用于自动重连

    override fun onCreate() {
        super.onCreate()
        log_i(TAG, "onCreate")

        mXmppApiManager.addConnectionListener(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log_i(TAG, "onStartCommand")

        mXmppApiManager.addIncomingChatMessageListener(this)

        if (networkChangeReceiver == null) {
            networkChangeReceiver = NetworkChangeReceiver()
        }
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, filter)

        RxBus2.getIntanceBus().registerRxBus(this, RxMessage::class.java, Consumer { (key, value) ->
            if (key == RxMessageConstants.MESSAGE_TYPE_NETWOKR) {
                // 网络发生变化
                if (value == RxMessageConstants.MESSAGE_NETWORK_WIFI_CONNECTED || value == RxMessageConstants.MESSAGE_NETWORK_MOBILE_CONNECTED) {
                    // 网络已连接
                    // 自动重连
                    if (!TextUtils.isEmpty(mCurLoginedName) && !TextUtils.isEmpty(mCurLoginPasswd)) {
                        binder?.xmppLogin(mCurLoginedName!!, mCurLoginPasswd!!)
                    }
                } else {
                    // 网络已断开
                    binder?.disconnectXmpp()
                }
            }
        })

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        log_i(TAG, "onDestroy")
        unregisterReceiver(networkChangeReceiver)
        binder?.disconnectXmpp()
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent): Boolean {
        log_i(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onConnected() {

    }

    override fun onConnectionClosed() {

    }

    override fun onConnectionClosedError() {
        if (!TextUtils.isEmpty(mCurLoginedName) && !TextUtils.isEmpty(mCurLoginPasswd)) {
            binder?.xmppLogin(mCurLoginedName!!, mCurLoginPasswd!!)
        }
    }

    override fun onReconnectionSuccessful() {

    }

    override fun onAuthenticated() {

    }

    override fun onReconnectionFailed() {

    }

    override fun onReconnectingIn() {

    }

    override fun newIncomingMessage(from: EntityBareJid, message: Message, chat: Chat) {

        for (listener in xmppIncomingMessageListeners) {
            listener?.newIncomingMessage(from, message, chat)
        }
    }

    inner class XMPPBinder : Binder() {

        fun xmppLogin(user_name: String, passwd: String) {
            mCurLoginedName = user_name
            mCurLoginPasswd = passwd
            XmppUtils.login(user_name, passwd)
        }

        fun xmppRegister(user_name: String, passwd: String) {
            XmppUtils.register(user_name, passwd)
        }

        fun disconnectXmpp() {
            mXmppApiManager.disconnect()
        }

        fun addIncomingMessageListener(listener: XMPPIncomingMessageListener) {
            if (!xmppIncomingMessageListeners.contains(listener)) {
                xmppIncomingMessageListeners.add(listener)
            }
        }

        fun removeIncomingMessageListener(listener: XMPPIncomingMessageListener) {
            if (xmppIncomingMessageListeners.contains(listener)) {
                xmppIncomingMessageListeners.remove(listener)
            }
        }

        fun sendSingleMessage(chat: Chat, message: String) {
            mXmppApiManager.sendSingleMessage(chat, message)
        }

    }



}
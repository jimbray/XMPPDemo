package xyz.jimbray.xmpp.xmppdemo.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.packet.Message
import org.jxmpp.jid.EntityBareJid
import xyz.jimbray.xmpp.xmppdemo.R
import xyz.jimbray.xmpp.xmppdemo.XMPPService
import xyz.jimbray.xmpp.xmppdemo.interfaces.XMPPIncomingMessageListener
import xyz.jimbray.xmpp.xmppdemo.log_d

class MainActivity : AppCompatActivity(), XMPPIncomingMessageListener {


    private var mServiceConnection: ServiceConnection? = null

    private var mBinder: XMPPService.XMPPBinder? = null

    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {

    }

    override fun onResume() {
        super.onResume()
        mServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                mBinder = service as XMPPService.XMPPBinder
                mBinder?.addIncomingMessageListener(this@MainActivity)

            }

            override fun onServiceDisconnected(name: ComponentName) {
                if (mBinder != null) {
                    mBinder?.removeIncomingMessageListener(this@MainActivity)
                }
                mBinder = null
            }
        }
        bindService(Intent(this, XMPPService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        mBinder?.removeIncomingMessageListener(this@MainActivity)
        unbindService(mServiceConnection)
    }

    override fun newIncomingMessage(from: EntityBareJid?, message: Message?, chat: Chat?) {
        log_d(TAG, "接收到来自${from}的消息（${message?.body}）")
    }
}

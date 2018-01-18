package xyz.jimbray.xmpp.xmppdemo.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*
import xyz.jimbray.xmpp.xmppdemo.R
import xyz.jimbray.xmpp.xmppdemo.XMPPService
import xyz.jimbray.xmpp.xmppdemo.api.XMPPApiManager

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private val xmppManager = XMPPApiManager.instance

    private var mServiceConnection: ServiceConnection? = null

    private var mBinder: XMPPService.XMPPBinder? = null

    private val TAG = LoginActivity::class.java.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login.setOnClickListener(this)
        btn_register.setOnClickListener(this)
        btn_disconnect.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_login ->      {
                mBinder?.xmppLogin(et_user_name.text.toString(), et_passwd.text.toString())
            }

            R.id.btn_register ->      {
                mBinder?.xmppRegister(et_user_name.text.toString(), et_passwd.text.toString())
            }

            R.id.btn_disconnect ->      {
                xmppManager.disconnect()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                mBinder = service as XMPPService.XMPPBinder

            }

            override fun onServiceDisconnected(name: ComponentName) {
                mBinder = null
            }
        }
        bindService(Intent(this, XMPPService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(mServiceConnection)
    }


}

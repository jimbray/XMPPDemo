package xyz.jimbray.xmpp.xmppdemo

import android.app.Application
import android.content.Context
import android.content.Intent



/**
 * Created by J on 2018/1/2.
 */
class App: Application() {

    private var mContext: Context? = null



    private val TAG = App::class.java.simpleName

    companion object {
        @Volatile private var appInstance: App? = null

        fun getInstance(): App? {
            return appInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
        appInstance = this

        startService(Intent(this, XMPPService::class.java))

    }


    fun getAppContext(): Context? {
        return mContext
    }

}
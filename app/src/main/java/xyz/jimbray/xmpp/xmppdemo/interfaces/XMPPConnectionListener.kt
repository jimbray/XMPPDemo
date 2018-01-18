package xyz.jimbray.xmpp.xmppdemo.api

/**
 * Created by Jimbray on 2018/1/2.
 */
interface XMPPConnectionListener {

    fun onConnected()

    fun onConnectionClosed()

    fun onConnectionClosedError()

    fun onReconnectionSuccessful()

    fun onAuthenticated()

    fun onReconnectionFailed()

    fun onReconnectingIn()
}
package xyz.jimbray.xmpp.xmppdemo.common

import android.os.Handler
import android.text.TextUtils
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import xyz.jimbray.xmpp.xmppdemo.App
import xyz.jimbray.xmpp.xmppdemo.api.XMPPApiManager
import xyz.jimbray.xmpp.xmppdemo.log_d

/**
 * Created by Jimbray on 2018/1/2.
 */
class XmppUtils {

    companion object {


        private val mXmppApiManager = XMPPApiManager.instance

        private val TAG = "XmppUtils"

        private var curUserName: String = ""
        private var curPasswd: String = ""

        private val RECONNECT_TIME_MILLSECOND: Long = 5000

        private val handler = Handler()

        private val reconnectRunnable = Runnable { login(curUserName, curPasswd) }


        fun register(user_name: String, passwd: String) {
            // 如果已经验证过的，需要退出登录?
            log_d(TAG, "register name->$user_name")

            if (!mXmppApiManager.isConnected()) {
                // 先进行连接
                Observable.create(ObservableOnSubscribe<Boolean> { emitter ->
                    emitter.onNext(mXmppApiManager.connect())
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .flatMap { isConnectSuccessful ->
                            if (isConnectSuccessful) {
                                // 连接成功后
                                // 进行注册
                                log_d(TAG, "xmpp connect successful")
                                Observable.just(mXmppApiManager.registerUser(user_name, passwd))
                            } else {
                                // 连接失败
                                log_d(TAG, "xmpp connect failed")
                                // 几秒后进行重连
                                handler.postDelayed(reconnectRunnable, RECONNECT_TIME_MILLSECOND)
                                Observable.just(false)
                            }
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { isRegisterSuccessful ->
                            if (isRegisterSuccessful) {
                                // 注册成功
                                // 进行登录
                                log_d(TAG, "xmpp register successful")
                            } else {
                                // 注册失败
                                log_d(TAG, "xmpp register failed")
                            }
                        }
            } else {
                // 直接进行注册操作
                Observable.create(ObservableOnSubscribe<Boolean> { emitter ->
                    emitter.onNext(mXmppApiManager.registerUser(user_name, passwd))
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { isRegisterSuccessful ->
                            if (isRegisterSuccessful) {
                                // 注册成功
                                log_d(TAG, "xmpp register successful")
                            } else {
                                // 注册失败
                                log_d(TAG, "xmpp register failed")
                            }
                        }
            }
        }

        fun login(user_name: String, passwd: String) {
            log_d(TAG, "login name->$user_name")

            curUserName = user_name
            curPasswd = passwd
            if (mXmppApiManager.isAuthenticated()) {
                // 已经登录过
                val userName = mXmppApiManager.getAuthenticatedUser()
                if (!TextUtils.isEmpty(userName)) {
                    log_d(TAG, "account logined as -> " + userName!!)
                } else {
                    log_d(TAG, "login successful as -> " + user_name)
                }
            } else {
                if (!mXmppApiManager.isConnected()) {
                    // 先进行连接

                    Observable.create(ObservableOnSubscribe<Boolean> { emitter ->
                        emitter.onNext(mXmppApiManager.connect())
                    })
                            .subscribeOn(Schedulers.io())
                            .flatMap { isConnectSuccessful ->
                                if (isConnectSuccessful) {
                                    // 连接成功后
                                    // 进行注册
                                    log_d(TAG, "xmpp connect successful")
                                    Observable.just(mXmppApiManager.registerUser(user_name, passwd))
                                } else {
                                    // 连接失败
                                    log_d(TAG, "xmpp connect failed")
                                    // 几秒后进行重连
                                    handler.postDelayed(reconnectRunnable, RECONNECT_TIME_MILLSECOND)
                                    Observable.just(false)
                                }
                            }
                            .flatMap { isRegisterSuccessful ->
                                if (isRegisterSuccessful) {
                                    // 注册成功
                                    // 进行登录
                                    log_d(TAG, "xmpp register successful")
                                    Observable.just(mXmppApiManager.login(user_name, passwd))
                                } else {
                                    // 注册失败
                                    log_d(TAG, "xmpp register failed")
                                    Observable.just(false)
                                }
                            }.observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ isLoginSuccessful ->
                                if (isLoginSuccessful!!) {
                                    log_d(TAG, "login successful as -> " + mXmppApiManager.getAuthenticatedUser()!!)
                                } else {
                                    log_d(TAG, "login failed")
                                }
                            })
                } else {
                    // 直接进行登录操作
                    Observable.create(ObservableOnSubscribe<Boolean> { emitter ->
                        emitter.onNext(mXmppApiManager.registerUser(user_name, passwd))
                    })
                            .subscribeOn(Schedulers.io())
                            .flatMap { isRegisterSuccessful ->
                                if (isRegisterSuccessful) {
                                    // 注册成功
                                    // 进行登录
                                    log_d(TAG, "xmpp register successful")
                                    Observable.just(mXmppApiManager.login(user_name, passwd))
                                } else {
                                    // 注册失败
                                    log_d(TAG, "xmpp register failed")
                                    Observable.just(false)
                                }
                            }.observeOn(AndroidSchedulers.mainThread())
                            .subscribe { isLoginSuccessful ->
                                if (isLoginSuccessful!!) {
                                    val userName = mXmppApiManager.getAuthenticatedUser()
                                    if (!TextUtils.isEmpty(userName)) {
                                        log_d(TAG, "account logined as -> " + userName!!)
                                    } else {
                                        log_d(TAG, "login successful as -> " + userName)
                                    }
                                } else {
                                    log_d(TAG, "login failed")
                                }
                            }
                }

            }
        }

        fun sendMessage(user_name: String, message: String?) {
            if (!TextUtils.isEmpty(message)) {
                Observable.create(ObservableOnSubscribe<Int> { emitter ->
                    mXmppApiManager.sendMessage(user_name, message!!)
                    emitter.onNext(0)
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { log_d(TAG, "send message complte") }
            }
        }

    }



}
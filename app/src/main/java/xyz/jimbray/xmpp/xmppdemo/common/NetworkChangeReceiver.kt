package xyz.jimbray.xmpp.xmppdemo.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import xyz.jimbray.xmpp.xmppdemo.log_i

/**
 * Created by Administrator on 2018/1/4.
 */
class NetworkChangeReceiver: BroadcastReceiver() {

    private val TAG = NetworkChangeReceiver::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        log_i(TAG, "onReceive 网络状态发生变化")

        // 如果api小于21，getNetworkinfo(int networType) 已弃用
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            log_i(TAG, "API 小于 21")

            val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // Wi-Fi 连接
            val wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            // 移动数据连接
            val dataNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

            if (wifiNetworkInfo.isConnected && dataNetworkInfo.isConnected) {
                log_i(TAG, "Wi-Fi 已连接，移动数据已连接")
                RxBus2.getIntanceBus().post(RxMessage(RxMessageConstants.MESSAGE_TYPE_NETWOKR, RxMessageConstants.MESSAGE_NETWORK_WIFI_CONNECTED) as Object)
            } else if (wifiNetworkInfo.isConnected && !dataNetworkInfo.isConnected) {
                log_i(TAG, "Wi-Fi 已连接，移动数据已断开")
                RxBus2.getIntanceBus().post(RxMessage(RxMessageConstants.MESSAGE_TYPE_NETWOKR, RxMessageConstants.MESSAGE_NETWORK_WIFI_CONNECTED) as Object)
            } else if (!wifiNetworkInfo.isConnected && dataNetworkInfo.isConnected) {
                log_i(TAG, "Wi-Fi 已断开，移动数据已连接")
                RxBus2.getIntanceBus().post(RxMessage(RxMessageConstants.MESSAGE_TYPE_NETWOKR, RxMessageConstants.MESSAGE_NETWORK_MOBILE_CONNECTED) as Object)
            } else {
                RxBus2.getIntanceBus().post(RxMessage(RxMessageConstants.MESSAGE_TYPE_NETWOKR, RxMessageConstants.MESSAGE_NETWORK_DISCONNETED) as Object)
                log_i(TAG, "Wi-Fi 已断开，移动数据已断开")
            }

        } else {
            log_i(TAG, "API 大于 21")

            val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val networks = connectivityManager.allNetworks

            var result = 0 // mobile false = 1, mobile true = 2 wifi = 4

            for (network in networks) {
                val networkInfo = connectivityManager.getNetworkInfo(network)

                networkInfo?.let {
                    //检测到有数据连接，但是并连接状态未生效，此种状态为wifi和数据同时已连接，以wifi连接优先
                    if (networkInfo.type == ConnectivityManager.TYPE_MOBILE && !networkInfo.isConnected) {
                        result += 1
                    }

                    //检测到有数据连接，并连接状态已生效，此种状态为只有数据连接，wifi并未连接上
                    if (networkInfo.type == ConnectivityManager.TYPE_MOBILE && networkInfo.isConnected) {
                        result += 2
                    }

                    //检测到有wifi连接，连接状态必为true
                    if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                        result += 4


                    }
                }


            }

            // 存在组合情况，以组合相加的唯一值作为最终状态的判断
            when (result) {
                0   ->  {
                    log_i(TAG, "Wi-Fi 已断开，移动数据已断开")
                    RxBus2.getIntanceBus().post(RxMessage(RxMessageConstants.MESSAGE_TYPE_NETWOKR, RxMessageConstants.MESSAGE_NETWORK_DISCONNETED) as Object)
                }
                2   ->  {
                    log_i(TAG, "Wi-Fi 已断开，移动数据已连接")
                    RxBus2.getIntanceBus().post(RxMessage(RxMessageConstants.MESSAGE_TYPE_NETWOKR, RxMessageConstants.MESSAGE_NETWORK_MOBILE_CONNECTED) as Object)
                }
                4   ->  {
                    log_i(TAG, "Wi-Fi 已连接，移动数据已断开")
                    RxBus2.getIntanceBus().post(RxMessage(RxMessageConstants.MESSAGE_TYPE_NETWOKR, RxMessageConstants.MESSAGE_NETWORK_WIFI_CONNECTED) as Object)
                }
                5   ->  {
                    log_i(TAG, "Wi-Fi 已连接，移动数据已连接")
                    RxBus2.getIntanceBus().post(RxMessage(RxMessageConstants.MESSAGE_TYPE_NETWOKR, RxMessageConstants.MESSAGE_NETWORK_WIFI_CONNECTED) as Object)
                }
            }
        }

    }

}
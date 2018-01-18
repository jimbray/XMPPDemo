package xyz.jimbray.xmpp.xmppdemo.api


import android.graphics.drawable.Drawable
import android.text.TextUtils
import org.jivesoftware.smack.*
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.chat2.IncomingChatMessageListener
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.packet.XMPPError
import org.jivesoftware.smack.provider.ProviderManager
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.roster.RosterEntry
import org.jivesoftware.smack.roster.RosterGroup
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smack.util.StringUtils
import org.jivesoftware.smackx.filetransfer.FileTransferManager
import org.jivesoftware.smackx.iqregister.AccountManager
import org.jivesoftware.smackx.muc.HostedRoom
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jivesoftware.smackx.offline.OfflineMessageManager
import org.jivesoftware.smackx.ping.PingFailedListener
import org.jivesoftware.smackx.ping.PingManager
import org.jivesoftware.smackx.search.UserSearchManager
import org.jivesoftware.smackx.vcardtemp.VCardManager
import org.jivesoftware.smackx.vcardtemp.packet.VCard
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider
import org.jivesoftware.smackx.xdata.FormField
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Localpart
import org.jxmpp.jid.parts.Resourcepart
import org.jxmpp.stringprep.XmppStringprepException
import xyz.jimbray.xmpp.xmppdemo.log_d
import xyz.jimbray.xmpp.xmppdemo.log_e
import xyz.jimbray.xmpp.xmppdemo.log_i
import java.io.*

import java.lang.Exception
import java.net.InetAddress
import java.net.URL
import java.net.UnknownHostException

/**
 * Created by Jimbray on 2017/12/29.
 */

class XMPPApi constructor(private val mConnectionListener: XMPPConnectionListener?,
                          private val mIncomingChatMessageListener: IncomingChatMessageListener?,
                          private val mOutgoingChatMessageListener: OutgoingChatMessageListener?) : ConnectionListener, IncomingChatMessageListener, OutgoingChatMessageListener, PingFailedListener {

    private val SERVER_IP = "SERVER_IP"
    private val SERVER_PORT = 5222

    private var mConnection: AbstractXMPPConnection? = null


//    private object XmppUtilsHolder {
//        val INSTANCE = XMPPApi()
//    }

    companion object {

        private val TAG = "xmpp-lib"

//        val instance: XMPPApi
//            get() = XmppUtilsHolder.INSTANCE

        val XMPP_STATUS_ONLINE = 0 // 在线
        val XMPP_STATUS_CHAT_ME = 1 // Q我吧
        val XMPP_STATUS_BUSY = 2 // 忙碌
        val XMPP_STATUS_LEAVE = 3 // 离开
        val XMPP_STATUS_STEALTH = 4 // 隐身（接口已失效？）
        val XMPP_STATUS_OFFLINE = 5 // 离线
    }


    fun getConnection() : XMPPConnection? {
        return mConnection
    }

    /**
     * 创建连接
     */
    fun connect(): Boolean {

        log_d(TAG, "connect")
        try {
            val configBuilder = XMPPTCPConnectionConfiguration.builder()
            // 设置主机ip
            configBuilder.setHostAddress(InetAddress.getByName(SERVER_IP))
            // 设置主机ip
            configBuilder.setHost(SERVER_IP)
            // 设置端口，默认5222
            configBuilder.setPort(SERVER_PORT)
            // 设置 demain
            configBuilder.setXmppDomain(SERVER_IP)
            // 禁用SSL连接
            configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
            // 设置 debug 模式
            configBuilder.setDebuggerEnabled(true)
            // 开启压缩
            configBuilder.setCompressionEnabled(true)
            // 设置离线状态
            configBuilder.setSendPresence(false)

            // 接受所有 加为好友 的请求
            Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all)

            mConnection = XMPPTCPConnection(configBuilder.build())
            mConnection?.addConnectionListener(this)

            mConnection?.connect()

            // 设定ping的间隔时间，保持连接状态
            val pingManager = PingManager.getInstanceFor(mConnection)
            pingManager.pingInterval = 300
            pingManager.registerPingFailedListener(this)


            log_d(TAG, "connection status is -> " + mConnection!!.isConnected.toString())
            return mConnection!!.isConnected
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            log_e(TAG, e.toString())
        } catch (e: XmppStringprepException) {
            e.printStackTrace()
            log_e(TAG, e.toString())
        } catch (e: InterruptedException) {
            e.printStackTrace()
            log_e(TAG, e.toString())
        } catch (e: IOException) {
            e.printStackTrace()
            log_e(TAG, e.toString())
        } catch (e: SmackException) {
            e.printStackTrace()
            log_e(TAG, e.toString())
        } catch (e: XMPPException) {
            e.printStackTrace()
            log_e(TAG, e.toString())
        }

        return false
    }

    /**
     * 注册新用户
     * @param user_name 用户名
     * @param passwd 密码
     * @return 是否成功
     */
    fun registerUser(user_name: String, passwd: String): Boolean {
        log_d(TAG, "resgieterUser")

        if (mConnection != null && mConnection!!.isConnected) {
            // 已经connect 上了，才可以进行注册操作
            try {
                val accountManager = AccountManager.getInstance(mConnection)
                if (accountManager.supportsAccountCreation()) {
                    accountManager.sensitiveOperationOverInsecureConnection(true)

                    val mapAttr = HashMap<String, String>()
                    mapAttr["name"] = user_name

                    accountManager.createAccount(Localpart.from(user_name), passwd, mapAttr)

                    log_d(TAG, "register is successful")

                    return true
                }
            } catch (e: SmackException.NoResponseException) {
                e.printStackTrace()
                log_e(TAG, e.toString())
            } catch (e: XMPPException.XMPPErrorException) {
                e.printStackTrace()
                log_e(TAG, e.toString())
                if (e.xmppError.condition == XMPPError.Condition.conflict) {
                    // 用户名已存在
                    log_d(TAG, "该用户名已存在")
                    return true
                }
            } catch (e: SmackException.NotConnectedException) {
                e.printStackTrace()
                log_e(TAG, e.toString())
                connect()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                log_e(TAG, e.toString())
            } catch (e: XmppStringprepException) {
                e.printStackTrace()
                log_e(TAG, e.toString())
            }

        }

        return false

    }

    /**
     * 登录
     * @param user_name 用户名
     * @param passwd 密码
     * @return 是否成功
     */
    fun login(user_name: String, passwd: String): Boolean {
        log_d(TAG, "login")
        if (mConnection != null && mConnection!!.isConnected) {
            try {
                mConnection?.login(user_name, passwd)
                setStatus(XMPP_STATUS_ONLINE)

//                mConnection?.addConnectionListener(this)
                ChatManager.getInstanceFor(mConnection).addIncomingListener(this)
                ChatManager.getInstanceFor(mConnection).addOutgoingListener(this)

                log_d(TAG, "login successful")
                return true

            } catch (e: XMPPException) {
                e.printStackTrace()
                log_e(TAG, e.toString())
            } catch (e: SmackException) {
                e.printStackTrace()
                log_e(TAG, e.toString())
                if (e is SmackException.AlreadyLoggedInException) {
                    return true
                }
            } catch (e: IOException) {
                e.printStackTrace()
                log_e(TAG, e.toString())
            } catch (e: InterruptedException) {
                e.printStackTrace()
                log_e(TAG, e.toString())
            }

        }
        return false
    }

    fun getAuthenticatedUser():String? {
        log_d(TAG, "getAuthenticatedUser")

        if (isAuthenticated()) {
            return AccountManager.getInstance(mConnection).getAccountAttribute("name")
        }
        return null
    }

    /**
     * 判断连接是否通过了验证
     * （是否登录成功）
     * @return 是否验证
     */
    fun isAuthenticated(): Boolean {
        if (mConnection != null) {
            return mConnection!!.isConnected!! && mConnection!!.isAuthenticated
        }
        return false
    }

    fun isConnected(): Boolean {
        if (mConnection != null) {
            return mConnection != null && mConnection?.isConnected!!
        }
        return false
    }


    /**
     * 更改用户状态
     * @param code 状态常量
     */
    fun setStatus(code: Int) {
        log_d(TAG, "setStatus")
        if (mConnection != null && mConnection!!.isConnected) {

            try {
                var presence: Presence? = null

                when (code) {
                    XMPP_STATUS_ONLINE -> {
                        log_d(TAG, "设置在线")
                        presence = Presence(Presence.Type.available)
                    }

                    XMPP_STATUS_CHAT_ME -> {
                        log_d(TAG, "设置Q我吧")
                        presence = Presence(Presence.Type.available)
                        presence.mode = Presence.Mode.chat
                    }

                    XMPP_STATUS_BUSY -> {
                        log_d(TAG, "设置忙碌")
                        presence = Presence(Presence.Type.available)
                        presence.mode = Presence.Mode.dnd
                    }

                    XMPP_STATUS_LEAVE -> {
                        log_d(TAG, "设置离开")
                        presence = Presence(Presence.Type.available)
                        presence.mode = Presence.Mode.away
                    }

                    XMPP_STATUS_STEALTH -> {
                        //                    Roster roster = con.getRoster();
//                    Collection<RosterEntry> entries = roster.getEntries();
//                    for (RosterEntry entry : entries) {
//                        presence = new Presence(Presence.Type.unavailable);
//                        presence.setPacketID(Packet.ID_NOT_AVAILABLE);
//                        presence.setFrom(con.getUser());
//                        presence.setTo(entry.getUser());
//                        con.sendPacket(presence);
//                        Log.v("state", presence.toXML());
//                    }
//                    // 向同一用户的其他客户端发送隐身状态
//                    presence = new Presence(Presence.Type.unavailable);
//                    presence.setPacketID(Packet.ID_NOT_AVAILABLE);
//                    presence.setFrom(con.getUser());
//                    presence.setTo(StringUtils.parseBareAddress(con.getUser()));
//                    con.sendStanza(presence);
//                    Log.v("state", "设置隐身");
                    }

                    XMPP_STATUS_OFFLINE -> {

                        log_d(TAG, "设置离线")
                        presence = Presence(Presence.Type.unavailable)
                    }
                }

                mConnection?.sendStanza(presence)
                log_d(TAG, "set status successful")
            } catch (e: SmackException.NotConnectedException) {
                e.printStackTrace()
                log_e(TAG, e.toString())
                connect()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                log_e(TAG, e.toString())
            }


        }
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        log_d(TAG, "disconnect connection")
        if (mConnection != null) {
            mConnection?.removeConnectionListener(this)
            if (mConnection!!.isConnected) {
                mConnection?.disconnect()
            }
            mConnection = null
        }
    }


    /**
     * 获取所有组
     * @return 所有组的 list
     */
    fun getGroups(): List<RosterGroup>? {
        log_d(TAG, "getGroups")
        if (mConnection != null) {
            val groupList = ArrayList<RosterGroup>()
            val rosterGroup = Roster.getInstanceFor(mConnection).groups
            groupList += rosterGroup
            return groupList
        }
        return null
    }

    /**
     * 获取指定组的所有好友
     * @param group_name 指定组的名称
     * @return 所有好友的列表 list
     */
    fun getFriendsInGroup(group_name: String): List<RosterEntry>? {
        log_d(TAG, "getFriendsInGroup")
        if (mConnection != null) {
            val entryList = ArrayList<RosterEntry>()
            val rosterGroup = Roster.getInstanceFor(mConnection).getGroup(group_name)
            val rosterEntryList = rosterGroup.entries
            entryList += rosterEntryList
            return entryList
        }
        return null
    }

    /**
     * 获取所有好友信息
     * @return 所有好友列表
     */
    fun getAllFriends(): List<RosterEntry>? {
        log_d(TAG, "getAllFriends")
        if (mConnection != null) {
            val entryList = ArrayList<RosterEntry>()
            val rotryEntry = Roster.getInstanceFor(mConnection).entries
            entryList += rotryEntry
            return entryList
        }

        return null
    }


    /**
     * 获取用户 vcard 信息
     * @param user_name user_name
     * @return 返回 vcard 信息
     */
    fun getUserVCard(user_name: String): VCard? {
        log_d(TAG, "getUserVCard")
        if (mConnection != null) {
            try {
                val vcard = VCardManager.getInstanceFor(mConnection).loadVCard(JidCreate.entityBareFrom(generateJID(user_name)))
                return vcard
            } catch (e: XmppStringprepException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: SmackException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: InterruptedException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: XMPPException.XMPPErrorException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }

        return null
    }

    /**
     * 获取指定用户的头像
     * @param user_name 用户jid
     * @return 返回 用户头像 drawable
     */
    fun getUserDrawable(user_name: String): Drawable? {
        log_d(TAG, "getUserDrawable")
        if (mConnection != null) {
            if (!TextUtils.isEmpty(user_name) || user_name.trim().isNotEmpty()) {
                val vcard = VCard()
                // 加入这句代码，解决 no VCard for
                ProviderManager.addIQProvider("vCard", "vcard-temp",
                        VCardProvider())

                try {
                    VCardManager.getInstanceFor(mConnection).loadVCard(
                            JidCreate.entityBareFrom(generateJID(user_name))
                    )
                } catch (e: XmppStringprepException) {
                    log_e(TAG, e.toString())
                    e.printStackTrace()
                } catch (e: SmackException) {
                    log_e(TAG, e.toString())
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    log_e(TAG, e.toString())
                    e.printStackTrace()
                } catch (e: XMPPException.XMPPErrorException) {
                    log_e(TAG, e.toString())
                    e.printStackTrace()
                }

                if (vcard.avatar != null) {
                    // 需要一个 inputstream 转 drawable 的方法
                    val bis = ByteArrayInputStream(vcard.avatar)

                }

            }
        }

        return null;
    }


    /**
     * 添加一个分组
     * @param group_name 新组名称
     */
    fun addGroup(group_name: String): Boolean {
        log_d(TAG, "addGroup")
        if (mConnection != null) {
            try {
                Roster.getInstanceFor(mConnection).createGroup(group_name)
                log_d(TAG, "创建 group 成功")
                return true
            } catch (e: Exception) {
                log_d(TAG, e.toString())
                e.printStackTrace()
            }
        }
        return false
    }

    /**
     * 删除分组
     * @param 分组名称
     * @return 没有分组
     */
    fun removeGroup(group_name: String): Boolean {
        // 并没有找到借口
        return true
    }


    /**
     * 添加好友 无分组
     * @param user_name jid
     * @param nick_name 用户昵称
     * @return 是否添加成功
     */
    fun addFriend(user_name: String, nick_name: String): Boolean {
        log_d(TAG, "addFriend")
        if (mConnection != null) {
            try {
                Roster.getInstanceFor(mConnection).createEntry(JidCreate.bareFrom(generateJID(user_name)),
                        nick_name, null)
                return true
            } catch (e: Exception) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }
        return false
    }

    /**
     * 添加好友 加入到指定分组
     * @param user_name jid
     * @param nick_name 用户昵称
     * @param group_name 用户组
     * @return 是否添加成功
     */
    fun addFriendToGroup(user_name: String, nick_name: String, group_name: String): Boolean {
        log_d(TAG, "addFriendToGroup")
        if (mConnection != null) {
            try {
                val subscription = Presence(Presence.Type.subscribe)
                subscription.to = JidCreate.entityBareFrom(generateJID(user_name))
                mConnection?.sendStanza(subscription)
                Roster.getInstanceFor(mConnection).createEntry(JidCreate.entityBareFrom(generateJID(user_name)),
                        nick_name,
                        arrayOf(group_name))

                return true
            } catch (e: Exception) {
                log_e(TAG, e.toString())
            }
        }
        return false
    }


    /**
     * 删除好友
     * @param user_name 还有名称
     * @return 删除是否成功
     */
    fun removeFriend(user_name: String): Boolean {
        log_d(TAG, "removeFriend")
        if (mConnection != null) {
            try {
                var entry: RosterEntry? = null
                val roster = Roster.getInstanceFor(mConnection)
                entry = roster.getEntry(JidCreate.
                        entityBareFrom(generateJID(user_name)))
                if (entry == null) {
                    entry = roster.getEntry(
                            JidCreate.entityBareFrom(user_name))
                }

                roster.removeEntry(entry)
                return true

            } catch (e: Exception) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }
        return false
    }


    /**
     * 搜索用户
     * @param user_name 用户名
     * @return 用户列表
     */
    fun searchUsers(user_name: String): List<HashMap<String, String>>? {
        log_d(TAG, "searchUsers")
        if (mConnection != null) {
            var map_user: HashMap<String, String>? = null
            val results = ArrayList<HashMap<String, String>>()

            try {
                val userSearchManager = UserSearchManager(mConnection)

                val searchForm = userSearchManager.getSearchForm(mConnection?.serviceName)

                val answerForm = searchForm?.createAnswerForm()
                answerForm?.setAnswer("userAccount", true)
                answerForm?.setAnswer("userPhote", user_name)
                val data = userSearchManager.getSearchResults(answerForm,
                        JidCreate.domainBareFrom(generateJID(user_name)))

                val rowList = data.rows
                for (row in rowList) {
                    map_user = HashMap()
                    map_user.put("userAccount", row.getValues("userAccount").toString())
                    map_user.put("userPhote", row.getValues("userPhote").toString())
                    results.add(map_user)
                    // 若存在，则有返回,UserName一定非空，其他两个若是有设，一定非空
                }

                return results
            } catch (e: XmppStringprepException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: SmackException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: InterruptedException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: XMPPException.XMPPErrorException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * 修改心情
     * @param status_msg 心情信息
     * @return 无
     */
    fun changeStateMessage(status_msg: String) {
        log_d(TAG, "changeStateMessage")
        if (mConnection != null) {
            val presence = Presence(Presence.Type.available)
            presence.status = status_msg

            try {
                mConnection?.sendStanza(presence)
            } catch (e: SmackException.NotConnectedException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
                connect()
            } catch (e: InterruptedException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }
    }


    /**
     * 修改用户头像
     * @param file 头像文件
     */
    fun changeAvater(file: File): Boolean {
        log_d(TAG, "changeAvatar")
        if (mConnection != null) {
            try {
                val vcard = VCardManager.getInstanceFor(mConnection).loadVCard()

                //从file 获取 byte数组
                var bytes = getFileBytes(file)

                val encodedImage = StringUtils.encodeHex(bytes)
                vcard.avatar = bytes
//                vcard.setAvatar(bytes, encodedImage)
//                vcard.setEncodedImage(encodedImage)
                vcard.setField("PHOTO", "<TYPE>image/jpg</TYPE><BINVAL>"
                        + encodedImage + "</BINVAL>", true)

                val bais = ByteArrayInputStream(
                        vcard.getAvatar())
                // bytes 转 bitmap
//                FormatTools.getInstance().InputStream2Bitmap(bais)

                VCardManager.getInstanceFor(mConnection).saveVCard(vcard)

            } catch (e: Exception) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }
        return false
    }

    /**
     * 文件转字节
     * @param file
     * @return byte[] 字节数组
     * @throws IOException
     */
    private fun getFileBytes(file: File): ByteArray {
        var bis: BufferedInputStream? = null
        try {
            bis = BufferedInputStream(FileInputStream(file))
            val fileByteLength = file.length().toInt()
            val buffer = ByteArray(fileByteLength)
            val readByteLength = bis.read(buffer)
            if (readByteLength != buffer.size) {
                throw IOException("Entire file not read")
            }
            return buffer
        } finally {
            if (bis != null) bis.close()
        }
    }


    /**
     * 删除当前账户
     * @return 是否成功删除
     */
    fun deleteAccount(): Boolean {
        log_d(TAG, "deleteAccount")
        if (mConnection != null) {
            try {
                AccountManager.getInstance(mConnection).deleteAccount()

                return true
            } catch (e: XMPPException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: SmackException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: InterruptedException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }

        return false
    }

    /**
     * 修改密码
     * @param 新密码
     * @return 是否成功
     * @question 不用验证旧密码？
     */
    fun changePasswd(new_passwd: String): Boolean {
        log_d(TAG, "changePasswd")
        if (mConnection != null) {
            try {
                AccountManager.getInstance(mConnection).changePassword(new_passwd)
                return true
            } catch (e: XMPPException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: SmackException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: InterruptedException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: XMPPException.XMPPErrorException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }
        return false
    }


    /**
     * 获取聊天室列表
     * @return 聊天室 列表
     */
    fun getHostRooms(): List<HostedRoom>? {
        log_d(TAG, "getHostRoom")
        if (mConnection != null) {
            try {
                val roomInfos = ArrayList<HostedRoom>()
                val hostRooms = MultiUserChatManager.getInstanceFor(mConnection)
                        .getHostedRooms(JidCreate.domainBareFrom(mConnection?.serviceName))

                for (room in hostRooms) {
                    roomInfos.add(room)
                    log_i(TAG, "获取聊天室 name -> ${room.name} - ID -> ${room.jid}")
                }
                log_i(TAG, "聊天室数量-> ${roomInfos.size}")

            } catch (e: XmppStringprepException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: SmackException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: InterruptedException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: XMPPException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }
        return null
    }


    /**
     * 创建聊天室
     * @param room_name 聊天室名称
     * @param passwd 聊天室密码
     * @return MultiUserChat 对象
     */
    fun createChatRoom(room_name: String, passwd: String): MultiUserChat? {
        log_d(TAG, "createChatRoom")
        if (mConnection != null) {
            try {

                // 创建一个 multiuserchat
                val multiUserChat = MultiUserChatManager.getInstanceFor(mConnection)
                        .getMultiUserChat(JidCreate.
                                entityBareFrom("$room_name@conference.${mConnection?.serviceName}"))
                // 创建聊天室
                multiUserChat.create(Resourcepart.from(room_name))
                // 获取聊天室的配置表单
                val form = multiUserChat.configurationForm
                // 根据原始的表单创建一个需要提交的新表单
                val submitForm = form.createAnswerForm()
                // 向要提交的表单添加默认答复
                form.fields
                        .filter { FormField.Type.hidden == it.type && it.variable != null }
                        .forEach {
                            // 设置默认值作为答复
                            submitForm.setDefaultAnswer(it.variable)
                        }
                // 设置聊天室的新拥有者
                val owners = ArrayList<String>()
                mConnection?.user?.asEntityBareJidString()?.let { owners.add(it) } // 用户 jid
                submitForm.setAnswer("muc#roomconfig_roomowners", owners)
                // 设置聊天室是持久聊天室, 即将要被保存下来
                submitForm.setAnswer("muc#roomconfig_persistentroom", true)
                //房间仅对成员开放
                submitForm.setAnswer("muc#roomconfig_membersonly", false)
                //允许占有者邀请其他人
                submitForm.setAnswer("muc#roomconfig_allowinvites", true)
                if (TextUtils.isEmpty(passwd)) {
                    // 进入是否需要密码
                    submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true)
                    // 设置进入密码
                    submitForm.setAnswer("muc#roomconfig_roomsecret", passwd)
                }

                // 能够发现占有者真实 JID 的角色
                // submitForm.setAnswer("muc#roomconfig_whois", "anyone");
                // 登录房间对话
                submitForm.setAnswer("muc#roomconfig_enablelogging", true)
                // 仅允许注册的昵称登录
                submitForm.setAnswer("x-muc#roomconfig_reservednick", true)
                // 允许使用者修改昵称
                submitForm.setAnswer("x-muc#roomconfig_canchangenick", false)
                // 允许用户注册房间
                submitForm.setAnswer("x-muc#roomconfig_registration", false)
                // 发送已完成的表单（有默认值）到服务器来配置聊天室
                multiUserChat.sendConfigurationForm(submitForm)

            } catch (e: XmppStringprepException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: SmackException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: InterruptedException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: XMPPException.XMPPErrorException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }
        return null
    }


    /**
     * 加入聊天室
     * @param user_name 用户昵称
     * @param room_name 聊天室名称
     */
    fun joinChatRoom(user_name: String, room_name: String): MultiUserChat? {
        log_d(TAG, "joinChatRoom")
        if (mConnection != null) {
            try {
                // 使用XMPPConnection 创建一个 multiuserchat 窗口
                val multiUserChat = MultiUserChatManager.getInstanceFor(mConnection)
                        .getMultiUserChat(
                                JidCreate.entityBareFrom(generateJID(user_name)))

                // 用户加入 聊天室
                multiUserChat.join(Resourcepart.from(user_name))

                log_i(TAG, "加入 $room_name 成功")
            } catch (e: XmppStringprepException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: SmackException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: InterruptedException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: XMPPException.XMPPErrorException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }
        return null
    }


    /**
     * 发送群组聊天消息
     * @param multiUserChat 聊天室对象
     * @param message 发送的信息
     */
    fun sendGroupMessage(multiUserChat: MultiUserChat, message: String) {
        log_d(TAG, "sendGroupMessage")
        try {
            multiUserChat.sendMessage(message)
        } catch (e: SmackException.NotConnectedException) {
            log_e(TAG, e.toString())
            e.printStackTrace()
            connect()
        } catch (e: InterruptedException) {
            log_e(TAG, e.toString())
            e.printStackTrace()
        }
    }

    /**
     * 查询聊天室成员名字
     * @param multiUserChat 聊天室对象
     * @return 成员名称列表
     */
    fun findChatRoomUser(multiUserChat: MultiUserChat): List<String>? {
        log_d(TAG, "findChatRoomUser")
        if (mConnection != null) {
            val userList = ArrayList<String>()
            val occupants = multiUserChat.occupants
            // 遍历出聊天室成员名称
            for (entityFullJid in occupants) {
                val name = entityFullJid.toString()
                userList.add(name)
            }

            return userList
        }
        return null
    }


    /**
     * 创建单人聊天室
     * @param target_user_name 对方jid
     * @return 单人聊天室对象
     */
    fun createSingleChat(target_user_name: String): Chat? {
        log_d(TAG, "createSingleChat")
        if (mConnection != null) {
            try {
                return ChatManager.getInstanceFor(mConnection)
                        .chatWith(JidCreate.entityBareFrom(target_user_name))
            } catch (e: XmppStringprepException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }

        return null
    }


    /**
     * 发送单人聊天 消息
     * @param chat 单人聊天室
     * @param message 发送的消息
     */
    fun sendSingleMessage(chat: Chat, message: String) {
        log_d(TAG, "sendSingleMessage->$message")
        if (mConnection != null) {
            try {
                val stanza = Message()
                stanza.body = message
                stanza.type = Message.Type.chat
                chat.send(stanza)
            } catch (e: SmackException.NotConnectedException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
                connect()
            } catch (e: InterruptedException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }
    }


    /**
     * 发送文件
     * @param user_name 对象
     * @param filePath 文件路径
     * @param message 附加消息
     */
    fun sendFile(user_name: String, filePath: String, message: String) {
        log_d(TAG, "sendFile")
        if (mConnection != null) {
            try {

                // 创建文件传输管理器
                val fileTransferManager = FileTransferManager.getInstanceFor(mConnection)

                // 创建输出的文件传输
                val transfer = fileTransferManager.createOutgoingFileTransfer(JidCreate.entityFullFrom(user_name))

                // 发送文件
                transfer?.sendFile(File(filePath), message)

            } catch (e: XmppStringprepException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            } catch (e: SmackException) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }
    }


    /**
     * 获取离线消息
     * @return 返回 fromUser key 对应的 message List 的 map
     */
    fun getOfflieMessage(): Map<String, List<HashMap<String, String>>>? {
        log_d(TAG, "getOfflineMessage")
        if (mConnection != null) {
            try {
                val offlineMessageManager = OfflineMessageManager(mConnection)

                if (offlineMessageManager.messageCount < 0) {
                    // 没有离线消息
                    return null
                }

                val offlineMessages = HashMap<String, List<HashMap<String, String>>>()

                for (message in offlineMessageManager.messages) {
                    val fromUser = message.from.toString()
                    val history = HashMap<String, String>()
                    mConnection?.user?.asEntityBareJidString()?.let { history.put("useraccount", it) }
                    history.put("friendaccount", fromUser)
                    history.put("info", message.body)
                    history.put("type", "left")
                    if (offlineMessages.containsKey(fromUser)) {
                        (offlineMessages[fromUser] as ArrayList).add(history)
                    } else {
                        val list_temp = ArrayList<HashMap<String, String>>()
                        list_temp.add(history)
                        offlineMessages.put(fromUser, list_temp)
                    }
                }

                // 删除离线消息
                offlineMessageManager.deleteMessages()
                return offlineMessages

            } catch (e: Exception) {
                log_e(TAG, e.toString())
                e.printStackTrace()
            }
        }

        return null
    }

    /**
     * 判断OpenFire用户的状态 strUrl :
     * url格式 - http://my.openfire.com:9090/plugins/presence
     * /status?jid=user1@SERVER_NAME&type=xml
     * 返回值 : 0 - 用户不存在; 1 - 用户在线; 2 - 用户离线
     * 说明 ：必须要求 OpenFire加载 presence 插件，同时设置任何人都可以访问
     */
    fun isUserOnLine(user_name: String): Int {
        val url = "http://" + SERVER_IP + ":9090/plugins/presence/status?" +
                "jid=" + user_name + "@" + SERVER_IP + "&type=xml"
        var shOnLineState = 0 // 不存在
        try {
            val oUrl = URL(url)
            val oConn = oUrl.openConnection()
            if (oConn != null) {
                val oIn = BufferedReader(InputStreamReader(
                        oConn.getInputStream()))
                val strFlag = oIn.readLine()
                oIn.close()
                println("strFlag" + strFlag)
                if (strFlag.contains("type=\"unavailable\"")) {
                    shOnLineState = 2 // 用户离线
                }
                if (strFlag.contains("type=\"error\"")) {
                    shOnLineState = 0 // 用户 不存在
                } else if (strFlag.contains("priority") || strFlag.contains("id=\"")) {
                    shOnLineState = 1 // 用户在线
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        return shOnLineState
    }


    /**
     * ------------连接状态监听---------------------
     */
    override fun connected(connection: XMPPConnection?) {
        log_d(TAG, "connection connected")
        mConnectionListener?.onConnected()
    }

    override fun connectionClosed() {
        log_d(TAG, "connection closed")
        mConnectionListener?.onConnectionClosed()
    }

    override fun connectionClosedOnError(e: Exception?) {
        log_d(TAG, "connection close error -> ${e.toString()}")
        mConnectionListener?.onConnectionClosedError()
    }

    override fun reconnectionSuccessful() {
        log_d(TAG, "connection successful")
        mConnectionListener?.onReconnectionSuccessful()
    }

    override fun authenticated(connection: XMPPConnection?, resumed: Boolean) {
        log_d(TAG, "authenticated resume-> $resumed")
        mConnectionListener?.onAuthenticated()
    }

    override fun reconnectionFailed(e: Exception?) {
        log_d(TAG, "reconnection failed")
        mConnectionListener?.onReconnectionFailed()
    }

    override fun reconnectingIn(seconds: Int) {
        log_d(TAG, "reconnecting")
        mConnectionListener?.onReconnectingIn()
    }

    override fun pingFailed() {
        log_d(TAG, "ping failed!")
    }

    override fun newIncomingMessage(from: EntityBareJid?, message: Message?, chat: Chat?) {
        log_i(TAG, "receive ${message?.body} from $from")
        mIncomingChatMessageListener?.newIncomingMessage(from, message, chat)
    }

    override fun newOutgoingMessage(to: EntityBareJid?, message: Message?, chat: Chat?) {
        log_i(TAG, "send ${message?.body} to $to")
        mOutgoingChatMessageListener?.newOutgoingMessage(to, message, chat)

    }

    fun generateJID(user_name: String): String {
        return "$user_name@${mConnection?.serviceName}"
    }
}

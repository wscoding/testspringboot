package cn.jufb.webSocket;


import cn.hutool.json.JSONUtil;
import cn.jufb.common.CommonCode;
import cn.jufb.entity.Msg;
import cn.jufb.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
@Slf4j
@ServerEndpoint("/webSocket/{userName}/{userId}")
public class WebSocketServer {

    private static int onlineCount = 0;

    private static ConcurrentHashMap<Long, WebSocketServer> webSocketUserMap = new ConcurrentHashMap<>();

    private Session session;

    private User user;

    /**
     * 连接建立成功
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId, @PathParam("userName") String userName) {
        try {
            User userInfo = new User(userId, userName);
            this.user = userInfo;
            this.session = session;

            if (webSocketUserMap.containsKey(user.getId())) { //判断是否已登陆
                Msg msg = new Msg("您的账号已在其他设备登陆！", CommonCode.FORCEDLOGINOUT);
                this.sendUserInfo(msg, user.getId());

                webSocketUserMap.remove(userId);
                webSocketUserMap.put(user.getId(), this);
            } else {
                webSocketUserMap.put(user.getId(), this);
                addOnlineCount();
            }

            //登陆成功
            Msg loginSuccessMsg = new Msg("登陆成功，用户名：" + this.user.getUserName() + "，用户ID：" + this.user.getId(), CommonCode.INFO);
            webSocketUserMap.get(user.getId()).sendMsgObjMessage(loginSuccessMsg);

            //向客服发送用户加入提示
            Msg fromUserMsg = new Msg(userInfo, CommonCode.FROMUSER);
            if (user.getId() != 1L) {
                webSocketUserMap.get(1L).sendMsgObjMessage(fromUserMsg);
            }


            log.info("加入提示，用户ID:{},用户名称:{},当前在线人数:{}", this.user.getId(), this.user.getUserName(), getOnlineCount());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose() {
        if (webSocketUserMap.containsKey(user.getId())) {
            webSocketUserMap.remove(user.getId());
            subOnlineCount();
        }

        log.info("退出提示，用户ID:{},用户名称:{},当前在线人数:{}", this.user.getId(), this.user.getUserName(), getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     **/
    @OnMessage
    public void onMessage(String message) {
        Msg msg = JSONUtil.parseObj(message).toBean(Msg.class);

        //判断消息是否不为空
        if (StringUtils.isNotBlank(msg.getContent())) {
            //设置发送的用户ID
            msg.setUserId(this.user.getId());
            msg.setType(CommonCode.MSG);

            //发送给指定的用户ID  判断对方是否在线
            if (msg.getToUserId() != null && webSocketUserMap.containsKey(msg.getToUserId())) {
                webSocketUserMap.get(msg.getToUserId()).sendMsgObjMessage(msg);

                log.info("发送者：{} -> 接受者：{}, 消息内容：{} - 发送消息成功", msg.getUserId(), msg.getToUserId(), msg.getContent());
            } else {
                //对方不在线
                log.info("发送者：{} -> 接受者：{}, 消息内容：{} - 对方不在线", msg.getUserId(), msg.getToUserId(), msg.getContent());
            }
        }
    }


    /**
     * 错误日志
     *
     * @param error
     */
    @OnError
    public void onError(Throwable error) {
        log.error("用户错误:" + user + ",原因:" + error.getMessage());
        error.printStackTrace();
    }

    /**
     * 发送文字消息
     *
     * @param msg
     */
    public void sendTestMessage(String msg) {
        try {
            session.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送Msg对象消息
     *
     * @param msg
     */
    public void sendMsgObjMessage(Msg msg) {
        try {
            String message = JSONUtil.parse(msg).toString();
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送自定义消息
     **/
    public static void sendUserInfo(Msg msg, Long userId) {
        if (userId != null && webSocketUserMap.containsKey(userId)) {
            webSocketUserMap.get(userId).sendMsgObjMessage(msg);
        }
    }

    /**
     * 获取当前在线用户集合
     *
     * @return
     */
    public static List<Long> getOnlineUserList() {
        List userList = new ArrayList();

        webSocketUserMap.entrySet().stream().forEach(new Consumer<Map.Entry<Long, WebSocketServer>>() {
            @Override
            public void accept(Map.Entry<Long, WebSocketServer> userWebSocketServerEntry) {
                Long userInfo = userWebSocketServerEntry.getKey();
                userList.add(userInfo);
            }
        });

        return userList;
    }

    /**
     * 获得此时的在线人数
     *
     * @return
     */
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    /**
     * 在线人数加1
     */
    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    /**
     * 在线人数减1
     */
    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}

package cn.jufb.service.impl;

import cn.jufb.webSocket.WebSocketServer;
import cn.jufb.entity.vo.RoomInfoVo;
import cn.jufb.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    /**
     * 获取房间内当前在线用户集合
     *
     * @return
     */
    @Override
    public RoomInfoVo getOnlineUserList() {
        int onlineCount = WebSocketServer.getOnlineCount();
        List<Long> onlineUserList = WebSocketServer.getOnlineUserList();

        RoomInfoVo roomInfoVo = new RoomInfoVo();
        roomInfoVo.setUserOnLineCount(onlineCount);
        roomInfoVo.setUserList(onlineUserList);

        return roomInfoVo;
    }
}

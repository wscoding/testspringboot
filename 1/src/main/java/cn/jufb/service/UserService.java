package cn.jufb.service;


import cn.jufb.entity.vo.RoomInfoVo;

public interface UserService {
    /**
     * 获取房间内当前在线用户集合
     *
     * @return
     */
    RoomInfoVo getOnlineUserList();
}

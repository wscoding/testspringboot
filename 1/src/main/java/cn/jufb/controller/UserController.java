package cn.jufb.controller;

import cn.jufb.entity.vo.RoomInfoVo;
import cn.jufb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取房间内当前在线用户集合
     *
     * @return
     */
    @GetMapping("/roomOnlineUser")
    public RoomInfoVo getOnlineUserList() {
        RoomInfoVo onlineUserList = userService.getOnlineUserList();
        return onlineUserList;
    }
}

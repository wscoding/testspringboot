package cn.jufb.entity;

import lombok.Data;

@Data
//@AllArgsConstructor
//@NoArgsConstructor
public class Msg {
    private Long userId;    //发送者ID
    private Long toUserId;  //接收者ID
    private String content; //消息内容
    private String type;    //消息类型
    private User user;      //用户信息

    public Msg(String content, String type) {
        this.content = content;
        this.type = type;
    }

    public Msg(User user, String type) {
        this.user = user;
        this.type = type;
    }
}

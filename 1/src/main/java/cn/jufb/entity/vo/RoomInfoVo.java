package cn.jufb.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomInfoVo {
    private Integer userOnLineCount;
    private List<Long> userList;
}

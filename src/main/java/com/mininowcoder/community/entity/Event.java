package com.mininowcoder.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by FeiPan on 2022/4/25.
 */

@Data
public class Event {
    private String topic; // 事件的类型-评论,点赞,关注
    private int userId; // 事件触发的人
    private int entityType; // 实体类型
    private int entityId; // 实体id
    private int entityUserId; // 实体作者
    private Map<String, Object> data = new HashMap<>();
}

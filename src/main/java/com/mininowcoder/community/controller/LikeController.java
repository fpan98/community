package com.mininowcoder.community.controller;

import com.mininowcoder.community.annotation.LoginRequired;
import com.mininowcoder.community.entity.Event;
import com.mininowcoder.community.entity.User;
import com.mininowcoder.community.event.EventProducer;
import com.mininowcoder.community.service.LikeService;
import com.mininowcoder.community.util.CommunityConstant;
import com.mininowcoder.community.util.CommunityUtil;
import com.mininowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by FeiPan on 2022/4/24.
 */
@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    // 异步请求
    @LoginRequired
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId){
        User user = hostHolder.getUser();
        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 获取点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 当前用户是否点赞了
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 包装结果给页面
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件 (取消赞就不要通知作者了)
        if(likeStatus==1){
            Event event = new Event();
            event.setTopic(TOPIC_LIKE);
            event.setUserId(hostHolder.getUser().getId());
            event.setEntityType(entityType);
            event.setEntityId(entityId);
            event.setEntityUserId(entityUserId);
            Map<String, Object> data = new HashMap<>();
            data.put("postId", postId);
            event.setData(data);

            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }
}

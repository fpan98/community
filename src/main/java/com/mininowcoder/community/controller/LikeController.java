package com.mininowcoder.community.controller;

import com.mininowcoder.community.annotation.LoginRequired;
import com.mininowcoder.community.entity.Event;
import com.mininowcoder.community.entity.User;
import com.mininowcoder.community.event.EventProducer;
import com.mininowcoder.community.service.LikeService;
import com.mininowcoder.community.util.CommunityConstant;
import com.mininowcoder.community.util.CommunityUtil;
import com.mininowcoder.community.util.HostHolder;
import com.mininowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate redisTemplate;

//    异步请求
//    @LoginRequired
//    对于异步请求的话不直接进行拦截，而是在程序中判断用户是否登录，然后将信息返回给前端
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId){
        User user = hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403, "你还没有登录哦！");
        }
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

        if(entityType==ENTITY_TYPE_POST){
            // 将帖子放入redis的set集合中，然后定期计算分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }
}

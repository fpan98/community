package com.mininowcoder.community.controller;

import com.mininowcoder.community.annotation.LoginRequired;
import com.mininowcoder.community.entity.Comment;
import com.mininowcoder.community.entity.DiscussPost;
import com.mininowcoder.community.entity.Event;
import com.mininowcoder.community.event.EventProducer;
import com.mininowcoder.community.service.CommentService;
import com.mininowcoder.community.service.DiscussPostService;
import com.mininowcoder.community.util.CommunityConstant;
import com.mininowcoder.community.util.CommunityUtil;
import com.mininowcoder.community.util.HostHolder;
import com.mininowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by FeiPan on 2022/4/23.
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    @LoginRequired
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        // 触发评论事件
        Event event = new Event();
        event.setTopic(TOPIC_COMMENT);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(comment.getEntityType());
        event.setEntityId(comment.getEntityId());
        Map<String, Object> data = new HashMap<>();
        data.put("postId", discussPostId);
        event.setData(data);

        // 获得发布帖子or评论的用户的id
        if(comment.getEntityType() == ENTITY_TYPE_POST){ // 评论的是帖子, 则获取帖子的id
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if(comment.getEntityType() == ENTITY_TYPE_COMMENT){ // 评论的是评论，则获取评论的id
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        // 如果评论的是帖子的话会修改帖子的评论数，因此也需要触发事件 将修改后的帖子存放到es服务器中
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            event = new Event();
            event.setTopic(TOPIC_PUBLISH);
            event.setUserId(comment.getUserId());
            event.setEntityType(ENTITY_TYPE_POST);
            event.setEntityId(discussPostId);
            eventProducer.fireEvent(event);

            // 将帖子放入redis的set集合中，然后定期计算分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, discussPostId);
        }

        return "redirect:/discuss/detail/" + discussPostId;
    }
}

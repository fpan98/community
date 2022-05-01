package com.mininowcoder.community.controller;

import com.mininowcoder.community.annotation.LoginRequired;
import com.mininowcoder.community.entity.Comment;
import com.mininowcoder.community.entity.DiscussPost;
import com.mininowcoder.community.entity.Event;
import com.mininowcoder.community.entity.User;
import com.mininowcoder.community.event.EventProducer;
import com.mininowcoder.community.service.CommentService;
import com.mininowcoder.community.service.DiscussPostService;
import com.mininowcoder.community.service.LikeService;
import com.mininowcoder.community.service.UserService;
import com.mininowcoder.community.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by FeiPan on 2022/4/23.
 */

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

//    @LoginRequired
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还没有登录哦！");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // 触发发帖事件 采用异步的方式将新发布的帖子存放到es服务器中
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(user.getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(post.getId());
        eventProducer.fireEvent(event);

        // 将帖子放入redis的set集合中，然后定期计算分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        // 报错的程序，系统将统一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId,
                                 Model model,
                                 Page page) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        // 帖子的点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // 当前用户对帖子的点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        // 评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // 评论：给帖子的评论； 回复：给评论的评论
        // 评论列表
        List<Comment> commentsList = commentService.findCommentsByEntity(CommunityConstant.ENTITY_TYPE_POST,
                post.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentWrapperList = new ArrayList<>();// 评论+用户信息
        if (commentsList != null) {
            for (Comment comment : commentsList) {
                Map<String, Object> commentWrapper = new HashMap<>();
                // 评论及评论人信息
                commentWrapper.put("comment", comment);
                commentWrapper.put("user", userService.findUserById(comment.getUserId()));

                // 评论的点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentWrapper.put("likeCount", likeCount);
                // 当前用户对该评论的点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentWrapper.put("likeStatus", likeStatus);

                // 回复列表-全部查询出来，不做分页处理
                // select * from comment where status=0 and entity_type=2 and entity_id=146 order by create_time limit 0, 100
                List<Comment> replayList = commentService.findCommentsByEntity(CommunityConstant.ENTITY_TYPE_COMMENT,
                        comment.getId(), 0, Integer.MAX_VALUE);
                // 在回复中也包装上用户信息
                List<Map<String, Object>> replayWrapperList = new ArrayList<>();
                if (replayList != null) {
                    for (Comment reply : replayList) {
                        Map<String, Object> replyWrapper = new HashMap<>();
                        // 回复及回复人信息
                        replyWrapper.put("reply", reply);
                        replyWrapper.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyWrapper.put("target", target);

                        // 回复的点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyWrapper.put("likeCount", likeCount);
                        // 当前用户对该回复的点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyWrapper.put("likeStatus", likeStatus);

                        replayWrapperList.add(replyWrapper);
                    }
                }
                commentWrapper.put("replys", replayWrapperList);

                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentWrapper.put("replyCount", replyCount);

                commentWrapperList.add(commentWrapper);
            }
        }
        model.addAttribute("comments", commentWrapperList);

        return "/site/discuss-detail";
    }

    // 置顶
//    @LoginRequired
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int postId, int userType){
        User user = hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403, "你还没有登录哦！");
        }
        if(user.getType()!=userType){
            return CommunityUtil.getJSONString(403, "你没有权限操作哦！");
        }
        discussPostService.updateType(postId, 1); // 0正常 1置顶
        // 触发发帖事件 更新帖子至es中
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(user.getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(postId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    // 加精华
//    @LoginRequired
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int postId, int userType){
        User user = hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403, "你还没有登录哦！");
        }
        if(user.getType()!=userType){
            return CommunityUtil.getJSONString(403, "你没有权限操作哦！");
        }

        discussPostService.updateStatus(postId, 1); //0正常 1精华 2拉黑
        // 触发发帖事件 更新帖子至es中
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(user.getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(postId);
        eventProducer.fireEvent(event);

        // 将帖子放入redis的set集合中，然后定期计算分数(加精华会影响帖子分数)
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, postId);

        return CommunityUtil.getJSONString(0);
    }

    // 拉黑
//    @LoginRequired
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int postId, int userType){
        User user = hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403, "你还没有登录哦！");
        }
        if(user.getType()!=userType){
            return CommunityUtil.getJSONString(403, "你没有权限操作哦！");
        }

        discussPostService.updateStatus(postId, 2);
        // 触发发帖事件 更新帖子至es中
        Event event = new Event();
        event.setTopic(TOPIC_DELETE);
        event.setUserId(user.getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(postId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }
}














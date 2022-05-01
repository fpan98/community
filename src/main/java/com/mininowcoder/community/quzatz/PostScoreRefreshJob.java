package com.mininowcoder.community.quzatz;

import com.mininowcoder.community.entity.DiscussPost;
import com.mininowcoder.community.service.DiscussPostService;
import com.mininowcoder.community.service.ElasticsearchService;
import com.mininowcoder.community.service.LikeService;
import com.mininowcoder.community.util.CommunityConstant;
import com.mininowcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by FeiPan on 2022/4/30.
 */
public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    // 论坛纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-04-30 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化社区论坛纪元失败！", e);
        }
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新score的帖子！");
            return;
        }
        logger.info("[任务开始] 正在刷新帖子分数！待刷新的帖子数量：" + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }

        logger.info("[任务完成] 帖子分数刷新完毕！");
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null) {
            logger.error("该帖子不存在：id = " + postId);
            return;
        }

        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // score = log(精华分+评论数*10+点赞数*2+收藏数*2)+(发布时间-论坛纪元) 单位天
        double score = Math.max(Math.log10((wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2), 0)
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId, score);

        // 同步elasticsearch数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);

    }
}

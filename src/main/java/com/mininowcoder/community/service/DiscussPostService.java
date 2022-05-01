package com.mininowcoder.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.mininowcoder.community.dao.DiscussPostMapper;
import com.mininowcoder.community.entity.DiscussPost;
import com.mininowcoder.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by FeiPan on 2022/4/20.
 */
@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口：Cache, LoadingCache同步缓存, AsyncLoadingCache异步缓存

    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    @PostConstruct
    public void init(){
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(String key) throws Exception {
                        if(key==null||key.length()==0){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        String[] params = key.split(":");
                        if(params==null||params.length!=2){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);
                        // TODO:二级缓存 redis

                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(-1, offset, limit, 1);
                    }
                });
        // 初始化帖子总数缓存,就一个数
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(Integer key) throws Exception {

                        logger.debug("load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });

    }

    // userId=-1时表示查询所有数据，否则查询指定用户的数据
    public List<DiscussPost> findDiscussPost(int userId, int offset, int limit, int orderMode){
        if(userId==-1&&orderMode==1){ // 热门帖子列表时查缓存
            return postListCache.get(offset+":"+limit);
        }

        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    // userId=-1时返回所有用户讨论帖的总数，否则返回指定用户发送讨论帖的数量
    public int findDisscussPostRows(int userId){
        if(userId==-1){
            return postRowsCache.get(userId);
        }

        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post){
        if(post==null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 敏感词过滤
        post.setTitle(sensitiveFilter.filterSensitiveWords(post.getTitle()));
        post.setContent(sensitiveFilter.filterSensitiveWords(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount){
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateType(int id, int type){
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status){
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id, double score){
        return discussPostMapper.updateScore(id, score);
    }
}

package com.mininowcoder.community.service;

import com.mininowcoder.community.dao.DiscussPostMapper;
import com.mininowcoder.community.entity.DiscussPost;
import com.mininowcoder.community.util.SensitiveFilter;
import com.mysql.cj.xdevapi.TableImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * Created by FeiPan on 2022/4/20.
 */
@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * @param userId=-1时表示查询所有数据，否则查询指定用户的数据
     * @param offset：数据的起始偏移量，用于计算分页后的页数
     * @param limit：每页中显示的数据量
     * @return：查询每页中的数据
     */
    public List<DiscussPost> findDiscussPost(int userId, int offset, int limit){
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    /**
     * @param userId=-1时返回所有用户讨论帖的总数，否则返回指定用户发送讨论帖的数量
     * @return
     */
    public int findDisscussPostRows(int userId){
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
}

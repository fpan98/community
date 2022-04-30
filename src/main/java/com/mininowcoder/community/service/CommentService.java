package com.mininowcoder.community.service;

import com.mininowcoder.community.dao.CommentMapper;
import com.mininowcoder.community.entity.Comment;
import com.mininowcoder.community.util.CommunityConstant;
import com.mininowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * Created by FeiPan on 2022/4/23.
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId){
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    // 因为该方法中包含两次操作：1、向comment表中存放评论 2、修改discuss_post表中的comment_count字段，所以需要使用事务
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment==null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));//过滤标签
        comment.setContent(sensitiveFilter.filterSensitiveWords(comment.getContent()));//过滤敏感词
        int rows = commentMapper.insertComment(comment);

        // 更新帖子评论数量（注意不是回复数量）
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            int commentCount = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), commentCount);
        }
        return rows;
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }
}

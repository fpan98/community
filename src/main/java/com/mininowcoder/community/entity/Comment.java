package com.mininowcoder.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * Created by FeiPan on 2022/4/20.
 */
@Data
public class Comment {
    private int id;
    private int userId;
    private int entityType; // 直接在帖子下的评论1，评论下的评论则为2
    private int entityId; // 评论所属帖子id
    private int targetId; // 评论下的评论，针对某个用户的回复targetId=user.id，只针对此评论的回复targedId=0
    private String content;
    private int status; // status=0：数据有效，=1数据无效
    private Date createTime;
}

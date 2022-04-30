package com.mininowcoder.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * Created by FeiPan on 2022/4/23.
 */
@Data
public class Message {
    private int id;
    private int fromId;// 消息放松方
    private int toId;// 消息接收方
    private String conversationId; // 会话: fromId和toId的组合，小的数在前-大的数在后
    private String content;
    private int status; // 0未读，1已读，2删除
    private Date createTime;
}

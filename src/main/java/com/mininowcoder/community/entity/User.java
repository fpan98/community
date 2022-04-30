package com.mininowcoder.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * Created by FeiPan on 2022/4/20.
 */
@Data
public class User {
    private int id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private int type; //0-普通用户; 1-超级管理员; 2-版主;
    private int status;  // 0-未激活; 1-已激活;
    private String activationCode;
    private String headerUrl;
    private Date createTime;
}

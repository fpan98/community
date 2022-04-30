package com.mininowcoder.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * Created by FeiPan on 2022/4/22.
 */
@Data
public class LoginTicket {
    private int id;
    private int userId;
    private String ticket;
    private int status; // 0-生效，1-失效
    private Date expired;
}

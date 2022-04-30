package com.mininowcoder.community.dao;

import com.mininowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * Created by FeiPan on 2022/4/22.
 * 后面用redis实现了
 */
@Mapper
@Deprecated
public interface LoginTicketMapper {

    @Insert("insert into login_ticket(user_id,ticket,status,expired) values(#{userId},#{ticket},#{status},#{expired})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicker(LoginTicket loginTicket);

    @Select("select * from login_ticket where ticket=#{ticket}")
    LoginTicket selectByTicket(String ticket);

    @Update("update login_ticket set status=#{status} where ticket=#{ticket}")
    int updateStatus(String ticket, int status);
}

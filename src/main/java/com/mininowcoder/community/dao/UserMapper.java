package com.mininowcoder.community.dao;

import com.mininowcoder.community.entity.User;
import org.apache.ibatis.annotations.*;

/**
 * Created by FeiPan on 2022/4/20.
 */
@Mapper
public interface UserMapper {

    @Select("select * from user where id=#{id}")
    User selectById(@Param("id") int id);

    @Select("select * from user where username=#{username}")
    User selectByName(@Param("username") String username);

    @Select("select * from user where email=#{email}")
    User selectByEmail(@Param("email") String email);

    @Insert("insert into user (username,password,salt,email,type,status,activation_code,header_url,create_time) " +
            "values(#{username},#{password},#{salt},#{email},#{type},#{status},#{activationCode},#{headerUrl},#{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(User user);

    @Update("update user set status=#{status} where id=#{id}")
    int updateStatusById(@Param("id") int id, @Param("status") int status);

    @Update("update user set header_url=#{headerUrl} where id=#{id}")
    int updateHeaderUrlById(@Param("id") int id, @Param("headerUrl") String headerUrl);

    @Update("update user set password=#{password} where id=#{id}")
    int updatePasswordById(@Param("id") int id, @Param("password") String password);


}

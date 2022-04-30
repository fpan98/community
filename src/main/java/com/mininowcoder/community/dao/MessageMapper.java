package com.mininowcoder.community.dao;

import com.mininowcoder.community.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by FeiPan on 2022/4/23.
 */
@Mapper
public interface MessageMapper {

    // 查询当前用户的会话列表，针对每个会话只返回一条最新的私信
    @Select("select * from message where id in " +
            "(select max(id) from message " +
            "where status!=2 and from_id!=1 and (from_id=#{userId} or to_id=#{userId})" +
            "group by conversation_id)" +
            "order by id desc limit #{offset}, #{limit}")
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量
    @Select("select count(m.maxId) from " +
            "(select max(id) as maxId from message " +
            "where status!=2 and from_id!=1 and (from_id=#{userId} or to_id=#{userId}) " +
            "group by conversation_id) as m")
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表
    @Select("select * from message where status!=2 and from_id!=1 and conversation_id=#{conversationId} " +
            "order by id asc limit #{offset}, #{limit}")
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量
    @Select("select count(id) from message where status!=2 and from_id!=1 and conversation_id=#{conversationId}")
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量
    @Select("<script>" +
            "select count(id) from message" +
            "<where>" +
            "status=0 and from_id!=1 and to_id=#{userId} " +
            "<if test='conversationId!=null'> AND conversation_id=#{conversationId}</if>" +
            "</where>" +
            "</script>")
    int selectLetterUnreadCount(int userId, String conversationId);

    // 新增消息
    @Insert("insert into message (from_id,to_id,conversation_id,content,status,create_time) " +
            "values(#{fromId},#{toId},#{conversationId},#{content},#{status},#{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMessage(Message message);


    // 修改消息的状态
    @Update("<script>" +
            "update message set status=#{status} where id in " +
            "<foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\" >" +
            " #{id} " +
            "</foreach>" +
            "</script>")
    int updateStatus(List<Integer> ids, int status);

    // 查询某个主题下最新的通知
    @Select("select * from message where id in " +
            "(select max(id) from message where status!=2 and from_id=1 and to_id=#{userId} and conversation_id=#{topic})")
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题所包含的通知数量
    @Select("select count(id) from message where status!=2 and from_id=1 and to_id=#{userId} and conversation_id=#{topic}")
    int selectNoticeCount(int userId, String topic);

    // 查询未读的通知数量 (topic==null则查询所有主题下的通知数量，topic!=null则查询指定主题下的通知数量)
    @Select("<script>" +
            "select count(id) from message " +
            "<where>" +
            "status=0 and from_id=1 and to_id=#{userId} " +
            "<if test='topic!=null'> AND conversation_id=#{topic}</if>" +
            "</where>" +
            "</script>"
            )
    int selectNoticeUnreadCount(int userId, String topic);

    // 查询某个主题所包含的通知列表
    @Select("select * from message where status!=2 and from_id=1 and to_id=#{userId} and conversation_id=#{topic} " +
            "order by create_time desc limit #{offset}, #{limit}")
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}

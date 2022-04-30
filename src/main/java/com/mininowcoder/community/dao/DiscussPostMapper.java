package com.mininowcoder.community.dao;

import com.mininowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by FeiPan on 2022/4/20.
 */
@Mapper
public interface DiscussPostMapper {

    /**
     * 动态拼接sql，如果userId==-1,则查全部用户；如果userId!=-1,则查指定用户；
     * status=2表示该帖子被拉黑；type字段：0-普通，1-置顶；
     * @return
     */
    @Select("<script>" +
                    "select * from discuss_post"+
                    "<where>"+
                    "status!=2 "+
                    "<if test='userId!=-1'> AND user_id=#{userId} </if>"+
                    "order by type desc, create_time desc"+
                    "</where>"+
                    "limit #{offset}, #{limit}"+
            "</script>")
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

    @Select("<script>" +
                "select count(1) from discuss_post"+
                "<where>"+
                "status!=2"+
                "<if test='userId!=-1'> AND user_id=#{userId}</if>"+
                "</where>"+
            "</script>")
    int selectDiscussPostRows(@Param("userId") int userId);

    @Insert("insert into discuss_post(user_id,title,content,type,status,create_time,comment_count,score) " +
            "values(#{userId},#{title},#{content},#{type},#{status},#{createTime},#{commentCount},#{score})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDiscussPost(DiscussPost discussPost);

    @Select("select * from discuss_post where id=#{id}")
    DiscussPost selectDiscussPostById(int id);

    @Update("update discuss_post set comment_count=#{commentCount} where id=#{id}")
    int updateCommentCount(int id, int commentCount);


    @Update("update discuss_post set type=#{type} where id=#{id}")
    int updateType(int id, int type);

    @Update("update discuss_post set status=#{status} where id=#{id}")
    int updateStatus(int id, int status);

}

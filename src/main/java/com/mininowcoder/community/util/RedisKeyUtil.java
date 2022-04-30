package com.mininowcoder.community.util;

/**
 * Created by FeiPan on 2022/4/24.
 */
public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

    private static final String PREFIX_FOLLOWEE = "followee"; // 以我为key，将实体作为关注列表存储
    private static final String PREFIX_FOLLOWER = "follower"; // 以实体为key，将我作为粉丝存储

    private static final String PREFIX_KAPTCHA = "kaptcha"; //验证码
    private static final String PREFIX_TICKET = "ticket"; //登录凭证

    private static final String PREFIX_USER = "user"; //缓存用户

    private static final String PREFIX_UV = "uv"; //unique visitor-独立访客，通过用户ip排重统计数据
    private static final String PREFIX_DAU = "dau"; // daily active user 日活跃用户，通过用户id排重统计

    // 实体赞的key like:entity:entityType:entityId 存放set(userId) 点赞的用户id
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 用户收到的赞 like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体 followee:userId:entityType -> zset(entityId, time)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝 follower:entityType:entityId -> zset(userId, time)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 登录验证码
    public static String getKaptchaKey(String owner) {
        // owner-用户临时的凭证
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 返回用户在redis中的key
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日UV
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }
}

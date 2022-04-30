package com.mininowcoder.community.service;

import com.mininowcoder.community.dao.UserMapper;
import com.mininowcoder.community.entity.LoginTicket;
import com.mininowcoder.community.entity.User;
import com.mininowcoder.community.util.CommunityConstant;
import com.mininowcoder.community.util.CommunityUtil;
import com.mininowcoder.community.util.MailClient;
import com.mininowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by FeiPan on 2022/4/20.
 */
@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    // @Autowired
    // private LoginTicketMapper loginTickerMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
        // return userMapper.selectById(id);
        User user = getCache(id);
        if(user==null){
            user = initCache(id);
        }
        return user;
    }

    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();
        // 空置处理
        if(user==null)
            throw new IllegalArgumentException("参数不能为空！");
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }

        // 验证账号是否存在
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }

        // 验证邮箱是否存在
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        // 注册用户, salt是为每一个用户生成的随机码，用于保证密码的安全性
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);// 0未激活
        user.setActivationCode(CommunityUtil.generateUUID());// 用于给用户发送的激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        // 数据库中user表中的id字段是自增的，插入数据的时候并没有设置
        // 需要设置将数据库中的属性注入到实体类user中，@Options(useGeneratedKeys = true, keyProperty = "id")

        //给用户发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/activation/userId/code 需要将数据库中的数据注入到实体类user中，否则user.getId=0
        String url = domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "牛客激活账号", content);

        return map;
    }

    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus()==1){
            return ACTIVATION_REPEAT;//重复激活
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatusById(userId, 1);

            clearCache(userId); // 修改user后，将缓存中的数据删除

            return ACTIVATION_SUCCESS;//激活成功
        }else{
            return ACTIVATION_FAILURE;//激活失败
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","该账号不存在！");
            return map;
        }
        if(user.getStatus()==0){
            map.put("usernameMsg","该账号尚未激活！");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确！");
            return map;
        }

        // 验证成功-生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*expiredSeconds));
        // loginTickerMapper.insertLoginTicker(loginTicket);

        // 重构将凭证存到redis中
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket); // redis会将loginTicket序列化成json字符串保存

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
        // loginTickerMapper.updateStatus(ticket, 1);

        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1); // 将status状态改成1，表示失效
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public LoginTicket findByTicket(String ticket) {
        //return loginTickerMapper.selectByTicket(ticket);

        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeaderUrl(int userId, String headerUrl){
        // return userMapper.updateHeaderUrlById(userId, headerUrl);
        int rows = userMapper.updateHeaderUrlById(userId, headerUrl);
        if(rows==1){
            clearCache(userId); // 更新成功后再清除缓存
        }
        return rows;
    }

    public int updatePassword(int userId, String password){
        // return userMapper.updatePasswordById(userId, password);
        int rows = userMapper.updatePasswordById(userId, password);
        if(rows==1){
            clearCache(userId); // 更新成功后再清除缓存
        }
        return rows;
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    /**
     * 1、优先从缓存中查用户
     * 2、取不到则从数据库中查，并初始化缓存
     * 3、用户数据变更时清楚缓存
     */
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);

        return user;
    }

    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

}

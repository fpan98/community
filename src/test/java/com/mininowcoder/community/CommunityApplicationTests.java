package com.mininowcoder.community;

import com.mininowcoder.community.dao.MessageMapper;
import com.mininowcoder.community.entity.Message;
import com.mininowcoder.community.util.CommunityUtil;
import com.mininowcoder.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class CommunityApplicationTests {


    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username", "张三");
        String content = templateEngine.process("/mail/demo", context);
        mailClient.sendMail("1192399340@qq.com", "Html", content);
    }

    @Test
    public void testSelectLetters(){
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : list) {
            System.out.println(message);
        }

        messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(count);
    }


    @Test
    public void valPassword(){
        String mysql = "7e7595dc51c0bb3f31fb25e7fed4489f";
        String password = CommunityUtil.md5("123"+"b71d9");
        System.out.println(mysql.equals(password));
    }


}

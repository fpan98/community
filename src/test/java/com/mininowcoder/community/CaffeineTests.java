package com.mininowcoder.community;

import com.mininowcoder.community.entity.DiscussPost;
import com.mininowcoder.community.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * Created by FeiPan on 2022/4/30.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {

    @Autowired
    private DiscussPostService postService;

    @Test
    public void initDataForTest(){
        for(int i=0;i<300000;i++){
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("互联网求职暖春计划");
            post.setContent("今年的就业形式，确实不容乐观，过了个年，仿佛跳水一般，整个讨论区哀鸿遍野！23届没有要了吗？22届要被优化了吗？");
            post.setCreateTime(new Date());
            post.setScore(Math.random()*2000);
            postService.addDiscussPost(post);
        }
    }

    @Test
    public void testCache(){
        System.out.println(postService.findDiscussPost(-1, 0, 10, 1));
        System.out.println(postService.findDiscussPost(-1, 0, 10, 1));
        System.out.println(postService.findDiscussPost(-1, 0, 10, 1));
        System.out.println(postService.findDiscussPost(-1, 0, 10, 0));
    }
}

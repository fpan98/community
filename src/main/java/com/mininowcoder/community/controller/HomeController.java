package com.mininowcoder.community.controller;

import com.mininowcoder.community.entity.DiscussPost;
import com.mininowcoder.community.service.LikeService;
import com.mininowcoder.community.util.CommunityConstant;
import com.mininowcoder.community.util.Page;
import com.mininowcoder.community.entity.User;
import com.mininowcoder.community.service.DiscussPostService;
import com.mininowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FeiPan on 2022/4/20.
 */
@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @GetMapping("/index")
    public String getIndexPage(Model model, Page page){
        // 方法调用前，SpringMVC会自动实例化Model和Page，并将Page注入到Model.
        // 因此，在thymeleaf中可以直接访问Page对象中的数据
        page.setRows(discussPostService.findDisscussPostRows(-1));
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.findDiscussPost(-1, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(list!=null){
            for(DiscussPost post: list){
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "index";
    }

    @GetMapping("/error")
    public String getErrorPage(){
        return "/error/500";
    }

    @GetMapping("/denied")
    public String getDeniedPage(){
        return "/error/404";
    }

}

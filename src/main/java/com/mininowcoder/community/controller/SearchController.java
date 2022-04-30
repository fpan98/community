package com.mininowcoder.community.controller;

import com.mininowcoder.community.entity.DiscussPost;
import com.mininowcoder.community.service.ElasticsearchService;
import com.mininowcoder.community.service.LikeService;
import com.mininowcoder.community.service.UserService;
import com.mininowcoder.community.util.CommunityConstant;
import com.mininowcoder.community.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FeiPan on 2022/4/28.
 */

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @GetMapping("/search")
    public String search(String keyword, Page page, Model model){
        // 搜索帖子
        Map<String, Object> res = elasticsearchService.searchDiscussPost(keyword, page.getOffset(), page.getLimit());
        List<DiscussPost> searchResult = null;
        int totalHits = 0;
        if(res!=null){
            searchResult = (List<DiscussPost>) res.get("discussPosts");
            totalHits = (int) res.get("totalHits");
        }

        // 包装数据给页面
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(searchResult!=null){
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // 分页信息
        page.setPath("/search?keyword="+keyword);
        page.setRows(totalHits);

        return "/site/search";
    }

}

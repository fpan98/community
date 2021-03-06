package com.mininowcoder.community.controller;
import com.mininowcoder.community.annotation.LoginRequired;
import com.mininowcoder.community.entity.DiscussPost;
import com.mininowcoder.community.entity.User;
import com.mininowcoder.community.service.DiscussPostService;
import com.mininowcoder.community.service.FollowService;
import com.mininowcoder.community.service.LikeService;
import com.mininowcoder.community.service.UserService;
import com.mininowcoder.community.util.CommunityConstant;
import com.mininowcoder.community.util.CommunityUtil;
import com.mininowcoder.community.util.HostHolder;
import com.mininowcoder.community.util.Page;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FeiPan on 2022/4/22.
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(Model model) {
        // ??????????????????
        String fileName = CommunityUtil.generateUUID();
        // ??????????????????
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));//???????????????{code:0}
        // ??????????????????
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);
        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName); // ??????????????????????????????

        return "/site/setting";
    }
    // ?????????????????????
    @PostMapping("/header/url")
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if(StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1, "????????????????????????");
        }
        String url = headerBucketUrl+"/"+fileName;
        userService.updateHeaderUrl(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }
    // ??????
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeaderUrl(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "??????????????????");
            return "/site/setting";
        }
        String filename = headerImage.getOriginalFilename();
        if(!StringUtils.isBlank(filename)){
            model.addAttribute("imageName", filename);
        }
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "???????????????????????????");
            return "/site/setting";
        }
        // ????????????????????????
        filename = CommunityUtil.generateUUID() + suffix;
        // ???????????????????????????
        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("?????????????????????" + e.getMessage());
            throw new RuntimeException("?????????????????????????????????????????????????????????", e);
        }
        // ???????????????????????????????????????web???????????????
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeaderUrl(user.getId(), headerUrl);

        return "redirect:/index";
    }

    // ??????
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // ????????????????????????
        fileName = uploadPath + "/" + fileName;
        // ????????????
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // ????????????
        response.setContentType("image/" + suffix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(fileName);
        ) {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error("?????????????????????" + e.getMessage());
        }
    }

    @LoginRequired
    @PostMapping("/changepwd")
    public String changePassword(String oldPassword, String newPassword, Model model,
                                 @CookieValue("ticket") String ticket){
        User user = hostHolder.getUser();
        oldPassword = CommunityUtil.md5(oldPassword+user.getSalt());
        // ???????????????
        if(!user.getPassword().equals(oldPassword)){
            model.addAttribute("passwordError", "??????????????????");
            return "/site/setting";
        }
        // ??????????????????
        newPassword = CommunityUtil.md5(newPassword+user.getSalt());
        // ????????????????????????
        userService.updatePassword(user.getId(), newPassword);
        // ??????????????????????????????????????????????????????????????????
        userService.logout(ticket);
        // ?????????????????????
        return "/site/login";
    }

    // ????????????
    @LoginRequired
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("?????????????????????");
        }
        // ??????
        model.addAttribute("user", user);
        // ????????????
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // ????????????(??????????????????????????????????????????entityType=user)
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // ????????????
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // ????????????????????????????????????
        boolean hasFollowed = false;
        if(hostHolder.getUser()!=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

    // ????????????
    @LoginRequired
    @GetMapping("/mypost/{userId}")
    public String getMyPost(Model model, Page page, @PathVariable("userId") int userId){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("?????????????????????");
        }
        page.setRows(discussPostService.findDisscussPostRows(userId));
        page.setPath("/user/mypost/"+userId);

        List<DiscussPost> list = discussPostService.findDiscussPost(userId, page.getOffset(), page.getLimit(), 0);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(list!=null){
            for(DiscussPost post: list){
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                map.put("user", user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("postCount", page.getRows());
        return  "/site/my-post";
    }

}

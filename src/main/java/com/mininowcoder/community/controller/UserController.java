package com.mininowcoder.community.controller;
import com.mininowcoder.community.annotation.LoginRequired;
import com.mininowcoder.community.entity.User;
import com.mininowcoder.community.service.FollowService;
import com.mininowcoder.community.service.LikeService;
import com.mininowcoder.community.service.UserService;
import com.mininowcoder.community.util.CommunityConstant;
import com.mininowcoder.community.util.CommunityUtil;
import com.mininowcoder.community.util.HostHolder;
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

/**
 * Created by FeiPan on 2022/4/22.
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeaderUrl(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "请选择图片！");
            return "/site/setting";
        }
        String filename = headerImage.getOriginalFilename();
        if(!StringUtils.isBlank(filename)){
            model.addAttribute("imageName", filename);
        }
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确！");
            return "/site/setting";
        }
        // 生成随机的文件名
        filename = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传用户头像文件失败，服务器发生异常！", e);
        }
        // 更新当前用户的头像的路径（web访问路径）
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeaderUrl(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放的路径
        fileName = uploadPath + "/" + fileName;
        // 图片后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
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
            logger.error("读取头像失败：" + e.getMessage());
        }
    }

    @LoginRequired
    @PostMapping("/changepwd")
    public String changePassword(String oldPassword, String newPassword, Model model,
                                 @CookieValue("ticket") String ticket){
        User user = hostHolder.getUser();
        oldPassword = CommunityUtil.md5(oldPassword+user.getSalt());
        // 验证原密码
        if(!user.getPassword().equals(oldPassword)){
            model.addAttribute("passwordError", "原密码错误！");
            return "/site/setting";
        }
        // 对新密码加密
        newPassword = CommunityUtil.md5(newPassword+user.getSalt());
        // 数据库中更新密码
        userService.updatePassword(user.getId(), newPassword);
        // 同时设置用户当前的登录态失效，让用户重新登录
        userService.logout(ticket);
        // 跳转至登录页面
        return "/site/login";
    }

    // 个人主页
    @LoginRequired
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在！");
        }
        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // 关注数量(这里查的是关注的用户的数量，entityType=user)
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 当前用户是否关注了该实体
        boolean hasFollowed = false;
        if(hostHolder.getUser()!=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

}
package com.mininowcoder.community.controller.interceptor;

import com.mininowcoder.community.service.DataService;
import com.mininowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by FeiPan on 2022/4/29.
 */
@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 统计uv
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

        // 统计DAU
        if(hostHolder.getUser()!=null){
            dataService.recordDAU(hostHolder.getUser().getId());
        }
        return true;
    }
}

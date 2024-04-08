package com.forum.community.config.interceptor;

import com.forum.community.entity.User;
import com.forum.community.service.DataService;
import com.forum.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class DataInterceptor implements HandlerInterceptor {
    @Autowired
    private DataService dataService;
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //UV
        String ip=request.getRemoteHost();
        dataService.recordUV(ip);
        //DAU
        User user = hostHolder.getUser();
        if (user!=null){
            dataService.recordDAU(user.getId());
        }

        return true;
    }
}

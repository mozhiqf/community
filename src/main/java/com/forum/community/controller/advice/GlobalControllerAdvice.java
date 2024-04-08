package com.forum.community.controller.advice;

import com.forum.community.entity.User;
import com.forum.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private HostHolder hostHolder;

    //将当前登录的用户加入到Model里面
    @ModelAttribute("loginUser")
    public User addLoginUserToModel() {
        return hostHolder.getUser(); // 返回当前登录的用户
    }
}

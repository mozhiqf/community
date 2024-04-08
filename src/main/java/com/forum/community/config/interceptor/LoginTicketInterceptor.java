package com.forum.community.config.interceptor;

import com.forum.community.entity.LoginTicket;
import com.forum.community.entity.User;
import com.forum.community.service.UserService;
import com.forum.community.util.CookieUtil;
import com.forum.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    private static Logger logger = LoggerFactory.getLogger(LoginTicketInterceptor.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");

        logger.debug("ticket = " + ticket);

        if (ticket != null) {
            // 查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 检查凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                // 根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户
                hostHolder.setUser(user);
                // 构建用户认证的结果,并存入SecurityContext,以便于Security进行授权.
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

                logger.debug("SecurityContextHolder.getContext = " + SecurityContextHolder.getContext());
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
        SecurityContextHolder.clearContext();
        logger.debug("SecurityContextHolder.getContext = " + SecurityContextHolder.getContext());
    }
}


//@Component
//public class LoginTicketInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private HostHolder hostHolder;
//
//    /**
//     * Controller执行前
//     */
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        // 从cookie中获取凭证
//        String ticket = CookieUtil.getValue(request, "ticket");
//
//        if (ticket != null) {
//            // 查询凭证
//            LoginTicket loginTicket = userService.findLoginTicket(ticket);
//            // 检查凭证是否有效
//            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
//                // 根据凭证查询用户
//                User user = userService.findUserById(loginTicket.getUserId());
//                // 在本次请求中持有用户
//                hostHolder.setUser(user);
//                //构建用户认证的结果，并存入SecurityContext,以便于Security进行授权
//                //方便后续获取权限
//                Authentication authentication = new UsernamePasswordAuthenticationToken(
//                        user, user.getPassword(), userService.getAuthorities(user.getId()));
//                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
//            }
//        }
//        return true;
//    }
//
//    /**
//     * Controller执行后
//     * 模板引擎执行前
//     */
//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        User user = hostHolder.getUser();
//        if (user != null && modelAndView != null) {
//            modelAndView.addObject("loginUser", user);
//        }
//    }
//
//    /**
//     * 模板引擎执行后
//     */
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        hostHolder.clear();
//        SecurityContextHolder.clearContext();
//    }
//}
package com.forum.community.config.filter;

import com.forum.community.entity.LoginTicket;
import com.forum.community.entity.User;
import com.forum.community.service.UserService;
import com.forum.community.util.CookieUtil;
import com.forum.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Component
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {

            String ticket = CookieUtil.getValue(request, "ticket");

            if (ticket != null) {
                LoginTicket loginTicket = userService.findLoginTicket(ticket);
                if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                    User user = userService.findUserById(loginTicket.getUserId());
                    hostHolder.setUser(user);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            user, null, userService.getAuthorities(user.getId()));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            // 请求完成后的清理工作
            hostHolder.clear();
            SecurityContextHolder.clearContext();
        }

    }
}

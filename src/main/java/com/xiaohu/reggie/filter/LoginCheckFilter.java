package com.xiaohu.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.xiaohu.reggie.common.BaseContext;
import com.xiaohu.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse respone = (HttpServletResponse) servletResponse;

        //1 获取本次请求的uri
        String requestURI = request.getRequestURI();
        //log.info("拦截请求:{}", requestURI);
        //定义不需要处理请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };
        //2判断本次请求是否需要处理
        boolean check = check(urls, requestURI);
        //3 不需要处理 则直接放行
        if (check) {
            //log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, respone);
            return;
        }
        // 4-1 判断登录状态，如果已登录 则直接放行
        if (request.getSession().getAttribute("employee") != null) {
            // log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("employee"));

            Long employeeId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(employeeId);
            filterChain.doFilter(request, respone);
            return;
        }

        // 4-2 移动端h5 判断登录状态，如果已登录 则直接放行
        if (request.getSession().getAttribute("user") != null) {
            // log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("employee"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request, respone);
            return;
        }
        // log.info("用户未登录");
        // 5 如果未登录 则返回未登录结果 通过输出流方式向 客户端页面响应数据
        respone.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }


    /**
     * 路径匹配，检查本次请求是否需要放行
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {

        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}

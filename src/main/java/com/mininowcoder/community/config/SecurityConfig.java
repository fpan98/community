package com.mininowcoder.community.config;

/**
 * Created by FeiPan on 2022/4/29.
 */
@Deprecated
public class SecurityConfig{

}
//@Configuration
//public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {
//
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers("/resources/**");//静态资源不拦截
//    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        // 授权
//        http.authorizeRequests()
//                .antMatchers(
//                        "/user/setting",
//                        "/user/upload",
//                        "/user/changepwd",
//                        "/discuss/add",
//                        "/comment/add/**",
//                        "/litter/**",
//                        "/notice/**",
//                        "/like",
//                        "/follow",
//                        "/unfollow"
//                )
//                .hasAnyAuthority( // 拥有以下权限之一才可以访问上述请求
//                        AUTHORITY_USER,
//                        AUTHORITY_ADMIN,
//                        AUTHORITY_MODERATOR
//                )
//                .antMatchers(
//                        "/discuss/top",
//                        "/discuss/wonderful"
//                ).hasAnyAuthority(
//                        AUTHORITY_MODERATOR
//                ).antMatchers(
//                        "/discuss/delete"
//                ).hasAnyAuthority(
//                        AUTHORITY_ADMIN
//                )
//                .anyRequest().permitAll()
//                .and().csrf().disable();
//
//        // 没有登录 and 权限不足时的处理
//        http.exceptionHandling()
//                .authenticationEntryPoint(new AuthenticationEntryPoint() { // 没有登录
//                    @Override
//                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
//                        // 同步请求，返回登录页面。异步请求，返回json字符串
//                        String xRequestedWith = request.getHeader("x-requested-with");
//                        if ("XMLHttpRequest".equals(xRequestedWith)) { // 异步
//                            response.setContentType("application/plain;charset=utf-8");
//                            PrintWriter writer = response.getWriter();
//                            writer.write(CommunityUtil.getJSONString(403, "你还没有登录哦!"));
//                        } else { // 同步
//                            response.sendRedirect(request.getContextPath() + "/login");
//                        }
//                    }
//                })
//                .accessDeniedHandler(new AccessDeniedHandler() { // 权限不足
//                    @Override
//                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
//                        String xRequestedWith = request.getHeader("x-requested-with");
//                        if ("XMLHttpRequest".equals(xRequestedWith)) { // 异步
//                            response.setContentType("application/plain;charset=utf-8");
//                            PrintWriter writer = response.getWriter();
//                            writer.write(CommunityUtil.getJSONString(403, "你没有访问此功能的权限！"));
//                        } else { // 同步
//                            response.sendRedirect(request.getContextPath() + "/denied");
//                        }
//                    }
//                });
//        // 为了执行自己的logout
//        // Security底层默认会拦截/logout请求,进行退出处理.
//        // 覆盖它默认的逻辑,才能执行我们自己的退出代码.
//        // 此处为一个欺骗，程序中没有"/securitylogout"，拦截到这个路径不会处理
//        http.logout().logoutUrl("/securitylogout");
//    }
//}

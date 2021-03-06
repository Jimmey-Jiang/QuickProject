package com.project.QuickProject.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.QuickProject.filter.CustomTokenFilter;
import com.project.QuickProject.log.SysLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * spring security??????
 *
 * @author Jimmey-Jiang
 */
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CustomTokenFilter customTokenFilter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf()// ??????????????????JWT????????????????????????csrf
                .disable()
                .sessionManagement()// ??????token??????????????????session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, // ????????????????????????????????????????????????
                        "/",
                        "/*.html",
                        "/favicon.ico",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/**/*.png",
                        "/auth/login/**",
                        "/swagger-resources/**",
                        "/v2/api-docs/**"
                )
                .permitAll()
                .antMatchers("/sys/sysUser/login","/sys/sysUser/loginValidateCode","/sys/sysUser/register","/sys/sysUser/login1")// ????????????????????????????????????
                .permitAll()
                /* .and()
                 .logout()
                 .invalidateHttpSession(true)
                 .permitAll()*/
                .antMatchers(HttpMethod.OPTIONS)//??????????????????????????????options??????
                .permitAll()
                .antMatchers("/**")//???????????????????????????
                .permitAll()
                .anyRequest()// ???????????????????????????????????????????????????
                .authenticated();
        //?????? /logout ??????????????????????????????session
        httpSecurity.logout().logoutSuccessUrl("/logoutSuccess");
        // ????????????
        httpSecurity.headers().cacheControl();
//        // ??????JWT filter
//        httpSecurity.addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
//        //????????????????????????????????????????????????
//        httpSecurity.exceptionHandling()
//                .accessDeniedHandler(restfulAccessDeniedHandler)
//                .authenticationEntryPoint(restAuthenticationEntryPoint);

    }


//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//
//        http.authorizeRequests()
//                .anyRequest().permitAll().and().logout().permitAll();//???????????????????????????
//    }

    /*
    UsernamePasswordAuthenticationFilter???AbstractAuthenticationProcessingFilter?????????????????????????????????????????????????????????????????????????????????
    ?????????????????????http.formLogin()?????????????????????????????????pattern???"/login"????????????POST?????????
    ?????????????????????????????????????????????loginProcessingUrl?????????????????????????????????????????????authenticationManager??????????????????
     */
    @Bean
    public UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter() throws Exception {
        UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter() {
            @Override
            public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
                if (!"POST".equals(request.getMethod())) {
                    throw new AuthenticationServiceException(
                            "Authentication method not supported: " + request.getMethod());
                }
                if (request.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)) {
                    UsernamePasswordAuthenticationToken authRequest;
                    try (InputStream is = request.getInputStream()) {
                        Map authenticationBean = objectMapper.readValue(is, Map.class);
                        String username = MapUtils.getString(authenticationBean, "username", "");
                        String password = MapUtils.getString(authenticationBean, "password", "");
                        authRequest = new UsernamePasswordAuthenticationToken(username, password);
                    } catch (IOException e) {
                        authRequest = new UsernamePasswordAuthenticationToken("", "");
                    }
                    setDetails(request, authRequest);
                    Authentication authenticate = getAuthenticationManager().authenticate(authRequest);
                    return authenticate;
                } else {
                    return super.attemptAuthentication(request, response);
                }
            }
        };
        filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/auth/login", "POST"));
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        filter.setAuthenticationFailureHandler(authenticationFailureHandler);
        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }

    //????????????????????????userDetailsService,??????????????????
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

}

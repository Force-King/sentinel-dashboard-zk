/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.controller;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.auth.SimpleWebAuthServiceImpl;
import com.alibaba.csp.sentinel.dashboard.config.DashboardConfig;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author cdfive
 * @since 1.6.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Value("${auth.username:sentinel}")
    private String authUsername;

    @Value("${auth.password:sentinel}")
    private String authPassword;

    @Value("${auth.admin.username}")
    private String adminUsername;

    @Value("${auth.admin.password}")
    private String adminPassword;

    @PostMapping("/login")
    public Result<AuthService.AuthUser> login(HttpServletRequest request, String username, String password) {
        if (StringUtils.isNotBlank(DashboardConfig.getAuthUsername())) {
            authUsername = DashboardConfig.getAuthUsername();
        }

        if (StringUtils.isNotBlank(DashboardConfig.getAuthPassword())) {
            authPassword = DashboardConfig.getAuthPassword();
        }

        /*
         * If auth.username or auth.password is blank(set in application.properties or VM arguments),
         * auth will pass, as the front side validate the input which can't be blank,
         * so user can input any username or password(both are not blank) to login in that case.
         */
        if(StringUtils.isBlank(username)) {
            LOGGER.error("Login failed: Invalid username is null");
            return Result.ofFail(-1, "用户名不能为空！");
        }
        if(!authUsername.equals(username) && !adminUsername.equals(username)) {
            LOGGER.error("Login failed: 用户名不正确");
            return Result.ofFail(-1, "用户名不正确！");
        }
        if(adminUsername.equals(username)) {
            if (StringUtils.isNotBlank(adminPassword) && !adminPassword.equals(password)) {
                LOGGER.error("Login failed: 密码不正确, username=" + username);
                return Result.ofFail(-1, "密码不正确");
            }
            AuthService.AuthUser authUser = new SimpleWebAuthServiceImpl.SimpleWebAuthUserImpl(username);
            request.getSession().setAttribute(SimpleWebAuthServiceImpl.WEB_SESSION_KEY_ADMIN, authUser);
            return Result.ofSuccess(authUser);
        } else {
            if (StringUtils.isNotBlank(authPassword) && !authPassword.equals(password)) {
                LOGGER.error("Login failed: Invalid username or password, username=" + username);
                return Result.ofFail(-1, "密码不正确");
            }
            AuthService.AuthUser authUser = new SimpleWebAuthServiceImpl.SimpleWebAuthUserImpl(username);
            request.getSession().setAttribute(SimpleWebAuthServiceImpl.WEB_SESSION_KEY, authUser);
            return Result.ofSuccess(authUser);
        }

    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public Result<?> logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return Result.ofSuccess(null);
    }
}

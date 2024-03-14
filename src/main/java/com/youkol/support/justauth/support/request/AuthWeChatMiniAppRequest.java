/*
 * Copyright (C) 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.youkol.support.justauth.support.request;

import com.alibaba.fastjson.JSONObject;
import com.youkol.support.justauth.support.config.AuthExtendSource;

import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.utils.UrlBuilder;

/**
 * <p>
 * 微信小程序登录，官方参考地址：<a href=
 * "https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html">
 * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html</a>
 * </p>
 *
 * @author jackiea
 * @since 1.0.0
 * @see AuthDefaultRequest
 */
public class AuthWeChatMiniAppRequest extends AuthDefaultRequest {

    public AuthWeChatMiniAppRequest(AuthConfig config) {
        super(config, AuthExtendSource.WECHAT_MINI_APP);
    }

    public AuthWeChatMiniAppRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthExtendSource.WECHAT_MINI_APP, authStateCache);
    }

    @Override
    protected AuthToken getAccessToken(AuthCallback authCallback) {
        String response = this.doGetAuthorizationCode(authCallback.getCode());
        return this.getAuthToken(response);
    }

    /**
     * 由于微信规则调整，获取用户信息，需要在小程序端调用处理后传递到后端。
     * 参考地址：https://developers.weixin.qq.com/miniprogram/dev/api/open-api/user-info/wx.getUserProfile.html
     */
    @Override
    protected AuthUser getUserInfo(AuthToken authToken) {
        return AuthUser.builder()
                .username(null)
                .nickname(null)
                .avatar(null)
                .uuid(authToken.getOpenId())
                .token(authToken)
                .source(this.source.toString())
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object == null) {
            throw new AuthException("Response data is null.");
        }

        int errCode = object.getIntValue("errcode");
        String errMsg = object.getString("errmsg");

        if (errCode != 0) {
            throw new AuthException(errCode, errMsg);
        }
    }

    private AuthToken getAuthToken(String response) {
        JSONObject accessTokenObject = JSONObject.parseObject(response);

        this.checkResponse(accessTokenObject);

        return AuthToken.builder()
                // 会话密钥session_key暂时放在uid中
                .uid(accessTokenObject.getString("session_key"))
                .openId(accessTokenObject.getString("openid"))
                .unionId(accessTokenObject.getString("unionid"))
                .build();
    }

    @Override
    protected String accessTokenUrl(String code) {
        return UrlBuilder.fromBaseUrl(this.source.accessToken())
                .queryParam("appid", this.config.getClientId())
                .queryParam("secret", this.config.getClientSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build();
    }

}

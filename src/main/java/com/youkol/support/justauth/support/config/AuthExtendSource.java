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
package com.youkol.support.justauth.support.config;

import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.request.AuthDefaultRequest;
import com.youkol.support.justauth.support.request.AuthWeChatMiniAppRequest;

/**
 * {@link AuthSource} 的扩展实现，{@link AuthDefaultSource}为JustAuth提供的实现
 *
 * @author jackiea
 * @since 1.0.0
 * @see AuthDefaultSource
 */
public enum AuthExtendSource implements AuthSource {

    WECHAT_MINI_APP {

        @Override
        public String authorize() {
            throw new AuthException(AuthResponseStatus.UNSUPPORTED);
        }

        /**
         * <p>
         * auth.code2Session 接口，换取 用户唯一标识 OpenID、
         * 用户在微信开放平台账号下的唯一标识UnionID（若当前小程序已绑定到微信开放平台账号
         * 和会话密钥 session_key
         * </p>
         * <p>
         * auth.code2Session 接口参考地址：
         * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
         * </p>
         */
        @Override
        public String accessToken() {
            return "https://api.weixin.qq.com/sns/jscode2session";
        }

        /**
         *
         * <ul>
         * <li>获取头像昵称参考地址：
         * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/userProfile.html：
         * <li>wx.getUserProfile 接口参考地址：
         * https://developers.weixin.qq.com/miniprogram/dev/api/open-api/user-info/wx.getUserProfile.html
         * <li>小程序用户头像昵称获取规则调整公告：
         * https://developers.weixin.qq.com/community/develop/doc/00022c683e8a80b29bed2142b56c01
         * </ul>
         *
         */
        @Override
        public String userInfo() {
            throw new AuthException(AuthResponseStatus.UNSUPPORTED);
        }

        @Override
        public Class<? extends AuthDefaultRequest> getTargetClass() {
            return AuthWeChatMiniAppRequest.class;
        }

    }

}

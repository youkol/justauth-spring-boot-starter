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

import java.util.Map;

import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthDefaultSource;

/**
 *
 * @author jackiea
 * @since 1.0.0
 */
public interface AuthConfigRepository {

    /**
     * 返回所有可用的{@link AuthConfig}, 否则返回{@code null}
     *
     * @return
     */
    Map<String, AuthConfig> listAuthConfig();

    /**
     * 返回找到的{@link AuthConfig}, 否则返回{@code null}
     *
     * <p>
     * <b>NOTE:</b> 配置标识不区分大小写
     *
     * @param authConfigId 配置标识，如{@link AuthDefaultSource}中的枚举名称
     * @return 返回找到的{@link AuthConfig}, 否则返回{@code null}
     */
    AuthConfig getAuthConfigById(String authConfigId);
}

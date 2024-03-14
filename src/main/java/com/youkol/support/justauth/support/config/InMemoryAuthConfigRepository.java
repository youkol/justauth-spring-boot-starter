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

import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

import me.zhyd.oauth.config.AuthConfig;

/**
 *
 * @author jackiea
 * @since 1.0.0
 */
public class InMemoryAuthConfigRepository implements AuthConfigRepository {

    private Map<String, AuthConfig> authConfigs = new LinkedCaseInsensitiveMap<>();

    public InMemoryAuthConfigRepository(Map<String, AuthConfig> authConfigs) {
        if (!CollectionUtils.isEmpty(authConfigs)) {
            this.authConfigs.putAll(authConfigs);
        }
    }

    @Override
    public Map<String, AuthConfig> listAuthConfig() {
        return this.authConfigs;
    }

    @Override
    public AuthConfig getAuthConfigById(String authConfigId) {
        return this.authConfigs.get(authConfigId);
    }

}

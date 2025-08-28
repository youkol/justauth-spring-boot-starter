/*
 * Copyright (C) 2024-present the original author or authors.
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
package com.youkol.support.justauth.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import me.zhyd.oauth.cache.AuthDefaultStateCache;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.exception.AuthException;

/**
 * Auto configuration for {@link AuthStateCache}
 *
 * @author jackiea
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(AuthStateCache.class)
@Import(JustAuthRedisStateCacheConfiguration.class)
public class JustAuthStateCacheConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = JustAuthProperties.JUSTAUTH_PREFIX, value = "cache.type", havingValue = "default", matchIfMissing = true)
    static class AuthDefaultStateCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public AuthStateCache authStateCache() {
            return AuthDefaultStateCache.INSTANCE;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = JustAuthProperties.JUSTAUTH_PREFIX, value = "cache.type", havingValue = "custom")
    static class AuthCustomStateCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public AuthStateCache authStateCache() {
            throw new AuthException(
                    "youkol.justauth.cache.type=custom, but not found any custom authStateCache bean.");
        }
    }
}

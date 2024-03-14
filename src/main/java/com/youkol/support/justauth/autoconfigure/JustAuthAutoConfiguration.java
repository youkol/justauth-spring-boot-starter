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
package com.youkol.support.justauth.autoconfigure;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.request.AuthRequest;

import com.youkol.support.justauth.support.config.AuthConfigRepository;
import com.youkol.support.justauth.support.config.AuthExtendSource;
import com.youkol.support.justauth.support.config.InMemoryAuthConfigRepository;
import com.youkol.support.justauth.support.request.AuthRequestFactory;

/**
 * Auto configuration for JustAuth.
 *
 * @author jackiea
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ AuthRequest.class, AuthSource.class })
@EnableConfigurationProperties(JustAuthProperties.class)
@ConditionalOnProperty(prefix = JustAuthProperties.JUSTAUTH_PREFIX, value = "enabled", matchIfMissing = true)
@Import({ JustAuthStateCacheConfiguration.class })
public class JustAuthAutoConfiguration {

    public static final String AUTH_SOURCE_WECHAT_MINI_APP = "wechatMiniAppAuthSource";

    @Bean
    @ConditionalOnMissingBean
    public AuthRequestFactory authRequestFactory(JustAuthProperties properties,
            AuthStateCache authStateCache, AuthConfigRepository authConfigRepository,
            ObjectProvider<AuthSource> authSource, ObjectProvider<List<AuthSource>> authSourceList) {
        Stream<AuthSource> authSourceFromList = authSourceList.orderedStream().flatMap(List::stream);
        Stream<AuthSource> authSourceFromSingle = authSource.orderedStream();
        List<AuthSource> authSources = Stream.concat(authSourceFromSingle, authSourceFromList)
                .distinct()
                .collect(Collectors.toList());

        return new AuthRequestFactory(authConfigRepository, authStateCache, authSources, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthConfigRepository authConfigRepository(JustAuthProperties properties) {
        return new InMemoryAuthConfigRepository(properties.getAuthConfigs());
    }

    @Bean(name = AUTH_SOURCE_WECHAT_MINI_APP)
    @ConditionalOnMissingBean(name = AUTH_SOURCE_WECHAT_MINI_APP)
    public AuthSource wechatMiniAppAuthSource() {
        return AuthExtendSource.WECHAT_MINI_APP;
    }
}

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

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import me.zhyd.oauth.cache.AuthStateCache;
import com.youkol.support.justauth.support.cache.AuthRedisStateCache;

/**
 * Auto configuration for {@link AuthRedisStateCache}
 *
 * @author jackiea
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@ConditionalOnClass({ AuthStateCache.class, RedisTemplate.class })
@ConditionalOnMissingBean(AuthStateCache.class)
@ConditionalOnProperty(prefix = JustAuthProperties.JUSTAUTH_PREFIX, value = "cache.type", havingValue = "redis")
public class JustAuthRedisStateCacheConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnSingleCandidate(RedisConnectionFactory.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthStateCache authStateCache(StringRedisTemplate redisTemplate, JustAuthProperties properties) {
        return new AuthRedisStateCache(redisTemplate, properties.getCache());
    }

}

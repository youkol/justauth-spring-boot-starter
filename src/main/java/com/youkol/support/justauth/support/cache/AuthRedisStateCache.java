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
package com.youkol.support.justauth.support.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import me.zhyd.oauth.cache.AuthDefaultStateCache;
import me.zhyd.oauth.cache.AuthStateCache;
import com.youkol.support.justauth.autoconfigure.JustAuthCacheProperties;

/**
 * A redis implementation of {@link AuthStateCache}
 *
 * @author jackiea
 * @since 1.0.0
 * @see AuthDefaultStateCache
 */
public class AuthRedisStateCache implements AuthStateCache {

    private StringRedisTemplate redisTemplate;

    private JustAuthCacheProperties cacheProperties;

    public AuthRedisStateCache(StringRedisTemplate redisTemplate, JustAuthCacheProperties cacheProperties) {
        this.redisTemplate = redisTemplate;
        this.cacheProperties = cacheProperties;
    }

    @Override
    public void cache(String key, String value) {
        this.cache(key, value, this.cacheProperties.getTimeout().toMillis());
    }

    @Override
    public void cache(String key, String value, long timeout) {
        this.redisTemplate.opsForValue().set(this.getCacheKey(key), value, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public String get(String key) {
        return this.redisTemplate.opsForValue().get(this.getCacheKey(key));
    }

    @Override
    public boolean containsKey(String key) {
        Long expire = this.redisTemplate.getExpire(this.getCacheKey(key), TimeUnit.MILLISECONDS);
        if (expire == null) {
            return false;
        }

        return expire > 0L;
    }

    @NonNull
    private String getCacheKey(String key) {
        if (!StringUtils.hasText(this.cacheProperties.getKeyPrefix())) {
            return JustAuthCacheProperties.DEFAULT_KEY_PREFIX + key;
        }

        if (this.cacheProperties.getKeyPrefix().endsWith(":")) {
            return this.cacheProperties.getKeyPrefix() + key;
        }

        return this.cacheProperties.getKeyPrefix() + ":" + key;
    }
}

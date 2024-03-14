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

import java.time.Duration;

import me.zhyd.oauth.cache.AuthCacheConfig;

/**
 * Configuration properties for the JustAuth cache.
 *
 * @author jackiea
 * @since 1.0.0
 */
public class JustAuthCacheProperties {

    public static final String DEFAULT_KEY_PREFIX = "YOUKOL:JUSTAUTH:STATE:";

    private CacheType type = CacheType.DEFAULT;

    /**
     * Key prefix.
     */
    private String keyPrefix = DEFAULT_KEY_PREFIX;

    /**
     * Entry expiration.
     * Default: 3 minutes.
     */
    private Duration timeout = Duration.ofMillis(AuthCacheConfig.timeout);

    public CacheType getType() {
        return this.type;
    }

    public void setType(CacheType type) {
        this.type = type;
    }

    public String getKeyPrefix() {
        return this.keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Duration getTimeout() {
        return this.timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

}

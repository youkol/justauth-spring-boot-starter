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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.StringUtils;

import com.xkcoding.http.config.HttpConfig;
import com.xkcoding.http.constants.Constants;

import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthSource;

/**
 * Configuration properties for JustAuth.
 *
 * @author jackiea
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = JustAuthProperties.JUSTAUTH_PREFIX)
public class JustAuthProperties {

    public static final String JUSTAUTH_PREFIX = "youkol.justauth";

    /**
     * Whether to enable JustAuth.
     */
    private boolean enabled = true;

    private Map<String, AuthConfig> type = new HashMap<>();

    /**
     * {@link AuthSource}的扩展实现类
     */
    private List<Class<? extends AuthSource>> extendAuthSourceClass = new ArrayList<>();

    @NestedConfigurationProperty
    private JustAuthCacheProperties cache = new JustAuthCacheProperties();

    @NestedConfigurationProperty
    private JustAuthHttpConfig httpConfig = new JustAuthHttpConfig();

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, AuthConfig> getType() {
        return this.type;
    }

    public void setType(Map<String, AuthConfig> type) {
        this.type = type;
    }

    public List<Class<? extends AuthSource>> getExtendAuthSourceClass() {
        return this.extendAuthSourceClass;
    }

    public void setExtendAuthSourceClass(List<Class<? extends AuthSource>> extendAuthSourceClass) {
        this.extendAuthSourceClass = extendAuthSourceClass;
    }

    public JustAuthCacheProperties getCache() {
        return this.cache;
    }

    public void setCache(JustAuthCacheProperties cache) {
        this.cache = cache;
    }

    public JustAuthHttpConfig getHttpConfig() {
        return this.httpConfig;
    }

    public void setHttpConfig(JustAuthHttpConfig httpConfig) {
        this.httpConfig = httpConfig;
    }

    /**
     * For {@link Proxy} configuration
     */
    public static class JustAuthHttpProxyConfig {

        private Type type = Type.HTTP;

        private String hostname;

        private int port;

        public Type getType() {
            return this.type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public String getHostname() {
            return this.hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public int getPort() {
            return this.port;
        }

        public void setPort(int port) {
            this.port = port;
        }

    }

    /**
     * For {@link AuthConfig#setHttpConfig(HttpConfig)} configuration
     */
    public static class JustAuthHttpConfig extends JustAuthHttpProxyConfig {

        /**
         * 超时时长，单位毫秒
         */
        private int timeout = Constants.DEFAULT_TIMEOUT;

        private Map<String, JustAuthHttpProxyConfig> proxy = new HashMap<>();

        public int getTimeout() {
            return this.timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public Map<String, JustAuthHttpProxyConfig> getProxy() {
            return this.proxy;
        }

        public void setProxy(Map<String, JustAuthHttpProxyConfig> proxy) {
            this.proxy = proxy;
        }

    }

    public Map<String, AuthConfig> getAuthConfigs() {
        return this.getType().entrySet()
                .stream()
                .map(this::configHttpConfig)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<String, AuthConfig> configHttpConfig(Map.Entry<String, AuthConfig> entry) {
        this.configHttpConfig(entry.getKey(), entry.getValue());
        return entry;
    }

    private HttpConfig createHttpConfig(int timeout, JustAuthHttpProxyConfig proxyConfig) {
        return HttpConfig.builder()
                .timeout(timeout)
                .proxy(new Proxy(proxyConfig.getType(),
                        new InetSocketAddress(proxyConfig.getHostname(), proxyConfig.getPort())))
                .build();
    }

    private AuthConfig configHttpConfig(String source, AuthConfig authConfig) {
        JustAuthHttpConfig authHttpConfig = this.getHttpConfig();
        if (authHttpConfig == null) {
            return authConfig;
        }

        JustAuthHttpProxyConfig authProxyConfig = authHttpConfig.getProxy()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(source))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(authHttpConfig);

        if (!StringUtils.hasText(authProxyConfig.getHostname())) {
            return authConfig;
        }

        authConfig.setHttpConfig(this.createHttpConfig(authHttpConfig.getTimeout(), authProxyConfig));

        return authConfig;
    }

}

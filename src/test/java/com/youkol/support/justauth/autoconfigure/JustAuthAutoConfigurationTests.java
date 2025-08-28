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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.youkol.support.justauth.support.cache.AuthRedisStateCache;
import com.youkol.support.justauth.support.config.AuthConfigRepository;
import com.youkol.support.justauth.support.request.AuthRequestFactory;

import me.zhyd.oauth.cache.AuthDefaultStateCache;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWechatMiniProgramRequest;

/**
 *
 * @author jackiea
 * @since 1.0.0
 */
class JustAuthAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JustAuthAutoConfiguration.class));

    @Test
    void justAuthEnabledMissing() {
        this.contextRunner.run(context -> {
            assertThat(context).getBeans(AuthRequestFactory.class).hasSize(1);
        });
    }

    @Test
    void justAuthEnabled() {
        this.contextRunner.withPropertyValues("youkol.justauth.enabled:true")
                .run(context -> {
                    assertThat(context).getBeans(AuthRequestFactory.class).hasSize(1);
                });
    }

    @Test
    void justAuthDisabled() {
        this.contextRunner.withPropertyValues("youkol.justauth.enabled:false")
                .run(context -> {
                    assertThat(context).getBeans(AuthRequestFactory.class).isEmpty();
                });
    }

    @Test
    void justAuthOnWithoutJustAuth() {
        this.contextRunner.withClassLoader(new FilteredClassLoader("me.zhyd.oauth"))
                .run(context -> {
                    assertThat(context).getBean(JustAuthAutoConfiguration.class).isNull();
                    assertThat(context).getBeans(AuthRequestFactory.class).isEmpty();
                    assertThat(context).getBean(AuthConfigRepository.class).isNull();
                });
    }

    @Test
    void justAuthConfiguredAuthSource() {
        this.contextRunner
                .withUserConfiguration(SimpleClassAuthSourceConfiguration.class)
                .withPropertyValues(
                        "youkol.justauth.extend-auth-source-class[0]:com.youkol.support.justauth.autoconfigure.JustAuthAutoConfigurationTests.CustomAuthSource",
                        "youkol.justauth.type.CUSTOM1.client-id:custom1-client-id",
                        "youkol.justauth.type.CUSTOM1.client-secret:custom1-client-secret",
                        "youkol.justauth.type.CUSTOM1.redirect-uri:http://test.justauth/test/oauth/custom1/callback",
                        "youkol.justauth.type.SIMPLE_CLASS.client-id:simple-class-client-id",
                        "youkol.justauth.type.SIMPLE_CLASS.client-secret:simple-class-client-secret",
                        "youkol.justauth.type.SIMPLE_CLASS.redirect-uri:http://test.justauth/test/oauth/simple_class/callback")
                .run(context -> {
                    AuthRequestFactory authRequestFactory = context.getBean(AuthRequestFactory.class);
                    AuthRequest authRequest = authRequestFactory.getAuthRequest("custom1");
                    assertThat(authRequest).isNotNull();
                    assertThat(authRequest).isInstanceOf(Custom1AuthRequest.class);

                    authRequest = authRequestFactory.getAuthRequest("simple_class");
                    assertThat(authRequest).isNotNull();
                    assertThat(authRequest).isInstanceOf(SimpleClassAuthRequest.class);

                    assertThat(authRequestFactory.getConfiguredOAuthNames()).size().isEqualTo(2);
                    assertThat(authRequestFactory.getConfiguredOAuthNames()).contains("CUSTOM1", "SIMPLE_CLASS");
                });
    }

    @Test
    void justAuthWithAuthSourceConfiguration() {
        this.contextRunner
                .withUserConfiguration(SimpleClassAuthSourceConfiguration.class, EnumClassAuthSourceConfiguration.class)
                .withPropertyValues(
                        "youkol.justauth.type.CUSTOM2.client-id:custom2-client-id",
                        "youkol.justauth.type.CUSTOM2.client-secret:custom2-client-secret",
                        "youkol.justauth.type.CUSTOM2.redirect-uri:http://test.justauth/test/oauth/custom2/callback",
                        "youkol.justauth.type.SIMPLE_CLASS.client-id:simple-class-client-id",
                        "youkol.justauth.type.SIMPLE_CLASS.client-secret:simple-class-client-secret",
                        "youkol.justauth.type.SIMPLE_CLASS.redirect-uri:http://test.justauth/test/oauth/simple_class/callback")
                .run(context -> {
                    AuthRequest authRequest = context.getBean(AuthRequestFactory.class).getAuthRequest("custom2");
                    assertThat(authRequest).isNotNull();
                    assertThat(authRequest).isInstanceOf(Custom2AuthRequest.class);

                    authRequest = context.getBean(AuthRequestFactory.class).getAuthRequest("simple_class");
                    assertThat(authRequest).isNotNull();
                    assertThat(authRequest).isInstanceOf(SimpleClassAuthRequest.class);
                });
    }

    @Test
    void justAuthWithCustomNameAuthSource() {
        this.contextRunner
                .withPropertyValues(
                        "youkol.justauth.extend-auth-source-class[0]:com.youkol.support.justauth.autoconfigure.JustAuthAutoConfigurationTests.CustomNameAuthSource",
                        "youkol.justauth.type.CUSTOM_NAME.client-id:custom_name-client-id",
                        "youkol.justauth.type.CUSTOM_NAME.client-secret:custom_name-client-secret",
                        "youkol.justauth.type.CUSTOM_NAME.redirect-uri:http://test.justauth/test/oauth/custom_name/callback")
                .run(context -> assertThatThrownBy(() -> context.getBean(AuthRequestFactory.class))
                        .hasCauseInstanceOf(BeanCreationException.class)
                        .hasRootCauseInstanceOf(NoSuchMethodException.class)
                        .hasStackTraceContaining(CustomNameAuthSource.class.getCanonicalName())
                        .hasStackTraceContaining("must have default no-argument constructor"));
    }

    @Test
    void justAuthUseDefaultStateCache() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).getBean(AuthStateCache.class)
                            .isInstanceOf(AuthDefaultStateCache.class);
                });
    }

    @Test
    void justAuthUseRedisStateCache() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JustAuthAutoConfiguration.class, RedisAutoConfiguration.class))
                .withPropertyValues("youkol.justauth.cache.type:REDIS")
                .run(context -> {
                    assertThat(context).getBean(AuthStateCache.class)
                            .isInstanceOf(AuthRedisStateCache.class);
                });
    }

    @Test
    void justAuthUseCustomStateCacheWithoutBeanDefinition() {
        this.contextRunner
                .withPropertyValues("youkol.justauth.cache.type:custom")
                .run(context -> {
                    assertThatThrownBy(() -> context.getBean(AuthStateCache.class))
                            .hasCauseInstanceOf(BeanCreationException.class)
                            .hasRootCauseInstanceOf(AuthException.class)
                            .getRootCause()
                            .hasMessageContaining("not found any custom authStateCache");
                });
    }

    @Test
    void justAuthUseCustomStateCacheWithBeanDefinition() {
        this.contextRunner
                .withUserConfiguration(CustomAuthStateCacheConfiguration.class)
                .withPropertyValues("youkol.justauth.cache.type:custom")
                .run(context -> {
                    assertThat(context.getBean(AuthStateCache.class))
                            .isNotNull()
                            .isInstanceOf(MyCustomAuthStateCache.class);

                    AuthStateCache authStateCache = context.getBean(AuthStateCache.class);
                    assertThatThrownBy(() -> authStateCache.containsKey("key"))
                            .isInstanceOf(UnsupportedOperationException.class);
                });
    }

    @Test
    void justAuthUseHttpProxy() {
        this.contextRunner
                .withPropertyValues("youkol.justauth.http-config.hostname:192.168.108.1",
                        "youkol.justauth.http-config.port:10080",
                        "youkol.justauth.type.WECHAT_MINI_PROGRAM.client-id:WECHAT_MINI_PROGRAM",
                        "youkol.justauth.type.WECHAT_MINI_PROGRAM.client-secret:WECHAT_MINI_PROGRAM",
                        "youkol.justauth.type.WECHAT_MINI_PROGRAM.redirect-uri:http://test.justauth/test/oauth/wechat_mini_program/callback")
                .run(context -> {
                    AuthConfig authConfig = context.getBean(AuthConfigRepository.class)
                            .getAuthConfigById("wechat_mini_program");
                    assertThat(authConfig.getHttpConfig()).isNotNull();
                    assertThat(authConfig.getHttpConfig().getProxy().address())
                            .isInstanceOf(InetSocketAddress.class);
                    InetSocketAddress address = (InetSocketAddress) authConfig.getHttpConfig().getProxy().address();
                    assertThat(address.getHostName()).isEqualTo("192.168.108.1");
                    assertThat(address.getPort()).isEqualTo(10080);
                    assertThat(context.getBean(AuthRequestFactory.class)
                            .getAuthRequest("wechat_mini_program"))
                            .isInstanceOf(AuthWechatMiniProgramRequest.class);
                });
    }

    @Test
    void justAuthUseSpecialHttpProxy() {
        this.contextRunner
                .withPropertyValues("youkol.justauth.http-config.hostname:192.168.108.1",
                        "youkol.justauth.http-config.port:10080",
                        "youkol.justauth.http-config.proxy.WECHAT_MINI_PROGRAM.hostname:192.168.108.2",
                        "youkol.justauth.http-config.proxy.WECHAT_MINI_PROGRAM.port:10081",
                        "youkol.justauth.type.WECHAT_MINI_PROGRAM.client-id:WECHAT_MINI_PROGRAM",
                        "youkol.justauth.type.WECHAT_MINI_PROGRAM.client-secret:WECHAT_MINI_PROGRAM",
                        "youkol.justauth.type.WECHAT_MINI_PROGRAM.redirect-uri:http://test.justauth/test/oauth/WECHAT_MINI_PROGRAM/callback")
                .run(context -> {
                    AuthConfig authConfig = context.getBean(AuthConfigRepository.class)
                            .getAuthConfigById("wechat_mini_program");
                    assertThat(authConfig.getHttpConfig()).isNotNull();
                    assertThat(authConfig.getHttpConfig().getProxy().address())
                            .isInstanceOf(InetSocketAddress.class);
                    InetSocketAddress address = (InetSocketAddress) authConfig.getHttpConfig().getProxy().address();
                    assertThat(address.getHostName()).isEqualTo("192.168.108.2");
                    assertThat(address.getPort()).isEqualTo(10081);
                    assertThat(context.getBean(AuthRequestFactory.class).getAuthRequest("WECHAT_MINI_PROGRAM"))
                            .isInstanceOf(AuthWechatMiniProgramRequest.class);
                });
    }

    @Test
    void justAuthUseDatabaseAuthConfigRepository() {
        this.contextRunner.withUserConfiguration(AuthConfigRepositoryConfiguration.class)
                .run(context -> {
                    AuthConfigRepository authConfigRepository = context.getBean(AuthConfigRepository.class);
                    assertThatThrownBy(() -> authConfigRepository.listAuthConfig())
                            .isInstanceOf(UnsupportedOperationException.class)
                            .hasMessageContaining("Unsupported listAuthConfig");
                });
    }

    public static class Custom1AuthRequest extends AuthDefaultRequest {

        public Custom1AuthRequest(AuthConfig config) {
            super(config, CustomAuthSource.CUSTOM1);
        }

        public Custom1AuthRequest(AuthConfig config, AuthStateCache authStateCache) {
            super(config, CustomAuthSource.CUSTOM1, authStateCache);
        }

        @Override
        public AuthToken getAccessToken(AuthCallback authCallback) {
            return AuthToken.builder()
                    .openId("openId")
                    .unionId("unionId")
                    .build();
        }

        @Override
        public AuthUser getUserInfo(AuthToken authToken) {
            return AuthUser.builder()
                    .username("")
                    .nickname("")
                    .avatar("")
                    .uuid(authToken.getOpenId())
                    .token(authToken)
                    .source(this.source.toString())
                    .build();
        }

    }

    public static class Custom2AuthRequest extends AuthDefaultRequest {

        public Custom2AuthRequest(AuthConfig config) {
            super(config, CustomAuthSource.CUSTOM2);
        }

        public Custom2AuthRequest(AuthConfig config, AuthStateCache authStateCache) {
            super(config, CustomAuthSource.CUSTOM2, authStateCache);
        }

        @Override
        public AuthToken getAccessToken(AuthCallback authCallback) {
            return AuthToken.builder()
                    .openId("openId")
                    .unionId("unionId")
                    .build();
        }

        @Override
        public AuthUser getUserInfo(AuthToken authToken) {
            return AuthUser.builder()
                    .username("")
                    .nickname("")
                    .avatar("")
                    .uuid(authToken.getOpenId())
                    .token(authToken)
                    .source(this.source.toString())
                    .build();
        }

    }

    static enum CustomAuthSource implements AuthSource {

        CUSTOM1 {

            @Override
            public String authorize() {
                return "http://test.justauth/custom1/authorize";
            }

            @Override
            public String accessToken() {
                return "http://test.justauth/custom1/accessToken";
            }

            @Override
            public String userInfo() {
                return "http://test.justauth/custom1/userInfo";
            }

            @Override
            public Class<? extends AuthDefaultRequest> getTargetClass() {
                return Custom1AuthRequest.class;
            }
        },

        CUSTOM2 {

            @Override
            public String authorize() {
                return "http://test.justauth/custom2/authorize";
            }

            @Override
            public String accessToken() {
                return "http://test.justauth/custom2/accessToken";
            }

            @Override
            public String userInfo() {
                return "http://test.justauth/custom2/userInfo";
            }

            @Override
            public Class<? extends AuthDefaultRequest> getTargetClass() {
                return Custom2AuthRequest.class;
            }
        }

    }

    public static class SimpleClassAuthRequest extends AuthDefaultRequest {

        public SimpleClassAuthRequest(AuthConfig config) {
            super(config, SimpleClassAuthSource.INSTANCE);
        }

        public SimpleClassAuthRequest(AuthConfig config, AuthStateCache authStateCache) {
            super(config, SimpleClassAuthSource.INSTANCE, authStateCache);
        }

        @Override
        public AuthToken getAccessToken(AuthCallback authCallback) {
            return AuthToken.builder()
                    .openId("openId")
                    .unionId("unionId")
                    .build();
        }

        @Override
        public AuthUser getUserInfo(AuthToken authToken) {
            return AuthUser.builder()
                    .username("")
                    .nickname("")
                    .avatar("")
                    .uuid(authToken.getOpenId())
                    .token(authToken)
                    .source(this.source.toString())
                    .build();
        }
    }

    static class SimpleClassAuthSource implements AuthSource {

        public static final String AUTH_SOURCE_NAME = "SIMPLE_CLASS";

        public static final SimpleClassAuthSource INSTANCE = new SimpleClassAuthSource();

        @Override
        public String authorize() {
            return "http://test.justauth/clazz/authorize";
        }

        @Override
        public String accessToken() {
            return "http://test.justauth/clazz/accessToken";
        }

        @Override
        public String userInfo() {
            return "http://test.justauth/clazz/userInfo";
        }

        @Override
        public Class<? extends AuthDefaultRequest> getTargetClass() {
            return SimpleClassAuthRequest.class;
        }

        @Override
        public String getName() {
            return AUTH_SOURCE_NAME;
        }

        @Override
        public String toString() {
            return this.getName();
        }

    }

    @Configuration(proxyBeanMethods = false)
    public static class SimpleClassAuthSourceConfiguration {

        @Bean
        public SimpleClassAuthSource simpleClassAuthSource() {
            return SimpleClassAuthSource.INSTANCE;
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class EnumClassAuthSourceConfiguration {

        @Bean
        public List<AuthSource> customAuthSources() {
            return Arrays.stream(CustomAuthSource.values()).collect(Collectors.toList());
        }
    }

    static class CustomNameAuthSource implements AuthSource {

        public static final String AUTH_SOURCE_DEFAULT_NAME = "CUSTOM_NAME";

        private String authSourceName = AUTH_SOURCE_DEFAULT_NAME;

        public CustomNameAuthSource(String sourceName) {
            this.authSourceName = sourceName;
        }

        @Override
        public String authorize() {
            return "http://test.justauth/clazz/authorize";
        }

        @Override
        public String accessToken() {
            return "http://test.justauth/clazz/accessToken";
        }

        @Override
        public String userInfo() {
            return "http://test.justauth/clazz/userInfo";
        }

        @Override
        public Class<? extends AuthDefaultRequest> getTargetClass() {
            return SimpleClassAuthRequest.class;
        }

        @Override
        public String getName() {
            return this.authSourceName;
        }

        @Override
        public String toString() {
            return this.getName();
        }
    }

    static class MyCustomAuthStateCache implements AuthStateCache {

        /**
         * 存入缓存
         *
         * @param key   缓存key
         * @param value 缓存内容
         */
        @Override
        public void cache(String key, String value) {
            throw new UnsupportedOperationException("cache put");
        }

        /**
         * 存入缓存
         *
         * @param key     缓存key
         * @param value   缓存内容
         * @param timeout 指定缓存过期时间（毫秒）
         */
        @Override
        public void cache(String key, String value, long timeout) {
            throw new UnsupportedOperationException("cache put with timeout");
        }

        /**
         * 获取缓存内容
         *
         * @param key 缓存key
         * @return 缓存内容
         */
        @Override
        public String get(String key) {
            throw new UnsupportedOperationException("cache get");
        }

        /**
         * 是否存在key，如果对应key的value值已过期，也返回false
         *
         * @param key 缓存key
         * @return true：存在key，并且value没过期；false：key不存在或者已过期
         */
        @Override
        public boolean containsKey(String key) {
            throw new UnsupportedOperationException("cache containsKey");
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class CustomAuthStateCacheConfiguration {

        @Bean
        public AuthStateCache authStateCache() {
            return new MyCustomAuthStateCache();
        }
    }

    static class DatabaseAuthConfigRepository implements AuthConfigRepository {

        @Override
        public Map<String, AuthConfig> listAuthConfig() {
            throw new UnsupportedOperationException("Unsupported listAuthConfig");
        }

        @Override
        public AuthConfig getAuthConfigById(String authConfigId) {
            throw new UnsupportedOperationException("Unsupported getAuthConfigById");
        }

    }

    @Configuration(proxyBeanMethods = false)
    public static class AuthConfigRepositoryConfiguration {

        @Bean
        public AuthConfigRepository authConfigRepository() {
            return new DatabaseAuthConfigRepository();
        }
    }
}

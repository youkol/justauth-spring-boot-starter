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
package com.youkol.support.justauth.support.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.zhyd.oauth.AuthRequestBuilder;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.request.AuthRequest;
import com.youkol.support.justauth.autoconfigure.JustAuthProperties;
import com.youkol.support.justauth.support.config.AuthConfigRepository;

/**
 * The factory class of {@link AuthRequest}
 *
 * @author jackiea
 * @since 1.0.0
 */
public class AuthRequestFactory {

    private AuthConfigRepository authConfigRepository;

    private AuthStateCache authStateCache;

    private List<AuthSource> extendAuthSources = new ArrayList<>();

    private JustAuthProperties properties;

    public AuthRequestFactory(AuthConfigRepository authConfigRepository, AuthStateCache authStateCache,
            List<AuthSource> extendAuthSources, JustAuthProperties properties) {
        this.authConfigRepository = authConfigRepository;
        this.authStateCache = authStateCache;
        this.extendAuthSources = extendAuthSources;
        this.properties = properties;
    }

    /**
     * Return the current configured OAuth names.
     *
     * @return Return the current configured OAuth names.
     */
    public List<String> getConfiguredOAuthNames() {
        return this.authConfigRepository.listAuthConfig().keySet()
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    /**
     * Return {@link AuthRequest}
     *
     * @param source the source of OAuth2 {@link AuthSource}
     * @return Return {@link AuthRequest} or null if not found.
     */
    public AuthRequest getAuthRequest(String source) {
        return AuthRequestBuilder.builder()
                .source(source)
                .authConfig(this::getAuthConfig)
                .authStateCache(this.authStateCache)
                .extendSource(this.mergeExtendAuthSources())
                .build();
    }

    private AuthConfig getAuthConfig(String source) {
        return this.authConfigRepository.getAuthConfigById(source);
    }

    private AuthSource[] mergeExtendAuthSources() {
        Stream<AuthSource> injected = this.extendAuthSources.stream();

        Stream<AuthSource> configured = this.properties.getExtendAuthSourceClass()
                .stream()
                .flatMap(this::instanceStreamFromClass);

        return Stream.concat(injected, configured).distinct().toArray(AuthSource[]::new);
    }

    private Stream<AuthSource> instanceStreamFromClass(Class<? extends AuthSource> clazz) {
        try {
            if (clazz.isEnum()) {
                return Arrays.stream(clazz.getEnumConstants());
            } else {
                return Stream.of(clazz.getConstructor().newInstance());
            }
        } catch (Exception ex) {
            String message = String.format("The class of %s must have default no-argument constructor.",
                    clazz.getCanonicalName());
            throw new AuthException(message, ex);
        }
    }

}

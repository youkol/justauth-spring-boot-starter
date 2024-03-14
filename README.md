# justauth-spring-boot-starter

> Spring boot starter for JustAuth（Spring Boot 集成 JustAuth）

## 1. JustAuth 开箱即用的整合第三方登录的开源组件

* JustAuth 官方文档: <https://www.justauth.cn>
* JustAuth Gitee: <https://gitee.com/justauth>
* JustAuth Github: <https://github.com/justauth/justauth>

## 2. 快速开始

### 2.1. 基础配置

* Maven依赖

```xml
<dependency>
  <groupId>com.youkol.support.justauth</groupId>
  <artifactId>justauth-spring-boot-starter</artifactId>
  <version>${justauth-spring-boot.version}</version>
</dependency>
```

* spring-boot配置 application.yml 中添加相关配置信息

```yaml
youkol:
  justauth:
    # default enabled is true
    enabled: true
    type:
      WECHAT_MP:
        client-id: 1**********
        client-secret: 1**************************
        redirect-uri: http://oauth.justauth.cn/oauth/wechat_mp/callback
      DINGTALK:
        client-id: 1**********
        client-secret: 1**************************
        redirect-uri: http://oauth.justauth.cn/oauth/dingtalk/callback
      ALIYUN:
        client-id: 1**********
        client-secret: 1**************************
        redirect-uri: http://oauth.justauth.cn/oauth/aliyun/callback
    cache:
      # default type of cache is default, options: default, redis, custom
      type: default
```

* 使用示例

```java
@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final AuthRequestFactory authRequestFactory;

    @GetMapping
    public List<String> listSupportedOAuthNames() {
        return this.authRequestFactory.getConfiguredOAuthNames();
    }

    @GetMapping("/oauth/{type}")
    public void oauthAuthorize(@PathVariable("type") String type, HttpServletResponse response) throws IOException {
        AuthRequest authRequest = this.authRequestFactory.getAuthRequest(type);
        String redirectUrl = authRequest.authorize(AuthStateUtils.createState());
        response.sendRedirect(redirectUrl);
    }

    @SuppressWarnings("rawtypes")
    @GetMapping("/{type}/callback")
    public AuthResponse oauthCallback(@PathVariable("type") String type, AuthCallback authCallback) {
        AuthRequest authRequest = this.authRequestFactory.getAuthRequest(type);
        return authRequest.login(authCallback);
    }

}
```

### 2.2. 缓存配置

justauth-spring-boot-starter内部集成了2种缓存实现（`CacheType`），并支持自定义实现。

1. 默认实现由JustAuth提供`AuthDefaultStateCache`
2. starter提供了基于redis的缓存实现`AuthRedisStateCache`
3. 自定义缓存实现

#### 2.2.1. 默认缓存实现

```yaml
youkol:
  justauth:
    cache:
      type: default
```

#### 2.2.2. 基于Redis的缓存实现

1. 添加依赖

```xml
<!-- spring-boot-data-redis依赖 -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- 对象池依赖，使用redis时使用 -->
<dependency>
  <groupId>org.apache.commons</groupId>
  <artifactId>commons-pool2</artifactId>
</dependency>
```

2. spring-boot配置

```yaml
youkol:
  justauth:
    cache:
      type: redis
      # 默认："YOUKOL:JUSTAUTH:STATE:"
      key-prefix: "JUSTAUTH:STATE:"
      # 默认：3 minutes
      timeout: 3m

spring:
  redis:
    # Redis主机地址
    host: localhost
    # 连接超时时间
    timeout: 10000ms
    # Redis默认情况下有16个分片，配置具体使用的分片
    database: 0
    lettuce:
      pool:
        # 连接池最大连接数（使用负值表示没有限制） 默认 8
        max-active: 8
        # 连接池最大阻塞等待时间（使用负值表示没有限制） 默认 -1
        max-wait: -1ms
        # 连接池中的最大空闲连接 默认 8
        max-idle: 8
        # 连接池中的最小空闲连接 默认 0
        min-idle: 0
```

#### 2.2.3. 自定义缓存实现

1. spring-boot配置

```yaml
youkol:
  justauth:
    cache:
      type: custom
```

2. 自定义缓存实现，必须实现JustAuth的`AuthStateCache`接口

```java
public class MyCustomAuthStateCache implements AuthStateCache {
    /**
     * 存入缓存
     *
     * @param key   缓存key
     * @param value 缓存内容
     */
    @Override
    public void cache(String key, String value) {
        // TODO 存入缓存
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
        // TODO 存入缓存
    }

    /**
     * 获取缓存内容
     *
     * @param key 缓存key
     * @return 缓存内容
     */
    @Override
    public String get(String key) {
        // TODO 获取缓存内容
        return null;
    }

    /**
     * 是否存在key，如果对应key的value值已过期，也返回false
     *
     * @param key 缓存key
     * @return true：存在key，并且value没过期；false：key不存在或者已过期
     */
    @Override
    public boolean containsKey(String key) {
        // TODO 获取缓存内容
        return false;
    }
}
```

3. 自定义缓存的spring-boot装配

```java
@Configuration
public class JustAuthStateConfiguration {

    @Bean
    public AuthStateCache authStateCache() {
        return new MyCustomAuthStateCache();
    }

}
```

### 2.3. 自定义第三方平台

添加自定义的平台当前支持两种配置方式：通过枚举类配置和普通类配置

### 2.3.1. 枚举类配置方式

```java
public class CustomAuthRequest extends AuthDefaultRequest {

    public CustomAuthRequest(AuthConfig config) {
        super(config, CustomAuthSource.CUSTOM);
    }

    public CustomAuthRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, CustomAuthSource.CUSTOM, authStateCache);
    }

    @Override
    protected AuthToken getAccessToken(AuthCallback authCallback) {
        return AuthToken.builder()
                .openId("openId")
                .unionId("unionId")
                .build();
    }

    @Override
    protected AuthUser getUserInfo(AuthToken authToken) {
        return AuthUser.builder()
                .username("username")
                .nickname("nickname")
                .avatar("avatar")
                .uuid(authToken.getOpenId())
                .token(authToken)
                .source(this.source.toString())
                .build();
    }

}

public enum CustomExtendAuthSource implements AuthSource {

    CUSTOM {

        @Override
        public String authorize() {
            return "http://test.justauth/custom/authorize";
        }

        @Override
        public String accessToken() {
            return "http://test.justauth/custom/accessToken";
        }

        @Override
        public String userInfo() {
            return "http://test.justauth/custom/userInfo";
        }

        @Override
        public Class<? extends AuthDefaultRequest> getTargetClass() {
            return CustomAuthRequest.class;
        }
    }
}
```

```yaml
youkol:
  justauth:
    enabled: true
    # 支持配置多个
    extend-auth-source-class: com.youkol.support.justauth.support.config.CustomExtendAuthSource
```

### 2.3.2. 普通类配置方式

```java
public class CustomAuthRequest extends AuthDefaultRequest {

    public CustomAuthRequest(AuthConfig config) {
        super(config, CustomAuthSource.CUSTOM);
    }

    public CustomAuthRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, CustomAuthSource.CUSTOM, authStateCache);
    }

    @Override
    protected AuthToken getAccessToken(AuthCallback authCallback) {
        return AuthToken.builder()
                .openId("openId")
                .unionId("unionId")
                .build();
    }

    @Override
    protected AuthUser getUserInfo(AuthToken authToken) {
        return AuthUser.builder()
                .username("username")
                .nickname("nickname")
                .avatar("avatar")
                .uuid(authToken.getOpenId())
                .token(authToken)
                .source(this.source.toString())
                .build();
    }

}

public class CustomExtendAuthSource implements AuthSource {
    public static final String AUTH_SOURCE_NAME = "CUSTOM2";
    public static final CustomExtendAuthSource INSTANCE = new CustomExtendAuthSource();

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
        return CustomAuthRequest.class;
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

@Configuration
public class JustAuthSourceConfiguration {

    @Bean
    public AuthSource customExtendAuthSource() {
        return CustomExtendAuthSource.INSTANCE;
    }
}

```

### 2.4. 数据库中获取第三方平台的配置信息

默认实现为基于spring-boot配置文件的`InMemoryAuthConfigRepository`，
可通过实现`AuthConfigRepository`接口，自定义第三方平台配置信息获取方式。

```java
public class DatabaseAuthConfigRepository implements AuthConfigRepository {

    @Override
    public Map<String, AuthConfig> listAuthConfig() {
        // TODO 从数据库中查询所有的第三方平台配置信息
        return new HashMap<>();
    }

    @Override
    public AuthConfig getAuthConfigById(String authConfigId) {
        // TODO 根据第三方平台标识，获取对应的配置信息
        return null;
    }

}

@Configuration
public class AuthConfigRepositoryConfiguration {

    @Bean
    public AuthConfigRepository authConfigRepository() {
        return new DatabaseAuthConfigRepository();
    }
}
```

## 3. 源码部分

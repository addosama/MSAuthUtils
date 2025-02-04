package com.yukibytes.utils.mc.msa;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 微软认证配置类 - 存储全局常量
 * Copyright (c) 2024 Deepseek-V3
 */
public class AuthConfig {
    // 应用注册信息
    private final String clientId;
    private final String clientSecret;
    private final int callbackPort;

    // 固定API端点
    public static final String MICROSOFT_AUTH_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize";
    public static final String TOKEN_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    public static final String XBOX_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    public static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    public static final String MINECRAFT_AUTH_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    public static final String PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    public AuthConfig(String clientId, String clientSecret, int callbackPort) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.callbackPort = callbackPort;
    }

    // Getter方法
    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public String getRedirectUri() { return "http://localhost:" + callbackPort + "/auth"; }
    public String getEncodedSecret() {
        try {
            return URLEncoder.encode(clientSecret, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    public int getCallbackPort() {
        return callbackPort;
    }
}
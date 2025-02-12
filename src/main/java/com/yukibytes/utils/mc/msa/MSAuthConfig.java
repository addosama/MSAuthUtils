package com.yukibytes.utils.mc.msa;

/**
 * 微软认证配置类 - 存储全局常量
 * Copyright (c) 2024 Deepseek-V3
 */
public class MSAuthConfig {
    // 应用注册信息
    private final String clientId;
    private final int callbackPort;

    // 固定API端点
    public static final String MICROSOFT_AUTH_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize";
    public static final String TOKEN_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    public static final String XBOX_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    public static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    public static final String MINECRAFT_AUTH_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    public static final String PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    public MSAuthConfig(String clientId, int callbackPort) {
        this.clientId = clientId;
        this.callbackPort = callbackPort;
    }

    // Getter方法
    public String getClientId() { return clientId; }
    public String getRedirectUri() {
        return URLEncodeUtils.utf8("http://localhost:" + callbackPort + "/auth");
    }
    public int getCallbackPort() {
        return callbackPort;
    }
}
package com.yukibytes.utils.mc.msa;

import com.sun.net.httpserver.*;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 账户认证处理器 - 每个账户实例独立使用
 * Copyright (c) 2024 Deepseek-V3
 */
public class MSAccountAuthenticator {
    private final MSAuthConfig config;
    private CompletableFuture<MSAccountData> authFuture;
    private final String codeVerifier;
    private final String codeChallenge;

    public MSAccountAuthenticator(MSAuthConfig config) {
        this.config = config;
        this.codeVerifier = PKCEUtils.generateCodeVerifier();
        this.codeChallenge = PKCEUtils.generateCodeChallenge(codeVerifier);
    }

    // 全新登录流程
    public CompletableFuture<MSAccountData> authenticate() throws IOException {
        authFuture = new CompletableFuture<>();
        startCallbackServer();
        openAuthPage(true);
        return authFuture;
    }

    // 通过refreshToken重新登录
    public CompletableFuture<MSAccountData> refreshLogin(String refreshToken) {
        CompletableFuture<MSAccountData> future = new CompletableFuture<>();

        try {
            JSONObject tokenResponse = postRequest(
                    MSAuthConfig.TOKEN_URL,
                    "client_id=" + config.getClientId() +
//                            "&client_secret=" + config.getEncodedSecret() +
                            "&refresh_token=" + refreshToken +
                            "&grant_type=refresh_token"
            );

            processTokenResponse(tokenResponse, future);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    // 处理令牌响应
    private void processTokenResponse(JSONObject tokenResponse, CompletableFuture<MSAccountData> future) {
        try {
            // Xbox和Minecraft认证流程
            JSONObject xboxToken = authenticateXbox(tokenResponse.getString("access_token"));
            JSONObject xstsToken = authenticateXSTS(xboxToken.getString("Token"));
            JSONObject mcToken = authenticateMinecraft(xstsToken);
            MSAccountData account = getProfileData(mcToken, tokenResponse);

            future.complete(account);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }

    // Xbox认证
    private JSONObject authenticateXbox(String msToken) throws IOException {
        return postJsonRequest(MSAuthConfig.XBOX_AUTH_URL,
                new JSONObject()
                        .put("Properties", new JSONObject()
                                .put("AuthMethod", "RPS")
                                .put("SiteName", "user.auth.xboxlive.com")
                                .put("RpsTicket", "d=" + msToken))
                        .put("RelyingParty", "http://auth.xboxlive.com")
                        .put("TokenType", "JWT"),
                "XBL3.0 x=", "TokenType");
    }

    // XSTS认证
    private JSONObject authenticateXSTS(String xboxToken) throws IOException {
        return postJsonRequest(MSAuthConfig.XSTS_AUTH_URL,
                new JSONObject()
                        .put("Properties", new JSONObject()
                                .put("SandboxId", "RETAIL")
                                .put("UserTokens", new String[]{xboxToken}))
                        .put("RelyingParty", "rp://api.minecraftservices.com/")
                        .put("TokenType", "JWT"),
                "XBL3.0 x=", "TokenType");
    }

    // Minecraft认证
    private JSONObject authenticateMinecraft(JSONObject xstsToken) throws IOException {
        String uhs = xstsToken.getJSONObject("DisplayClaims")
                .getJSONArray("xui")
                .getJSONObject(0)
                .getString("uhs");

        return postJsonRequest(MSAuthConfig.MINECRAFT_AUTH_URL,
                new JSONObject().put("identityToken", "XBL3.0 x=" + uhs + ";" + xstsToken.getString("Token")),
                "Bearer", "identityToken");
    }

    // 获取玩家信息
    private MSAccountData getProfileData(JSONObject mcToken, JSONObject msToken) throws IOException {
        JSONObject profile = getRequest(
                MSAuthConfig.PROFILE_URL,
                mcToken.getString("token_type") + " " + mcToken.getString("access_token")
        );

        String skinUrl = parseSkinUrl(profile);
        return new MSAccountData(
                profile.getString("name"),
                profile.getString("id"),
                mcToken.getString("access_token"),
                msToken.optString("refresh_token"),
                skinUrl
        );
    }

    // 解析皮肤URL
    private String parseSkinUrl(JSONObject profile) {
        if (profile.has("skins")) {
            for (Object skinObj : profile.getJSONArray("skins")) {
                JSONObject skin = (JSONObject) skinObj;
                if ("ACTIVE".equals(skin.optString("state"))) {
                    return skin.getString("url");
                }
            }
        }
        return null;
    }

    // HTTP POST请求（表单数据）
    private JSONObject postRequest(String url, String params) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes(StandardCharsets.UTF_8));
        }

        return parseResponse(conn);
    }

    // HTTP POST请求（JSON数据）
    private JSONObject postJsonRequest(String url, JSONObject data, String authPrefix, String s) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        if (authPrefix != null) {
            conn.setRequestProperty("Authorization", authPrefix + data.getString(s));
        }

        try (OutputStream os = conn.getOutputStream()) {
            os.write(data.toString().getBytes(StandardCharsets.UTF_8));
        }

        return parseResponse(conn);
    }

    // HTTP GET请求
    private JSONObject getRequest(String url, String auth) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", auth);
        return parseResponse(conn);
    }

    // 解析HTTP响应
    private JSONObject parseResponse(HttpURLConnection conn) throws IOException {
        int status = conn.getResponseCode();
        try (InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
//            System.out.println("========================");
            while ((line = reader.readLine()) != null) {
                response.append(line);
//                System.out.println(line);
            }
//            System.out.println("========================");
            JSONObject json = new JSONObject(response.toString());
            if (status >= 400) {
                throw new IOException("HTTP Error " + status + ": " + json.optString("error", "Unknown error"));
            }
            return json;
        }
    }

    // 启动回调服务器
    private void startCallbackServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", config.getCallbackPort()), 0);

//        System.out.println(server.getAddress().toString());
//        System.out.println(server.getAddress().getHostName());
//        System.out.println(server.getAddress().getHostString());

        server.createContext("/auth", exchange -> {
            try {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                if (params.containsKey("code")) {
                    handleAuthCode(params.get("code"), exchange);
                } else if (params.containsKey("error")) {
                    String error = params.get("error");
                    String errorDescription = params.get("error_description");
                    System.err.println("LOGIN FAILED: " + error + " - " + errorDescription);
                } else {
                    sendErrorResponse(exchange, "Authentication failed - Unknown Error");
                }
            } finally {
                server.stop(0);
            }
        });
        server.start();
    }

    // 打开认证页面
    private void openAuthPage(boolean selectAcc) throws IOException {
        String authUrl = MSAuthConfig.MICROSOFT_AUTH_URL + "?" +
                "client_id=" + config.getClientId() +
                "&response_type=code" +
                "&redirect_uri=" + config.getRedirectUri() +
                "&scope=XboxLive.signin%20offline_access" +
                "&code_challenge=" + codeChallenge +
                "&code_challenge_method=S256" +
                "&response_mode=query" +
                (selectAcc ? "&prompt=select_account" : "");

        Desktop.getDesktop().browse(URI.create(authUrl));
    }

    // 处理认证码
    private void handleAuthCode(String code, HttpExchange exchange) {
        try {
            JSONObject tokenResponse = postRequest(
                    MSAuthConfig.TOKEN_URL,
                    "client_id=" + config.getClientId() +
                            //"&client_secret=" + config.getEncodedSecret() +
                            "&code=" + code +
                            "&redirect_uri=" + config.getRedirectUri() +
                            "&grant_type=authorization_code" +
                            "&code_verifier=" +codeVerifier
            );

            processTokenResponse(tokenResponse, authFuture);
            sendSuccessResponse(exchange);
        } catch (Exception e) {
            authFuture.completeExceptionally(e);
            try {
                sendErrorResponse(exchange, e.getMessage());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // 解析查询参数
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    try {
                        params.put(keyValue[0], URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name()));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return params;
    }

    // 发送成功响应
    private void sendSuccessResponse(HttpExchange exchange) throws IOException {
        String response = "<h1>You can close this page now</h1>";
        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    // 发送错误响应
    private void sendErrorResponse(HttpExchange exchange, String error) throws IOException {
        String response = "<h1>Login Failed: " + error + "</h1>";
        exchange.sendResponseHeaders(400, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
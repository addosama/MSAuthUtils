package com.yukibytes.utils.mc.msa;

/**
 * 需要保存的账户数据
 * Copyright (c) 2024 Deepseek-V3
 */
public class MSAccountData {
 private final String username;
 private final String uuid;
 private final String accessToken;
 private String refreshToken; // 需要更新保存
 private final String skinUrl;

 public MSAccountData(String username, String uuid, String accessToken,
                      String refreshToken, String skinUrl) {
  this.username = username;
  this.uuid = uuid;
  this.accessToken = accessToken;
  this.refreshToken = refreshToken;
  this.skinUrl = skinUrl;
 }

 // Getter方法
 public String getUsername() { return username; }
 public String getUuid() { return uuid; }
 public String getAccessToken() { return accessToken; }
 public String getRefreshToken() { return refreshToken; }
 public String getSkinUrl() { return skinUrl; }

 void updateRefreshToken(String newToken) {
  this.refreshToken = newToken;
 }
}
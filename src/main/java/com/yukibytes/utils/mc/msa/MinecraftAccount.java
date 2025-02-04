package com.yukibytes.utils.mc.msa;

/**
 * @author addo6544
 * 2025/2/3 22:24
 **/
public class MinecraftAccount {
    private final String username;
    private final String uuid;
    private final String accessToken;
    private final String refreshToken;
    private String skinURL;
    private boolean classic;

    public MinecraftAccount(String username, String uuid, String accessToken, String refreshToken, String skinURL, boolean classic) {
        this.username = username;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.skinURL = skinURL;
        this.classic = classic;
    }

    // Getter方法
    public String getUsername() { return username; }
    public String getUuid() { return uuid; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getSkinURL() {
        return skinURL;
    }
    public boolean isClassic() {
        return classic;
    }

    // Setter方法
    public void setSkinURL(String skinURL) {
        this.skinURL = skinURL;
    }
    public void setClassic(boolean classic) {
        this.classic = classic;
    }
}

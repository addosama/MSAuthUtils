package com.yukibytes.utils.msauthentication;

/**
 * @author addo6544
 * 2025/2/3 18:09
 **/
public class MSAuthenticator {
    public final String CLIENT_ID;
    public final String REDIRECT_URI;

    public MSAuthenticator(String CLIENT_ID, String REDIRECT_URI) {
        this.CLIENT_ID = CLIENT_ID;
        this.REDIRECT_URI = REDIRECT_URI;
    }

    public void tryLogin(){
        StringBuilder sb = new StringBuilder();

        sb
                .append("https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize?client_id=")
                .append(CLIENT_ID)

                .append("&response_type=code")

                .append("&redirect_uri=")
                .append(REDIRECT_URI)

                .append("&response_mode=query")
                .append("&scope=XboxLive.signin+offline_access");


    }
}

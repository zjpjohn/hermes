package pl.allegro.tech.hermes.consumers.consumer.oauth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthAccessTokenResponse {

    private final String accessToken;

    private final int expiresIn;

    private final String tokenType;

    @JsonCreator
    public OAuthAccessTokenResponse(@JsonProperty("access_token") String accessToken,
                                    @JsonProperty("token_type") String tokenType,
                                    @JsonProperty("expires_in") int expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public OAuthAccessToken toAccessToken() {
        return new OAuthAccessToken(accessToken, expiresIn);
    }
}

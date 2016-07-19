package pl.allegro.tech.hermes.test.helper.builder;

import pl.allegro.tech.hermes.api.OAuthProvider;

public class OAuthProviderBuilder {

    private String name;

    private String tokenEndpoint = "http://example.com/token";

    private String clientId = "testClient123";

    private String clientSecret = "testPassword123";

    private int tokenRequestDelay = 1;

    public OAuthProviderBuilder(String name) {
        this.name = name;
    }

    public static OAuthProviderBuilder oAuthProvider(String name) {
        return new OAuthProviderBuilder(name);
    }

    public OAuthProvider build() {
        return new OAuthProvider(name, tokenEndpoint, clientId, clientSecret, tokenRequestDelay);
    }

    public OAuthProviderBuilder withTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
        return this;
    }

    public OAuthProviderBuilder withClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public OAuthProviderBuilder withClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public OAuthProviderBuilder withTokenRequestDelay(int tokenRequestDelay) {
        this.tokenRequestDelay = tokenRequestDelay;
        return this;
    }
}

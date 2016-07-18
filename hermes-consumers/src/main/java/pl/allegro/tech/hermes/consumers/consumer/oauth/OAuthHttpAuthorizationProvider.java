package pl.allegro.tech.hermes.consumers.consumer.oauth;

import pl.allegro.tech.hermes.api.Subscription;
import pl.allegro.tech.hermes.api.SubscriptionOAuthPolicy;
import pl.allegro.tech.hermes.consumers.consumer.Message;
import pl.allegro.tech.hermes.consumers.consumer.sender.http.HttpAuthorizationProvider;

import static pl.allegro.tech.hermes.api.SubscriptionOAuthPolicy.GrantType.RESOURCE_OWNER_USERNAME_PASSWORD;

public class OAuthHttpAuthorizationProvider implements HttpAuthorizationProvider {

    private final static String BEARER_TOKEN_PREFIX = "Bearer ";
    private final static String BASIC_AUTH_TOKEN_PREFIX = "Basic ";

    private final Subscription subscription;
    private final OAuthAccessTokenCache tokenCache;

    public OAuthHttpAuthorizationProvider(Subscription subscription, OAuthAccessTokenCache tokenCache) {
        this.subscription = subscription;
        this.tokenCache = tokenCache;
    }

    @Override
    public String authorizationToken(Message message) {
        String token = tokenCache.getToken(subscription).getTokenValue();
        return subscription.getSubscriptionOAuthPolicy().getGrantType().equals(RESOURCE_OWNER_USERNAME_PASSWORD) ?
                BEARER_TOKEN_PREFIX + token : BASIC_AUTH_TOKEN_PREFIX + token;
    }
}

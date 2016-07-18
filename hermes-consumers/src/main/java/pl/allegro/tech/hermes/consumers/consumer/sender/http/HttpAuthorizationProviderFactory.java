package pl.allegro.tech.hermes.consumers.consumer.sender.http;

import pl.allegro.tech.hermes.api.Subscription;
import pl.allegro.tech.hermes.consumers.consumer.oauth.OAuthAccessTokenCache;
import pl.allegro.tech.hermes.consumers.consumer.oauth.OAuthHttpAuthorizationProvider;

import javax.inject.Inject;
import java.util.Optional;

public class HttpAuthorizationProviderFactory {

    private final OAuthAccessTokenCache oAuthAccessTokenCache;

    @Inject
    public HttpAuthorizationProviderFactory(OAuthAccessTokenCache oAuthAccessTokenCache) {
        this.oAuthAccessTokenCache = oAuthAccessTokenCache;
    }

    public Optional<HttpAuthorizationProvider> create(Subscription subscription) {
        if(subscription.getEndpoint().containsCredentials()) {
            return Optional.of(new BasicAuthProvider(subscription.getEndpoint()));
        } else if (subscription.hasSubscriptionOAuthPolicy()) {
            return Optional.of(new OAuthHttpAuthorizationProvider(subscription, oAuthAccessTokenCache));
        } else {
            return Optional.empty();
        }
    }

}
